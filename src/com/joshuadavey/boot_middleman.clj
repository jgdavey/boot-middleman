(ns com.joshuadavey.boot-middleman
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [boot.core :as boot :refer [deftask]]
            [boot.pod :as pod])
  (:import [java.util Properties]
           [java.io File]))

(defonce ^:private default-gem-dir
  (str (File. (System/getProperty "user.dir") ".gems")))

(defonce ^:private clj-rb-version
  (let [props (doto (Properties.)
                (.load (-> "META-INF/maven/clj.rb/clj.rb/pom.properties"
                         io/resource
                         io/reader)))]
    (.getProperty props "version")))

(defonce ^:private gems [["middleman-core" "3.3.7"]
                         ["middleman-sprockets" "3.4.1"]
                         ["haml" "4.0.5"]
                         ["sass" "3.4.11"]
                         ["compass-import-once" "1.0.5"]
                         ["compass" "1.0.3"]
                         ["kramdown" "1.5.0"]])

(defn prepare-runtime [pod]
  (pod/with-eval-in pod
    (require
      '[clj.rb :as rb]
      '[clojure.java.io :as io])
    (let [rt (rb/runtime {:gem-paths [~default-gem-dir]
                          :env {"GEM_HOME" ~default-gem-dir}})]
      (try
        (doseq [[name version] ~gems]
          (rb/install-gem rt name version {:install-dir ~default-gem-dir}))
        (finally
          (rb/shutdown-runtime rt))))))

(def script (-> "middleman_build.rb" io/resource slurp))

(defn- changed [before after]
  (->> (boot/fileset-diff before after :hash)
       boot/input-files
       (filter #(.startsWith (:path %) "source/"))
       seq))

(defn- should-build? [before after]
  (if before
    (changed before after)
    true))

(deftask middleman
  "Build middleman application.

  Assumes a source directory, not the root of the project, contains
  the middleman project structure. By default, this directory is `assets`,
  but you can override using the :dir option, or -d from the commandline.

  When used after the `watch` built-in boot task, only builds when
  files with the `<middleman-dir>/source` directory change.

  Respects middleman's config.rb, with the exception of :build_dir."
  [d dir DIR str "directory of middleman app (defaults to assets)"
   e env ENV str "middleman environment (defaults to development)"]
  (let [root (or dir "assets")
        root-dir (.getAbsolutePath (io/file root))
        _ (boot/set-env! :source-paths #(conj % root))
        env (or env "development")
        pod (pod/make-pod (-> (boot/get-env)
                              (update-in [:dependencies] conj ['clj.rb clj-rb-version])))
        prev (atom nil)
        target (boot/temp-dir!)]
    (prepare-runtime pod)
    (boot/with-pre-wrap fileset
      (when (should-build? @prev fileset)
        (println "Building middleman, based on" (map :path (changed @prev fileset)))
        (pod/with-eval-in pod
          (require
            '[clj.rb :as rb]
            '[clojure.java.io :as io])
          (let [rt (rb/runtime {:gem-paths [~default-gem-dir]})]
            (try
              (rb/setenv rt "MM_ENV" ~env)
              (rb/setenv rt "MM_ROOT" ~root-dir)
              (rb/setenv rt "MM_BUILD" ~(.getAbsolutePath target))
              (rb/eval rt ~script)
              (finally
                (rb/shutdown-runtime rt))))))
      (reset! prev fileset)
      (-> fileset
          (boot/add-resource target)
          boot/commit!))))

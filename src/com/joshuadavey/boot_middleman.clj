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

(def prepare-script (-> "middleman_prepare.rb" io/resource slurp))

(defn- make-pod []
  (pod/make-pod (-> (boot/get-env)
                    (assoc :dependencies [['clj.rb clj-rb-version]]))))

(defn- prepare-runtime [pod dir]
  (pod/with-eval-in pod
    (require
      '[clj.rb :as rb]
      '[clojure.java.io :as io])
    (let [rt (rb/runtime {:gem-paths [~default-gem-dir]
                          :env {"GEM_HOME" ~default-gem-dir
                                "MM_ROOT" ~dir}})]
      (try
        (rb/install-gem rt "bundler" "1.10.6")
        (rb/eval rt ~prepare-script)
        (finally
          (.terminate rt))))))

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
   e env ENV str "middleman environment (defaults to development)"
   o out OUT str "middleman output directory (relative to target, defaults to target)"]
  (let [root (or dir "assets")
        root-dir (.getAbsolutePath (io/file root))
        _ (boot/set-env! :source-paths #(conj % root))
        env (or env "development")
        pod (make-pod)
        prev (atom nil)
        target (boot/tmp-dir!)
        output-dir (.toString (.resolve (.toPath target) (or out ".")))]
    (prepare-runtime pod root-dir)
    (boot/with-pre-wrap fileset
      (when (should-build? @prev fileset)
        (println "<< Building middleman, based on" (map :path (changed @prev fileset)) " >>")
        (pod/with-eval-in pod
          (require
            '[clj.rb :as rb]
            '[clojure.java.io :as io])
          (let [rt (rb/runtime {:gem-paths [~default-gem-dir]
                                :env {"MM_ENV" ~env
                                      "MM_ROOT" ~root-dir
                                      "MM_BUILD" ~output-dir}})]
            (try
              (rb/require rt "bundler/setup")
              (rb/eval rt ~script)
              (finally
                (rb/shutdown-runtime rt))))))
      (reset! prev fileset)
      (-> fileset
          (boot/add-resource target)
          boot/commit!))))

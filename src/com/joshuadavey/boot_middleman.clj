(ns com.joshuadavey.boot-middleman
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [boot.core :as boot :refer [deftask]]
            [boot.pod :as pod])
  (:import java.util.Properties))

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
    (let [rt (rb/runtime {:preserve-locals? true})]
      (try
        (doseq [[name version] ~gems]
          (rb/install-gem rt name version))
        (finally
          (rb/shutdown-runtime rt))))))

(def script (-> "middleman_build.rb" io/resource slurp))

(deftask middleman
  "Dir can be provided, or it defaults to ./assets"
  [d dir DIR str "directory of middleman app (defaults to assets)"
   e env ENV str "middleman environment (defaults to development)"]
  (let [root (or dir "assets")
        root-dir (.getAbsolutePath (io/file root))
        _ (boot/set-env! :resource-paths #(conj % root))
        env (or env "development")
        pod (pod/make-pod (-> (boot/get-env)
                              (update-in [:dependencies] conj ['clj.rb clj-rb-version])
                              (update-in [:resource-paths] conj root)))
        target (boot/temp-dir!)]
    (prepare-runtime pod)
    (boot/with-pre-wrap fileset
      (pod/with-eval-in pod
        (require
          '[clj.rb :as rb]
          '[clojure.java.io :as io])
        (let [rt (rb/runtime {:preserve-locals? true})]
          (try
            (rb/setenv rt "MM_ENV" ~env)
            (rb/setenv rt "MM_ROOT" ~root-dir)
            (rb/setenv rt "MM_BUILD" ~(.getAbsolutePath target))
            (rb/eval rt ~script)
            (finally
              (rb/shutdown-runtime rt)))))
      (-> fileset
          (boot/add-resource target)
          boot/commit!))))

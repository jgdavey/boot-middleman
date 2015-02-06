(set-env!
  :source-paths   #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure  "1.6.0"     :scope "provided"]
                  [boot/core            "2.0.0-rc9" :scope "provided"]
                  [clj.rb               "0.3.0"]
                  [adzerk/bootlaces     "0.1.9"     :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.0.1-SNAPSHOT")

(bootlaces! +version+)

(task-options!
 pom  {:project     'com.joshuadavey/boot-middleman
       :version     +version+
       :description "Boot task to compile haml to html"
       :url         "https://github.com/jgdavey/boot-haml"
       :scm         {:url "https://github.com/jgdavey/boot-haml"}
       :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

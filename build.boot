(set-env!
  :source-paths   #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure  "1.7.0"   :scope "provided"]
                  [boot/core            "2.3.0"   :scope "provided"]
                  [clj.rb               "0.3.0"]
                  [adzerk/bootlaces     "0.1.12"  :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.0.5")

(bootlaces! +version+)

(task-options!
 pom  {:project     'com.joshuadavey/boot-middleman
       :version     +version+
       :description "Boot task to compile a middleman project"
       :url         "https://github.com/jgdavey/boot-middleman"
       :scm         {:url "https://github.com/jgdavey/boot-middleman"}
       :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

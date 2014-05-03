(defproject ominate "0.1.0"
  :description "Animate your Om components"
  :url "http://github.com/danielytics/ominate"
  :author "Dan Kersten"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [om "0.6.2"]
                 [domina "1.0.2"]]
  :plugins  [[lein-cljsbuild "1.0.2"]]
  :cljsbuild {:builds []})

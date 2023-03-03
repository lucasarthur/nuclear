(defproject com.lv.reactor/reactor-clj "1.0.0"
  :description "clojure wrapper for project reactor"
  :url "https://github.com/lucasarthur/reactor-clj"
  :license {:name "GNU General Public License v3.0"
            :url "https://www.gnu.org/licenses/gpl-3.0.pt-br.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [io.projectreactor/reactor-core "3.5.3"]
                 [io.projectreactor/reactor-test "3.5.3"]
                 [io.projectreactor.addons/reactor-adapter "3.5.0"]]
  :target-path "target/%s")

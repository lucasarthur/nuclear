(defproject nuclear "0.1.3"
  :description "A Clojure wrapper for Project Reactor"
  :url "https://github.com/lucasarthur/nuclear"
  :license {:name "GNU General Public License v3.0"
            :url "https://www.gnu.org/licenses/gpl-3.0.pt-br.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [io.projectreactor/reactor-core "3.5.3"]
                 [io.projectreactor/reactor-test "3.5.3"]
                 [io.projectreactor.addons/reactor-adapter "3.5.0"]
                 [io.reactivex.rxjava2/rxjava "2.2.8"]
                 [io.reactivex.rxjava3/rxjava "3.1.5"]
                 [org.clojure/core.async "1.6.673"]
                 [manifold "0.3.0"]
                 [com.spikhalskiy.futurity/futurity-core "0.3-RC3"]])

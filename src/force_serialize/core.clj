(ns force-serialize.core
  "Serializable functions EVERYWHERE!"
  (:refer-clojure :exclude [fn])
  (:import java.io.Writer))

;; from flatland.useful
(defn var-name
  "Get the namespace-qualified name of a var."
  [v]
  (apply symbol (map str ((juxt (comp ns-name :ns)
                                :name)
                          (meta v)))))

(defn alias-var
  "Create a var with the supplied name in the current namespace, having the same
  metadata and root-binding as the supplied var."
  [name ^clojure.lang.Var var]
  (apply intern *ns* (with-meta name (merge {:dont-test (str "Alias of " (var-name var))}
                                            (meta var)
                                            (meta name)))
         (when (.hasRoot var) [@var])))

(defmacro defalias
  "Defines an alias for a var: a new var with the same root binding (if
  any) and similar metadata. The metadata of the alias is its initial
  metadata (as provided by def) merged into the metadata of the original."
  [dst src]
  `(alias-var (quote ~dst) (var ~src)))
;; ================


(defmacro with-namespace
  "Execute body in the given namespace."
  [namespace & body]
  `(let [current-ns# (ns-name *ns*)]
     (do (in-ns (quote ~namespace))
         ~@body
         (in-ns current-ns#))))

(defalias old-clojure-core-fn clojure.core/fn)
;; in case we would want to get it back

(defn- save-env [locals form]
  (let [quoted-form `(quote ~form)]
    (if locals
      `(list `let [~@(for [local locals,
                           let-arg [`(quote ~local)
                                    `(list `quote ~local)]]
                       let-arg)]
             ~quoted-form)
      quoted-form)))

(defmacro ^{:doc (str (:doc (meta #'old-clojure-core-fn))
                      "\n\n  Oh, but it also allows serialization!!!111eleven")}
  fn [& sigs]
  `(with-meta (old-clojure-core-fn ~@sigs)
     {:type ::serializable-fn
      ::source ~(save-env (keys &env) &form)}))

(defmethod print-method ::serializable-fn [o ^Writer w]
  (print-method (::source (meta o)) w))

(defn replace-fn
  "Replace the fn macro in clojure.core by the serialized version." []
  (with-namespace clojure.core
    (defalias fn force-serialize.core/fn)))

(defn restore-fn
  "Restore the original clojure.core/fn function."[]
  (with-namespace clojure.core
    (defalias fn force-serialize.core/old-clojure-core-fn)))
  
;; and now, replace the core 'fn' macro. Go!
(replace-fn)
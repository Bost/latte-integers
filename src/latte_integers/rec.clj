
(ns latte-integers.rec
  "The recursion theorems for ℤ."

  (:refer-clojure :exclude [and or not int =])

  (:require [latte.core :as latte :refer [defaxiom defthm definition
                                          deflemma
                                          lambda forall proof assume have
                                          pose try-proof ==>]]

            [latte.prop :as p :refer [and or not <=>]]
            [latte.equal :as eq :refer [equal]]
            [latte.quant :as q]
            [latte.classic :as classic]
            [latte.fun :as fun]

            [latte-sets.core :as set :refer [elem forall-in]]

            [latte-integers.core :as int :refer [zero succ pred int =]]
            [latte-integers.nat :as nat :refer [positive negative]]))

(definition int-recur-prop
  "The property of the recursion principle
for integers."
  [[T :type] [x T] [f-succ (==> T T)] [f-pred (==> T T)]]
  (lambda [g (==> int T)]
    (and (equal T (g zero) x)
         (forall [y int]
           (and (==> (positive (succ y))
                     (equal T (g (succ y)) (f-succ (g y))))
                (==> (negative (pred  y))
                     (equal T (g (pred y)) (f-pred (g y)))))))))

(defaxiom int-recur
  "The recursion principle for integers.

cf. [[int-recur-prop]]

According to [TT&FP,p. 318], this is derivable,
 but we introduce it as an axiom since the
derivation seems rather complex."
  [[T :type] [x T] [f-succ (==> T T)] [f-pred (==> T T)]]
  (q/unique
   (==> int T)
   (int-recur-prop T x f-succ f-pred)))

(definition int-recur-bijection-prop
  "Property of the recursion principle for integers, for bijections.
This is a much simpler principle if the function under study
 is bijective on ℤ (e.g. addition)."
  [[T :type] [x T] [f (==> T T)] [b (fun/bijective T T f)]]
  (lambda [g (==> int T)]
    (and (equal T (g zero) x)
         (forall [y int]
           (equal T (g (succ y)) (f (g y)))))))

(defthm int-recur-bijection
  "The recursion principle for integers, for bijections.
This is a consequence of [[int-rec]], cf. [[int-recur-bijection-prop]]."
  [[T :type] [x T] [f (==> T T)] [b (fun/bijective T T f)]]
  (q/unique
   (==> int T)
   (int-recur-bijection-prop T x f b)))

(deflemma int-recur-bijection-lemma-1
  [[T :type] [f (==> T T)] [b (fun/bijective T T f)] [g (==> int T)]]
  (==> (forall [y int]
         (and (==> (positive (succ y))
                   (equal T (g (succ y)) (f (g y))))
              (==> (negative (pred y))
                   (equal T (g (pred y)) ((fun/inverse T T f b) (g y))))))
       (forall [y int]
         (equal T (g (succ y)) (f (g y))))))

(proof int-recur-bijection-lemma-1
    :script
  (pose inv-f := (fun/inverse T T f b))
  (assume [H (forall [y int]
                     (and (==> (positive (succ y))
                               (equal T (g (succ y)) (f (g y))))
                          (==> (negative (pred y))
                               (equal T (g (pred y)) (inv-f (g y))))))]
    (assume [y int]
      "We proceed by case analysis."
      "  - first case: y is positive"
      (assume [Hpos (positive y)]
        (have <a1> (positive (succ y)) :by ((nat/positive-succ-strong y) Hpos))
        (have <a> (equal T (g (succ y)) (f (g y)))
              :by ((p/and-elim-left% (H y)) <a1>)))
      "  - second case: y is zero"
      (assume [Hzero (= y zero)]
        (have <b1> (positive (succ zero))
              :by ((nat/positive-succ zero)
                   nat/nat-zero))
        (have <b2> (positive (succ y))
              :by ((eq/eq-subst int
                                (lambda [z int] (positive (succ z)))
                                zero y)
                   ((eq/eq-sym int y zero) Hzero)
                   <b1>))
        (have <b> (equal T (g (succ y)) (f (g y)))
              :by ((p/and-elim-left% (H y)) <b2>)))
      "we regroup the first two cases"
      (assume [Hnat (or (= y zero)
                        (positive y))]
        (have <c> (equal T (g (succ y)) (f (g y)))
              :by (p/or-elim% 
                   Hnat
                   (equal T (g (succ y)) (f (g y)))
                   <b> <a>)))
      "  - third case: y is negative"
      (assume [Hneg (negative y)]
        (have <d1> (negative (pred (succ y)))
              :by ((eq/eq-subst int (lambda [z int] (negative z)) y (pred (succ y)))
                   ((eq/eq-sym int (pred (succ y)) y) (int/pred-of-succ y))
                   Hneg))
        (have <d2> (equal T (g (pred (succ y))) (inv-f (g (succ y))))
              :by ((p/and-elim-right% (H (succ y))) <d1>))
        (have <d3> (equal T (g y) (inv-f (g (succ y))))
              :by ((eq/eq-subst int (lambda [z int] (equal T (g z) (inv-f (g (succ y))))) (pred (succ y)) y)
                   (int/pred-of-succ y)
                   <d2>))
        (have <d4> (equal T (f (g y)) (f (inv-f (g (succ y)))))
              :by ((eq/eq-cong T T f (g y) (inv-f (g (succ y))))
                   <d3>))
        (have <d5> (equal T (f (inv-f (g (succ y)))) (g (succ y)))
              :by ((fun/inverse-prop T T f b)
                   (g (succ y))))
        (have <d> (equal T (g (succ y)) (f (g y)))
              :by ((eq/eq-sym T (f (g y)) (g (succ y)))
                   ((eq/eq-trans T (f (g y)) (f (inv-f (g (succ y)))) (g (succ y)))
                    <d4> <d5>))))
      "We regroup the cases (or elimination)"
      (have <e> (equal T (g (succ y)) (f (g y)))
            :by (p/or-elim% 
                 (nat/int-split y)
                 (equal T (g (succ y)) (f (g y)))
                 <c> <d>))
      (qed <e>))))

(deflemma int-recur-bijection-lemma-2
  [[T :type] [f (==> T T)] [b (fun/bijective T T f)] [g (==> int T)]]
  (==> (forall [y int]
         (equal T (g (succ y)) (f (g y))))
       (forall [y int]
         (and (==> (positive (succ y))
                   (equal T (g (succ y)) (f (g y))))
              (==> (negative (pred y))
                   (equal T (g (pred y)) ((fun/inverse T T f b) (g y))))))))

(proof int-recur-bijection-lemma-2
    :script
  (pose inv-f := (fun/inverse T T f b))
  (assume [H (forall [y int]
               (equal T (g (succ y)) (f (g y))))]
    (assume [y int]
      (assume [Hpos (positive (succ y))]
        (have <a> (equal T (g (succ y)) (f (g y))) :by (H y)))
      (assume [Hneg (negative (pred y))]
        (have <b1> (equal T (g (succ (pred y))) (f (g (pred y))))
              :by (H (pred y)))
        (have <b2> (equal T (g y) (f (g (pred y))))
              :by ((eq/eq-subst int
                                (lambda [z int] (equal T (g z) (f (g (pred y)))))
                                (succ (pred y)) y)
                   (int/succ-of-pred y)
                   <b1>))
        (have <b3> (equal T (f (g (pred y))) (g y))
              :by ((eq/eq-sym T (g y) (f (g (pred y)))) <b2>))
        (have <b4> (equal T (inv-f (f (g (pred y)))) (inv-f (g y)))
              :by ((eq/eq-cong T T inv-f (f (g (pred y))) (g y))
                   <b3>))
        (have <b5> (equal T (inv-f (f (g (pred y)))) (g (pred y)))
              :by ((fun/inverse-prop-conv T T f b) (g (pred y))))
        (have <b6> (equal T (g (pred y)) (inv-f (f (g (pred y)))))
              :by ((eq/eq-sym T (inv-f (f (g (pred y)))) (g (pred y))) <b5>))
        (have <b> (equal T (g (pred y)) (inv-f (g y)))
              :by ((eq/eq-trans T (g (pred y)) (inv-f (f (g (pred y)))) (inv-f (g y)))
                   <b6> <b4>)))
      "regroup the two conjuncts."
      (have <c> _ :by (p/and-intro% <a> <b>))
      (qed <c>))))

(deflemma int-recur-bijection-lemma
  [[T :type] [f (==> T T)] [b (fun/bijective T T f)] [g (==> int T)]]
  (<=> (forall [y int]
         (and (==> (positive (succ y))
                   (equal T (g (succ y)) (f (g y))))
              (==> (negative (pred y))
                   (equal T (g (pred y)) ((fun/inverse T T f b) (g y))))))
       (forall [y int]
         (equal T (g (succ y)) (f (g y))))))

(proof int-recur-bijection-lemma
    :term
  ((p/iff-intro 
       (forall [y int]
         (and (==> (positive (succ y))
                   (equal T (g (succ y)) (f (g y))))
              (==> (negative (pred y))
                   (equal T (g (pred y)) ((fun/inverse T T f b) (g y))))))
     (forall [y int]
       (equal T (g (succ y)) (f (g y)))))
   (int-recur-bijection-lemma-1 T f b g)
   (int-recur-bijection-lemma-2 T f b g)))

(deflemma int-recur-bijection-ex
  [[T :type] [x T] [f (==> T T)] [b (fun/bijective T T f)]]
  (q/ex (==> int T)
        (int-recur-bijection-prop T x f b)))

(proof int-recur-bijection-ex
    :script
  (have <ex> (q/ex
                (==> int T)
                (int-recur-prop T x f (fun/inverse T T f b)))
        :by (p/and-elim-left% (int-recur T x f (fun/inverse T T f b))))
  "Our goal is to eliminate the existential."
  (assume [g (==> int T)
           Hg ((int-recur-prop T x f (fun/inverse T T f b)) g)] 
    (have <a> ((int-recur-bijection-prop T x f b) g)
          :by (p/and-intro%
               (p/and-elim-left% Hg)
               (((int-recur-bijection-lemma-1 T f b) g)
                (p/and-elim-right% Hg))))
    (have <b> (q/ex (==> int T)
                    (int-recur-bijection-prop T x f b))
          :by ((q/ex-intro (==> int T)
                           (int-recur-bijection-prop T x f b)
                           g)
               <a>)))
  (have <c> (q/ex (==> int T)
                  (int-recur-bijection-prop T x f b))
        :by ((q/ex-elim (==> int T)
                        (int-recur-prop T x f (fun/inverse T T f b))
                        (q/ex (==> int T)
                              (int-recur-bijection-prop T x f b)))
             <ex> <b>))
  (qed <c>))

(deflemma int-recur-bijection-single
  [[T :type] [x T] [f (==> T T)] [b (fun/bijective T T f)]]
  (q/single (==> int T)
            (int-recur-bijection-prop T x f b)))

(proof int-recur-bijection-single
    :script
  (have <single> (forall [g h (==> int T)]
                   (==> ((int-recur-prop T x f (fun/inverse T T f b)) g)
                        ((int-recur-prop T x f (fun/inverse T T f b)) h)
                        (equal (==> int T) g h)))
        :by (p/and-elim-right% (int-recur T x f (fun/inverse T T f b))))
  (assume [g (==> int T)
           h (==> int T)
           Hg ((int-recur-bijection-prop T x f b) g)
           Hh ((int-recur-bijection-prop T x f b) h)]
    (have <a> ((int-recur-prop T x f (fun/inverse T T f b)) g)
          :by (p/and-intro%
               (p/and-elim-left% Hg)
               ((int-recur-bijection-lemma-2 T f b g)
                (p/and-elim-right% Hg))))
    (have <b> ((int-recur-prop T x f (fun/inverse T T f b)) h)
          :by (p/and-intro%
               (p/and-elim-left% Hh)
               ((int-recur-bijection-lemma-2 T f b h)
                (p/and-elim-right% Hh))))
    (have <c> (equal (==> int T) g h)
          :by (<single> g h <a> <b>)))
  (qed <c>))

(proof int-recur-bijection
    :script
  (have <a> _ :by (p/and-intro%
                   (int-recur-bijection-ex T x f b)
                   (int-recur-bijection-single T x f b)))
  (qed <a>))

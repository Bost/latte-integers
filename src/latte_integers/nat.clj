(ns latte-integers.nat
  "The natural integers in ℕ as a subset of ℤ."

  (:refer-clojure :exclude [and or not int])

  (:require [latte.core :as latte :refer [defaxiom defthm definition
                                          lambda forall proof assume have
                                          ==>]]

            [latte.prop :as p :refer [and or not <=>]]

            [latte.equal :as eq :refer [equal]]

            [latte-integers.core :as int :refer [zero succ pred int]]

            [latte-sets.core :as set :refer [elem forall-in]]))

(definition nat-succ-prop
  "A property verified by all successors of natural integers."
  [[P (==> int :type)]]
  (forall [y int] (==> (P y) (P (succ y)))))

(definition nat
  "The subset of natural integers."
  []
  (lambda [x int]
    (forall [P (==> int :type)]
      (==> (and (P zero)
                (nat-succ-prop P))
           (P x)))))

(defthm nat-zero
  "Zero is a natural integer."
  []
  (elem int zero nat))

(proof nat-zero :script
  (assume [P (==> int :type)
           H (and (P zero)
                  (nat-succ-prop P))]
    (have a (P zero) :by (p/and-elim-left% H))
    (qed a)))

(defthm nat-succ
  "The successor of a natural integer is a natural integer."
  [[x int]]
  (==> (elem int x nat)
       (elem int (succ x) nat)))

(proof nat-succ :script
  (assume [H (elem int x nat)]
    (assume [Q (==> int :type)
             H2 (and (Q zero)
                     (nat-succ-prop Q))]
      (have a (==> (and (Q zero)
                        (nat-succ-prop Q))
                   (Q x)) :by (H Q))
      (have b (Q x) :by (a H2))
      (have c (==> (Q x) (Q (succ x)))
            :by ((p/and-elim-right% H2) x))
      (have d (Q (succ x)) :by (c b))
      (qed d))))

(defaxiom nat-zero-has-no-pred
  "An important axiom of the natural integer subset
wrt. [[pred]]."
  []
  (not (elem int (pred zero) nat)))

(defthm nat-zero-is-not-succ
  "Zero is not a successor of a natural integer.

This is the first Peano 'axiom' (here theorem, based
 on integers) for natural integers."
  []
  (forall [x int]
    (==> (elem int x nat)
         (not (equal int (succ x) zero)))))

(proof nat-zero-is-not-succ :script
  (assume [x int
           H (elem int x nat)]
    (assume [H2 (equal int (succ x) zero)]
      (have a (equal int (pred (succ x)) (pred zero))
            :by ((eq/eq-cong int int pred (succ x) zero) H2))
      (have b (equal int x (pred (succ x)))
            :by ((eq/eq-sym int (pred (succ x)) x)
                 (int/pred-of-succ x)))
      (have c (equal int x (pred zero))
            :by ((eq/eq-trans int x (pred (succ x)) (pred zero))
                 b a))
      (have d (elem int (pred zero) nat)
            :by ((eq/eq-subst int nat x (pred zero))
                 c H))
      (have e p/absurd :by (nat-zero-has-no-pred d))
      (qed e))))

(defthm nat-succ-injective
  "Successor is injective, the second Peano 'axiom'
here a simple consequence of [[succ-injective]]."
  []
  (forall [x y int]
    (==> (elem int x nat)
         (elem int y nat)
         (equal int (succ x) (succ y))
         (equal int x y))))

(proof nat-succ-injective :script
  (assume [x int
           y int
           H1 (elem int x nat)
           H2 (elem int y nat)
           H3 (equal int (succ x) (succ y))]
    (have a (equal int x y)
          :by (int/succ-injective x y H3))
    (have b _ :discharge [H1 H2 H3 a])
    (qed b)))

(defthm nat-induct
  "The induction principle for natural integers.
This is the third Peano axiom but it can be
derived from [[int-induct]]."
  [[P (==> int :type)]]
  (==> (P zero)
       (forall [x int]
         (==> (elem int x nat)
              (P x)
              (P (succ x))))
       (forall [x int]
         (==> (elem int x nat)
              (P x)))))

(proof nat-induct :script
  (have Q _ :by (lambda [z int]
                  (and (elem int z nat)
                       (P z))))
  (assume [Hz (P zero)
           Hs (forall [x int]
                (==> (elem int x nat)
                     (P x)
                     (P (succ x))))]
    (have <a> (Q zero)
          :by (p/and-intro% 
               nat-zero Hz))
    (assume [y int
             Hy (Q y)]
      (have <b> (elem int y nat)
            :by (p/and-elim-left% Hy))
      (have <c> (P y)
            :by (p/and-elim-right% Hy))
      (have <d> (elem int (succ y) nat)
            :by ((nat-succ y) <b>))
      (have <e> (==> (P y) (P (succ y)))
            :by (Hs y <b>))
      (have <f> (P (succ y)) :by (<e> <c>))
      (have <g> (Q (succ y)) :by (p/and-intro% <d> <f>))
      (have <h> (nat-succ-prop Q) :discharge [y Hy <g>]))
    (have <i> (and (Q zero)
                   (nat-succ-prop Q)) :by (p/and-intro% <a> <h>))
    (assume [x int
             Hx (elem int x nat)]
      (have <j> (Q x) :by (Hx Q <i>))
      (have <k> (P x) :by (p/and-elim-right% <j>))
      (have <l> (forall [x int]
                  (==> (elem int x nat)
                       (P x))) :discharge [x Hx <k>])
      (qed <l>))))

(defthm nat-pred-split
  "A split theorem for natural numbers."
  []
  (forall-in [x int nat]
    (==> (not (equal int x zero))
         (elem int (pred x) nat))))

(proof nat-pred-split
    :script
  (have P _ :by (lambda [x int]
                  (==> (not (equal int x zero))
                       (elem int (pred x) nat))))
  "Let's proceed by induction"
  "First with (P zero)"
  (assume [Hnz (not (equal int zero zero))]
    (have <a1> (equal int zero zero) :by (eq/eq-refl int zero))
    (have <a2> p/absurd :by (Hnz <a1>))
    (have <a3> (elem int (pred zero) nat) :by (<a2> (elem int (pred zero) nat)))
    (have <a> (P zero) :discharge [Hnz <a3>]))
  "Then the inductive case."
  (assume [n int
           Hn (elem int n nat)
           Hind (P n)]
    "We aim to prove (P (succ n))"
    (assume [Hs (not (equal int (succ n) zero))]
      (have <b1> (equal int n (pred (succ n)))
            :by ((eq/eq-sym int (pred (succ n)) n) (int/pred-of-succ n)))
      (have <b2> (elem int (pred (succ n)) nat)
            :by ((eq/eq-subst int nat n (pred (succ n)))
                 <b1> Hn))
      (have <b3> (P (succ n)) :discharge [Hs <b2>]))
    (have <b> _ :discharge [n Hn Hind <b3>]))
  (have <c> (forall-in [x int nat] (P x))
        :by ((nat-induct P) <a> <b>))
  (qed <c>))



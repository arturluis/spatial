package pcc.traversal
package transform

import pcc.core._

trait SubstTransformer extends Transformer {
  val allowUnsafeSubst: Boolean = false
  val allowDuplication: Boolean = false

  var subst: Map[Sym[_],Sym[_]] = Map.empty

  /**
    * Register a substitution rule.
    * Usage: register(a -> a').
    */
  def register[A](rule: (A,A)): Unit = register(rule._1,rule._2)

  /**
    * Register an unsafe substitution rule.
    * where a' replaces a but a' is not a subtype of a.
    */
  def registerUnsafe[A,B](rule: (A,B)): Unit = register(rule._1,rule._2,unsafe = true)

  /**
    * Register a substitution rule orig -> sub.
    * If unsafe is true, does not do type checking.
    */
  def register[A,B](orig: A, sub: B, unsafe: Boolean = allowUnsafeSubst): Unit = (orig,sub) match {
    case (s1: Sym[_], s2: Sym[_]) =>
      if (s2 <:< s1 || unsafe) subst += s1 -> s2
      else throw new Exception(s"Substitution $s1 -> $s2: $s2 is not a subtype of $s1")

    case _ => throw new Exception(s"Cannot register non-symbol ${orig.getClass}, ${sub.getClass}")
  }


  override protected def transformSym[T](sym: Sym[T]): Sym[T] = subst.get(sym) match {
    case Some(y) => y.asInstanceOf[Sym[T]]
    case None if sym.isSymbol && !allowDuplication =>
      throw new Exception(s"Used untransformed symbol $sym!")
    case None => sym
  }

  /**
    * Isolate all substitution rules created within the given scope.
    * Substitution rules are reset at the end of this scope.
    */
  def isolateSubst[A](scope: => A): A = {
    val save = subst
    val result = scope
    subst = save
    result
  }

  /**
    * Isolate the substitution rules created within the given scope,
    * with the given rule(s) added within the scope prior to evaluation.
    */
  def isolateSubstWith[A](rules: (Sym[_],Sym[_])*)(scope: => A): A = {
    isolateSubst{
      rules.foreach{rule => register(rule) }
      scope
    }
  }

}

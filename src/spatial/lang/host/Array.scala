package spatial.lang
package host

import core._
import forge.tags._
import spatial.node._

/** A one-dimensional array on the host. */
@ref class Array[A:Type] extends Top[Array[A]] with Ref[scala.Array[Any],Array[A]] {
  override val __isPrimitive = false

  /** Returns the size of this Array. */
  @api def length: I32 = stage(ArrayLength(this))

  /** Returns the element at index `i`. */
  @api def apply(i: I32): A = stage(ArrayApply(this, i))

  /** Updates the element at index `i` to data. */
  @api def update(i: I32, data: A): Void = stage(ArrayUpdate(this, i, data))

  /** Applies the function **func** on each element in the Array. */
  @api def foreach(func: A => Void): Void = {
    val arr = bound[Array[A]]
    val i   = bound[I32]
    val applyBlk = stageLambda2(arr,i){ arr(i) }
    val funcBlk  = stageLambda1(applyBlk.result){ func(applyBlk.result.unbox) }
    stage(ArrayForeach(this, applyBlk, funcBlk))
  }

  /** Returns a new Array created using the mapping `func` over each element in this Array. */
  @api def map[B:Type](func: A => B): Array[B] = {
    val arr = bound[Array[A]]
    val i   = bound[I32]
    val applyBlk = stageLambda2(arr,i){ arr(i) }
    val funcBlk  = stageLambda1(applyBlk.result){ func(applyBlk.result.unbox) }
    stage(ArrayMap(arr,applyBlk,funcBlk))
  }

  /** Returns a new Array created using the pairwise mapping `func` over each element in this Array
    * and the corresponding element in `that`.
    */
  @api def zip[B:Type,C:Type](that: Array[B])(func: (A,B) => C): Array[C] = {
    val arrA = bound[Array[A]]
    val arrB = bound[Array[B]]
    val i    = bound[I32]
    val applyA = stageLambda2(arrA,i){ arrA(i) }
    val applyB = stageLambda2(arrB,i){ arrB(i) }
    val funcBlk = stageLambda2(applyA.result,applyB.result){ func(applyA.result.unbox,applyB.result.unbox) }
    stage(ArrayZip(this,that,applyA,applyB,funcBlk))
  }

  /** Reduces the elements in this Array into a single element using associative function `rfunc`. */
  @api def reduce(rfunc: (A,A) => A): A = {
    val arr = bound[Array[A]]
    val i   = bound[I32]
    val a1  = bound[A]
    val a2  = bound[A]
    val applyBlk  = stageLambda2(arr,i){ arr(i) }
    val reduceBlk = stageLambda2(a1,a2){ rfunc(a1,a2) }
    stage(ArrayReduce(this,applyBlk,reduceBlk))
  }

  /**
    * Reduces the elements in this Array and the given initial value into a single element
    * using associative function `rfunc`.
    */
  @api def fold(init: A)(rfunc: (A,A) => A): A = {
    val arr = bound[Array[A]]
    val i   = bound[I32]
    val a1  = bound[A]
    val a2  = bound[A]
    val applyBlk  = stageLambda2(arr,i){ arr(i) }
    val reduceBlk = stageLambda2(a1,a2){ rfunc(a1,a2) }
    stage(ArrayFold(this,init,applyBlk,reduceBlk))
  }

  /** Returns a new Array with all elements in this Array which satisfy the given predicate `cond`. */
  @api def filter(cond: A => Bit): Array[A] = {
    val arr = bound[Array[A]]
    val i   = bound[I32]
    val applyBlk = stageLambda2(arr,i){ arr(i) }
    val condBlk  = stageLambda1(applyBlk.result){ cond(applyBlk.result.unbox) }
    stage(ArrayFilter(this,applyBlk,condBlk))
  }

  /** Returns a new Array created by concatenating the results of `func` applied to all elements in this Array. */
  @api def flatMap[B:Type](func: A => Array[B]): Array[B] = {
    val arr = bound[Array[A]]
    val i   = bound[I32]
    val applyBlk = stageLambda2(arr,i){ arr(i) }
    val funcBlk  = stageLambda1(applyBlk.result){ func(applyBlk.result.unbox) }
    stage(ArrayFlatMap(this,applyBlk,funcBlk))
  }

  @api def forall(func: A => Bit): Bit = this.map(func).reduce{_&&_}
  @api def exists(func: A => Bit): Bit = this.map(func).reduce{_||_}

  /** Returns a string representation using the given `delimeter`. */
  @api def mkString(delimeter: Text): Text = this.mkString("", delimeter, "")

  /** Returns a string representation using the given `delimeter`, bracketed by `start` and `stop`. */
  @api def mkString(start: Text, delimeter: Text, stop: Text): Text = {
    stage(ArrayMkString(this,start,delimeter,stop))
  }

  /** Returns true if this Array and `that` contain the same elements, false otherwise. */
  @api override def neql(that: Array[A]): Bit = this.zip(that){(x,y) => x === y }.forall(x => x)

  /** Returns true if this Array and `that` differ by at least one element, false otherwise. */
  @api override def eql(that: Array[A]): Bit = this.zip(that){(x,y) => x !== y }.exists(x => x)

  @api override def toText: Text = this.mkString(", ")
}
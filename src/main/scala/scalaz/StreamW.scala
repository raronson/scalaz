package scalaz

sealed trait StreamW[A] {
  val value: Stream[A]

  import S._
  import MA._

  def string(f: A => Char) = value map f mkString

  def stringj(f: A => Stream[Char]) = value flatMap f mkString

  def |!| = ZipStream.zip(value)

  def merge(s: Stream[A]): Stream[A] =
    if (value.isEmpty) Stream.empty
    else Stream.cons(value.head, s.merge(value.tail))

  def zipper = value match {
    case Stream.empty => None
    case Stream.cons(h, t) => Some(S.zipper(Stream.empty, h, t))
  }

  def zipperEnd = value match {
    case Stream.empty => None
    case _ => {
      val x = value.reverse
      Some(S.zipper(x.tail, x.head, Stream.empty))  
    }
  }

  def zapp[B, C](fs: ZipStream[A => B => C]) = S.zip(value) <*> fs

  def zipWith[B, C](f: A => B => C, bs: ZipStream[B]) = bs <*> zapp(f.repeat[ZipStream])

  def unfoldForest[B](f: A => (B, Stream[A])): Stream[Tree[B]] = value.map(_.unfoldTree(f))

  def unfoldForestM[B,M[_]](f: A => M[(B, Stream[A])])(implicit m: Monad[M]): M[Stream[Tree[A]]] = 
    value.traverse(_.unfoldTreeM(f))
}

object StreamW {
  implicit def StreamTo[A](as: Stream[A]): StreamW[A] = new StreamW[A] {
    val value = as
  }

  implicit def StreamFrom[A](as: StreamW[A]): Stream[A] = as.value
}

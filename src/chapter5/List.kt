package chapter5

sealed class List<A> {

    fun cons(a: A): List<A> = Cons(a, this)

    fun setHead(a: A): List<A> = when(this) {
        Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> tail.cons(a)
    }

    fun drop(n: Int): List<A> = drop(this, n)

    fun dropWhile(p: (A) -> Boolean): List<A> = dropWhile(this, p)

    abstract fun isEmpty(): Boolean

    private object Nil: List<Nothing>() {

        override fun isEmpty(): Boolean = true

        override fun toString(): String = "[NIL]"
    }

    private class Cons<A>(
            internal val head: A,
            internal val tail: List<A>): List<A>() {

        override fun isEmpty(): Boolean = false

        override fun toString(): String = "[${toString("", this)}NIL]"

        tailrec fun toString(acc: String, list: List<A>): String =
                when(list) {
                    is Nil -> acc
                    is Cons -> toString("$acc${list.head}", list.tail)
                }
    }

    companion object {

        operator
        fun <A> invoke(vararg az:A): List<A> =
                az.foldRight(Nil as List<A>) { a: A, list: List<A> -> Cons(a, list)}

        tailrec fun <A> drop(list: List<A>, n: Int): List<A> = when (list) {
            List.Nil -> list
            is List.Cons -> if (n <= 0) list else drop(list.tail, n - 1)
        }

        tailrec fun <A> dropWhile(list: List<A>, p: (A) -> Boolean): List<A> =
                when(list) {
                    Nil -> list
                    is Cons -> if(p(list.head)) dropWhile(list.tail, p) else list
                }
    }

}
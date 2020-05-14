package chapter5

sealed class List<A> {

    fun cons(a: A): List<A> = Cons(a, this)

    fun setHead(a: A): List<A> = when (this) {
        Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> tail.cons(a)
    }

    fun drop(n: Int): List<A> = drop(this, n)

    fun dropWhile(p: (A) -> Boolean): List<A> = dropWhile(this, p)

    fun init(): List<A> = reverse().drop(1).reverse()

    fun reverse(): List<A> = foldLeft(List.invoke(), { acc -> { acc.cons(it) } })

    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B = foldRight(this, identity, f)

    fun <B> foldRightViaFoldLeft(identity: B, f: (A) -> (B) -> B): B =
        this.reverse().foldLeft(identity) { a -> { b -> f(b)(a) } }

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B = foldLeft(this, identity, f)

    fun length(): Int = foldRight(0) { { it + 1 } }

    abstract fun isEmpty(): Boolean

    private object Nil : List<Nothing>() {

        override fun isEmpty(): Boolean = true

        override fun toString(): String = "[NIL]"
    }

    internal class Cons<A>(
        val head: A,
        val tail: List<A>
    ) : List<A>() {

        override fun isEmpty(): Boolean = false

        override fun toString(): String = "[${toString("", this)}NIL]"

        tailrec fun toString(acc: String, list: List<A>): String =
            when (list) {
                is Nil -> acc
                is Cons -> toString("$acc${list.head}", list.tail)
            }
    }

    companion object {

        operator
        fun <A> invoke(vararg az: A): List<A> =
            az.foldRight(Nil as List<A>) { a: A, list: List<A> -> Cons(a, list) }

        tailrec fun <A> drop(list: List<A>, n: Int): List<A> = when (list) {
            List.Nil -> list
            is List.Cons -> if (n <= 0) list else drop(list.tail, n - 1)
        }

        tailrec fun <A> dropWhile(list: List<A>, p: (A) -> Boolean): List<A> =
            when (list) {
                Nil -> list
                is Cons -> if (p(list.head)) dropWhile(list.tail, p) else list
            }

        tailrec fun <A> reverse(acc: List<A>, list: List<A>): List<A> =
            when (list) {
                is Nil -> acc
                is Cons -> reverse(acc.cons(list.head), list.tail)
            }

        fun <A, B> foldRight(list: List<A>, identity: B, f: (A) -> (B) -> B): B =
            when (list) {
                List.Nil -> identity
                is List.Cons -> f(list.head)(foldRight(list.tail, identity, f))
            }

        tailrec fun <A, B> foldLeft(list: List<A>, identity: B, f: (B) -> (A) -> B): B =
            when (list) {
                List.Nil -> identity
                is List.Cons -> foldLeft(list.tail, f(identity)(list.head), f)
            }

        fun sum(list: List<Int>): Int = foldLeft(list, 0) { a -> { b -> a + b } }
        fun product(list: List<Int>): Int = foldLeft(list, 1) { a -> { b -> a * b } }
    }

}
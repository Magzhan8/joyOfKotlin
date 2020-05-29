package chapter9

import chapter5.List
import chapter7.Result
import java.lang.Math.random

sealed class Stream<out A> {

    abstract fun isEmpty(): Boolean

    abstract fun head(): Result<A>

    abstract fun tail(): Result<Stream<A>>

    abstract fun takeAtMost(n: Int): Stream<A>

    abstract fun dropAtMost(n: Int): Stream<A>

    abstract fun takeWhile(p: (A) -> Boolean): Stream<A>

    abstract fun <B> foldRight(z: Lazy<B>, f: (A) -> (Lazy<B>) -> B): B

    fun dropWhile(p: (A) -> Boolean): Stream<A> = Companion.dropWhile(this, p)

    fun takeWhileViaFoldRight(p: (A) -> Boolean): Stream<A> =
            foldRight(Lazy { Empty }) { a ->
                { b: Lazy<Stream<A>> ->
                    if (p(a)) cons(Lazy { a }, b)
                    else Empty
                }
            }

    private object Empty : Stream<Nothing>() {

        override fun dropAtMost(n: Int): Stream<Nothing> = this

        override fun takeAtMost(n: Int): Stream<Nothing> = this

        override fun isEmpty(): Boolean = true

        override fun head(): Result<Nothing> = Result()

        override fun tail(): Result<Stream<Nothing>> = Result()

        override fun takeWhile(p: (Nothing) -> Boolean): Stream<Nothing> = this

        override fun <B> foldRight(z: Lazy<B>, f: (Nothing) -> (Lazy<B>) -> B): B = z()

    }

    private class Cons<out A>(internal val hd: Lazy<@UnsafeVariance A>, internal val tl: Lazy<Stream<@UnsafeVariance A>>) : Stream<A>() {

        override fun dropAtMost(n: Int): Stream<A> = Companion.dropAtMost(n, this)

        override fun takeAtMost(n: Int): Stream<A> = when {
            n > 0 -> cons(hd, Lazy { tl().takeAtMost(n - 1) })
            else -> Empty
        }

        override fun isEmpty(): Boolean = false

        override fun head(): Result<A> = Result(hd())

        override fun tail(): Result<Stream<A>> = Result(tl())

        override fun takeWhile(p: (A) -> Boolean): Stream<A> = when {
            p(hd()) -> cons(hd, Lazy { tl().takeWhile(p) })
            else -> Empty
        }

        override fun <B> foldRight(z: Lazy<B>, f: (A) -> (Lazy<B>) -> B): B = f(hd())(Lazy { tl().foldRight(z, f) })

    }

    companion object {

        fun <A> cons(hd: Lazy<A>, tl: Lazy<Stream<A>>): Stream<A> = Cons(hd, tl)

        operator
        fun <A> invoke(): Stream<A> = Empty

        fun <A> repeat(f: () -> A): Stream<A> = cons(Lazy { f() }, Lazy { repeat(f) })

        fun from(i: Int): Stream<Int> = iterate(i) { it + 1 }

        tailrec fun <A> dropAtMost(n: Int, stream: Stream<A>): Stream<A> = when {
            n > 0 -> when (stream) {
                is Empty -> stream
                is Cons -> dropAtMost(n - 1, stream.tl())
            }
            else -> stream
        }

        tailrec fun <A> dropWhile(stream: Stream<A>, p: (A) -> Boolean): Stream<A> = when (stream) {
            is Empty -> stream
            is Cons -> when {
                p(stream.hd()) -> dropWhile(stream.tl(), p)
                else -> stream
            }
        }

        tailrec fun <A> exists(stream: Stream<A>, p: (A) -> Boolean): Boolean = when (stream) {
            is Empty -> false
            is Cons -> when {
                p(stream.hd()) -> true
                else -> exists(stream.tl(), p)
            }
        }

        fun <A> toList(list: List<A>, stream: Stream<A>): List<A> {
            tailrec fun <A> toList(list: List<A>, stream: Stream<A>): List<A> =
                    when (stream) {
                        is Empty -> list
                        is Cons -> toList(list.cons(stream.hd()), stream.tl())
                    }
            return toList(list, stream).reverse()
        }

        fun <A> iterate(seed: A, f: (A) -> A): Stream<A> =
                cons(Lazy { seed }, Lazy { iterate(f(seed), f) })
    }

}


fun main() {
    val stream = Stream.repeat(::random).dropAtMost(60000).takeAtMost(60000)
    Stream.toList(List(), stream)
    stream.head().forEach(::println)

    val stream2 = Stream.iterate(1, { it + 1 }).takeWhile { it < 100 }
    println(Stream.toList(List(), stream2))
}
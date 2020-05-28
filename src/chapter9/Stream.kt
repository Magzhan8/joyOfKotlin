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

    private object Empty : Stream<Nothing>() {

        override fun dropAtMost(n: Int): Stream<Nothing> = this

        override fun takeAtMost(n: Int): Stream<Nothing> = this

        override fun isEmpty(): Boolean = true

        override fun head(): Result<Nothing> = Result()

        override fun tail(): Result<Stream<Nothing>> = Result()

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

    }

    companion object {

        fun <A> cons(hd: Lazy<A>, tl: Lazy<Stream<A>>): Stream<A> = Cons(hd, tl)

        operator
        fun <A> invoke(): Stream<A> = Empty

        fun <A> repeat(f: () -> A): Stream<A> = cons(Lazy { f() }, Lazy { repeat(f) })

        fun from(i: Int): Stream<Int> = cons(Lazy { i }, Lazy { from(i + 1) })

        tailrec fun <A> dropAtMost(n: Int, stream: Stream<A>): Stream<A> = when {
            n > 0 -> when (stream) {
                is Empty -> stream
                is Cons -> dropAtMost(n - 1, stream.tl())
            }
            else -> stream
        }

        fun <A> toList(list: List<A>, stream: Stream<A>): List<A> {
            tailrec fun <A> toList(list: List<A>, stream: Stream<A>): List<A> =
                    when(stream) {
                        is Empty -> list
                        is Cons -> toList(list.cons(stream.hd()), stream.tl())
                    }
            return toList(list, stream).reverse()
        }

    }

}


fun main() {
    val stream = Stream.repeat(::random).dropAtMost(60000).takeAtMost(60000)
    Stream.toList(List(), stream)
    stream.head().forEach(::println)
}
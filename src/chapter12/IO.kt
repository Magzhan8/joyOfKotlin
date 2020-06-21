package chapter12

import chapter5.List
import chapter9.Lazy
import chapter9.Stream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.IllegalStateException

class IO<out A>(private val f: () -> A) {

    operator
    fun invoke() = f()

    operator
    fun plus(io: IO<@UnsafeVariance A>) = IO {
        f()
        io.f()
    }

    fun <B> map(g: (A) -> B): IO<B> = IO { g(this()) }

    fun <B> flatMap(g: (A) -> IO<B>): IO<B> = IO { g(this())() }

    fun <A> repeat(n: Int, io: IO<A>): IO<List<A>> = Stream.fill(n, Lazy { io })
            .foldRight(Lazy { IO { List<A>() } }) { ioa ->
                { sioLa ->
                    map2(ioa, sioLa()) { a -> { la: List<A> -> la.cons(a) } }
                }
            }

    object Console {

        private val br = BufferedReader(InputStreamReader(System.`in`))

        fun readln(): IO<String> = IO {
            try {
                br.readLine()
            } catch (e: IOException) {
                throw IllegalStateException(e)
            }
        }

        fun println(o: Any): IO<Unit> = IO { kotlin.io.println(o.toString()) }

        fun print(o: Any): IO<Unit> = IO { kotlin.io.print(o.toString()) }
    }

    companion object {
        val empty: IO<Unit> = IO {}

        operator
        fun <A> invoke(a: A): IO<A> = IO { a }

        fun <A, B, C> map2(ioa: IO<A>, iob: IO<B>, f: (A) -> (B) -> C): IO<C> = ioa.flatMap { a -> iob.map { b -> f(a)(b) } }

    }


}
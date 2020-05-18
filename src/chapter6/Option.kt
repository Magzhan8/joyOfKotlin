package chapter6

import chapter5.List
import java.util.*

sealed class Option<out A> {

    abstract fun isEmpty(): Boolean

    fun getOrElse(default: () -> @UnsafeVariance A): A =
            when (this) {
                is None -> default()
                is Some -> value
            }

    fun getOrElse(default: @UnsafeVariance A): A =
            when (this) {
                is None -> default
                is Some -> value
            }

    fun orElse(default: () -> Option<@UnsafeVariance A>): Option<A> = map { this }.getOrElse(default)

    fun <B> map(f: (A) -> B): Option<B> =
            when (this) {
                is None -> None
                is Some -> Some(f(value))
            }

    fun <B> flatMap(f: (A) -> Option<B>): Option<B> = map(f).getOrElse(None)

    fun filter(p: (A) -> Boolean): Option<A> = flatMap { x -> if (p(x)) this else None }

    internal object None : Option<Nothing>() {

        override fun isEmpty(): Boolean = true

        override fun equals(other: Any?): Boolean = other === None

        override fun hashCode(): Int = 0

        override fun toString(): String = "None"
    }

    internal data class Some<out A>(internal val value: A) : Option<A>() {

        override fun isEmpty(): Boolean = false
    }

    companion object {

        operator
        fun <A> invoke(a: A? = null): Option<A> =
                when (a) {
                    null -> None
                    else -> Some(a)
                }

        fun <A, B> lift(f: (A) -> (B)): (Option<A>) -> Option<B> = {
            try {
                it.map(f)
            } catch (e: Exception) {
                invoke()
            }
        }

        fun <A, B> hLift(f: (A) -> (B)): (A) -> Option<B> = {
            try {
                Option(it).map(f)
            } catch (e: Exception) {
                invoke()
            }
        }

        fun <A, B, C> map2(oa: Option<A>,
                           ob: Option<B>,
                           f: (A) -> (B) -> C): Option<C> =
                oa.flatMap { a -> ob.map { b -> f(a)(b) } }

        fun <A, B, C, D> map3(oa: Option<A>,
                              ob: Option<B>,
                              oc: Option<C>,
                              f: (A) -> (B) -> (C) -> D) =
                oa.flatMap { a -> ob.flatMap { b -> oc.map { c -> f(a)(b)(c) } } }

        fun <A> sequence(list: List<Option<A>>): Option<List<A>> = traverse(list) { x -> x }

        fun <A, B> traverse(list: List<A>, f: (A) -> Option<B>): Option<List<B>> =
                list.foldRight(Option(List())) { x ->
                    { y: Option<List<B>> ->
                        map2(f(x), y) { a ->
                            { b: List<B> -> b.cons(a) }
                        }
                    }
                }

    }

}

fun main() {
    val max1 = max(listOf(3, 5, 7, 2, 1)).getOrElse(::getDefault)
    println(max1)
    val max2 = max(listOf()).orElse { Option(-1) }.map { it * 2 }.getOrElse(1)
    println(max2)

}

val mean: (List<Double>) -> Option<Double> = { list ->
    when {
        list.isEmpty() -> Option()
        else -> Option(list.sum() / list.size)
    }
}

val variance: (List<Double>) -> Option<Double> = { list ->
    mean(list).flatMap { m ->
        mean(list.map { x -> Math.pow((x - m), 2.0) })
    }
}


fun max(list: List<Int>): Option<Int> = Option(list.max())
fun getDefault(): Int = throw RuntimeException()
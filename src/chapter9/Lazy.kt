package chapter9

import chapter5.List
import chapter7.Result


class Lazy<A>(val f: () -> A) : () -> A {

    private val value: A by lazy(f)

    operator override fun invoke(): A = value

    fun <B> map(f: (A) -> B): Lazy<B> = Lazy { f(value) }

    fun <B> flatMap(f: (A) -> Lazy<B>): Lazy<B> = Lazy { f(value)() }
}

fun <A, B, C> lift2(f: (A) -> (B) -> C): (Lazy<A>) -> (Lazy<B>) -> Lazy<C> =
        { a ->
            { b ->
                Lazy { f(a())(b()) }
            }
        }

fun <A> sequence(list: List<Lazy<A>>): Lazy<List<A>> =
        Lazy { list.map { it() } }

fun <A> sequenceResult(list: List<Lazy<A>>): Lazy<Result<List<A>>> =
        Lazy { chapter8.sequence(list.map { Result(it()) }) }


fun main(args: Array<String>) {
    val first = Lazy {
        println("Evaluating first")
        true
    }
    val second: Lazy<Boolean> = Lazy {
        println("Evaluating second")
        throw IllegalStateException()
    }
    println(first() || second())
    println(first() || second())
    println(or(first, second))

    val greetings = Lazy {
        println("Evaluating greetings")
        "Hello"
    }
    val name = Lazy {
        println("Computing name")
        "Mickey"
    }

    val message = constructMessage(greetings, name)
    println(if (false) message() else "")
    println(if (true) message() else "")

    val constructMessage: (Lazy<String>) -> (Lazy<String>) -> Lazy<String> =
            { first ->
                { second ->
                    Lazy { "${first()}, ${second()}" }
                }
            }
}

fun constructMessage(greetings: Lazy<String>, name: Lazy<String>): Lazy<String> = Lazy { "${greetings()},  ${name()}" }

fun or(a: Lazy<Boolean>, b: Lazy<Boolean>): Boolean = if (a()) true else b()
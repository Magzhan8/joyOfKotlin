fun inc(n: Int) = n + 1
fun dec(n: Int) = n - 1

tailrec fun add(a: Int, b: Int): Int = if (b == 0) a else add(inc(a), dec(b))

object Factorial {
    private lateinit var f: (Int) -> Int

    init {
        f = { n: Int ->
            if (n <= 1) n
            else n * f(n - 1)
        }
    }

    val calc = f
}

fun factorial(n: Int): Int {
    fun factorial(n: Int, acc: Int): Int =
        if (n <= 1) acc
        else factorial(dec(n), acc * n)
    return factorial(n, 1)
}

fun sum(list: List<Int>): Int =
    if (list.isEmpty()) 0
    else list.head() + sum(list.tail())

fun <T> List<T>.head(): T =
    if (this.isEmpty()) throw IllegalArgumentException("head called on empty list")
    else this[0]

fun <T> List<T>.tail(): List<T> =
    if (this.isEmpty()) throw IllegalArgumentException("tail called on empty list")
    else this.drop(1)

fun fibonacci(n: Int): Int =
    if (n == 0 || n == 1) 1
    else fibonacci(n - 1) + fibonacci(n - 2)


fun fibonacci_(n: Int): Int {
    tailrec fun fibonacci_(v1: Int, v2: Int, n: Int): Int =
        if (n == 0) 1
        else if (n == 1) v1 + v2
        else fibonacci_(v2, v1 + v2, dec(n))
    return fibonacci_(0, 1, n)
}

fun <T> makeString(list: List<T>, delim: String): String =
    when {
        list.isEmpty() -> ""
        list.tail().isEmpty() -> "${list.head()}${makeString(list.tail(), delim)}"
        else -> "${list.head()}$delim${makeString(list.tail(), delim)}"
    }

fun <T> makeString_(list: List<T>, delim: String): String {
    tailrec fun makeString_(list: List<T>, acc: String): String =
        if (list.isEmpty()) acc
        else if (acc.isEmpty()) makeString_(list.tail(), "${list.head()}")
        else makeString_(list.tail(), "$acc$delim${list.head()}")
    return makeString_(list, "")
}

fun <T, U> foldLeft(list: List<T>, acc: U, f: (U, T) -> U): U {
    tailrec fun foldLeft(list: List<T>, acc: U): U =
        if (list.isEmpty()) acc
        else foldLeft(list.tail(), f(acc, list.head()))
    return foldLeft(list, acc)
}

fun <T, U> foldRight(list: List<T>, identity: U, f: (T, U) -> U): U =
    if (list.isEmpty()) identity
    else f(list.head(), foldRight(list.tail(), identity, f))

fun sumInt(list: List<Int>) = foldLeft(list, 0, Int::plus)
fun string(list: List<Char>) = foldLeft(list, "", String::plus)
fun <T> makeString_2(list: List<T>, delim: String): String =
    foldLeft(list, "") { s, t -> if (s.isEmpty()) "$t" else "$s$delim$t" }

fun <T> prepend(list: List<T>, elem: T): List<T> = listOf(elem) + list
fun <T> prepend_(list: List<T>, elem: T): List<T> = foldLeft(list, listOf(elem), { s, t -> s + t })

fun <T> reverse(list: List<T>): List<T> = foldLeft(list.tail(), listOf(), ::prepend)

fun <T> unfold(seed: T, f: (T) -> T, p: (T) -> Boolean): List<T> =
    if (p(seed)) prepend(unfold(f(seed), f, p), seed)
    else listOf()

fun <T> unfold_(seed: T, f: (T) -> T, p: (T) -> Boolean): List<T> {
    tailrec fun unfold_(acc: List<T>, seed: T): List<T> =
        if(p(seed)) unfold_(acc + seed, f(seed))
        else acc
    return unfold_(listOf(), seed)
}
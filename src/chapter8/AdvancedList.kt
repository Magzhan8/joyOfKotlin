package chapter8

import chapter5.List
import chapter7.Result

fun <A> flattenResult(list: List<Result<A>>): List<A> =
        list.flatMap { a -> a.map { List(it) }.getOrElse { List.invoke() } }

fun <A> sequence(list: List<Result<A>>): Result<List<A>> =
        list.foldRight(Result(List())) { x ->
            { y: Result<List<A>> -> Result.map2(x, y) { a -> { b: List<A> -> b.cons(a) } } }
        }

fun <A> sequence2(list: List<Result<A>>): Result<List<A>> =
        traverse(list) { a -> a }

fun <A, B> traverse(list: List<A>, f: (A) -> Result<B>): Result<List<B>> =
        list.foldRight(Result(List())) { x ->
            { y: Result<List<B>> -> Result.map2(f(x), y) { a -> { b: List<B> -> b.cons(a) } } }
        }

fun <A, B, C> zipWith(list1: List<A>,
                      list2: List<B>,
                      f: (A) -> (B) -> C): List<C> {
    tailrec
    fun zipWith(acc: List<C>,
                list1: List<A>,
                list2: List<B>): List<C> =
            when (list1) {
                List.Nil -> acc
                is List.Cons -> when (list2) {
                    List.Nil -> acc
                    is List.Cons -> zipWith(acc.cons(f(list1.head)(list2.head)),
                            list1.tail, list2.tail)
                }
            }
    return zipWith(List(), list1, list2).reverse()
}

fun <A, B, C> product(list1: List<A>,
                      list2: List<B>,
                      f: (A) -> (B) -> C): List<C> =
        list1.flatMap { a -> list2.map { f(a)(it) } }

fun <A, B> unzip(list: List<Pair<A, B>>): Pair<List<A>, List<B>> = unzip(list) { it }

fun <A, A1, A2> unzip(list: List<A>, f: (A) -> Pair<A1, A2>): Pair<List<A1>, List<A2>> =
        list.foldRight(Pair(List(), List())) { a ->
            { b: Pair<List<A1>, List<A2>> ->
                val pair = f(a)
                Pair(b.first.cons(pair.first),
                        b.second.cons(pair.second))
            }
        }

fun <A> getAt(index: Int, list: List<A>): Result<A> {
    tailrec fun getAt(i: Int, tail: List.Cons<A>): Result<A> =
            if (index == 0) Result(tail.head)
            else getAt(i - 1, tail.tail)
    return if (index < 0 || index >= list.length())
        Result.failure("Index out of bound")
    else getAt(index, list)
}


fun main() {
    val list1 = List("a", "b", "c")
    val list2 = List("d", "e", "f")
    println(list1)
    println(product(list1, list2, { a -> { b: String -> "$a$b" } }))

    val result = zipWith(List(1, 2), List(4, 5, 6)) { x -> { y: Int -> Pair(x, y) } }
    println(result)
    println(unzip(result))
}
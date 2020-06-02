package chapter10

import chapter5.List
import chapter7.Result
import kotlin.math.max

sealed class Tree<out A : Comparable<@kotlin.UnsafeVariance A>> {

    internal abstract val value: A
    internal abstract val left: Tree<A>
    internal abstract val right: Tree<A>

    abstract val size: Int
    abstract val height: Int

    abstract fun isEmpty(): Boolean
    abstract fun max(): Result<A>
    abstract fun min(): Result<A>

    abstract fun merge(tree: Tree<@UnsafeVariance A>): Tree<A>

    abstract fun <B> foldLeft(identity: B,
                              f: (B) -> (A) -> B,
                              g: (B) -> (B) -> B): B

    abstract fun <B> foldRight(identity: B,
                               f: (A) -> (B) -> B,
                               g: (B) -> (B) -> B): B

    abstract fun <B> foldInOrder(identity: B, f: (B) -> (A) -> (B) -> B): B
    abstract fun <B> foldPreOrder(identity: B, f: (A) -> (B) -> (B) -> B): B
    abstract fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (A) -> B): B

    abstract fun rotateRight(): Tree<A>
    abstract fun rotateLeft(): Tree<A>

    fun toListInOrderRight(): List<@UnsafeVariance A> = unBalanceRight(List(), this)

    operator
    fun plus(element: @UnsafeVariance A): Tree<A> = when (this) {
        Empty -> T(Empty, element, Empty)
        is T -> when {
            element < this.value -> T(left + element, value, right)
            element > this.value -> T(left, value, right + element)
            else -> T(left, value, right)
        }
    }

    fun contains(element: @UnsafeVariance A): Boolean = when (this) {
        Empty -> false
        is T -> when {
            element < this.value -> left.contains(element)
            element > this.value -> right.contains(element)
            else -> true
        }
    }

    fun removeMerge(ta: Tree<@UnsafeVariance A>): Tree<A> = when (this) {
        Empty -> ta
        is T -> when (ta) {
            Empty -> this
            is T -> when {
                ta.value < value -> T(left.removeMerge(ta), value, right)
                else -> T(left, value, right.removeMerge(ta))
            }
        }
    }

    fun remove(a: @UnsafeVariance A): Tree<A> = when (this) {
        Empty -> this
        is T -> when {
            a < this.value -> left.remove(a)
            a > this.value -> right.remove(a)
            else -> left.removeMerge(right)
        }
    }

    fun <B : Comparable<B>> map(f: (A) -> B): Tree<B> =
            foldInOrder(invoke()) { left -> { value -> { right -> Tree(left, f(value), right) } } }

    fun <A> unfold(a: A, f: (A) -> Result<A>): A {
        tailrec fun <A> unfold(a: Pair<Result<A>, Result<A>>,
                               f: (A) -> Result<A>): Pair<Result<A>, Result<A>> {
            val x = a.second.flatMap { f(it) }
            return when (x) {
                is Result.Success -> unfold(Pair(a.second, x), f)
                else -> a
            }
        }
        return Result(a).let { unfold(Pair(it, it), f).second.getOrElse(a) }
    }

    internal object Empty : Tree<Nothing>() {

        override val value: Nothing by lazy {
            throw IllegalStateException("No value in Empty")
        }
        override val left: Tree<Nothing> by lazy {
            throw IllegalStateException("No left in Empty")
        }
        override val right: Tree<Nothing> by lazy {
            throw IllegalStateException("No right in Empty")
        }

        override fun merge(tree: Tree<Nothing>): Tree<Nothing> = tree

        override fun max(): Result<Nothing> = Result.Empty

        override fun min(): Result<Nothing> = Result.Empty

        override val size: Int = 0

        override val height: Int = -1

        override fun isEmpty(): Boolean = true

        override fun toString(): String = "E"

        override fun <B> foldLeft(identity: B, f: (B) -> (Nothing) -> B, g: (B) -> (B) -> B): B = identity

        override fun <B> foldRight(identity: B, f: (Nothing) -> (B) -> B, g: (B) -> (B) -> B): B = identity

        override fun <B> foldInOrder(identity: B, f: (B) -> (Nothing) -> (B) -> B): B = identity

        override fun <B> foldPreOrder(identity: B, f: (Nothing) -> (B) -> (B) -> B): B = identity

        override fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (Nothing) -> B): B = identity

        override fun rotateRight(): Tree<Nothing> = this

        override fun rotateLeft(): Tree<Nothing> = this
    }

    internal class T<out A : Comparable<@kotlin.UnsafeVariance A>>(
            override val left: Tree<A>,
            override val value: A,
            override val right: Tree<A>) : Tree<A>() {
        override fun merge(tree: Tree<@UnsafeVariance A>): Tree<A> = when (tree) {
            Empty -> this
            is T -> when {
                tree.value > this.value -> T(left, value, right.merge(T(Empty, tree.value, tree.right)))
                        .merge(tree.left)
                tree.value < this.value -> T(left.merge(T(tree.left, tree.value, Empty)), value, right)
                        .merge(tree.right)
                else -> T(left.merge(tree.left), value, right.merge(tree.right))
            }
        }

        override fun max(): Result<A> = right.max().orElse { Result(value) }

        override fun min(): Result<A> = left.min().orElse { Result(value) }

        override val size: Int = 1 + left.size + right.size

        override val height: Int = 1 + max(left.height, right.height)

        override fun isEmpty(): Boolean = false

        override fun toString(): String {
            return "T(left=$left, value=$value, right=$right)"
        }

        override fun <B> foldLeft(identity: B, f: (B) -> (A) -> B, g: (B) -> (B) -> B): B =
                g(left.foldLeft(identity, f, g))(f(right.foldLeft(identity, f, g))(this.value))

        override fun <B> foldRight(identity: B, f: (A) -> (B) -> B, g: (B) -> (B) -> B): B =
                g(f(this.value)(left.foldRight(identity, f, g)))(right.foldRight(identity, f, g))

        override fun <B> foldInOrder(identity: B, f: (B) -> (A) -> (B) -> B): B =
                f(left.foldInOrder(identity, f))(this.value)(right.foldInOrder(identity, f))

        override fun <B> foldPreOrder(identity: B, f: (A) -> (B) -> (B) -> B): B =
                f(this.value)(left.foldPreOrder(identity, f))(right.foldPreOrder(identity, f))

        override fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (A) -> B): B =
                f(left.foldPostOrder(identity, f))(right.foldPostOrder(identity, f))(this.value)

        override fun rotateRight(): Tree<A> = when(left) {
            is Empty -> this
            is T -> T(left.left, left.value, T(left.right, value, right))
        }

        override fun rotateLeft(): Tree<A> = when(right) {
            is Empty -> this
            is T -> T(T(left, value, right.left), right.value, right.right)
        }
    }

    companion object {

        operator
        fun <A : Comparable<A>> invoke(): Tree<A> = Empty

        operator
        fun <A : Comparable<A>> invoke(list: List<A>): Tree<A> = list.foldLeft(invoke()) { tree: Tree<A> -> { a: A -> tree.plus(a) } }

        operator fun <A : Comparable<A>> invoke(vararg az: A): Tree<A> =
                az.fold(Empty) { tree: Tree<A>, a: A -> tree.plus(a) }

        operator
        fun <A : Comparable<A>> invoke(left: Tree<A>, a: A, right: Tree<A>): Tree<A> = when {
            ordered(left, a, right) -> T(left, a, right)
            ordered(right, a, left) -> T(right, a, left)
            else -> Tree(a).merge(left).merge(right)
        }

        fun <A : Comparable<A>> lt(first: A, second: A): Boolean = first < second

        fun <A : Comparable<A>> lt(first: A, second: A, third: A): Boolean = lt(first, second) && lt(second, third)

        fun <A : Comparable<A>> ordered(left: Tree<A>, a: A, right: Tree<A>): Boolean =
                (left.max().flatMap { lmax ->
                    right.min().map { rmin ->
                        lt(lmax, a, rmin)
                    }
                }.getOrElse(left.isEmpty() && right.isEmpty() ||
                        left.min().mapEmpty()
                                .flatMap { right.min().map { rmin -> lt(a, rmin) } }.getOrElse(false) ||
                        right.min().mapEmpty()
                                .flatMap { left.max().map { lmax -> lt(lmax, a) } }.getOrElse(false)
                )
                        )


        tailrec fun <A: Comparable<A>> unBalanceRight(acc: List<A>, tree: Tree<A>): List<A> = when(tree) {
            Empty -> acc
            is T -> when(tree.left) {
                Empty -> unBalanceRight(acc.cons(tree.value), tree.right)
                is T -> unBalanceRight(acc, tree.rotateRight())
            }
        }

        fun <A: Comparable<A>> isUnbalanced(tree: Tree<A>): Boolean =
                when(tree) {
                    Empty -> false
                    is T -> Math.abs(tree.left.height - tree.right.height) > (tree.size - 1) % 2
                }



    }


}

fun main() {
    val result: Tree<Int> = Tree.invoke(4, 2,1,3,6,5,7)
    println(result)
    println(result.toListInOrderRight())
    println(result.map { it * 2 })
}
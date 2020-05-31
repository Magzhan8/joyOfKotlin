package chapter10

import chapter5.List
import chapter7.Result
import kotlin.math.max

sealed class Tree<out A : Comparable<@kotlin.UnsafeVariance A>> {

    abstract val size: Int
    abstract val height: Int

    abstract fun isEmpty(): Boolean
    abstract fun max(): Result<A>
    abstract fun min(): Result<A>

    abstract fun merge(tree: Tree<@UnsafeVariance A>): Tree<A>

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

    internal object Empty : Tree<Nothing>() {

        override fun merge(tree: Tree<Nothing>): Tree<Nothing> = tree

        override fun max(): Result<Nothing> = Result.Empty

        override fun min(): Result<Nothing> = Result.Empty

        override val size: Int = 0

        override val height: Int = -1

        override fun isEmpty(): Boolean = true

        override fun toString(): String = "E"
    }

    internal class T<out A : Comparable<@kotlin.UnsafeVariance A>>(
            internal val left: Tree<A>,
            internal val value: A,
            internal val right: Tree<A>) : Tree<A>() {

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
    }

    companion object {

        operator
        fun <A : Comparable<A>> invoke(): Tree<A> = Empty

        operator
        fun <A : Comparable<A>> invoke(list: List<A>): Tree<A> = list.foldLeft(Empty) { tree: Tree<A> -> { a: A -> tree.plus(a) } }

    }


}
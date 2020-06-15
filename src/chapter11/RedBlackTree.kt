package chapter11

import kotlin.math.max

sealed class RedBlackTree<out A : Comparable<@UnsafeVariance A>> {

    abstract val size: Int
    abstract val height: Int

    internal abstract val color: Color
    internal abstract val isTB: Boolean
    internal abstract val isTR: Boolean
    internal abstract val right: RedBlackTree<A>
    internal abstract val left: RedBlackTree<A>
    internal abstract val value: A

    abstract fun add(newVal: @UnsafeVariance A): RedBlackTree<A>
    abstract fun blacken(): RedBlackTree<A>

    fun balance(color: Color, left: RedBlackTree<@UnsafeVariance A>, value: @UnsafeVariance A, right: RedBlackTree<@UnsafeVariance A>): RedBlackTree<A> = when {
        // balance B (T R (T R a x b) y c) z d = T R (T B a x b) y (T B c z d)
        color == Color.B && left.isTR && left.left.isTR ->
            T(Color.R, left.left.blacken(), left.value, T(Color.B, left.right, value, right))
        // balance B (T R a x (T R b y c)) z d = T R (T B a x b) y (T B c z d)
        color == Color.B && left.isTR && left.right.isTR ->
            T(Color.R, T(Color.B, left.left, left.value, left.right.left), left.right.value,
                    T(Color.B, left.right.right, value, right))
        // balance B a x (T R (T R b y c) z d) = T R (T B a x b) y (T B c z d)
        color == Color.B && right.isTR && right.left.isTR ->
            T(Color.R, T(Color.B, left, value, right.left.left), right.left.value,
                    T(Color.B, right.left.right, right.value, right.right))
        // balance B a x (T R b y (T R c z d)) = T R (T B a x b) y (T B c z d)
        color == Color.B && right.isTR && right.right.isTR ->
            T(Color.R, T(Color.B, left, value, right.left), right.value, right.right.blacken())
        // balance color a x b = T color a x b
        else -> T(color, left, value, right)
    }


    internal abstract
    class Empty<out A : Comparable<@UnsafeVariance A>> : RedBlackTree<A>() {

        override val size: Int = 0
        override val height: Int = -1
        override val isTB: Boolean = false
        override val isTR: Boolean = false
        override val color: Color = Color.B
        override val left: RedBlackTree<A> = throw IllegalStateException("left called on empty RedBlackTree")
        override val right: RedBlackTree<A> = throw IllegalStateException("right called on empty RedBlackTree")
        override val value: A = throw IllegalStateException("value called on empty RedBlackTree")

        override fun toString(): String = "E"

        override fun add(newVal: @UnsafeVariance A): RedBlackTree<A> = T(Color.B, E, newVal, E)
        override fun blacken(): RedBlackTree<A> = E
    }

    internal object E : Empty<Nothing>()

    internal
    class T<out A : Comparable<@UnsafeVariance A>>(
            override val color: Color,
            override val left: RedBlackTree<A>,
            override val value: A,
            override val right: RedBlackTree<A>
    ) : RedBlackTree<A>() {

        override fun add(newVal: @UnsafeVariance A): RedBlackTree<A> = when {
            newVal < value -> balance(color, left.add(newVal), value, right)
            newVal > value -> balance(color, left, value, right.add(newVal))
            else -> when (color) {
                is Color.R -> T(Color.R, left, value, right)
                is Color.B -> T(Color.B, left, value, right)
            }
        }

        override fun blacken(): RedBlackTree<A> = T(Color.B, left, value, right)

        override val size: Int = left.size + 1 + right.size

        override val height: Int = max(left.size, right.size) + 1

        override val isTB: Boolean = color == Color.B
        override val isTR: Boolean = color == Color.R

        override fun toString(): String = "(T $color $left $value $right)"
    }

    companion object {

        operator
        fun <A : Comparable<A>> invoke(): RedBlackTree<A> = E
    }


}

sealed class Color {

    internal object R : Color() {
        override fun toString(): String = "R"
    }

    internal object B : Color() {
        override fun toString(): String = "B"
    }

}
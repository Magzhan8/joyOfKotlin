package chapter7

sealed class Either<out E, out A> {

    abstract fun <B> map(f: (A) -> B): Either<E, B>
    abstract fun <B> flatMap(f: (A) -> Either<@UnsafeVariance E, B>): Either<E, B>

    fun getOrElse(defaultValue: () -> @UnsafeVariance A): A = when (this) {
        is Left -> defaultValue()
        is Right -> this.value
    }

    fun orElse(defaultValue: () -> Either<@UnsafeVariance E, @UnsafeVariance A>): Either<E, A> =
            map { this }.getOrElse(defaultValue)

    internal
    class Left<out E, out A>(internal val value: E) : Either<E, A>() {

        override fun toString(): String = "Left($value)"

        override fun <B> map(f: (A) -> B): Either<E, B> = Left(value)

        override fun <B> flatMap(f: (A) -> Either<@UnsafeVariance E, B>): Either<E, B> = Left(value)
    }

    internal
    class Right<out E, out A>(internal val value: A) : Either<E, A>() {
        override fun toString(): String = "Right($value)"

        override fun <B> map(f: (A) -> B): Either<E, B> = Right(f(value))

        override fun <B> flatMap(f: (A) -> Either<@UnsafeVariance E, B>): Either<E, B> = f(value)
    }

    companion object {
        fun <E, A> left(value: E): Either<E, A> = Left(value)
        fun <E, A> right(value: A): Either<E, A> = Right(value)
    }


}
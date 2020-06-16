package chapter11

import chapter10.Tree
import chapter7.Result

class Map<out K : Comparable<@kotlin.UnsafeVariance K>, V>(
        private val delegate: Tree<MapEntry<K, V>> = Tree()) {

    operator
    fun plus(entry: Pair<@UnsafeVariance K, V>): Map<K, V> = Map(delegate + MapEntry(entry))

    operator
    fun minus(key: @UnsafeVariance K): Map<K, V> = Map(delegate - MapEntry(key))

    fun contains(key: @UnsafeVariance K): Boolean = delegate.contains(MapEntry(key))

    operator
    fun get(key: @UnsafeVariance K): Result<MapEntry<@UnsafeVariance K, V>> = delegate[MapEntry(key)]

    fun isEmpty(): Boolean = delegate.isEmpty()

    fun size(): Int = delegate.size

    companion object {

        operator fun <K : Comparable<@UnsafeVariance K>, V> invoke(): Map<K, V> = Map()
    }

    class MapEntry<K : Comparable<@kotlin.UnsafeVariance K>, V>
    private constructor(private val key: K, val value: Result<V>) : Comparable<MapEntry<K, V>> {

        override fun compareTo(other: MapEntry<K, V>): Int = this.key.compareTo(other.key)

        override fun toString(): String = "MapEntry($key, $value)"

        override fun equals(other: Any?): Boolean =
                this === other || when (other) {
                    is MapEntry<*, *> -> key == other.key
                    else -> false
                }

        override fun hashCode() = key.hashCode()

        companion object {

            fun <K : Comparable<K>, V> of(key: K, value: V): MapEntry<K, V> = MapEntry(key, Result(value))

            operator
            fun <K : Comparable<K>, V> invoke(pair: Pair<K, V>): MapEntry<K, V> = MapEntry(pair.first, Result(pair.second))

            operator
            fun <K : Comparable<K>, V> invoke(key: K): MapEntry<K, V> = MapEntry(key, Result())
        }

    }

}
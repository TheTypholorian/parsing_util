package net.typho.parsing_util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
open class ParsingIterator<V> @JvmOverloads constructor(
    @JvmField
    val list: List<V>,
    @JvmField
    val range: IntRange = list.indices,
    @JvmField
    var index: Int = range.first
) : ListIterator<V> {
    override fun hasNext() = index in range

    override fun hasPrevious() = index in range

    override fun next(): V {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        return list[index++]
    }

    fun skip(amount: Int) {
        if ((index + amount) !in range) {
            throw NoSuchElementException()
        }

        index += amount
    }

    fun peek(): V {
        if (index !in range) {
            throw NoSuchElementException()
        }

        return list[index]
    }

    fun peekOrNull(): V? {
        if (index !in range) {
            return null
        }

        return list[index]
    }

    fun currentMatches(list: List<V>): Boolean {
        if ((index + list.size) !in range) {
            return false
        }

        repeat(list.size) { i ->
            if (this.list[index + i] != list[i]) {
                return false
            }
        }

        return true
    }

    fun indexOfNext(value: V) = list.subList(index, list.size).indexOf(value)

    override fun nextIndex() = index

    override fun previous(): V {
        if (!hasPrevious()) {
            throw NoSuchElementException()
        }

        return list[index--]
    }

    override fun previousIndex() = index - 1

    operator fun get(index: Int) = list[index]

    fun nextUntil(predicate: (index: Int, value: V) -> Boolean): List<V>? {
        contract {
            callsInPlace(predicate)
        }

        val start = index
        var i = 0

        while (index in range) {
            val value = next()

            if (predicate(i++, value)) {
                return list.subList(start, index - 1)
            }
        }

        return null
    }

    fun nextUntilOrEnd(predicate: (index: Int, value: V) -> Boolean): List<V> {
        contract {
            callsInPlace(predicate)
        }

        val start = index
        var i = 0

        while (index in range) {
            val value = next()

            if (predicate(i++, value)) {
                return list.subList(start, index - 1)
            }
        }

        return list.subList(start, index)
    }

    fun takeUntil(predicate: (index: Int, value: V) -> Boolean): List<V>? {
        contract {
            callsInPlace(predicate)
        }

        val start = index
        var i = 0

        while (index in range) {
            val value = peek()

            if (predicate(i++, value)) {
                return list.subList(start, index)
            }

            next()
        }

        return null
    }

    fun takeUntilOrEnd(predicate: (index: Int, value: V) -> Boolean): List<V> {
        contract {
            callsInPlace(predicate)
        }

        val start = index
        var i = 0

        while (index in range) {
            val value = peek()

            if (predicate(i++, value)) {
                break
            }

            next()
        }

        return list.subList(start, index)
    }

    fun skipUntil(predicate: (index: Int, value: V) -> Boolean): Int? {
        contract {
            callsInPlace(predicate)
        }

        var skipped = 0
        var i = 0

        while (index in range) {
            val value = peek()

            if (predicate(i++, value)) {
                return skipped
            }

            next()
            skipped++
        }

        return null
    }

    fun skipUntilOrEnd(predicate: (index: Int, value: V) -> Boolean): Int {
        contract {
            callsInPlace(predicate)
        }

        var skipped = 0
        var i = 0

        while (index in range) {
            val value = peek()

            if (predicate(i++, value)) {
                break
            }

            next()
            skipped++
        }

        return skipped
    }

    @JvmOverloads
    fun fork(range: IntRange = this.range, index: Int = this.index) = Child(list, range, index)

    override fun toString(): String {
        return "ParsingIterator(index=$index, range=$range)"
    }

    open inner class Child @JvmOverloads constructor(
        input: List<V>,
        range: IntRange = input.indices,
        index: Int = range.first
    ) : ParsingIterator<V>(input, range, index) {
        fun merge(): ParsingIterator<V> {
            val parent = this@ParsingIterator
            parent.index = index
            return parent
        }
    }
}
package chapter12

import chapter5.List
import chapter7.Result
import chapter9.Stream
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader

interface Input : Closeable {
    fun readString(): Result<Pair<String, Input>>
    fun readInt(): Result<Pair<Int, Input>>

    fun readString(message: String): Result<Pair<String, Input>> = readString()
    fun readInt(message: String): Result<Pair<Int, Input>> = readInt()
}

abstract class AbstractReader(private val reader: BufferedReader) : Input {

    override fun readString(): Result<Pair<String, Input>> = try {
        reader.readLine().let {
            when {
                it.isEmpty() -> Result()
                else -> Result(Pair(it, this))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }


    override fun readInt(): Result<Pair<Int, Input>> = try {
        reader.readLine().let {
            when {
                it.isEmpty() -> Result()
                else -> Result(Pair(it.toInt(), this))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun close() = reader.close()

}

class ConsoleReader(reader: BufferedReader) : AbstractReader(reader) {

    override fun readString(message: String): Result<Pair<String, Input>> {
        print("$message")
        return readString()
    }

    override fun readInt(message: String): Result<Pair<Int, Input>> {
        print("$message")
        return readInt()
    }

    companion object {

        operator
        fun invoke(): ConsoleReader = ConsoleReader(BufferedReader(InputStreamReader(System.`in`)))
    }

}

data class Person(val id: Int, val firstName: String, val lastName: String)

fun readPersonsFromConsole(): List<Person> = Stream.unfold(ConsoleReader(), ::person).toList()

fun person(input: Input): Result<Pair<Person, Input>> =
        input.readInt("Enter ID:").flatMap { id ->
            id.second.readString("Enter first name:")
                    .flatMap { firstName ->
                        firstName.second.readString("Enter last name:")
                                .map { lastName ->
                                    Pair(Person(id.first,
                                            firstName.first,
                                            lastName.first), lastName.second)
                                }
                    }
        }

fun main(args: Array<String>) {
    readPersonsFromConsole().forEach(::println)
}
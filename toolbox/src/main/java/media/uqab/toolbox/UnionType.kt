
import kotlin.reflect.KClass

typealias Undefined = Unit


/**
 * Union type like python or typescript
 * @author github/fcat97
 */
abstract class UnionType(private val clazz: Array<Any>) {
    var value: Any = Undefined
        set(value) {
            field = if (isSupported(value::class)) {
                value
            } else {
                throw IllegalArgumentException("can't set value of \"${value::class}\" type! supported types: [${clazz.joinToString { "$it".replace("class ", "") }}]")
            }
        }

    private fun <T: Any> isSupported(k: KClass<T>): Boolean = clazz.any { it == k }
    private fun <T: Any> isInstance(k: KClass<T>): Boolean = k == value::class

    fun <T : Any, R> ifInstanceOf(k: KClass<T>, block: (T) -> R): R? {
        return get(k)?.let(block)
    }

    fun <T: Any> get(k: KClass<T>): T? {
        return if (isInstance(k)) value as T
        else null
    }

    infix operator fun plus(b: UnionType): UnionType {
        val c = clazz.toSet().plus(b.clazz).toList()
        return when(c.size) {
            2 -> UnionType2(c[0]::class, c[1]::class)
            3 -> UnionType3(c[0]::class, c[1]::class, c[2]::class)
            4 -> UnionType4(c[0]::class, c[1]::class, c[2]::class, c[3]::class)
            else -> of(c)
        }.also {
            if (value != Undefined) it.value = value
        }
    }

    companion object {

        /**
         * Flexible but no indication of what classes are supported.
         * Use [UnionType2], [UnionType3] etc.
         */
        fun of(vararg clazz: Any): UnionType {
            return object : UnionType(arrayOf(*clazz)) {

            }
        }
    }
}

class UnionType2<A: Any, B: Any>(
    a: KClass<A>,
    b: KClass<B>
): UnionType(arrayOf(a, b))
class UnionType3<A: Any, B: Any, C: Any>(
    a: KClass<A>,
    b: KClass<B>,
    c: KClass<C>
): UnionType(arrayOf(a, b, c))
class UnionType4<A: Any, B: Any, C: Any, D: Any>(
    a: KClass<A>,
    b: KClass<B>,
    c: KClass<C>,
    d: KClass<D>
): UnionType(arrayOf(a, b, c, d))

fun test(): UnionType2<String, Float> {
    val clazz2 = UnionType2(String::class, Float::class)
    clazz2.value = 0f
    return clazz2
}

fun main() {
    val result = test()
    println(result::class)
}

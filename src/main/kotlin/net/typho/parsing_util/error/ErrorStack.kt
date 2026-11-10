package net.typho.parsing_util.error

@Suppress("NOTHING_TO_INLINE")
inline fun <R> ErrorStack?.push(key: String, noinline func: () -> R): R {
    return if (this == null) func() else push(key, func)
}

class ErrorStack(
    @JvmField
    var valueBeingSerialized: Any? = null,
    @JvmField
    val path: MutableList<String> = mutableListOf()
) {
    @JvmField
    val attributes = mutableMapOf<String, Any>()

    fun push(key: String) {
        path.add(key)
    }

    fun <R> push(key: String, func: () -> R): R {
        push(key)
        val r = func()
        pop()
        return r
    }

    fun pop() = path.removeLast()

    fun isEmpty() = valueBeingSerialized == null && attributes.isEmpty() && path.isEmpty()

    override fun toString() = buildList {
        if (path.isNotEmpty()) {
            add("Path: ${path.joinToString(separator = " > ")}")
        }

        valueBeingSerialized?.let { add("Value being serialized: $it") }
        attributes.forEach { (key, value) -> add("$key: $value") }
    }.joinToString(separator = "\n")
}
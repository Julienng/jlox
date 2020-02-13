package org.lox

class LoxClass(
    val name: String,
    val methods: MutableMap<String, LoxFunction>
) : LoxCallable {
    override val arity: Int
        get() {
            val initializer = findMethod("init")
            return initializer?.arity ?: 0
        }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)

        return instance
    }

    fun findMethod(name: String): LoxFunction? {
        if (methods.containsKey(name)) {
            return methods[name]
        }

        return null
    }

    override fun toString(): String {
        return name
    }
}
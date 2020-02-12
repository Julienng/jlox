package org.lox

class LoxClass(
    val name: String,
    val methods: MutableMap<String, LoxFunction>
) : LoxCallable {
    override val arity: Int
        get() = 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return LoxInstance(this)
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
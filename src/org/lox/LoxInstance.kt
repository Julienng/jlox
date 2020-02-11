package org.lox

class LoxInstance(private val klass: LoxClass) {
    private val fields = mutableMapOf<String, Any?>()

    operator fun get(name: Token) =
        if (fields.containsKey(name.lexeme)) {
            fields[name.lexeme]
        } else {
            throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
        }

    operator fun set(name: Token, value: Any?) = fields.put(name.lexeme, value)

    override fun toString(): String {
        return "${klass.name} instance"
    }
}
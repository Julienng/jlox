package org.lox

class Environment(private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    operator fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        if (enclosing != null) return enclosing[name]

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    operator fun get(depth: Int, name: String): Any? {
        return ancestor(depth).values[name]
    }

    fun ancestor(depth: Int): Environment {
        var env = this
        for (i in 0 until depth) {
            // we already calculate the non-nullability of enclosing
            // during the static analysis
            env = env.enclosing!!
        }

        return env
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assignAt(depth: Int, name: Token, value: Any?) {
        ancestor(depth).values[name.lexeme] = value
    }
}
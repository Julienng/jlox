package org.lox

import org.lox.ast.Stmt

class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment
) : LoxCallable {
    override val arity: Int
        get() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)

        for (i in declaration.params.indices) {
            environment.define(declaration.params[i].lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val env = Environment(closure)
        env.define("this", instance)
        return LoxFunction(declaration, env)
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme} >"
    }
}
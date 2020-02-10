package org.lox

interface LoxCallable {
	val arity: Int
		get() = 0

	fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
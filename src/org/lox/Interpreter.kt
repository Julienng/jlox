package org.lox

import org.lox.expr.Expr

fun stringify(value: Any?): String {
	if (value == null) return "nil"

	// Hack. Work around Java adding ".0" to integer-valued doubles
	if (value is Double) {
		var text = value.toString()
		if (text.endsWith(".0")) {
			text = text.substring(0, text.length - 2)
		}
		return text
	}

	return value.toString()
}

class Interpreter : Expr.Visitor<Any?> {

	fun interpret(expression: Expr) {
		try {
			val result = evaluate(expression)
			println(stringify(result))
		} catch (err: RuntimeError) {
			runtimeError(err)
		}
	}

	override fun visitBinaryExpr(expr: Expr.Binary): Any? {
		val left = evaluate(expr.left)
		val right = evaluate(expr.right)

		return when (expr.operator.type) {
			TokenType.MINUS -> {
				checkNumberOperands(expr.operator, left, right)
				(left as Double) - (right as Double)
			}
			TokenType.SLASH -> {
				checkNumberOperands(expr.operator, left, right)
				checkNumberDivideByZero(expr.operator, right)

				(left as Double) / (right as Double)
			}
			TokenType.STAR -> {
				checkNumberOperands(expr.operator, left, right)
				(left as Double) * (right as Double)
			}
			TokenType.PLUS -> {
				if (left is Double && right is Double)
					left + right
				else if (left is String && right is String)
					left + right
				else
					throw RuntimeError(
						expr.operator,
						"Operands must be two numbers or two strings."
					)
			}
			TokenType.GREATER -> {
				checkNumberOperands(expr.operator, left, right)
				(left as Double) > (right as Double)
			}
			TokenType.GREATER_EQUAL -> {
				checkNumberOperands(expr.operator, left, right)
				(left as Double) >= (right as Double)
			}
			TokenType.LESS -> {
				checkNumberOperands(expr.operator, left, right)
				(left as Double) < (right as Double)
			}
			TokenType.LESS_EQUAL -> {
				checkNumberOperands(expr.operator, left, right)
				(left as Double) <= (right as Double)
			}
			TokenType.BANG_EQUAL -> !isEqual(left, right)
			TokenType.EQUAL_EQUAL -> isEqual(left, right)
			// unreachable
			else -> null
		}
	}

	override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
		return evaluate(expr.expression)
	}

	override fun visitLiteralExpr(expr: Expr.Literal): Any? {
		return expr.value
	}

	override fun visitUnaryExpr(expr: Expr.Unary): Any? {
		val right = evaluate(expr.right)

		return when (expr.operator.type) {
			TokenType.MINUS -> {
				checkNumberOperand(expr.operator, right)
				-(right as Double)
			}
			TokenType.BANG -> !isTruthy(right)
			// unreachable
			else -> null
		}
	}

	private fun evaluate(expr: Expr): Any? {
		return expr.accept(this)
	}

	private fun isTruthy(value: Any?): Boolean {
		if (value == null) return false
		if (value is Boolean) return value
		return true
	}

	private fun isEqual(left: Any?, right: Any?): Boolean {
		if (left == null && right == null) return true
		if (left == null) return false

		return left == right
	}

	private fun checkNumberDivideByZero(operator: Token, operand: Any?) {
		if (operand is Double && operand == 0.0) {
			throw RuntimeError(operator, "Divide by zero is not valid.")
		}
	}

	private fun checkNumberOperand(operator: Token, operand: Any?) {
		if (operand is Double) return
		throw RuntimeError(operator, "Operand must be a number.")
	}

	private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
		if (left is Double && right is Double) return
		throw RuntimeError(operator, "Operands must be numbers.")
	}
}

class RuntimeError(val token: Token, message: String) : RuntimeException(message) {
}
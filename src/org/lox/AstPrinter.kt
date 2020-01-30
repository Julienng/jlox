package org.lox

import org.lox.expr.Expr
import org.lox.expr.Expr.*

abstract class Printer : Visitor<String> {
	fun print(expr: Expr?): String {
		if (expr == null) return "no expression to parse"
		return expr.accept(this);
	}

	protected fun parenthesize(name: String, vararg exprs: Expr): String {
		var builder = "";

		builder += "($name"
		for (expr in exprs) {
			builder += " ${expr.accept(this)}"
		}
		builder += ")"

		return builder
	}
}

class AstPrinter : Printer() {
	override fun visitBinaryExpr(expr: Binary): String {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right)
	}

	override fun visitGroupingExpr(expr: Grouping): String {
		return parenthesize("group", expr.expression)
	}

	override fun visitLiteralExpr(expr: Literal): String {
		return expr.value?.toString() ?: "nil"
	}

	override fun visitUnaryExpr(expr: Unary): String {
		return parenthesize(expr.operator.lexeme, expr.right)
	}
}


class RpnPrinter : Printer() {
	override fun visitBinaryExpr(expr: Binary): String {
		val left = expr.left.accept(this)
		val right = expr.right.accept(this)
		return "$left $right ${expr.operator.lexeme}"
	}

	override fun visitGroupingExpr(expr: Grouping): String {
		return parenthesize("group", expr.expression)
	}

	override fun visitLiteralExpr(expr: Literal): String {
		return expr.value?.toString() ?: "nil"
	}

	override fun visitUnaryExpr(expr: Unary): String {
		return parenthesize(expr.operator.lexeme, expr.right)
	}
}

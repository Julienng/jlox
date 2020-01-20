package org.lox.expr

import org.lox.Token

internal abstract class Expr {
	interface Visitor<R> {
		fun visitBinaryExpr(expr: Binary): R
		fun visitGroupingExpr(expr: Grouping): R
		fun visitLiteralExpr(expr: Literal): R
		fun visitUnaryExpr(expr: Unary): R
	}

	internal class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {

		override fun <R> accept(visitor: Visitor<R>): R {
			return visitor.visitBinaryExpr(this)
		}
	}

	internal class Grouping(val expression: Expr) : Expr() {

		override fun <R> accept(visitor: Visitor<R>): R {
			return visitor.visitGroupingExpr(this)
		}
	}

	internal class Literal(val value: Any) : Expr() {

		override fun <R> accept(visitor: Visitor<R>): R {
			return visitor.visitLiteralExpr(this)
		}
	}

	internal class Unary(val operator: Token, val right: Expr) : Expr() {

		override fun <R> accept(visitor: Visitor<R>): R {
			return visitor.visitUnaryExpr(this)
		}
	}

	abstract fun <R> accept(visitor: Visitor<R>): R
}

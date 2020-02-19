package org.lox

import org.lox.ast.Expr
import org.lox.ast.Expr.*

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
    override fun visitAssignExpr(expr: Assign): String {
        return parenthesize(expr.name.lexeme, expr.value)
    }

    override fun visitBinaryExpr(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitCallExpr(expr: Call): String {
        return parenthesize("fun", expr.callee)
    }

    override fun visitGroupingExpr(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Literal): String {
        return expr.value?.toString() ?: "nil"
    }

    override fun visitLogicalExpr(expr: Logical): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitUnaryExpr(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitVariableExpr(expr: Variable): String {
        return expr.name.lexeme
    }

    override fun visitGetExpr(expr: Get): String {
        return parenthesize("get-${expr.name.lexeme}", expr.`object`)
    }

    override fun visitSetExpr(expr: Expr.Set): String {
        return parenthesize("set-${expr.name.lexeme}", expr.value)
    }

    override fun visitSuperExpr(expr: Super): String {
        return "super.${expr.method.lexeme}"
    }

    override fun visitThisExpr(expr: This): String {
        return expr.keyword.lexeme
    }
}
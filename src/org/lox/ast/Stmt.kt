package org.lox.ast

import org.lox.Token

abstract class Stmt {
  interface Visitor<R> {
    fun visitBlockStmt(stmt: Block): R
    fun visitExpressionStmt(stmt: Expression): R
    fun visitPrintStmt(stmt: Print): R
    fun visitVarStmt(stmt: Var): R
  }

  class Block(val statements: List<Stmt?>) : Stmt() {

    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitBlockStmt(this)
    }
  }

  class Expression(val expression: Expr) : Stmt() {

    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitExpressionStmt(this)
    }
  }

  class Print(val expression: Expr) : Stmt() {

    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitPrintStmt(this)
    }
  }

  class Var(val name: Token, val initializer: Expr?) : Stmt() {

    override fun <R> accept(visitor: Visitor<R>): R {
      return visitor.visitVarStmt(this)
    }
  }

  abstract fun <R> accept(visitor: Visitor<R>): R
}

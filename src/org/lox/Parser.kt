package org.lox

import org.lox.TokenType.*
import org.lox.ast.Expr
import org.lox.ast.Stmt
import org.lox.error as loxError

internal class ParseError : RuntimeException() {}

class Parser(private val tokens: List<Token>) {
	private var current = 0

	fun parse(): List<Stmt?> {
		val statements = mutableListOf<Stmt?>()
		while (!isAtEnd()) {
			statements.add(declaration())
		}
		return statements
	}

	private fun declaration(): Stmt? {
		try {
			if (match(FUN)) return function("function")
			if (match(VAR)) return varDeclaration()

			return statement()
		} catch (error: ParseError) {
			synchronize()
			return null
		}
	}

	private fun function(kind: String): Stmt.Function {
		val name = consume(IDENTIFIER, "Expect $kind name.")
		consume(LEFT_PAREN, "Expect '(' after $kind name.")

		val parameters = mutableListOf<Token>()
		if (!check(RIGHT_PAREN)) {
			do {
				if (parameters.size >= 255) {
					this.error(peek(), "Cannot have more than 255 parameters")
				}
				parameters.add(consume(IDENTIFIER, "Expect parameter name."))
			} while (match(COMMA))
		}
		consume(RIGHT_PAREN, "Expect ')' after parameters.")

		consume(LEFT_BRACE, "Expect '{' before $kind body.")
		val body = block()
		return Stmt.Function(name, parameters, body)
	}

	private fun varDeclaration(): Stmt {
		val name = consume(IDENTIFIER, "Expect variable name.")
		var initializer: Expr? = null
		if (match(EQUAL)) {
			initializer = expression()
		}

		consume(SEMICOLON, "Expect ';' after variable declaration.")
		return Stmt.Var(name, initializer)
	}

	private fun statement(): Stmt {
		if (match(FOR)) return forStatement()
		if (match(IF)) return ifStatement()
		if (match(PRINT)) return printStatement()
		if (match(WHILE)) return whileStatement()
		if (match(LEFT_BRACE)) return Stmt.Block(block())

		return expressionStatement()
	}

	private fun forStatement(): Stmt {
		consume(LEFT_PAREN, "Expect '(' after 'for'.")

		val initializer = if (match(SEMICOLON)) {
			null
		} else if (match(VAR)) {
			varDeclaration()
		} else {
			expressionStatement()
		}

		var condition: Expr? = null
		if (!check(SEMICOLON)) {
			condition = expression()
		}
		consume(SEMICOLON, "Expect ';' after loop condition.")

		var increment: Expr? = null
		if (!check(RIGHT_PAREN)) {
			increment = expression()
		}
		consume(RIGHT_PAREN, "Expect ')' after for clauses.")

		var body = statement()

		if (increment != null) {
			body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
		}

		if (condition == null) condition = Expr.Literal(true)
		body = Stmt.While(condition, body)

		if (initializer != null) body = Stmt.Block(listOf(initializer, body))

		return body
	}

	private fun ifStatement(): Stmt {
		consume(LEFT_PAREN, "Expect '(' after if.")
		val condition = expression()
		consume(RIGHT_PAREN, "Expect ')' after if condition.")

		val thenBranch = statement()
		var elseBranch: Stmt? = null
		if (match(ELSE)) {
			elseBranch = statement()
		}
		return Stmt.If(condition, thenBranch, elseBranch)
	}

	private fun block(): List<Stmt?> {
		val statements = mutableListOf<Stmt?>()
		while (!check(RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "Expect '}' after block.")
		return statements
	}

	private fun printStatement(): Stmt {
		val value = expression()
		consume(SEMICOLON, "Expect ';' after value")
		return Stmt.Print(value)
	}

	private fun whileStatement(): Stmt {
		consume(LEFT_PAREN, "Expect '(' after while.")
		val condition = expression()
		consume(RIGHT_PAREN, "Expect ')' after condition.")
		val body = statement()

		return Stmt.While(condition, body)
	}

	private fun expressionStatement(): Stmt {
		val expr = expression()
		consume(SEMICOLON, "Expect ';' after expression.")
		return Stmt.Expression(expr)
	}

	private fun expression(): Expr {
		return assignment()
	}

	private fun assignment(): Expr {
		val expr = or()

		if (match(EQUAL)) {
			val equals = previous()
			val value = assignment()

			if (expr is Expr.Variable) {
				val name = expr.name
				return Expr.Assign(name, value)
			}

			error(equals, "Invalid assigment target.")
		}

		return expr
	}

	private fun or(): Expr {
		var expr = and()

		while (match(OR)) {
			val operator = previous()
			val right = and()
			expr = Expr.Logical(expr, operator, right)
		}

		return expr
	}

	private fun and(): Expr {
		var expr = equality()

		while (match(AND)) {
			val operator = previous()
			val right = equality()
			expr = Expr.Logical(expr, operator, right)
		}

		return expr
	}

	private fun equality(): Expr {
		var expr = comparison()

		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			val operator = previous()
			val right = comparison()
			expr = Expr.Binary(expr, operator, right)
		}
		return expr
	}

	private fun comparison(): Expr {
		var expr = addition()

		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			val operator = previous()
			val right = addition()

			expr = Expr.Binary(expr, operator, right)
		}

		return expr
	}

	private fun addition(): Expr {
		var expr = multiplication()

		while (match(MINUS, PLUS)) {
			val operator = previous()
			val right = multiplication()
			expr = Expr.Binary(expr, operator, right)
		}

		return expr
	}

	private fun multiplication(): Expr {
		var expr = unary()

		while (match(SLASH, STAR)) {
			val operator = previous()
			val right = unary()
			expr = Expr.Binary(expr, operator, right)
		}

		return expr
	}

	private fun unary(): Expr {
		if (match(BANG, MINUS)) {
			val operator = previous()
			val right = unary()
			return Expr.Unary(operator, right)
		} else {
			return call()
		}
	}

	private fun call(): Expr {
		var expr = primary()

		while (true) {
			if (match(LEFT_PAREN)) {
				expr = finishCall(expr)
			} else {
				break
			}
		}

		return expr;
	}

	private fun finishCall(callee: Expr): Expr {
		val arguments = mutableListOf<Expr>()
		if (!check(RIGHT_PAREN)) {
			do {
				if (arguments.size > 255) {
					this.error(peek(), "Cannot have more than 255 arguments.")
				}
				arguments.add(expression())
			} while (match(COMMA))
		}

		val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")

		return Expr.Call(callee, paren, arguments)
	}

	private fun primary(): Expr {
		if (match(FALSE)) return Expr.Literal(false)
		if (match(TRUE)) return Expr.Literal(true)
		if (match(NIL)) return Expr.Literal(null)

		if (match(NUMBER, STRING)) return Expr.Literal(previous().literal)

		if (match(IDENTIFIER)) return Expr.Variable(previous())

		if (match(LEFT_PAREN)) {
			val expr = expression()
			consume(RIGHT_PAREN, "Expect ')' after expression.")
			return Expr.Grouping(expr)
		}

		throw this.error(peek(), "Expect expression.")
	}

	private fun match(vararg types: TokenType): Boolean {
		for (type in types) {
			if (check(type)) {
				advance()
				return true
			}
		}
		return false
	}

	private fun check(type: TokenType): Boolean {
		if (isAtEnd()) return false
		return peek().type == type
	}

	private fun consume(type: TokenType, message: String): Token {
		if (check(type)) return advance()

		throw this.error(peek(), message)
	}

	private fun synchronize() {
		advance()

		while (!isAtEnd()) {
			if (previous().type == SEMICOLON) return

			if (peek().type in listOf(
					CLASS, FUN, VAR, FOR, IF, WHILE,
					PRINT, RETURN
				)
			) {
				return
			}
			advance()
		}
	}

	private fun error(token: Token, message: String): ParseError {
		loxError(token, message)
		return ParseError()
	}

	private fun advance(): Token {
		if (!isAtEnd()) current++
		return previous()
	}

	private fun isAtEnd(): Boolean {
		return peek().type == TokenType.EOF
	}

	private fun peek(): Token {
		return tokens[current]
	}

	private fun previous(): Token {
		return tokens[current - 1]
	}
}
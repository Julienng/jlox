package org.lox

data class Token(var type: TokenType, var lexeme: String, var literal: Any?, var line: Int) {
    override fun toString(): String {
        return "$type $lexeme $literal"
    }
}
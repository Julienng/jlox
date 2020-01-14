package org.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


fun main(args: Array<String>) {
	if (args.size > 1) {
		println("Usage: jlox [script]")
		exitProcess(64)
	} else if (args.size == 1) {
		runFile(args[0])
	} else {
		runPrompt()
	}
}

var hadError = false

fun run(source: String) {
	val scanner = Scanner(source)
	val tokens = scanner.scanTokens()

	// For now, just print the tokens.
	// For now, just print the tokens.
	for (token in tokens) {
		println(token)
	}
}

fun runFile(path: String) {
	val bytes = Files.readAllBytes(Paths.get(path))
	run(String(bytes, Charset.defaultCharset()))

	if (hadError) exitProcess(65)
}

fun runPrompt() {
	val input = InputStreamReader(System.`in`)
	val reader = BufferedReader(input)

	while (true) {
		print("> ")
		run(reader.readLine())
	}
}

fun error(line: Int, message: String) {
	report(line, "", message)
}

fun report(line: Int, where: String, message: String) {
	System.err.println("[line $line] Error $where: $message")
	hadError = true
}
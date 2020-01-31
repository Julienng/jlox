package org.lox

import java.io.PrintWriter
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>")
        exitProcess(1)
    }
    val outputDir = args[0]

    defineAst(
        outputDir, "Expr", listOf(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Any? value",
            "Unary    : Token operator, Expr right"
        )
    )
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.println("package org.lox.expr;")
    writer.println()
//	writer.println("import java.util.List;")
    writer.println("import org.lox.Token")
    writer.println()
    writer.println("abstract class $baseName {")

    defineVisitor(writer, baseName, types)

    for (type in types) {
        val className = type.split(":")[0].trim()
        val fields = type.split(":")[1].trim()
        defineType(writer, baseName, className, fields)
    }

    writer.println()
    writer.println("  abstract fun <R> accept(visitor: Visitor<R>): R")

    writer.println("}")
    writer.close()
}

fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
    writer.print(
        "  class $className"
    )
    // Store parameters in fields.
    val fields = fieldList.split(", ")
    val parameters = mutableListOf<String>()
    for (field in fields) {
        val name = field.split(" ".toRegex()).toTypedArray()[1]
        val type = field.split(" ".toRegex()).toTypedArray()[0]
        parameters.add("val $name: $type")
    }

    writer.print(parameters.joinToString(separator = ",", prefix = "(", postfix = ")") { it -> it })
    writer.println(" : $baseName() {")
    writer.println()
    writer.println("    override fun <R> accept(visitor: Visitor<R>): R {")
    writer.println("      return visitor.visit$className$baseName(this)")
    writer.println("    }")
    writer.println("  }")
}

fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
    writer.println("  interface Visitor<R> {")

    for (type in types) {
        val typeName = type.split(":")[0].trim()
        writer.println("  fun visit$typeName$baseName(${baseName.toLowerCase()} : $typeName): R")
    }
    writer.println("  }")
}
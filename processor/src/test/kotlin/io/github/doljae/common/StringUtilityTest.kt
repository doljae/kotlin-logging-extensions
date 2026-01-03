package io.github.doljae.common

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class StringUtilityTest {
    private val reservedWords =
        setOf(
            "as",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "for",
            "fun",
            "if",
            "in",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "typeof",
            "val",
            "var",
            "when",
            "while",
        )
    private val quoteChar = '`'
    private val delimiter = '.'

    @Test
    fun `should not modify string without reserved words`() {
        val input = "com.example.project"
        val expected = "com.example.project"

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = delimiter,
                reservedWords = reservedWords,
                quoteChar = quoteChar,
            )

        actual shouldBe expected
    }

    @Test
    fun `should wrap single reserved word in a path`() {
        val input = "com.package.example"
        val expected = "com.`package`.example"

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = delimiter,
                reservedWords = reservedWords,
                quoteChar = quoteChar,
            )

        actual shouldBe expected
    }

    @Test
    fun `should wrap multiple reserved words in a path`() {
        val input = "my.package.is.fun"
        val expected = "my.`package`.`is`.`fun`"

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = delimiter,
                reservedWords = reservedWords,
                quoteChar = quoteChar,
            )

        actual shouldBe expected
    }

    @Test
    fun `should wrap reserved words at beginning of path`() {
        val input = "fun.in.my.project"
        val expected = "`fun`.`in`.my.project"

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = delimiter,
                reservedWords = reservedWords,
                quoteChar = quoteChar,
            )

        actual shouldBe expected
    }

    @Test
    fun `should wrap single word that is reserved`() {
        val input = "package"
        val expected = "`package`"

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = delimiter,
                reservedWords = reservedWords,
                quoteChar = quoteChar,
            )

        actual shouldBe expected
    }

    @Test
    fun `should handle empty string`() {
        val input = ""
        val expected = ""

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = delimiter,
                reservedWords = reservedWords,
                quoteChar = quoteChar,
            )

        actual shouldBe expected
    }

    @Test
    fun `should handle different delimiter`() {
        val input = "one/if/two/when"
        val expected = "one/`if`/two/`when`"

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = '/',
                reservedWords = reservedWords,
                quoteChar = quoteChar,
            )

        actual shouldBe expected
    }

    @Test
    fun `should handle different quote character`() {
        val input = "com.package.class"
        val expected = "com.'package'.'class'"

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = delimiter,
                reservedWords = reservedWords,
                quoteChar = '\'',
            )

        actual shouldBe expected
    }

    @Test
    fun `should handle custom reserved words set`() {
        val input = "foo.bar.baz"
        val expected = "`foo`.bar.`baz`"
        val customReservedWords = setOf("foo", "baz")

        val actual =
            StringUtility.wrapReservedWords(
                target = input,
                delimiter = delimiter,
                reservedWords = customReservedWords,
                quoteChar = quoteChar,
            )

        actual shouldBe expected
    }
}
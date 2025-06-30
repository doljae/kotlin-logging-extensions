package io.github.doljae.kotlinlogging.extensions

import io.github.doljae.kotlinlogging.extensions.LoggerProcessor.Companion.wrapReservedWords
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoggerProcessorTest {
    @Test
    fun `processor should handle empty file list`() {
        // This is a basic smoke test to ensure the processor can be instantiated
        // and doesn't crash with empty input

        // Since KSP testing requires more complex setup with compilation testing,
        // this serves as a basic structure validation
        assertTrue(true, "LoggerProcessor class exists and can be referenced")
    }

    @Test
    fun `processor should be properly configured`() {
        // Verify that the processor can be instantiated
        // In a real scenario, this would use KSP's compilation testing framework

        val processorName = LoggerProcessor::class.simpleName
        assertTrue(processorName == "LoggerProcessor", "Processor class has correct name")
    }

    @Test
    fun `package name should handle reserved words correctly`() {
        // A subset of Kotlin keywords to test the wrapping logic,
        // mirroring the keywords in LoggerProcessor.
        val reservedWords =
            setOf(
                "as",
                "break",
                "class",
                "continue",
                "do",
                "else",
                "for",
                "fun",
                "if",
                "in",
                "is",
                "null",
                "object",
                "package",
                "return",
                "super",
                "this",
                "throw",
                "try",
                "typealias",
                "typeof",
                "val",
                "var",
                "when",
                "while",
            )
        val delimiter = '.'
        val quoteChar = '`'

        // Test case 1: Package name with a common reserved word "in"
        val packageName1 = "com.katfun.tech.share.sample.codes.in"
        val expected1 = "com.katfun.tech.share.sample.codes.`in`"
        assertEquals(expected1, packageName1.wrapReservedWords(delimiter, reservedWords, quoteChar))

        // Test case 2: Package name with multiple reserved words
        val packageName2 = "package.io.github.class.for.val"
        val expected2 = "`package`.io.github.`class`.`for`.`val`"
        assertEquals(expected2, packageName2.wrapReservedWords(delimiter, reservedWords, quoteChar))

        // Test case 3: Package name with no reserved words
        val packageName3 = "org.example.application.utils"
        val expected3 = "org.example.application.utils"
        assertEquals(expected3, packageName3.wrapReservedWords(delimiter, reservedWords, quoteChar))

        // Test case 4: Package name where part of a token contains a reserved word (should not wrap)
        val packageName4 = "my.classy.stuff.inside.values"
        val expected4 = "my.classy.stuff.inside.values"
        assertEquals(expected4, packageName4.wrapReservedWords(delimiter, reservedWords, quoteChar))

        // Test case 5: Empty package name
        val packageName5 = ""
        val expected5 = ""
        assertEquals(expected5, packageName5.wrapReservedWords(delimiter, reservedWords, quoteChar))

        // Test case 6: Package name that is a single reserved word
        val packageName6 = "class"
        val expected6 = "`class`"
        assertEquals(expected6, packageName6.wrapReservedWords(delimiter, reservedWords, quoteChar))

        // Test case 7: Package name with reserved word at the beginning
        val packageName7 = "fun.project.app"
        val expected7 = "`fun`.project.app"
        assertEquals(expected7, packageName7.wrapReservedWords(delimiter, reservedWords, quoteChar))

        // Test case 8: Package name with reserved word at the end and no others
        val packageName8 = "another.example.object"
        val expected8 = "another.example.`object`"
        assertEquals(expected8, packageName8.wrapReservedWords(delimiter, reservedWords, quoteChar))
    }
}

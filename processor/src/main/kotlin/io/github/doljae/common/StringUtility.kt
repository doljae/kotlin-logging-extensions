package io.github.doljae.common

object StringUtility {
    fun wrapReservedWords(
        target: String,
        delimiter: Char,
        reservedWords: Collection<String>,
        quoteChar: Char,
    ): String {
        val tokens = target.split(delimiter)
        return tokens.joinToString(delimiter.toString()) { token ->
            if (reservedWords.contains(token)) {
                "$quoteChar$token$quoteChar"
            } else {
                token
            }
        }
    }
}

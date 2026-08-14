package liba

import io.github.doljae.kotlinlogging.extensions.Log

@Log
open class Base {
    fun who() = log.name
}

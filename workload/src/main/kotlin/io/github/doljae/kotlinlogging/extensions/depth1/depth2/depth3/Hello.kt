package io.github.doljae.kotlinlogging.extensions.depth1.depth2.depth3

class Hello {
    fun test() {
        log.info { "hello" }
        log.error { "hello" }
        log.warn { "hello" }
    }
}

package io.github.doljae.kotlinlogging.extensions.depth1.depth2

class Hello2 {
    fun test() {
        log.info { "hello" }
        log.error { "hello" }
        log.warn { "hello" }
    }
}

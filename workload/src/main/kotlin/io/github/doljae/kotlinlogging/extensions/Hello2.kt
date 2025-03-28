package io.github.doljae.kotlinlogging.extensions

class Hello2 {
    fun test() {
        log.info { "hello" }
        log.error { "hello" }
        log.warn { "hello" }
    }
}

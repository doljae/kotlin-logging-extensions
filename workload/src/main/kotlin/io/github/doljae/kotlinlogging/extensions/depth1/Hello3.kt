package io.github.doljae.kotlinlogging.extensions.depth1

class Hello3 {
    fun test() {
        log.info { "hello" }
        log.error { "hello" }
        log.warn { "hello" }
    }
}

package io.github.doljae.kotlinlogging.extensions

fun main() {
    val hello1 = Hello()
    val hello2 = io.github.doljae.kotlinlogging.extensions.depth1.Hello()
    val hello3 = io.github.doljae.kotlinlogging.extensions.depth1.depth2.Hello()

    hello1.test()
    hello2.test()
    hello3.test()
}

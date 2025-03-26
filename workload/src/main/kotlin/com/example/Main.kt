package com.example

fun main() {
    val hello1 = com.example.Hello()
    val hello2 = com.example.depth1.Hello()
    val hello3 = com.example.depth1.depth2.Hello()

    hello1.test()
    hello2.test()
    hello3.test()
}

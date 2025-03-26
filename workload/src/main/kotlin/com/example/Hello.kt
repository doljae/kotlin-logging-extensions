package com.example

class Hello {
    fun test(){
        log.info { "it works!!" }
    }
}

fun main() {
    val hello = Hello()
    hello.test()
}

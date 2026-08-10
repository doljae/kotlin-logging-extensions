package examples.reserved.`is`

import io.github.doljae.kotlinlogging.extensions.Log

@Log
class ReservedClass {
    fun doSomething() {
        log.info { "Reserved class logging" }
    }
}

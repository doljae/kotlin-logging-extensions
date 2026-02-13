package examples.reserved.`is`

import io.github.doljae.kotlinlogging.extensions.AutoLog

@AutoLog
class ReservedClass {
    fun doSomething() {
        log.info { "Reserved class logging" }
    }
}

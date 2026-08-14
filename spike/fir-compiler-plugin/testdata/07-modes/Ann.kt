package annmode

import io.github.doljae.kotlinlogging.extensions.Log

@Log
open class Base {
    fun who() = log.name
}

/** Not annotated. Under AnnotationOnly it is not selected — but it still inherits `log`. */
class Sub : Base() {
    fun mine() = log.name
}

/** Not annotated and inherits nothing. Must get no `log` at all. */
class Plain

fun main() {
    println("Base().who()  = " + Base().who())
    println("Sub().who()   = " + Sub().who())
    println("Sub().mine()  = " + Sub().mine())
    println("Plain has log = " + Plain::class.java.methods.any { it.name == "getLog" })
    println("Base has log  = " + Base::class.java.declaredMethods.any { it.name == "getLog" })
    println("Sub has log   = " + Sub::class.java.declaredMethods.any { it.name == "getLog" })
}

package appb

import liba.Base

/**
 * No annotation, and none could survive anyway: `@Log` is SOURCE-retained, so it is gone from
 * mod-a's class file. What mod-b sees instead is the generated `open val log: KLogger` member.
 */
class Sub : Base() {
    fun mine() = log.name
}

class Unrelated

fun main() {
    println("Sub().who()         = " + Sub().who())
    println("Sub().mine()        = " + Sub().mine())
    println("Unrelated has log   = " + Unrelated::class.java.methods.any { it.name == "getLog" })
}

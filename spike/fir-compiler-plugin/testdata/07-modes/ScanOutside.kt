package manual

import scanned.ScannedBase

/** Outside every scanned package, so PackageScan does not select it — but it inherits `log`. */
class OutsideSub : ScannedBase() {
    fun mine() = log.name
}

/** Outside, and inherits nothing. Must get no `log`. */
class OutsideOnly

fun main() {
    println("ScannedBase().who()   = " + ScannedBase().who())
    println("OutsideSub().who()    = " + OutsideSub().who())
    println("OutsideSub().mine()   = " + OutsideSub().mine())
    println("OutsideOnly has log   = " + OutsideOnly::class.java.methods.any { it.name == "getLog" })
}

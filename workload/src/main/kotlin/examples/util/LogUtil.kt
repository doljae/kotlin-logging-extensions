package examples.util

object LogUtil {

    fun error(
        cause: Throwable,
        message: String? = "No message provided"
    ) {
        log.atError {
            this.cause = cause
            this.message = message
        }
    }
}

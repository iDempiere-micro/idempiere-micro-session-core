package company.bigger.util

import company.bigger.service.LoginService

internal fun <T> String.asResource(work: (String) -> T): T {
    val content = LoginService::class.java.getResource(this).readText()
    return work(content)
}

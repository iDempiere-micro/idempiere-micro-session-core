package company.bigger.util

internal fun <T> String.asResource(work: (String) -> T): T {
    val content = Ini::class.java.getResource(this).readText()
    return work(content)
}

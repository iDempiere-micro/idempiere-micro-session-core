package company.bigger.test.support

import java.util.Random

/**
 * Generate a random string (small letters)
 */
internal fun randomString(length: Int): String {
    fun ClosedRange<Char>.randomString(length: Int) =
            (1..length)
                    .map { (Random().nextInt(endInclusive.toInt() - start.toInt()) + start.toInt()).toChar() }
                    .joinToString("")
    return ('a'..'z').randomString(length)
}

/**
 * Load a test resource
 */
internal fun String.asResource(work: (String) -> Unit) {
    val content = BaseTest::class.java.getResource(this).readText()
    work(content)
}

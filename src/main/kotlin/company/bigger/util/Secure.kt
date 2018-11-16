package company.bigger.util

import mu.KotlinLogging
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val log = KotlinLogging.logger {}

/**
 * Convert Hex String to Byte Array
 *
 * @param hexString hex string
 * @return byte array
 */
internal fun convertHexString(hexString: String?): ByteArray? {
    if (hexString == null || hexString.isEmpty()) return null
    val size = hexString.length / 2
    val retValue = ByteArray(size)
    val inString = hexString.toLowerCase()

    for (i in 0 until size) {
        val index = i * 2
        val ii = Integer.parseInt(inString.substring(index, index + 2), 16)
        retValue[i] = ii.toByte()
    }
    return retValue
}

/**
 * ************************************************************************ Convert Byte Array to
 * Hex String
 *
 * @param bytes bytes
 * @return HexString
 */
internal fun convertToHexString(bytes: ByteArray): String {
    // 	see also Util.toHex
    val size = bytes.size
    val buffer = StringBuilder(size * 2)
    for (i in 0 until size) {
        // convert byte to an int
        var x = bytes[i].toInt()
        // account for int being a signed type and byte being unsigned
        if (x < 0) x += 256
        val tmp = Integer.toHexString(x)
        // pad out "1" to "01" etc.
        if (tmp.length == 1) buffer.append("0")
        buffer.append(tmp)
    }
    return buffer.toString()
} //  convertToHexString

/**
 * Convert String and salt to SHA-512 hash with iterations
 * https://www.owasp.org/index.php/Hashing_Java
 *
 * @param value message
 * @return HexString of message (length = 128 characters)
 * @throws NoSuchAlgorithmException
 * @throws UnsupportedEncodingException
 */
@Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
internal fun getSHA512Hash(iterations: Int, value: String, salt: ByteArray?): String {
    val digest = MessageDigest.getInstance("SHA-512")
    digest.reset()
    digest.update(salt)
    var input = digest.digest(value.toByteArray(charset("UTF-8")))
    for (i in 0 until iterations) {
        digest.reset()
        input = digest.digest(input)
    }
    digest.reset()
    //
    return convertToHexString(input)
}
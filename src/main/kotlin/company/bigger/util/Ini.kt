package company.bigger.util

/**
 * The original Ini class from iDempiere to collect the configuration parameters.
 * It is still used for the connection string etc.
 * Note some local connection variables (like `jwt.issuer`) if used once-only
 * are declared in the place where needed.
 */
open class Ini (
    internal val url: String,
    internal val username: String,
    internal val password: String
)

/**
 * Get system configuration property of type boolean
 *
 * @param Name
 * @param defaultValue
 * @return boolean
 */
internal fun getBooleanValue(s: String, defaultValue: Boolean = false): Boolean {
    if (s.isEmpty()) return defaultValue

    return if ("Y".equals(s, ignoreCase = true))
        true
    else if ("N".equals(s, ignoreCase = true))
        false
    else
        java.lang.Boolean.valueOf(s)
}

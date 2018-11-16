package company.bigger.util

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

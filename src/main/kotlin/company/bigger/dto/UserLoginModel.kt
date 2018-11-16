package company.bigger.dto

/**
 * A data class to send the login parameters.
 * See [ILogin] for notes and limitations.
 */
data class UserLoginModel(
    /**
     * The user name or email to log in with.
     */
    override val loginName: String,
    /**
     * The unencrypted password. Always use SSL!
     */
    override val password: String,
    /**
     * The non mandatory clientId necessary to be sent only if the user has access to more clients.
     */
    override val clientId: Int? = null
) : ILogin
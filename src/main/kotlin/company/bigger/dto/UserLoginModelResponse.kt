package company.bigger.dto

/**
 * The response we send to the calling site as a result of an attempted login.
 * See [ILoginResponse] for the details and limitations
 */
data class UserLoginModelResponse(
    /**
     * Was the request authenticated?
     */
    override val logged: Boolean = false,
    /**
     * Then token to be then sent with the requests
     */
    override val token: String? = null,
    /**
     * The loginName that was used to send the login request
     */
    override val loginName: String,
    /**
     * The clientId of the authenticated user or null
     */
    override val clientId: Int? = null,
    /**
     * The authenticated user Id or null
     */
    override val userId: Int? = null
) : ILoginResponse
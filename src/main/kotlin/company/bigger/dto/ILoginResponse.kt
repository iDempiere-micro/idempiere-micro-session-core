package company.bigger.dto

/**
 * The response we send to the calling site as a result of an attempted login.
 * We always copy the login name back,
 */
interface ILoginResponse {
    /**
     * The login name used to login (or email, see [ILogin])
     */
    val loginName: String
    /**
     * Was the login successful? Please note for users with more client this can also fail if [clientId] is not sent
     * in [ILogin]
     */
    val logged: Boolean
    /**
     * The token to be used to authenticate requests. The token then can be sent:
     * - as request header Authorization: Bearer <token here>
     * - or as request header Authorization: Token <token here>
     * - or in a request parameter `access_token` e.g. in the URL
     * to authenticate the request.
     */
    val token: String?
    /**
     * The clientId the authenticated user belongs to or null if not authenticated
     */
    val clientId: Int?
    /**
     * The internal userId of the authenticated user or null if not authenticated
     */
    val userId: Int?
}
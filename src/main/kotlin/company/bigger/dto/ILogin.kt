package company.bigger.dto

/**
 * Login parameters we support in idempiere-micro-spring-session.
 * Please note it is much less than in the full iDempiere.
 * Basically currently the login works only for users that are assigned to a single client.
 * This is however on par with the idempiere-micro-spring full backend see `setCurrentUser` in `LoginService`.
 */
interface ILogin {
    /**
     * The login name. Can be also the user email (we decided to support both by default, no need to setup USE_EMAIL_FOR_LOGIN)
     */
    val loginName: String
    /**
     * The unencrypted user password. We rely on the infrastructure to protect the information send to the microservice.
     * Always use SSL.
     */
    val password: String
    /**
     * Id of the Client (=Tenant) the user is trying to log in. Not mandatory, useful only if the user has access
     * to more clients.
     * Please note if user with an access to more clients will try to login without sending the clientId,
     * the login will fail as we do not know what client the user want to login to.
     */
    val clientId: Int?
}
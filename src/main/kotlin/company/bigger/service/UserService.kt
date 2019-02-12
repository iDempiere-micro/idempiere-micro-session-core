package company.bigger.service

import company.bigger.dto.UserLoginModel
import company.bigger.dto.UserLoginModelResponse
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotliquery.Session
import java.util.*

/**
 * The service to login the user and return the token.
 */
class UserService(
    val loginService: LoginService,

    /**
     * Secret key to be used by JWT.
     * This allows the microservices running to validate the token generated by another service instance once the token
     * is stored out of the microservice.
     */
    val jwtSecret: String,
    /**
     * Token issuer. This is checked in [isTokenValid].
     */
    val jwtIssuer: String,
    /**
     * Token expiratation. The default is 10 days.
     */
    val jwtExpiration: Int = 14400
) {
    // user login result by UserName
    private val users = mutableMapOf<String, UserLoginModelResponse>()

    /**
     * for testing only: you can set this instead of the [jwtExpiration]
     */
    internal var overrideExpiration: Int? = null

    private fun newToken(user: UserLoginModelResponse): String {
        val expirationInMillis = overrideExpiration ?: (jwtExpiration * 60 * 1000)
        return Jwts.builder()
                .setIssuedAt(Date())
                .setSubject(user.loginName)
                .setIssuer(jwtIssuer)
                .setExpiration(Date(System.currentTimeMillis() + expirationInMillis))
                .signWith(SignatureAlgorithm.HS256, jwtSecret).compact()
    }

    fun findByToken(token: String) = users.values.firstOrNull { it.token == token }

    /**
     * Make sure the token is valid. The io.jsonwebtoken makes sure the token is not expired.
     * We do check:
     * - the [loginName] is the same as when the token was created
     * - and the [jwtIssuer] to be same as in the ENV variables
     * Note we do NOT do any database call here so we claim the token be valid even if the user was deactivated meanwhile.
     */
    private fun isTokenValid(token: String, user: UserLoginModelResponse?): Boolean {
        try {
            val claims = Jwts.parser().setSigningKey(jwtSecret)
                    .parseClaimsJws(token).body
            // we do not check for the expiration, that is handled by DefaultJwtParser.parse
            // would throw something like:
            // io.jsonwebtoken.ExpiredJwtException: JWT expired at 2018-11-15T13:43:41Z. Current time: 2018-11-15T13:43:41Z,
            // a difference of 811 milliseconds.  Allowed clock skew: 0 milliseconds.
            return claims.subject == user?.loginName && claims.issuer == jwtIssuer
        } catch (e: ExpiredJwtException) {
            return false
        }
    }

    private fun updateToken(user: UserLoginModelResponse): UserLoginModelResponse {
        val result = user.copy(token = newToken(user))
        users[result.loginName] = result
        return result
    }

    /**
     * Login the user and return the token if successful together with other helpful attributes.
     * See also [LoginService]
     */
    fun login(session: Session, login: UserLoginModel): UserLoginModelResponse? {
        val user = loginService.login(session, login)
        return if (user.logged) updateToken(user) else user
    }

    /**
     * Validate a token and return the associated user
     * Note tokens do not survive session service restart now yet.
     */
    fun validateToken(token: String): UserLoginModelResponse? {
        val user = findByToken(token) ?: return null
        val isTokenValid = isTokenValid(token, user)
        return if (isTokenValid) user else null
    }
}
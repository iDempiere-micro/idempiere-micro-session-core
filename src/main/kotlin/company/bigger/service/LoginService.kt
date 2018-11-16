package company.bigger.service

import company.bigger.dto.ILogin
import company.bigger.dto.UserLoginModelResponse
import company.bigger.util.asResource
import company.bigger.util.getBooleanValue
import company.bigger.util.getSHA512Hash
import company.bigger.util.convertHexString
import company.bigger.util.User
import company.bigger.util.unlockUser
import company.bigger.util.lockUnauthenticatedUser
import company.bigger.util.getNumberOfDays
import kotliquery.Session
import kotliquery.queryOf
import mu.KotlinLogging
import java.io.UnsupportedEncodingException
import java.security.NoSuchAlgorithmException

private val log = KotlinLogging.logger {}

/**
 * The login service to login the user.
 * It validates the user in the database, check the password and is also responsible for user locking/unlocking.
 * Note this service does NOT
 */
class LoginService(
    val USER_PASSWORD_HASH: String = "N",
    val USER_LOCKING_MAX_ACCOUNT_LOCK_MINUTES: Int = 180,
    val USER_LOCKING_MAX_INACTIVE_PERIOD_DAY: Int = 180,
    val USER_LOCKING_MAX_PASSWORD_AGE_DAY: Int = 365,
    val USER_LOCKING_MAX_LOGIN_ATTEMPT: Int = 10
) {
    private fun lockUser(session: Session, user: User) {
        val MAX_INACTIVE_PERIOD_DAY = USER_LOCKING_MAX_INACTIVE_PERIOD_DAY.toInt()

        if (MAX_INACTIVE_PERIOD_DAY > 0 && !user.isLocked && user.dateLastLogin != null) {
            val days = getNumberOfDays(user.dateLastLogin)
            if (days > MAX_INACTIVE_PERIOD_DAY) {
                "/sql/lockUser.sql".asResource { s2 ->
                    session.run(queryOf(s2, user.id).asUpdate)
                }
            }
        }
    }

    private fun lockOrUnlockUsers(session: Session, users: List<User>) {
        val MAX_ACCOUNT_LOCK_MINUTES = USER_LOCKING_MAX_ACCOUNT_LOCK_MINUTES.toInt()
        val MAX_INACTIVE_PERIOD_DAY = USER_LOCKING_MAX_INACTIVE_PERIOD_DAY.toInt()

        for (user in users) {
            lockUser(session, user)
            unlockUser(session, user, MAX_ACCOUNT_LOCK_MINUTES, MAX_INACTIVE_PERIOD_DAY)
        }
    }

    private fun findByUsername(session: Session, appUser: String?): List<User> {
        log.info("User=$appUser")

        if (appUser == null || appUser.isEmpty()) {
            log.warn("No Apps User")
            return listOf()
        }

        return "/sql/findByUsername.sql".asResource { s ->
            val usersQuery = queryOf(s, appUser, appUser).map { row -> User(
                row.int(1), row.boolean(42), row.sqlTimestampOrNull(43), row.sqlTimestampOrNull(46),
                row.stringOrNull(11), row.stringOrNull(41), row.int(2), row.intOrNull(44),
                row.string(9)
            ) }.asList

            val users = session.run(usersQuery)

            if (users.isEmpty()) {
                log.error("UserPwdError {} {}", appUser, false)
                listOf<Int>()
            }
            lockOrUnlockUsers(session, users)

            users
        }
    }

    private fun doFilterAuthenticatedUsers(users: List<User>, appPwd: String): Pair<List<User>, List<User>> {
        val hashPassword = getBooleanValue(USER_PASSWORD_HASH)
        val MAX_PASSWORD_AGE = USER_LOCKING_MAX_PASSWORD_AGE_DAY.toInt()

        val authenticatedUsers = users.filter {
            when {
                hashPassword -> authenticateHash(it, appPwd)
                else -> // password not hashed
                    it.password != null && it.password == appPwd
            } && !it.isLocked
        }

        val failedUsers = users - authenticatedUsers

        return Pair(authenticatedUsers, failedUsers)
    }

    private fun lockUnauthenticatedUsers(session: Session, failedUsers: List<User>) {
        val maxLoginAttempt = USER_LOCKING_MAX_LOGIN_ATTEMPT.toInt()

        failedUsers.forEach {
            if (!it.isLocked) {
                lockUnauthenticatedUser(session, it, maxLoginAttempt)
            }
        }
    }

    private fun getUsers(session: Session, appUser: String, appPwd: String): Array<User> {
        log.info("User=$appUser")

        if (appUser.isEmpty()) {
            log.warn("No Apps User")
            return arrayOf()
        }
        if (appPwd.isEmpty()) {
            log.warn("No Apps Password")
            return arrayOf()
        }
        val users = findByUsername(session, appUser)
        if (users.isEmpty()) {
            log.error("UserPwdError {}", appUser)
            return arrayOf()
        }

        val (authenticatedUsers, failedUsers) = doFilterAuthenticatedUsers(users, appPwd)

        lockUnauthenticatedUsers(session, failedUsers)

        return authenticatedUsers.toTypedArray()
    }

    private fun authenticateHash(user: User, planText: String): Boolean {
        val hashedText = user.password ?: "0000000000000000"
        val hexSalt = user.salt ?: "0000000000000000"

        return try {
            getSHA512Hash(1000, planText, convertHexString(hexSalt)) == hashedText
        } catch (ignored: NoSuchAlgorithmException) {
            log.warn("Password hashing not supported by JVM")
            false
        } catch (ignored: UnsupportedEncodingException) {
            log.warn("Password hashing not supported by JVM")
            false
        }
    }

    /**
     * Verify if the user sent in [login] exists in the database, if it has access to one client only or the clientId
     * is provided, the password fits. Also make sure that the user is locked or unlocked.
     * Note this function does not work with token.
     */
    fun login(session: Session, login: ILogin): UserLoginModelResponse {
        val users = getUsers(session, login.loginName, login.password)
        val user = users.firstOrNull { users.count() == 1 || it.clientId == login.clientId }
        if (user != null) {
            return UserLoginModelResponse(checkUserAccess(session, user), null, login.loginName, user.clientId, user.id)
        }
        return UserLoginModelResponse(loginName = login.loginName)
    }

    private fun checkUserAccess(session: Session, user: User): Boolean {
        val businessPartnersIds: List<Int?> = "/sql/checkUserAccess.sql".asResource { s ->
            val usersQuery = queryOf(s, user.id).map { row ->
                    row.intOrNull(2) }.asList

            session.run(usersQuery)
        }
        return !businessPartnersIds.none { it != null }
    }
}
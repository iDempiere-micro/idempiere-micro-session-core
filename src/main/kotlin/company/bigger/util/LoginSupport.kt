package company.bigger.util

import kotliquery.Session
import kotliquery.queryOf
import mu.KotlinLogging
import java.sql.Timestamp
import java.util.Date

private val log = KotlinLogging.logger {}

internal data class User(
    val id: Int, // 1
    val isLocked: Boolean, // 42
    val dateAccountLocked: Timestamp?, // 43
    val dateLastLogin: Timestamp?, // 46
    val password: String?, // 11
    val salt: String?, // 41
    val clientId: Int, // 2
    val failedLoginCount: Int?, // 44
    val userName: String // 9
)

internal fun getNumberOfDays(date: Timestamp): Long {
    val now = Date().time
    return (now - date.time) / (1000 * 60 * 60 * 24)
}

internal fun unlockUser(session: Session, user: User, MAX_ACCOUNT_LOCK_MINUTES: Int, MAX_INACTIVE_PERIOD_DAY: Int) {
    val now = Date().time
    if (MAX_ACCOUNT_LOCK_MINUTES == 0 || !user.isLocked || user.dateAccountLocked == null)
        return

    val minutes = (now - user.dateAccountLocked.time) / (1000 * 60)
    if (minutes <= MAX_ACCOUNT_LOCK_MINUTES) return

    val inactive =
        if (MAX_INACTIVE_PERIOD_DAY > 0 && user.dateLastLogin != null) {
            val days = getNumberOfDays(user.dateLastLogin)
            days > MAX_INACTIVE_PERIOD_DAY
        } else {
            false
        }

    if (!inactive) {
        "/sql/unlockUser.sql".asResource {
            session.run(queryOf(it, user.id).asUpdate)
        }
    }
}

internal fun lockUnauthenticatedUser(session: Session, failedUser: User, maxLoginAttempt: Int) {
    val count = (failedUser.failedLoginCount ?: 0) + 1
    val reachMaxAttempt = when {
        maxLoginAttempt in 1..count -> {
            log.warn { "Reached the maximum number of setSecurityContext attempts, user account (${failedUser.userName}) is locked" }
            true
        }
        maxLoginAttempt > 0 -> {
            log.warn { "Invalid User ID or Password (${failedUser.userName}) (Login Attempts: $count / $maxLoginAttempt" }
            false
        }
        else -> false
    }
    "/sql/updateUserWithFailedCount.sql".asResource { s ->
        session.run(
            queryOf(
                s,
                if (reachMaxAttempt) "Y" else "N",
                count,
                if (reachMaxAttempt) Timestamp(Date().time) else null,
                failedUser.id
            ).asUpdate
        )
    }
}

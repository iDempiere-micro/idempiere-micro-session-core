package company.bigger.test.session

import company.bigger.dto.UserLoginModel
import company.bigger.service.LoginService
import company.bigger.service.UserService
import kotliquery.HikariCP
import kotliquery.sessionOf
import kotliquery.using
import org.junit.Before
import org.junit.Test
import java.util.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal val sessionUrl = System.getenv("SESSION_URL") ?: "jdbc:postgresql://localhost:5433/idempiere"

/**
 * Generate a random string (small letters)
 */
fun randomString(length: Int): String {
    fun ClosedRange<Char>.randomString(length: Int) =
        (1..length)
            .map { (Random().nextInt(endInclusive.toInt() - start.toInt()) + start.toInt()).toChar() }
            .joinToString("")
    return ('a'..'z').randomString(length)
}


/**
 * Testing the user service
 */
class UserServiceTest {
    companion object {
        const val GardenAdmin = "GardenAdmin"
        const val GardenUser = "GardenUser"
        const val System = "System"
    }

    init {
        HikariCP.default(sessionUrl, "adempiere", "adempiere")
    }

    private val userService = UserService(
        LoginService(),
        jwtSecret = randomString(20),
        jwtIssuer = "Issuer"
    )

    /**
     * Makes sure the hidden field to allow tweak the expiration is turned off before every test
     */
    @Before
    fun prepareExpiration() {
        // be 100% sure the expiration works normally
        userService.overrideExpiration = null
    }

    /**
     * GardenUser can login (service)
     */
    @Test
    fun `GardenUser can login (service)`() {
        using(sessionOf(HikariCP.dataSource())) { session ->
            val result = userService.login(session, UserLoginModel(GardenUser, GardenUser))
            assertNotNull(result?.token)
        }
    }

    /**
     * GardenUser can not login with wrong password (service)
     */
    @Test
    fun `GardenUser can not login with wrong password (service)`() {
        using(sessionOf(HikariCP.dataSource())) { session ->
            val result = userService.login(session, UserLoginModel(GardenUser, randomString(20)))
            assertNull(result?.token)
        }
    }

    /**
     * Joe Sales cannot login as he does not have a password (service)
     */
    @Test
    fun `Joe Sales cannot login as he does not have a password (service)`() {
        using(sessionOf(HikariCP.dataSource())) { session ->
            val result = userService.login(session, UserLoginModel("Joe Sales", ""))
            assertNull(result?.token)
        }
    }

    /**
     * System can not login as he does not have the business partner associated (service)
     */
    @Test
    fun `System can not login as he does not have the business partner associated (service)`() {
        using(sessionOf(HikariCP.dataSource())) { session ->
            val result = userService.login(session, UserLoginModel(System, System))
            assertNull(result?.token)
        }
    }

    /**
     * GardenAdmin can not login after too much unsuccessful logins (service)
     */
    @Test
    fun `GardenAdmin can not login after too much unsuccessful logins (service)`() {
        using(sessionOf(HikariCP.dataSource())) { session ->
            val okResult = userService.login(session, UserLoginModel(GardenAdmin, GardenAdmin))
            assertNotNull(okResult?.token)
            val badPassword = randomString(20)
            for (i in 1..20) {
                val result = userService.login(session, UserLoginModel(GardenAdmin, badPassword))
                assertNull(result?.token)
            }
            val result = userService.login(session, UserLoginModel(GardenAdmin, GardenAdmin))
            assertNull(result?.token)
        }
    }

    /**
     * GardenUser can login (service) and the token is valid (name + clientId)
     */
    @Test
    fun `GardenUser can login (service) and the token is valid`() {
        using(sessionOf(HikariCP.dataSource())) { session ->
            val result = userService.login(session, UserLoginModel(GardenUser, GardenUser))
            val user = userService.validateToken(result?.token ?: "")
            assertEquals(GardenUser, user?.loginName)
            assertEquals(11, user?.clientId)
        }
    }

    /**
     * GardenUser can login (service) and the token is invalid when expired
     */
    @Test
    fun `GardenUser can login (service) and the token is not valid if expired`() {
        using(sessionOf(HikariCP.dataSource())) { session ->
            // we set the expiration the way it is immediately invalid
            userService.overrideExpiration = -1
            val result = userService.login(session, UserLoginModel(GardenUser, GardenUser))
            // return back to normal expiration
            userService.overrideExpiration = null
            val user = userService.validateToken(result?.token ?: "")
            assertNull(user)
        }
    }

    /**
     * GardenUser can login twice(service) and the first token is invalid then
     */
    @Test
    fun `GardenUser can login twice(service) and the first token is invalid then`() {
        using(sessionOf(HikariCP.dataSource())) { session ->
            val result = userService.login(session, UserLoginModel(GardenUser, GardenUser))
            val user = userService.validateToken(result?.token ?: "")
            assertNotNull(user)

            // we need to sleep here for the while in order to make the tokens different
            Thread.sleep(1500)

            val result2 = userService.login(session, UserLoginModel(GardenUser, GardenUser))
            val user2 = userService.validateToken(result2?.token ?: "")

            assertNotEquals(user2?.token, user.token)

            assertNotNull(user2)
            val user3 = userService.validateToken(result?.token ?: "")
            assertNull(user3)
        }
    }

    /**
     * Random token is invalid
     */
    @Test
    fun `Random token is invalid`() {
        assertNull(userService.validateToken(randomString(10)))
    }
}
package company.bigger.test.session

import company.bigger.dto.UserLoginModel
import company.bigger.service.LoginService
import company.bigger.service.UserService
import company.bigger.test.support.BaseTest
import company.bigger.test.support.randomString
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Testing the user service
 */
class UserServiceTest : BaseTest() {
    companion object {
        const val GardenAdmin = "GardenAdmin"
        const val GardenUser = "GardenUser"
        const val System = "System"
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
        val result = userService.login(UserLoginModel(GardenUser, GardenUser))
        assertNotNull(result?.token)
    }

    /**
     * GardenUser can not login with wrong password (service)
     */
    @Test
    fun `GardenUser can not login with wrong password (service)`() {
        val result = userService.login(UserLoginModel(GardenUser, randomString(20)))
        assertNull(result?.token)
    }

    /**
     * Joe Sales cannot login as he does not have a password (service)
     */
    @Test
    fun `Joe Sales cannot login as he does not have a password (service)`() {
        val result = userService.login(UserLoginModel("Joe Sales", ""))
        assertNull(result?.token)
    }

    /**
     * System can not login as he does not have the business partner associated (service)
     */
    @Test
    fun `System can not login as he does not have the business partner associated (service)`() {
        val result = userService.login(UserLoginModel(System, System))
        assertNull(result?.token)
    }

    /**
     * GardenAdmin can not login after too much unsuccessful logins (service)
     */
    @Test
    fun `GardenAdmin can not login after too much unsuccessful logins (service)`() {
        val okResult = userService.login(UserLoginModel(GardenAdmin, GardenAdmin))
        assertNotNull(okResult?.token)
        val badPassword = randomString(20)
        for (i in 1..20) {
            val result = userService.login(UserLoginModel(GardenAdmin, badPassword))
            assertNull(result?.token)
        }
        val result = userService.login(UserLoginModel(GardenAdmin, GardenAdmin))
        assertNull(result?.token)
    }

    /**
     * GardenUser can login (service) and the token is valid (name + clientId)
     */
    @Test
    fun `GardenUser can login (service) and the token is valid`() {
        val result = userService.login(UserLoginModel(GardenUser, GardenUser))
        val user = userService.validateToken(result?.token ?: "")
        assertEquals(GardenUser, user?.loginName)
        assertEquals(11, user?.clientId)
    }

    /**
     * GardenUser can login (service) and the token is invalid when expired
     */
    @Test
    fun `GardenUser can login (service) and the token is not valid if expired`() {
        // we set the expiration the way it is immediately invalid
        userService.overrideExpiration = -1
        val result = userService.login(UserLoginModel(GardenUser, GardenUser))
        // return back to normal expiration
        userService.overrideExpiration = null
        val user = userService.validateToken(result?.token ?: "")
        assertNull(user)
    }

    /**
     * GardenUser can login twice(service) and the first token is invalid then
     */
    @Test
    fun `GardenUser can login twice(service) and the first token is invalid then`() {
        val result = userService.login(UserLoginModel(GardenUser, GardenUser))
        val user = userService.validateToken(result?.token ?: "")
        assertNotNull(user)

        // we need to sleep here for the while in order to make the tokens different
        Thread.sleep(1500)

        val result2 = userService.login(UserLoginModel(GardenUser, GardenUser))
        val user2 = userService.validateToken(result2?.token ?: "")

        assertNotEquals(user2?.token, user?.token)

        assertNotNull(user2)
        val user3 = userService.validateToken(result?.token ?: "")
        assertNull(user3)
    }

    /**
     * Random token is invalid
     */
    @Test
    fun `Random token is invalid`() {
        assertNull(userService.validateToken(randomString(10)))
    }
}
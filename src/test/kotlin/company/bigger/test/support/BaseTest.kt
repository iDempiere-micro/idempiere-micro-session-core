package company.bigger.test.support

import org.junit.runner.RunWith
import company.bigger.util.Ini
import kotliquery.HikariCP
import org.junit.Before
import org.junit.Test
import org.junit.runners.JUnit4

/**
 * Base Unit test running without the web environment
 */
@RunWith(JUnit4::class)
abstract class BaseTest {
    companion object {
        private var setUpIsDone = false
        private const val localhost = "jdbc:postgresql://localhost:5432/idempiere"
        private const val user = "adempiere"
    }
    /**
     * At the beginning of the tests setup the Hikari Connection Pool to connect to the Ini-provided PgSQL
     */
    @Before
    open fun prepare() {
        val ini = Ini(localhost, user, user)

        if (!setUpIsDone) {
            HikariCP.default(ini.url, ini.username, ini.password)
            setUpIsDone = true
        }
    }

    @Test
    fun test() {
    }
}

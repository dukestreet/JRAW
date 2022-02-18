package net.dean.jraw.test.unit

import com.winterbe.expekt.should
import net.dean.jraw.filterValuesNotNull
import net.dean.jraw.models.OAuthData
import net.dean.jraw.models.PersistedAuthData
import net.dean.jraw.oauth.AuthManager
import net.dean.jraw.oauth.DeferredPersistentTokenStore
import net.dean.jraw.test.createMockOAuthData
import net.dean.jraw.test.expectException
import net.dean.jraw.test.withExpiration
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.*

class DeferredPersistentStoreTest : Spek({
    // Sample data
    val oauthData = createMockOAuthData()
    val refreshToken = "<refresh token>"
    val username = "username"
    val data = mapOf(username to PersistedAuthData(oauthData, refreshToken))

    // Constructor shortcut
    fun newStore(initialData: Map<String, PersistedAuthData> = mapOf()) = MockDeferredPersistentTokenStore(initialData)

    describe("load") {
        it("should make the persisted data available") {
            val store = newStore()
            store._persisted = data.toMutableMap()
            store.load()

            store.fetchLatest(username).should.equal(oauthData)
            store.fetchRefreshToken(username).should.equal(refreshToken)
        }

        it("shouldn't load insignificant data") {
            val store = newStore()
            val expiredOAuthData = OAuthData(
                accessToken = "<access_token>", // Access token, scope, and refresh token are irrelevant
                scopes = listOf("scope1", "scope2"),
                refreshToken = null,
                expiration = Date(Date().time - 1)  // Make the expiration 1 ms in the past
            )

            // Make sure our logic is correct: a PersistedAuthData with (1) either no OAuthData or one that is expired
            // and (2) no refresh token makes this auth data insignificant.
            val insignificantAuthData = PersistedAuthData(expiredOAuthData, refreshToken = null)
            insignificantAuthData.isSignificant.should.be.`false`
            store._persisted = mutableMapOf(username to insignificantAuthData)

            // load() shouldn't load any insignificant entries
            store.load()
            store.size().should.equal(0)
        }
    }

    describe("persist") {
        it("should save the data") {
            val store = newStore(data)
            store._persisted.should.be.empty

            store.persist()
            store._persisted.should.not.be.empty

            store._persisted.should.equal(data)

            store.hasUnsaved().should.be.`false`
        }

        it("shouldn't save usernames with expired data by default") {
            val insignificantData = mapOf(username to PersistedAuthData(oauthData.withExpiration(Date(0L)), refreshToken = null))
            insignificantData[username]!!.isSignificant.should.be.`false`
            val store = newStore(insignificantData)

            store.persist()
            store._persisted.should.be.empty
        }

        it("should persist expired data as null") {
            val store = newStore(mapOf(username to PersistedAuthData(oauthData.withExpiration(Date(0L)), refreshToken)))
            store.persist()

            store._persisted[username]!!.should.equal(PersistedAuthData(latest = null, refreshToken))
        }
    }

    describe("hasUnsaved") {
        it("should change based on whether there are unsaved changes") {
            val store = newStore(data)
            store.persist()

            val name = store.usernames[0]

            val prev = store.fetchRefreshToken(name)!!
            store.storeRefreshToken(name, prev.repeat(2))

            store.hasUnsaved().should.be.`true`

            store.storeRefreshToken(name, prev)
            store.hasUnsaved().should.be.`false`
        }
    }

    describe("autoPersist") {
        it("should persist changes immediately after storing data") {
            val store = newStore()
            store.autoPersist = true

            store._persisted[username]?.refreshToken.should.be.`null`
            store.storeRefreshToken(username, "foo")
            store._persisted[username]?.refreshToken.should.equal("foo")
        }

        it("should persist changes immediately after deleting data") {
            val store = newStore(data)
            store.autoPersist = true
            store.persist()

            // Make sure deleteLatest persists
            store.deleteLatest(username)
            store._persisted[username].should.equal(PersistedAuthData(latest = null, refreshToken))

            // Make sure deleteRefreshToken persists
            store.deleteRefreshToken(username)
            store._persisted[username].should.be.`null`
        }
    }

    describe("inspect") {
        it("shouldn't keep empty auth data references") {
            val store = newStore()
            store.inspect(username).should.be.`null`

            store.storeRefreshToken(username, "foo")
            store.inspect(username).should.equal(PersistedAuthData(latest = null, refreshToken = "foo"))

            store.deleteRefreshToken(username)
            store.inspect(username).should.be.`null`
        }
    }

    describe("usernames") {
        it("shouldn't include usernames that once had data but now don't") {
            val store = newStore()
            store.storeRefreshToken(username, "foo")
            store.usernames.should.equal(listOf(username))

            store.deleteRefreshToken(username)
            store.usernames.should.be.empty
        }
    }

    describe("clear") {
        it("should remove all in-memory data") {
            val store = newStore(initialData = mapOf("foo" to PersistedAuthData(createMockOAuthData(), refreshToken = null)))
            store.data().should.have.size(1)

            store.clear()
            store.data().should.have.size(0)
        }
    }

    describe("storeLatest/storeRefreshToken") {
        it("should not accept data for a username of USERNAME_USERLESS") {
            val store = newStore()
            expectException(IllegalArgumentException::class) {
                store.storeRefreshToken(AuthManager.USERNAME_UNKOWN, "")
            }
            expectException(IllegalArgumentException::class) {
                store.storeLatest(AuthManager.USERNAME_UNKOWN, createMockOAuthData())
            }
        }
    }
})

class MockDeferredPersistentTokenStore(initialData: Map<String, PersistedAuthData>) :
    DeferredPersistentTokenStore(initialData) {

    var _persisted: MutableMap<String, PersistedAuthData> = HashMap()

    override fun doPersist(data: Map<String, PersistedAuthData>) {
        this._persisted = data.toMutableMap()
    }

    override fun doLoad(): Map<String, PersistedAuthData> {
        return this._persisted.filterValuesNotNull().toMutableMap()
    }
}

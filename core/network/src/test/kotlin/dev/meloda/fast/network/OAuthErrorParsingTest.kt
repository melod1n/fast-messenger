package dev.meloda.fast.network

import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import com.squareup.moshi.Moshi
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.model.api.responses.AuthDirectErrorOnlyResponse
import dev.meloda.fast.model.api.responses.GetSilentTokenResponse
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface TestOAuthService {
    @DecodeErrorBody
    @GET("token")
    suspend fun getSilentToken(
        @QueryMap param: Map<String, String>
    ): ApiResult<GetSilentTokenResponse, AuthDirectErrorOnlyResponse>
}

class OAuthErrorParsingTest {

    private lateinit var server: MockWebServer
    private lateinit var service: TestOAuthService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(ApiResultConverterFactory)
            .addCallAdapterFactory(ApiResultCallAdapterFactory)
            .addConverterFactory(ResponseConverterFactory(TestJsonConverter(), FastLogger()))
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .client(OkHttpClient())
            .build()
            .create()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `flood control error with http 401 parsed as AuthDirectErrorOnlyResponse`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error":"9;Flood control","error_description":"too many attempts","error_type":"password_bruteforce_attempt","view":"alert"}""")
        )

        val result = runBlocking { service.getSilentToken(mapOf("grant_type" to "password")) }

        assertTrue(result.toString(), result is ApiResult.Failure.HttpFailure<*>)
        val failure = result as ApiResult.Failure.HttpFailure<*>
        assertTrue(
            "expected AuthDirectErrorOnlyResponse but was ${failure.error?.javaClass}: ${failure.error}",
            failure.error is AuthDirectErrorOnlyResponse
        )
    }

    @Test
    fun `need validation error with http 200 parsed as AuthDirectErrorOnlyResponse`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"error":"need_validation","validation_type":"2fa_sms","validation_sid":"123","phone_mask":"+7 ***"}""")
        )

        val result = runBlocking { service.getSilentToken(mapOf("grant_type" to "password")) }

        assertTrue(result.toString(), result is ApiResult.Failure.HttpFailure<*> || result is ApiResult.Failure.ApiFailure<*>)
        when (result) {
            is ApiResult.Failure.HttpFailure<*> -> assertTrue(
                "expected AuthDirectErrorOnlyResponse but was ${result.error?.javaClass}",
                result.error is AuthDirectErrorOnlyResponse
            )
            is ApiResult.Failure.ApiFailure<*> -> assertTrue(
                "expected AuthDirectErrorOnlyResponse but was ${result.error?.javaClass}",
                result.error is AuthDirectErrorOnlyResponse
            )
            else -> error("Unexpected result: $result")
        }
    }

    @Test
    fun `success body parsed as GetSilentTokenResponse`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"silent_token":"abc","silent_token_uuid":"uuid","silent_token_ttl":600,"trusted_hash":null,"error":null}""")
        )

        val result = runBlocking { service.getSilentToken(mapOf("grant_type" to "password")) }

        assertTrue(result.toString(), result is ApiResult.Success<*>)
    }

    private class TestJsonConverter : JsonConverter {
        private val moshi = Moshi.Builder().build()

        override fun fromJson(clazz: Class<*>, jsonString: String): Any? =
            moshi.adapter(clazz).fromJson(jsonString)

        override fun fromJson(type: java.lang.reflect.Type, jsonString: String): Any? =
            moshi.adapter<Any>(type).fromJson(jsonString)
    }
}

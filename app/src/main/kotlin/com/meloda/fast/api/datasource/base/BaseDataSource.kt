package com.meloda.fast.api.datasource.base

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.meloda.fast.api.Resource
import com.meloda.fast.api.model.ApiResponse
import com.meloda.fast.api.ErrorCodes
import com.meloda.fast.api.VKException
import okhttp3.ResponseBody
import retrofit2.HttpException

class BaseDataSource {

    private val TAG = BaseDataSource::class.simpleName

    //TODO: move to resources
    private val DEFAULT_ERROR = "Internal server error"

    protected suspend fun <T> getResult(apiCall: suspend () -> ApiResponse<T>): Resource<T> {
        try {
            val response = apiCall()
            return if (response.isSuccessful) {
                Resource.success(response.response)
            } else {
                Log.d(TAG, "Server response unsuccessful")
                if (response.error != null) {
                    Log.w(TAG, "Unsuccessful response with code 2XX")
                    Resource.error(response.error.message, response.response)
                } else {
                    Log.e(TAG, "Unsuccessful result without error!")
                    Resource.error(DEFAULT_ERROR)
                }
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Error while executing request ${e.message}")
            val errorBody = e.response()?.errorBody() ?: return Resource.error(DEFAULT_ERROR)
            val errorResponse = parseErrorBody<T>(errorBody) ?: return Resource.error(DEFAULT_ERROR)

            return Resource.error(errorResponse.message)
        } catch (e: Exception) {
            Log.e(TAG, "Error while executing request ${e.message}")

            return Resource.error(DEFAULT_ERROR)
        }
    }

    private fun <T> parseErrorBody(responseBody: ResponseBody?): Exception? {
        if (responseBody == null) return null

        val jsonResponse: JsonObject?
        try {
            jsonResponse = JsonParser.parseString(responseBody.string()) as? JsonObject
            if (jsonResponse == null) {
                Log.d(TAG, "Response body is empty while parsing error body.")
                return null
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error while parsing json ${e.message}")
            return null
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Unknown error ${e.message}")
            return null
        }

        if (jsonResponse.has("error")) {
            val error = jsonResponse["error"].asJsonObject

            val message = error["error_msg"].asString
            val code = error["error_code"].asInt

            val e = VKException("", message, code)

            //TODO: add checking invalid session
            if (code == 5 && message.contains("invalid session")) {
//                context?.startActivity(Intent(context, DropUserDataActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                })
            }

            if (code == ErrorCodes.CAPTCHA_NEEDED) {
                e.captchaImg = error["captcha_img"].asString
                e.captchaSid = error["captcha_sid"].asString
            }

            if (code == ErrorCodes.VALIDATION_REQUIRED) {
                e.redirectUri = error["redirect_uri"].asString
            }

            return e
        }

        return null
    }

}
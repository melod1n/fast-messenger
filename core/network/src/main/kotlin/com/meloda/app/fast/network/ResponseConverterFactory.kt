package com.meloda.app.fast.network

import com.slack.eithernet.ApiException
import com.slack.eithernet.errorType
import com.slack.eithernet.toType
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * конвертер пытается перевести string с сервера в SuccessType
 * если не получается, то в ErrorType и выбрасывает [ApiException]
 *
 * допускает Unit как SuccessType в случае невозможности каста ответа в ErrorType
 */
class ResponseConverterFactory(private val converter: JsonConverter) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val (errorType, b) = annotations.errorType() ?: return null
        val errorRaw = getRawType(errorType.toType())
        return ResponseBodyConverter(
            successType = type,
            errorRaw = errorRaw,
            converter = converter,
        )
    }

    class ResponseBodyConverter(
        private val successType: Type,
        private val errorRaw: Class<*>,
        private val converter: JsonConverter,
    ) : Converter<ResponseBody, Any?> {
        override fun convert(value: ResponseBody): Any? {
            val string = value.string()
            kotlin.runCatching {
                converter.fromJson(successType, string)
            }.fold(
                onSuccess = { successModel -> return successModel },
                onFailure = {
                    val isUnit = successType == Unit::class.java

                    kotlin.runCatching {
                        converter.fromJson(errorRaw, string)
                    }.fold(
                        onSuccess = { errorModel -> throw ApiException(errorModel) },
                        onFailure = { exception ->
                            if (!isUnit) {
                                throw exception
                            } else {
                                return Unit
                            }
                        }
                    )
                }
            )
        }
    }
}

package dev.meloda.fast.network

import com.slack.eithernet.ApiException
import com.slack.eithernet.errorType
import com.slack.eithernet.toType
import com.squareup.moshi.JsonDataException
import dev.meloda.fast.logger.FastLogger
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
class ResponseConverterFactory(
    private val converter: JsonConverter,
    private val logger: FastLogger
) : Converter.Factory() {

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
            logger = logger
        )
    }

    class ResponseBodyConverter(
        private val successType: Type,
        private val errorRaw: Class<*>,
        private val converter: JsonConverter,
        private val logger: FastLogger
    ) : Converter<ResponseBody, Any?> {
        override fun convert(value: ResponseBody): Any? {
            val string = value.string()
            kotlin.runCatching {
                converter.fromJson(successType, string)
            }.fold(
                onSuccess = { successModel ->
                    if (successModel is ApiResponse<*>) {
                        if (successModel.error != null) {
                            throw ApiException(successModel.error)
                        }
                    }

                    return successModel
                },
                onFailure = { failure ->
                    val errorModel = kotlin.runCatching {
                        converter.fromJson(errorRaw, string)
                    }.getOrNull()

                    if (errorModel != null) {
                        logger.debug(this::class, "convert(): errorModel: $errorModel")
                        throw ApiException(errorModel)
                    }

                    if (failure is JsonDataException) {
                        logger.error(this::class, "convert(): ERROR", failure)
                        throw ApiException(
                            RestApiError(
                                errorCode = -1,
                                errorMsg = failure.message.orEmpty()
                            )
                        )
                    }

                    val isUnit = successType == Unit::class.java

                    if (!isUnit) {
                        throw failure
                    } else {
                        return Unit
                    }
                }
            )
        }
    }
}

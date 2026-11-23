package com.nexus.tune.core.common

sealed interface MusicResult<out T> {
    data class Success<T>(val data: T) : MusicResult<T>
    data class Failure(val error: ExtractorError) : MusicResult<Nothing>
}

sealed class ExtractorError(
    val message: String,
    val cause: Throwable? = null,
    val canRetry: Boolean = false
) {
    class NetworkError(cause: Throwable? = null) :
        ExtractorError("Network connection error", cause, canRetry = true)

    class RateLimitedError(val retryAfterSeconds: Int? = null) :
        ExtractorError("Rate limited by streaming service (429)", null, canRetry = true)

    class GeoBlockedError(val country: String? = null) :
        ExtractorError("Content unavailable in your geographical region", null, canRetry = false)

    class AgeRestrictedError(val videoId: String) :
        ExtractorError("Content requires user sign-in (Age-restricted)", null, canRetry = false)

    class TrackRemovedError(val videoId: String) :
        ExtractorError("Track has been deleted or made private", null, canRetry = false)

    class ExtractorBrokenError(cause: Throwable? = null) :
        ExtractorError("Extractor failed to parse stream metadata", cause, canRetry = false)

    class UnknownError(message: String, cause: Throwable? = null) :
        ExtractorError(message, cause, canRetry = true)
}

inline fun <T> MusicResult<T>.onSuccess(action: (T) -> Unit): MusicResult<T> {
    if (this is MusicResult.Success) action(data)
    return this
}

inline fun <T> MusicResult<T>.onFailure(action: (ExtractorError) -> Unit): MusicResult<T> {
    if (this is MusicResult.Failure) action(error)
    return this
}


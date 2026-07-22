package com.example.data.remote

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object InsforgeErrorHandler {

    private val _errorEvents = MutableSharedFlow<String>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    fun emitError(message: String) {
        _errorEvents.tryEmit(message)
    }

    fun handleNetworkException(e: Throwable, actionDescription: String = "Insforge Request") {
        val userFriendlyMessage = when {
            e is java.net.UnknownHostException || e is java.net.ConnectException ->
                "Network connection issue while communicating with Insforge. Using cached data."
            e is java.net.SocketTimeoutException ->
                "Insforge request timed out ($actionDescription)."
            e.message?.contains("401") == true || e.message?.contains("403") == true ->
                "Insforge API key authentication issue."
            else ->
                "Insforge API notice: ${e.localizedMessage ?: "Network error occurred"}"
        }
        _errorEvents.tryEmit(userFriendlyMessage)
    }
}

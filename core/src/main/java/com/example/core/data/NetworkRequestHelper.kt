package com.example.core.data


import com.example.core.data.models.BaseResponse
import com.example.core.data.models.Result
import com.example.core.utils.isValidJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

fun <T : Any> requestOnFlow(
    apiReqFunc: suspend () -> Response<BaseResponse<T>>
) = flow {
    emit(handleRequest(apiReqFunc))
}.flowOn(Dispatchers.IO)

suspend fun <T : Any> handleRequest(apiReqFunc: suspend () -> Response<BaseResponse<T>>): Result<T> {
    return try {
        val result = apiReqFunc.invoke()
        var errorMsg = "Something went wrong"
        if (result.isSuccessful) {
            val data = result.body()?.data
            return if (data != null) {
                Result.Success(data)
            } else {
                Result.EmptySuccess
            }
        } else {
            result.errorBody()?.string()?.let {
                if (it.isValidJson()) {
                    val jsonObject = JSONObject(it)
                    errorMsg = jsonObject.optString("message", errorMsg)
                }
            }
            return Result.Error(HttpException(result), errorMsg)
        }

    } catch (hE: HttpException) {
        Result.Error(hE)
    } catch (nE: NoInternetException) {
        Result.Error(nE)
    } catch (uE: UnknownHostException) {
        Result.Error(uE)
    } catch (sslE: SSLHandshakeException) {
        Result.Error(sslE)
    } catch (ioE: IOException) {
        Result.Error(ioE)
    } catch (soE: SocketTimeoutException) {
        Result.Error(soE)
    } catch (coE: ConnectException) {
        Result.Error(coE)
    }
}


suspend fun <T : Any> handleNullableRequest(apiReqFunc: suspend () -> Response<BaseResponse<T?>>): Result<T> {
    return try {
        val result = apiReqFunc.invoke()
        var errorMsg = "Something went wrong"
        if (result.isSuccessful) {
            val data = result.body()?.data

            return if (data != null) {
                Result.Success(data)
            } else {
                Result.EmptySuccess
            }
        } else {
            result.errorBody()?.string()?.let {
                if (it.isValidJson()) {
                    val jsonObject = JSONObject(it)
                    errorMsg = jsonObject.optString("message", errorMsg)
                }
            }
            return Result.Error(HttpException(result), errorMsg)
        }

    } catch (hE: HttpException) {
        Result.Error(hE)
    } catch (nE: NoInternetException) {
        Result.Error(nE)
    } catch (uE: UnknownHostException) {
        Result.Error(uE)
    } catch (sslE: SSLHandshakeException) {
        Result.Error(sslE)
    } catch (ioE: IOException) {
        Result.Error(ioE)
    } catch (soE: SocketTimeoutException) {
        Result.Error(soE)
    } catch (coE: ConnectException) {
        Result.Error(coE)
    }
}
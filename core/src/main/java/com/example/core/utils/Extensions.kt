package com.example.core.utils

import android.net.Uri
import androidx.core.net.MailTo
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

fun String.isValidJson() = try {
    JSONObject(this)
    true
} catch (ex: JSONException) {
    false
}

fun <T : Annotation> KAnnotatedElement.hasAnnotation(klass: KClass<T>) =
    findAnnotations(klass).isNotEmpty()

fun Request.printBody(): String {
    val copy = newBuilder().build()
    val buffer = okio.Buffer()
    copy.body?.writeTo(buffer)
    return buffer.readUtf8()
}

fun Uri?.mailTo(): MailTo? {
    return this?.takeIf { MailTo.isMailTo(it) }?.let { MailTo.parse(it) }
}

inline fun <P, reified C> P.executeIf(action: C.() -> P): P {
    return if (this is C)
        action(this)
    else
        this
}


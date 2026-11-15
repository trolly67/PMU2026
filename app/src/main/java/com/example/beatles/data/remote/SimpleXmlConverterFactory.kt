package com.example.beatles.data.remote

import okhttp3.ResponseBody
import org.simpleframework.xml.Root
import org.simpleframework.xml.core.Persister
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class SimpleXmlConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (type !is Class<*> || !type.isAnnotationPresent(Root::class.java)) return null
        return Converter<ResponseBody, Any> { body ->
            Persister().read(type as Class<Any>, body.byteStream())
        }
    }
}
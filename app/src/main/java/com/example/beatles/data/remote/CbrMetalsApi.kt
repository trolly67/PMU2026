package com.example.beatles.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import okhttp3.ResponseBody

interface CbrMetalsApi {
    @Streaming
    @GET("scripts/xml_metall.asp")
    suspend fun getMetals(
        @Query("date_req1") dateFrom: String,
        @Query("date_req2") dateTo: String
    ): ResponseBody
}
package com.example.beatles.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface CbrMetalsApi {
    @GET("scripts/xml_metall.asp")
    suspend fun getMetals(
        @Query("date_req1") dateFrom: String,
        @Query("date_req2") dateTo: String
    ): CbrMetalsResponse
}
package com.aegislink.network

import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface VirusTotalService {
    @GET("api/v3/urls/{encodedId}")
    suspend fun getUrlReport(
        @Header("x-apikey") apiKey: String,
        @Path("encodedId") encodedId: String
    ): ResponseBody

    @FormUrlEncoded
    @POST("api/v3/urls")
    suspend fun submitUrl(
        @Header("x-apikey") apiKey: String,
        @Field("url") url: String
    ): ResponseBody

    @GET("api/v3/analyses/{analysisId}")
    suspend fun getAnalysis(
        @Header("x-apikey") apiKey: String,
        @Path("analysisId") analysisId: String
    ): ResponseBody
}

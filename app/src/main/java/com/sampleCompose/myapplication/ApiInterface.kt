package com.sampleCompose.myapplication

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather?")
     suspend fun getDataByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units:String,
    ):Response<DataItem>
}
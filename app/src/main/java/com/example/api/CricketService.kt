package com.example.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface CricketService {
    @GET
    suspend fun getRssFeed(@Url url: String): ResponseBody

    @GET
    suspend fun getHtml(@Url url: String): ResponseBody
}

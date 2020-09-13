package ru.temnik.findpict.network.api.imageAPI

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.temnik.findpict.entityDTO.ApiRootDTO

interface ImageAPI {
    @GET("/api/")
    fun getPageImages(
        @Query("key") apikey: String,
        @Query("q") search: String,
        @Query("page") page: Int,
        @Query("image_type") type: String
    ): Call<ApiRootDTO>
}
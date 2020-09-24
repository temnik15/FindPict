package ru.temnik.findpict.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.temnik.findpict.AppData
import ru.temnik.findpict.network.api.imageAPI.ImageAPI

object  NetworkService {
    private val retrofit:Retrofit = Retrofit.Builder()
        .baseUrl(AppData.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getImageApi(): ImageAPI {
        return retrofit.create(ImageAPI::class.java)
    }
}
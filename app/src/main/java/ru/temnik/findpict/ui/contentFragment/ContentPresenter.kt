package ru.temnik.findpict.ui.contentFragment

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.temnik.findpict.AppData
import ru.temnik.findpict.ProfilePresenter
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.entityDTO.ApiRootDTO
import ru.temnik.findpict.network.NetworkService
import ru.temnik.findpict.network.api.imageAPI.PhotoType
import javax.inject.Inject

class ContentPresenter @Inject constructor(adapter: ContentAdapter) : ProfilePresenter {
    private var profileView: ProfileView? = null
    var contentAdapter: ContentAdapter = adapter

    var currentQuery: String = ""
        private set
    private var contentPage = 1


    fun search(query: String) {
        contentPage=1
        contentAdapter.updateItems(emptyList())
        profileView?.visibilityLoading(true)
        currentQuery = query
        loadContent(query)
    }

    fun loadContent(query: String) {
        if (profileView != null) {
            NetworkService.getImageApi()
                .getPageImages(AppData.apiKey, query, contentPage, PhotoType.ALL.name)
                .enqueue(object : Callback<ApiRootDTO> {
                    override fun onFailure(call: Call<ApiRootDTO>, t: Throwable) {
                        profileView?.visibilityLoading(false)
                        profileView?.visibilityError(true)
                        Log.d(AppData.debugTag,"[ContentPresenter] Retrofit --> Запрос: \"$query\". Ошибка загрузки контента!")
                        return
                        // TODO("Ошибка, например нет интернета")
                    }

                    override fun onResponse(
                        call: Call<ApiRootDTO>,
                        response: Response<ApiRootDTO>
                    ) {
                        // пересмотреть
                        profileView?.visibilityLoading(false)
                        profileView?.visibilityError(false)
                        //
                        val items = response.body()?.hits
                        if (items != null) {
                            contentAdapter.updateItems(items,true)
                            contentPage++
                            contentAdapter.isLoading = false
                            Log.d(AppData.debugTag,"[ContentPresenter] Retrofit --> Запрос: \"$query\". Загрузка контента выполнена! items.size = ${items.size}")
                        }
                    }
                })
        }
    }

    override fun onAttach(view: ProfileView) {
        profileView = view
    }

    override fun onDetach() {
        profileView = null
    }
}
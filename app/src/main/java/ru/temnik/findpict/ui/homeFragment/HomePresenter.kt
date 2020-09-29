package ru.temnik.findpict.ui.homeFragment

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

class HomePresenter @Inject constructor(homeAdapter: HomeAdapter) : ProfilePresenter {
    private val logName = "[HomePresenter]"
    private var profileView: ProfileView? = null
    var homeAdapter: HomeAdapter = homeAdapter
        private set

    var isLoading = false
    private var currentQuery: String = ""
    private var contentPage = 1


    fun search(query: String) {
        contentPage = 1
        homeAdapter.updateItems(emptyList())
        currentQuery = query
        profileView?.visibilityLoading(true)
        profileView?.visibilityNoContent(false)
        loadContent()
    }
    fun loadContent(query: String = currentQuery){
        isLoading = true
        if (profileView != null) {
            NetworkService.getImageApi()
                .getPageImages(AppData.API_KEY, query, contentPage, PhotoType.ALL.name)
                .enqueue(object : Callback<ApiRootDTO> {
                    override fun onFailure(call: Call<ApiRootDTO>, t: Throwable) {
                        profileView?.visibilityLoading(false)
                        profileView?.visibilityTextError(contentPage == 1)
                        profileView?.visibilityError(true)

                        Log.d(
                            AppData.DEBUG_TAG,
                            "$logName Retrofit --> Запрос: \"$query\". Ошибка загрузки контента!"
                        )
                        return
                    }

                    override fun onResponse(
                        call: Call<ApiRootDTO>,
                        response: Response<ApiRootDTO>
                    ) {
                        profileView?.visibilityLoading(false)
                        profileView?.visibilityError(false)
                        profileView?.visibilityTextError(false)


                        val items = response.body()?.hits
                        if (items != null) {
                            if (isLoading) {
                                if (items.isEmpty() && contentPage == 1) profileView?.visibilityNoContent(
                                    true
                                )
                                homeAdapter.updateItems(items, true)
                            }
                            if (items.isNotEmpty()) contentPage++
                            isLoading = false
                            Log.d(
                                AppData.DEBUG_TAG,
                                "$logName Retrofit --> Запрос: \"$query\". Загрузка контента выполнена! items.size = ${items.size}"
                            )
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
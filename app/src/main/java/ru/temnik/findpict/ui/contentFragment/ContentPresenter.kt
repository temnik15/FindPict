package ru.temnik.findpict.ui.contentFragment

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

class ContentPresenter @Inject constructor(adapter: ContentAdapter):ProfilePresenter{
    private var profileView:ProfileView?=null
    var contentAdapter: ContentAdapter = adapter
    private var contentPage = 1

    override fun onAttach(view:ProfileView){
        profileView=view
    }

    override fun onDetach() {
        profileView=null
    }

    fun loadContent(){
        if(profileView!=null){
            NetworkService.getImageApi()
                .getPageImages(AppData.apiKey,"car",contentPage,PhotoType.ALL.name)
                .enqueue(object: Callback<ApiRootDTO>{
                    override fun onFailure(call: Call<ApiRootDTO>, t: Throwable) {
                        profileView?.setErrorFragment()
                        return
                       // TODO("Ошибка, например нет интернета")
                    }

                    override fun onResponse(
                        call: Call<ApiRootDTO>,
                        response: Response<ApiRootDTO>
                    ) {
                        val items = response.body()?.hits
                        if(items!=null){
                            contentAdapter.updateItems(items)
                            contentPage++
                            contentAdapter.isLoading=false
                        }
                    }
                })
        }
    }
}
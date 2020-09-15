package ru.temnik.findpict.ui.contentFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.content_fragment.view.*
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.homeFragment.HomeFragment
import ru.temnik.findpict.ui.contentFragment.dagger.DaggerContentPresenterComponent

class ContentFragment : Fragment(), ProfileView {
    companion object {
        const val tag = "ContentFragment"
    }

    private val presenter = DaggerContentPresenterComponent.create().getContentPresenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.content_fragment, container, false)
        presenter.onAttach(this)
        HomeFragment.contentPresenter=presenter
        view.recycler.adapter = presenter.contentAdapter
        view.recycler.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        addPaginationListener(view.recycler)
        retainInstance = true
        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.onAttach(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.onDetach()
    }

    private fun addPaginationListener(recycler:RecyclerView){
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = recycler.layoutManager as StaggeredGridLayoutManager
                val firstVisibleItems =manager.findFirstVisibleItemPositions(intArrayOf(0, 0, 0))[2]
                val visibleItemCount = manager.childCount
                val totalItemCount = manager.itemCount
                if(!presenter.contentAdapter.isLoading && presenter.contentAdapter.itemCount>0){
                    if((visibleItemCount+firstVisibleItems+30)>=totalItemCount){
                        presenter.contentAdapter.isLoading=true
                        presenter.loadContent(presenter.currentQuery)
                    }
                }
            } })
    }

    override fun visibilityError(visible: Boolean) {
        val activity = this.activity
        if(activity!=null && activity is ProfileView){
            activity.visibilityError(visible)
        }
    }

    override fun visibilityLoading(visible: Boolean) {
        val activity = this.activity
        if(activity!=null && activity is ProfileView){
            activity.visibilityLoading(visible)
        }
    }
}


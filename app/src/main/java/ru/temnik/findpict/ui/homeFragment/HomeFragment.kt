package ru.temnik.findpict.ui.homeFragment

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment
import ru.temnik.findpict.ui.homeFragment.dagger.DaggerHomePresenterComponent


class HomeFragment : Fragment(), ProfileView,
    HomeAdapter.OnImageClickListener {
    companion object {
        val tag = HomeFragment::class.java.simpleName
        val presenter: HomePresenter = DaggerHomePresenterComponent.create().getHomePresenter()
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (presenter.homeAdapter.itemCount > 0) visibilityAppName(false)
        val recycler = view.recycler
        recycler.adapter = presenter.homeAdapter
        recycler.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        configSearch(view.view_search)
        addPaginationListener(recycler)
    }

    override fun onResume() {
        presenter.onAttach(this)
        presenter.homeAdapter.onAttach(this)
        super.onResume()
    }

    override fun onPause() {
        presenter.onDetach()
        presenter.homeAdapter.onDetach()
        super.onPause()
    }


    override fun visibilityError(visible: Boolean) {
        if (visible) {
            view_error.visibility = View.VISIBLE
        } else {
            view_error.visibility = View.GONE
        }
    }

    override fun visibilityLoading(visible: Boolean) {
        if (visible) {
            view_loading.visibility = View.VISIBLE
        } else {
            view_loading.visibility = View.GONE
        }
    }

    override fun visibilityAppName(visible: Boolean) {
        if (visible) {
            view_appName.visibility = View.VISIBLE
        } else {
            view_appName.visibility = View.GONE
        }
    }

    override fun onBackPressed(): Boolean {
        val adapter = presenter.homeAdapter
        return if (adapter.itemCount > 0) {
            presenter.isLoading = false
            adapter.updateItems(listOf())
            view_search.setQuery("", false)
            true
        } else {
            false
        }

    }




    override fun onImageClick(holder: HomeAdapter.ViewHolder) {
        val transitionView = holder.imageView
        val transitionName =
            context?.resources?.getString(R.string.default_transition_name) ?: "image_view"
        ViewCompat.setTransitionName(transitionView, transitionName)
        val transition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        transition.duration = 1000

        val newFragment = DetailsImageFragment.newInstance(holder.item)
        newFragment.sharedElementEnterTransition = transition
        fragmentManager?.beginTransaction()
            ?.addSharedElement(transitionView, transitionName)
            ?.addToBackStack(DetailsImageFragment.tag)
            ?.replace(R.id.activity_main_frame, newFragment, DetailsImageFragment.tag)
            ?.commit()
    }


    private fun configSearch(search: SearchView) {
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query != "") {
                    search.clearFocus()
                    view_appName.visibility = View.GONE
                    view_loading.visibility = View.VISIBLE
                    presenter.search(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun addPaginationListener(recycler: RecyclerView) {
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = recycler.layoutManager as StaggeredGridLayoutManager
                val firstVisibleItems =
                    manager.findFirstVisibleItemPositions(intArrayOf(0, 0, 0))[2]
                val visibleItemCount = manager.childCount
                val totalItemCount = manager.itemCount
                if (!presenter.isLoading && presenter.homeAdapter.itemCount > 0) {
                    if ((visibleItemCount + firstVisibleItems + 30) >= totalItemCount) {
                        presenter.loadContent(presenter.currentQuery)
                    }
                }
            }
        })
    }


}
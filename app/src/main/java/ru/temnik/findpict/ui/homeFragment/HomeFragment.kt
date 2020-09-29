package ru.temnik.findpict.ui.homeFragment

import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.detailsImageFragment.DetailsImageFragment
import ru.temnik.findpict.ui.dialogFragments.DialogNotificationFragment
import ru.temnik.findpict.ui.homeFragment.dagger.DaggerHomePresenterComponent


class HomeFragment : Fragment(), ProfileView,
    HomeAdapter.OnImageClickListener, SwipeRefreshLayout.OnRefreshListener {
    companion object {
        val tag = HomeFragment::class.java.simpleName
        val presenter: HomePresenter = DaggerHomePresenterComponent.create().getHomePresenter()
        var errorVisible = View.GONE
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
        view_error_frame.visibility = errorVisible
        val recycler = view.recycler
        recycler.adapter = presenter.homeAdapter
        recycler.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        configSearch(view.view_search)
        addPaginationListener(recycler)
        view_error.setOnClickListener {
            presenter.loadContent()
        }
        view_swipe_refresh.setOnRefreshListener(this)
        view_swipe_refresh.setColorSchemeResources(
            R.color.default_refresh_color)
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
            if (view_error_frame.visibility == View.VISIBLE) {
                showNotificationDialog()
            } else {
                if (view_first_error.visibility == View.GONE) {
                    view_error_frame.visibility = View.VISIBLE
                }

            }

        } else {
            view_error_frame.visibility = View.GONE
        }
    }

    private fun showNotificationDialog(
        title: String = getString(R.string.home_fragment_default_failed_load_images),
        message: String = getString(R.string.default_check_internet)
    ) {
        val fragment = DialogNotificationFragment.newInstance(
            title,
            message
        )
        fragment.show(childFragmentManager, fragment.tag)
    }

    override fun visibilityTextError(visible: Boolean) {
        if (view_swipe_refresh.visibility == View.VISIBLE) {
            Handler().postDelayed({ view_swipe_refresh.isRefreshing = false }, 500)
            if (visible) {
                showNotificationDialog()
            }
        }
        if (visible) {
            view_first_error.visibility = View.VISIBLE
            view_swipe_refresh.visibility = View.VISIBLE
        } else {
            view_first_error.visibility = View.GONE
            view_swipe_refresh.visibility = View.GONE
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

    override fun visibilityNoContent(visible: Boolean) {
        if (visible) {
            view_no_content.visibility = View.VISIBLE
        } else {
            view_no_content.visibility = View.GONE
        }
    }

    override fun onBackPressed(): Boolean {
        val adapter = presenter.homeAdapter
        return if (adapter.itemCount > 0) {
            presenter.isLoading = false
            adapter.updateItems(listOf())
            view_search.setQuery("", false)
            visibilityError(false)
            true
        } else {
            false
        }

    }


    override fun onImageClick(holder: HomeAdapter.ViewHolder) {
        errorVisible = view_error_frame.visibility
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
                    visibilityAppName(false)
                    visibilityTextError(false)
                    visibilityLoading(true)
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
                        presenter.loadContent()
                    }
                }
            }
        })
    }

    override fun onRefresh() {
        presenter.loadContent()
    }
}
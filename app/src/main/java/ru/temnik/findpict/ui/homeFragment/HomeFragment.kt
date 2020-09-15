package ru.temnik.findpict.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.home_fragment.view.*
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.contentFragment.ContentFragment
import ru.temnik.findpict.ui.contentFragment.dagger.DaggerContentPresenterComponent

class HomeFragment : Fragment(), ProfileView {
    companion object {
        const val tag = "HomeFragment"
    }
    private val presenter = HomePresenter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        view.tv_appName.visibility=View.GONE
        childFragmentManager
            .beginTransaction()
            .addToBackStack(ContentFragment.tag)
            .add(
                R.id.home_fragment_frame,
                ContentFragment(),
                ContentFragment.tag
            )
            .commit()

        //
        val search = view.search_view as SearchView
        // search.imeOptions = EditorInfo.IME_ACTION_SEARCH
        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        //
        retainInstance=true
        return view
    }

    override fun setErrorFragment() {
        childFragmentManager
            .beginTransaction()
            .addToBackStack(ErrorFragment.tag)
            .add(R.id.home_fragment_frame,ErrorFragment(),ErrorFragment.tag)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        presenter.onAttach(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.onDetach()
    }
}
package ru.temnik.findpict.ui.homeFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.contentFragment.ContentFragment
import ru.temnik.findpict.ui.contentFragment.ContentPresenter

class HomeFragment : Fragment(), ProfileView {
    companion object {
        const val tag = "HomeFragment"
        var contentPresenter:ContentPresenter? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        setContentFragment()
        val search = view.search_view as SearchView
        configSearch(search)
        retainInstance=true
        return view
    }



    private fun configSearch(search:SearchView){
        // search.imeOptions = EditorInfo.IME_ACTION_SEARCH
        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
               if(query!=null && query!=""){
                   activity?.tv_appName?.visibility=View.GONE
                   contentPresenter?.search(query)
               }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        //
    }
    private fun setContentFragment(){
        childFragmentManager
            .beginTransaction()
            .addToBackStack(ContentFragment.tag)
            .add(
                R.id.home_fragment_frame,
                ContentFragment(),
                ContentFragment.tag
            )
            .commit()
    }

    override fun visibilityError(visible: Boolean) {

    }

}
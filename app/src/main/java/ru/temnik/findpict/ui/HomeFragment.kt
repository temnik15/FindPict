package ru.temnik.findpict.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimatorRes
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.home_fragment.view.*
import ru.temnik.findpict.ProfileView
import ru.temnik.findpict.R
import ru.temnik.findpict.ui.contentFragment.ContentFragment

class HomeFragment : Fragment(), ProfileView {
    companion object {
        const val tag = "HomeFragment"
    }

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
        return view
    }

    override fun setErrorFragment() {
        childFragmentManager
            .beginTransaction()
            .addToBackStack(ErrorFragment.tag)
            .add(R.id.home_fragment_frame,ErrorFragment(),ErrorFragment.tag)
            .commit()
    }

}
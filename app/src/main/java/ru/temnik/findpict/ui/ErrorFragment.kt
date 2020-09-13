package ru.temnik.findpict.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.temnik.findpict.R

class ErrorFragment:Fragment() {
    companion object {
        const val tag = "ErrorFragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.error_fragment,container,false)
    }
}
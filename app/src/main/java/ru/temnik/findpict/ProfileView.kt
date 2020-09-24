package ru.temnik.findpict

import android.content.Context


interface ProfileView {
    fun visibilityError(visible:Boolean = false){}
    fun visibilityLoading(visible:Boolean = false){}
    fun visibilityAppName(visible:Boolean = false){}
    fun onBackPressed():Boolean
}
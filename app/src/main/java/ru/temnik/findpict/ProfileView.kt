package ru.temnik.findpict



interface ProfileView {
    fun visibilityError(visible:Boolean = false)
    fun visibilityLoading(visible:Boolean = false){}
}
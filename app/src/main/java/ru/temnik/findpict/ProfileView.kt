package ru.temnik.findpict


interface ProfileView {
    fun visibilityError(visible: Boolean) {}
    fun visibilityTextError(visible: Boolean) {}
    fun visibilityLoading(visible: Boolean) {}
    fun visibilitySave(visible: Boolean) {}
    fun visibilityNoContent(visible: Boolean) {}
    fun visibilityAppName(visible: Boolean) {}
    fun onBackPressed(): Boolean
}
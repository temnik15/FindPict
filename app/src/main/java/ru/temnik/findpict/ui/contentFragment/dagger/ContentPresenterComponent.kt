package ru.temnik.findpict.ui.contentFragment.dagger

import dagger.Component
import ru.temnik.findpict.ui.contentFragment.ContentPresenter


@Component
interface ContentPresenterComponent {
    fun getContentPresenter():ContentPresenter
}
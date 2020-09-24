package ru.temnik.findpict.ui.homeFragment.dagger

import dagger.Component
import ru.temnik.findpict.ui.homeFragment.HomePresenter


@Component
interface HomePresenterComponent {
    fun getHomePresenter():HomePresenter
}
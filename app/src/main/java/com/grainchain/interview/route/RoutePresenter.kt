package com.grainchain.interview.route

import com.grainchain.interview.data.Route
import com.grainchain.interview.helpers.RoutesManager

class RoutePresenterImpl(val view: RouteView) : RoutePresenter {
    private val routesManager = RoutesManager()

    override fun deleteRoute(route: Route) {
        routesManager.deleteRoute(route)

        view.onRouteDeleted()
    }
}

interface RoutePresenter {
    fun deleteRoute(route: Route)
}
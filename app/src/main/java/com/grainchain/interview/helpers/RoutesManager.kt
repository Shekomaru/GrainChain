package com.grainchain.interview.helpers

import com.grainchain.interview.data.Route

class RoutesManager(val listener: RoutesManagerListener? = null) {
    private companion object {
        var routes: MutableList<Route> = mutableListOf()
    }

    fun addRoute(route: Route) {
        routes.add(route)
        listener?.onRoutesChanged(routes)
    }

    fun deleteRoute(route: Route) {
        routes.firstOrNull { it.name == route.name }?.let {
            routes.remove(it)
        }
        listener?.onRoutesChanged(routes)
    }

    fun getRoutes(): MutableList<Route> {
        return routes
    }
}

interface RoutesManagerListener {
    fun onRoutesChanged(routes: List<Route>)
}
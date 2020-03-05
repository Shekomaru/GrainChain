package com.grainchain.interview.main

import com.google.android.gms.location.LocationResult
import com.grainchain.interview.data.Route
import com.grainchain.interview.helpers.RoutesManager
import com.grainchain.interview.helpers.RoutesManagerListener
import java.util.Date

class MainPresenterImpl(val view: MainView) : MainPresenter, RoutesManagerListener {
    private val routesManager = RoutesManager(this)

    override fun saveRoute(
        name: String,
        points: List<LocationResult>,
        startTime: Date,
        endTime: Date
    ) {
        val route = Route(
            name,
            points.map {
                it.lastLocation.run {
                    Pair(latitude, longitude)
                }
            },
            startTime,
            endTime
        )

        routesManager.addRoute(route)

        view.showRoute(route)
    }

    override fun onRoutesChanged(routes: List<Route>) {
        //todo: notify the activity that the routes has changed, so we can show the correct list at the bottom of the screen
    }

}

interface MainPresenter {
    fun saveRoute(name: String, points: List<LocationResult>, startTime: Date, endTime: Date)
}
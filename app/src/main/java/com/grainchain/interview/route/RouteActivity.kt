package com.grainchain.interview.route

import android.R
import android.location.Location
import android.os.Bundle
import android.text.format.DateUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.grainchain.interview.R.id
import com.grainchain.interview.R.layout
import com.grainchain.interview.data.Route
import kotlinx.android.synthetic.main.activity_route.delete_button
import kotlinx.android.synthetic.main.activity_route.info_text

class RouteActivity : AppCompatActivity(), OnMapReadyCallback, RouteView {

    private lateinit var presenter: RoutePresenter

    private lateinit var mMap: GoogleMap
    private lateinit var route: Route

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = RoutePresenterImpl(this)

        setContentView(layout.activity_route)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        route = intent.extras?.getParcelable("route") ?: Route()

        // add back arrow to toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        showRouteInfo()

        delete_button.setOnClickListener { presenter.deleteRoute(route) }
    }

    private fun showRouteInfo() {
        title = route.name

        val points = route.points

        var distances = floatArrayOf(0f)
        Location.distanceBetween(
            points.first().first,
            points.first().second,
            points.last().first,
            points.last().second,
            distances
        )

//        val time = DateUtils.formatElapsedTime((route.endTime.time - route.startTime.time) / 1000)
        //todo: improve time shown
        val time2 = DateUtils.formatDateRange(
            this,
            route.startTime.time,
            route.endTime.time,
            (DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_WEEKDAY)
        )

        info_text.text = "Distance: ${distances[0] / 1000} km\n" +
            "Time ellapsed: $time2"
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        showPoints(route.points)
        showCriticalMarkers(route.points.first(), route.points.last())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRouteDeleted() {
        finish()
    }

    private fun showPoints(points: List<Pair<Double, Double>>) {
        val polylineOptions = PolylineOptions()

        polylineOptions.addAll(points.map { LatLng(it.first, it.second) })
            .width(25f)
        mMap.addPolyline(polylineOptions)
    }

    private fun showCriticalMarkers(start: Pair<Double, Double>, end: Pair<Double, Double>) {
        // Add a marker in the starting point
        val startingPoint = LatLng(start.first, start.second)
        mMap.addMarker(
            MarkerOptions()
                .position(startingPoint)
                .title("Start of the route")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        // Add a marker in the ending point
        val endingPoint = LatLng(end.first, end.second)
        mMap.addMarker(
            MarkerOptions()
                .position(endingPoint)
                .title("End of the route")
        )

        // Move the camera
        try {
            //If we only have one point, or if the start and end points are very close to each other, this will crash
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds(
                        startingPoint,
                        endingPoint
                    ), 10
                )
            )
        } catch (_: Exception) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(startingPoint))
        }
    }
}

interface RouteView {
    fun onRouteDeleted()
}
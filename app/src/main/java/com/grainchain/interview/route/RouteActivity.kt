package com.grainchain.interview.route

import android.R
import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_route.share_button

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
        share_button.setOnClickListener { shareRoute() }
    }

    private fun shareRoute() {
        val textToShare = "Route name: ${route.name}\n" +
            "Route origin location: https://www.google.com/maps/search/?api=1&query=${route.points.first().first},${route.points.first().second}\n" +
            "Route destination location: https://www.google.com/maps/search/?api=1&query=${route.points.last().first},${route.points.last().second}\n" +
            "Route start time: ${DateUtils.formatDateTime(
                this,
                route.startTime.time,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
            )}\n" +
            "Route end time: ${DateUtils.formatDateTime(
                this,
                route.endTime.time,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
            )}"

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textToShare)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun showRouteInfo() {
        title = route.name

        val distance = getDistanceFromPoints()

        val time2 = DateUtils.formatDateRange(
            this,
            route.startTime.time,
            route.endTime.time,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
        )

        info_text.text = "Distance: ${distance / 1000} km\n" +
            "Start and end times: $time2"
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

        drawRouteLine(route.points)
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

    private fun drawRouteLine(points: List<Pair<Double, Double>>) {
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
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.builder().apply {
                        include(LatLng(start.first, start.second))
                        include(LatLng(end.first, end.second))
                    }.build()
                    , 10
                )
            )
        } catch (_: Exception) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(startingPoint))
        }
    }

    /**
     * Gets the total distance traveled
     *
     * This distance is given in meters
     *
     * @return the total distance traveled
     */
    private fun getDistanceFromPoints(): Float {
        val points = route.points
        val distances = floatArrayOf(0f)

        val totalDistance = points.foldRightIndexed(0f, { index, pair, acc ->
            if (index == 0) {
                acc
            } else {
                Location.distanceBetween(
                    pair.first,
                    pair.second,
                    points[index - 1].first,
                    points[index - 1].second,
                    distances
                )
                distances[0]
            }
        }
        )

        return totalDistance
    }
}

interface RouteView {
    fun onRouteDeleted()
}
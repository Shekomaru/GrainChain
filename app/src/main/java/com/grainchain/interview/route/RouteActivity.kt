package com.grainchain.interview.route

import android.R
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.grainchain.interview.R.id
import com.grainchain.interview.R.layout
import com.grainchain.interview.data.Route
import java.lang.Exception


class RouteActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var route: Route

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_route)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        route = intent.extras?.getParcelable("route") ?: Route()

        title = route.name

        // add back arrow to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
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

    private fun showPoints(points: List<Pair<Double, Double>>) {
        val polylineOptions = PolylineOptions()

        polylineOptions.addAll(points.map { LatLng(it.first, it.second) })
            .width(25f)
        mMap.addPolyline(polylineOptions)
    }

    private fun showCriticalMarkers(start: Pair<Double, Double>, end: Pair<Double, Double>) {
        // Add a marker in the starting point and move the camera
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

        try {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds(startingPoint, endingPoint), 10))
        } catch (_: Exception) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(startingPoint))
        }
    }
}

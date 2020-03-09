package com.grainchain.interview.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.grainchain.interview.R
import com.grainchain.interview.R.id
import com.grainchain.interview.R.layout
import com.grainchain.interview.data.Route
import com.grainchain.interview.data.RouteWithCoords
import com.grainchain.interview.route.RouteActivity
import kotlinx.android.synthetic.main.activity_main.track_button
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity(), OnMapReadyCallback, MainView, RouteClickListener {

    private lateinit var fuseLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var mMap: GoogleMap
    private var routeLine: Polyline? = null
    private lateinit var routesList: RecyclerView
    private var isTracking = false

    private var locations: List<LocationResult> = listOf()
    private lateinit var startTime: Date
    private lateinit var endTime: Date

    private lateinit var presenter: MainPresenter

    private val FINE_LOCATION_PERMISSION_CODE = 2345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        fuseLocationClient = FusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        track_button.setOnClickListener {
            if (!isTracking) {
                startTime = Calendar.getInstance().time
                startTrackingLocation()
            } else {
                endTime = Calendar.getInstance().time
                stopTrackingLocation()
                assignNameAndSave()
            }
            isTracking = !isTracking
        }

        initRecyclerView()

        presenter = MainPresenterImpl(this, this)

        (presenter as MainPresenterImpl).routes.observe(this, Observer { routes ->
            updateRoutesList(routes)
        })
    }

/*
    override fun onResume() {
        super.onResume()
        (presenter as MainPresenterImpl).routes.observe(this, Observer { routes ->
            updateRoutesList(routes)
        })
    }
*/

    private fun initRecyclerView() {
        routesList = findViewById<RecyclerView>(R.id.routes_list).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = RoutesAdapter(listener = this@MainActivity)
        }
    }

    private fun askForLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    onLocationReceived(locationResult)
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == FINE_LOCATION_PERMISSION_CODE) {
            if (permissions.size == 1 &&
                permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        onLocationReceived(locationResult)
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startTrackingLocation() {
        locations = listOf()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationRequest = LocationRequest.create()
            locationRequest.interval = 2000
            locationRequest.fastestInterval = 1000
            locationRequest.smallestDisplacement = 0f //Todo: make this 5 before going to prod
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            fuseLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            track_button.text = "Stop tracking"
        } else {
            Snackbar.make(
                findViewById(R.id.layout),
                "We don't have the permissions to do that",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopTrackingLocation() {
        fuseLocationClient.removeLocationUpdates(locationCallback)
        routeLine?.remove()

        track_button.text = "Start Tracking"
    }

    private fun assignNameAndSave() {
        if (locations.size < 2) {
            Snackbar.make(
                findViewById(id.layout),
                "We need at least 2 points in order to save the route",
                Snackbar.LENGTH_SHORT
            ).show()

            return
        }
        val editText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Save Route")
            .setMessage("Assign a name to this route:")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                presenter.saveRoute(
                    editText.text.toString(),
                    locations,
                    startTime,
                    endTime
                )
            }
            .setNegativeButton("Delete rute") { _, _ ->
                //todo: just close, and bye bye
            }
            .show()
    }

    fun onLocationReceived(result: LocationResult?) {
        result?.let {
            locations = locations + it
            Snackbar.make(
                findViewById(id.layout),
//                "${it.lastLocation.latitude} ${it.lastLocation.longitude}",
                "We have ${result.locations.size} locations",
                Snackbar.LENGTH_SHORT
            ).show()

            if (locations.size == 1) {
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            result.lastLocation.latitude,
                            result.lastLocation.longitude
                        ),
                        15f
                    )
                )
            } else {
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            result.lastLocation.latitude,
                            result.lastLocation.longitude
                        )
                    )
                )

            }
        }

        drawRouteLine()
    }

    private fun drawRouteLine() {
        val polylineOptions = PolylineOptions()

        polylineOptions.addAll(locations.map {
                LatLng(
                    it.lastLocation.latitude,
                    it.lastLocation.longitude
                )
            })
            .width(25f)
        if (routeLine?.isVisible == true) {
            routeLine?.remove()
        }
        routeLine = mMap.addPolyline(polylineOptions)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        askForLocationPermissions()
    }

    override fun showRoute(route: Route) {
        startActivityForResult(
            Intent(
                this,
                RouteActivity::class.java
            ).apply {
                val extras = Bundle()
                extras.putParcelable(
                    "route",
                    /*(presenter as MainPresenterImpl).getCompleteRoute(route.id)*/
                    RouteWithCoords(
                        route,
                        (presenter as MainPresenterImpl).routesRepository.getCoordsOfRoute(route.id)
                    )
                )
                putExtras(extras)
            },
            2
        )
    }

    override fun updateRoutesList(routes: List<Route>) {
        (routesList.adapter as RoutesAdapter).updateRoutes(routes)
    }

    override fun onRouteClicked(route: Route) {
        showRoute(route)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (resultCode == 2345) {
                val routeId: Long = data?.getLongExtra("routeId", 0) ?: 0
                (presenter as MainPresenterImpl).routesRepository.deleteRouteById(routeId)
            }
        }
    }

    override fun onDestroy() {
        stopTrackingLocation()
        super.onDestroy()
    }
}

interface MainView {
    fun showRoute(route: Route)
    fun updateRoutesList(routes: List<Route>)
}
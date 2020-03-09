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
import androidx.lifecycle.ViewModelProvider
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

class MainActivity : AppCompatActivity(), OnMapReadyCallback, RouteClickListener {

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

    private lateinit var routesViewModel: RoutesViewModel

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


        routesViewModel = ViewModelProvider(this).get(RoutesViewModel::class.java)

        routesViewModel.routes.observe(this, Observer { routes ->
            updateRoutesList(routes)
        })
    }

    /**
     * Function to set a layoutManager and an adapter to the recyclerView that will show the routes
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

    /**
     * Function that has to be called to start tracking the location
     */
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
            locationRequest.smallestDisplacement = 5f
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

    /**
     * Function that has to be called to stop tracking the location
     */
    private fun stopTrackingLocation() {
        fuseLocationClient.removeLocationUpdates(locationCallback)
        routeLine?.remove()

        track_button.text = "Start Tracking"
    }

    /**
     * Function that asks the user for a name of this new route
     *
     * This new route is saved on the database
     *
     * If no route available, we only show a Snackbar
     */
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
                routesViewModel.saveRoute(
                    editText.text.toString(),
                    locations,
                    startTime,
                    endTime
                )
            }
            .setNegativeButton("Delete rute") { _, _ ->
                // Just close, and bye bye
            }
            .show()
    }

    /**
     * Function that handles when a new location is received
     *
     * We update the list, and move the camera to the new point
     */
    fun onLocationReceived(result: LocationResult?) {
        result?.let {
            locations = locations + it

            /*Snackbar.make(
                findViewById(id.layout),
                "We have ${result.locations.size} locations",
                Snackbar.LENGTH_SHORT
            ).show()*/

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

    /**
     * Function that draws an updated line once the points are updated
     *
     * This usually happens when a new point is registered on the app
     */
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        askForLocationPermissions()
    }

    /**
     * Function that opens a second activity showing the route
     */
    private fun showRoute(route: Route) {
        startActivityForResult(
            Intent(
                this,
                RouteActivity::class.java
            ).apply {
                val extras = Bundle()
                extras.putParcelable(
                    "route",
                    RouteWithCoords(
                        route,
                        routesViewModel.getCoordsOfRoute(route.id)
                    )
                )
                putExtras(extras)
            },
            2
        )
    }

    /**
     * Function to handle any modifications happening on the list
     */
    private fun updateRoutesList(routes: List<Route>) {
        (routesList.adapter as RoutesAdapter).updateRoutes(routes)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (resultCode == 2345) {
                val routeId: Long = data?.getLongExtra("routeId", 0) ?: 0
                routesViewModel.deleteRouteById(routeId)
            }
        }
    }

    override fun onDestroy() {
        // In case of Activity destroyed: Stop tracking to avoid memory leaks
        stopTrackingLocation()
        super.onDestroy()
    }

    /**
     * Function to do an action based on the element of the list the user just clicked
     */
    override fun onRouteClicked(route: Route) {
        showRoute(route)
    }

}
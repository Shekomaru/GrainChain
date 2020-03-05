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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.grainchain.interview.R
import com.grainchain.interview.R.id
import com.grainchain.interview.R.layout
import com.grainchain.interview.data.Route
import com.grainchain.interview.route.RouteActivity
import kotlinx.android.synthetic.main.activity_main.main_button
import kotlinx.android.synthetic.main.activity_main.track_button
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity(), OnMapReadyCallback, MainView, RouteClickListener {

    private lateinit var fuseLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var mMap: GoogleMap
    private lateinit var routesList: RecyclerView
    private var isTracking = false

    private var locations: List<LocationResult> = listOf()
    private lateinit var startTime: Date
    private lateinit var endTime: Date

    private lateinit var presenter: MainPresenter

    private val FINE_LOCATION_PERMISSION_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        fuseLocationClient = FusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        main_button.setOnClickListener {
            this.startActivity(
                Intent(
                    this,
                    RouteActivity::class.java
                )
            )
        }

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

        presenter = MainPresenterImpl(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.onOnResume()
    }

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

//            startTrackingLocation()
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
//                startTrackingLocation()
            } else {
                //Location permission was denied
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
            locationRequest.interval = 3000
            locationRequest.fastestInterval = 1000
            locationRequest.smallestDisplacement = 0f
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    onLocationReceived(locationResult)
                }
            }

            fuseLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            track_button.text = "Stop tracking"
        }
    }

    private fun stopTrackingLocation() {
        fuseLocationClient.removeLocationUpdates(locationCallback)

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
            .setPositiveButton("Save") { dialog, id ->
                presenter.saveRoute(
                    editText.text.toString(),
                    locations,
                    startTime,
                    endTime
                )
            }
            .setNegativeButton("Delete rute") { dialog, id ->
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

        askForLocationPermissions()

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun showRoute(route: Route) {
        startActivity(
            Intent(
                this,
                RouteActivity::class.java
            ).apply {
                val extras = Bundle()
                extras.putParcelable("route", route)
                putExtras(extras)
            }
        )
    }

    override fun updateRoutesList(routes: List<Route>) {
        (routesList.adapter as RoutesAdapter).updateRoutes(routes)
    }

    override fun onRouteClicked(route: Route) {
        showRoute(route)
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
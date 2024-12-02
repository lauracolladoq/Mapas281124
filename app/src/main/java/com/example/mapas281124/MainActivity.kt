package com.example.mapas281124

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

    private val LOCATION_CODE = 1000
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
            if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
                ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                gestionarLocalizacion()
            } else {
                Toast.makeText(this, "El usuario denegó los permisos.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        iniciarFragment()
    }

    private fun iniciarFragment() {
        val fragment = SupportMapFragment()
        fragment.getMapAsync(this)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.fm_maps, fragment)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map.uiSettings.isZoomControlsEnabled = true
        //map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        ponerMarcador(LatLng(36.8532683, -2.4674304))
        gestionarLocalizacion()
        // -----------------
        ponerRuta()
    }

    private fun ponerRuta() {
        val c1 = LatLng(36.850416, -2.464815)
        val c2 = LatLng(36.852408, -2.468324)
        val c3 = LatLng(36.848690, -2.461865)
        val c4 = LatLng (36.849283, -2.462873)
        val c5 = LatLng(36.850579, -2.464579)
        // val c6 = c1
        val polylineOptions = PolylineOptions()
            // En vez de c6, c1 de nuevo
            .add(c1, c2, c3, c4, c5, c1)
            .width(15f)

        val polyline = map.addPolyline(polylineOptions)
    }

    private fun gestionarLocalizacion() {
        // :: al ser lateinit para saber si está inicializada y evitar un NullPointer si no lo está
        if (!::map.isInitialized) return
        // Comprobar permisos y si no están concedidos preguntar para añadirlos
        if (
        // Importar Manifest de Android
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        } else {
            pedirPermisos()
        }
    }

    private fun pedirPermisos() {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            mostrarExplicacion()
        } else {
            escogerPermisos()
        }
    }

    private fun escogerPermisos() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun mostrarExplicacion() {
        AlertDialog.Builder(this).setTitle("Permisos de ubicación")
            .setMessage("Para el uso adecuado de esta incredible aplicación necesitamos los permisos de ubicación")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
            }
            .create()
            .dismiss()
    }

    private fun ponerMarcador(coordenadas: LatLng) {
        val marker = MarkerOptions().position(coordenadas).title("IES Al-Ándalus")
        map.addMarker(marker)
        mostrarAnimación(coordenadas, 12f)
    }

    // Añadir animación para mostrar la coordenada
    private fun mostrarAnimación(coordenadas: LatLng, zoom: Float) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordenadas, zoom),
            4500,
            null
        )
    }

    // ---------------------------------------------------------------------------------------------
    // Para que indique que ya están concedidos los permisos
    override fun onRestart() {
        (super.onRestart())
        gestionarLocalizacion()
    }
}
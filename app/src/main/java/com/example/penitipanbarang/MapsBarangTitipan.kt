package com.example.penitipanbarang

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapsBarangTitipan : Fragment() {
    private lateinit var mMap: GoogleMap
    private var long:Double?=null
    private var lat:Double?=null

    private val callback = OnMapReadyCallback { googleMap ->
        mMap=googleMap
        if(long!=null){
            val latLng = lat?.let { LatLng(it, long!!) }
            val markerOptions = latLng?.let { MarkerOptions().position(it) }
            if (markerOptions != null) {
                mMap.addMarker(markerOptions)
            }
            latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 10f) }
                ?.let { mMap.animateCamera(it) }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            long = it.getDouble("long")
            lat = it.getDouble("lat")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.GONE
        return inflater.inflate(R.layout.fragment_maps_barang_titipan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }



}
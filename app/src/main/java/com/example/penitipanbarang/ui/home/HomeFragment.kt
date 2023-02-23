package com.example.penitipanbarang.ui.home

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.penitipanbarang.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var db : FirebaseFirestore
    private lateinit var imageBitmap: Bitmap
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataPhoto:Intent
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var longitude = 0.0
    private  var latitude = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        db = FirebaseFirestore.getInstance()
        binding.ivQrCodeData.visibility= View.GONE
        if(this.context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(),Array(5){
                Manifest.permission.CAMERA
            },100)
        }
        binding.btnTakePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent,100)
        }
        binding.btnSave.setOnClickListener {
            fetchLocation()
        }


        return root
    }
    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode !== Activity.RESULT_CANCELED) {
            if(requestCode==100&& data!==null){
                val extras = data?.getExtras()
                imageBitmap = extras?.get("data") as Bitmap
                binding.iVData.setImageBitmap(imageBitmap)
                dataPhoto=data
                Log.d("data", extras.toString())
            }
        }

    }

    private fun storeData() {
        val simpleDate = SimpleDateFormat("dd/M/yyyy HH:mm:ss")
        val simpleDateTanggal = SimpleDateFormat("dd/M/yyyy")
        val timestamp = simpleDate.format(Date())
        val tanggal=simpleDateTanggal.format(Date())
        if(binding.iVData.drawable!=null){
            if(binding.etNamaData.text.toString()!=""){
                val items = HashMap<String,Any>()
                items.put("namapenitip", binding.etNamaData.text.toString())
                items.put("timestamp",timestamp)
                items.put("tanggal",tanggal)
                items.put("long",longitude)
                items.put("lat",latitude)
                items.put("diambil",false)
                val collection = db.collection("titipBarang").document()
                items.put("uniqueid",collection.id)
                collection.set(items).addOnSuccessListener {
                    storePhoto(imageBitmap,collection.id)
                    generateQRforData(collection.id)
                    binding.iVData.setImageResource(0);
                    binding.etNamaData.text.clear()
                    Toast.makeText(this.context,"Sukses", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this.context,"it", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this.context,"Tolong buat Nama untuk data", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this.context,"Tolong ambil Foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateQRforData(uniqueid: String) {
        if (uniqueid.isEmpty()){
            Toast.makeText(this.context,"enter some data",Toast.LENGTH_SHORT).show()
        }else {
            val writer = QRCodeWriter()
            try {
                val bitMatrix = writer.encode(uniqueid, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (i in 0 until width) {
                    for (j in 0 until height) {
                        bmp.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                    }
                }
                binding.ivQrCodeData.setImageBitmap(bmp)
                storeQR(bmp,uniqueid)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }
    }

    private fun storeQR(bitmap: Bitmap, uniqueid: String) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val storage = FirebaseStorage.getInstance()
        val data = baos.toByteArray()
        val storageRef = storage.reference
        val uploadTask = storageRef.child("QRCODE/"+ binding.etNamaData.text.toString()+"/"+uniqueid).putBytes(data)

        uploadTask.addOnSuccessListener {
            Log.d("berhasil upload data","haha")
        }.addOnFailureListener{
            Log.d("gagal upload","hehe")
        }

    }

    private fun storePhoto(bitmap: Bitmap, uniqueid: String) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val storage = FirebaseStorage.getInstance()
        val data = baos.toByteArray()
        val storageRef = storage.reference
        val uploadTask = storageRef.child("Foto/"+ binding.etNamaData.text.toString()+"/"+uniqueid).putBytes(data)

        uploadTask.addOnSuccessListener {

            Log.d("berhasil upload data","haha")
        }.addOnFailureListener{
            Log.d("gagal upload","hehe")
        }
    }

    private var locationCallback2: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            db = FirebaseFirestore.getInstance()
            super.onLocationResult(p0)
            if(p0.equals(null)!=true){
                longitude = p0.lastLocation.longitude
                latitude = p0.lastLocation.latitude
                storeData()
            }
        }
    }
    private fun fetchLocation() {
        val task = fusedLocationClient.lastLocation
        checkLocPermission()
        val locationRequest = LocationRequest.create()
        locationRequest.setSmallestDisplacement(20f)
        locationRequest.setInterval(20000)
        locationRequest.setFastestInterval(20000)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (isLocationEnabled()) {
            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback2,
                Looper.getMainLooper())
            Handler().postDelayed({
                fusedLocationClient.removeLocationUpdates(locationCallback2)
            }, 1000)

        } else {
            Toast.makeText(this.context, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }


    }
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun checkLocPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(this.requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this.requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
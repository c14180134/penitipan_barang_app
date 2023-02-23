package com.example.penitipanbarang

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.penitipanbarang.databinding.FragmentDetailTitipanBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.HashMap


class DetailTitipan : Fragment() {
    private var _binding: FragmentDetailTitipanBinding? = null
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
    private var uniqueid: String? = null
    private var namaData: String? = null
    private var timestamp: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uniqueid = it.getString("uniqueid")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        _binding = FragmentDetailTitipanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.GONE

        fetchDetailData()

        binding.btnClaim.setOnClickListener {
            changeStatus()
            it.findNavController().navigate(R.id.navigation_dashboard)
        }



        return root
    }

    private fun changeStatus(){
        var collection= db.collection("titipBarang")
        var query= collection.whereEqualTo("uniqueid",uniqueid).get()
        query.addOnSuccessListener {
            val items =HashMap<String,Any>()
            items.put("diambil",true)
            for(document in it){
                db.collection("titipBarang").document(document.id).set(items, SetOptions.merge())
            }

        }
    }

    private fun fetchDetailData(){
        var collection= db.collection("titipBarang")
        var query= collection.whereEqualTo("uniqueid",uniqueid).get()
        query.addOnSuccessListener {
            namaData = it.documents[0].get("namapenitip").toString()
            timestamp = it.documents[0].get("timestamp").toString()
            fetchPhoto()
            binding.tvNamaPenitip.text = namaData
            binding.tvTanggalDetail.text = timestamp
        }
    }

    private fun fetchPhoto(){
        val storageRef =  FirebaseStorage.getInstance().reference.child("Foto/"+namaData+"/"+uniqueid)
        val localfile = File.createTempFile("tempImage","jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.ivDetailData.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Toast.makeText(this.context,it.toString()   , Toast.LENGTH_SHORT).show()
        }

    }
}
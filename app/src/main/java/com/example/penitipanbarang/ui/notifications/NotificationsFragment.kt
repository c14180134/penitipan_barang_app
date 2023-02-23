package com.example.penitipanbarang.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penitipanbarang.R
import com.example.penitipanbarang.adapter.RecAdapterListHistoryBarang
import com.example.penitipanbarang.databinding.FragmentNotificationsBinding
import com.example.penitipanbarang.dataclass.BarangTitipan
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var recAdapterListdataQR: RecAdapterListHistoryBarang
    private lateinit var db : FirebaseFirestore
    var listData : ArrayList<BarangTitipan> = ArrayList()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fetchDataQR()
        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.VISIBLE
        val simpleDateTanggal = SimpleDateFormat("dd/M/yyyy")
        val tanggal=simpleDateTanggal.format(Date())
        binding.tvToday.text= tanggal
        initRecyclerDataQR(root)


        return root
    }

    private fun initRecyclerDataQR(view: View){
        val recylcerView = view.findViewById<RecyclerView>(R.id.rvData)
        recylcerView.layoutManager= LinearLayoutManager(activity)
        recAdapterListdataQR = RecAdapterListHistoryBarang(listData)
        recylcerView.adapter=recAdapterListdataQR

    }

    fun fetchDataQR(){
        listData.clear()
        db = FirebaseFirestore.getInstance()
        val simpleDateTanggal = SimpleDateFormat("dd/M/yyyy")
        val tanggal=simpleDateTanggal.format(Date())
        db.collection("titipBarang").whereEqualTo("tanggal",tanggal).whereEqualTo("diambil",false).
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if(error !=null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }
                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){

                        listData.add(dc.document.toObject(BarangTitipan::class.java))
                    }
                }
                Log.d("hako",listData.toString())
                recAdapterListdataQR.notifyDataSetChanged()
            }
        })
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
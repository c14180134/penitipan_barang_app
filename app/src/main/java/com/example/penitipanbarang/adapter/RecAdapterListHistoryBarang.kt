package com.example.penitipanbarang.adapter

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.penitipanbarang.R
import com.example.penitipanbarang.dataclass.BarangTitipan
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class RecAdapterListHistoryBarang(private val listData:ArrayList<BarangTitipan>):RecyclerView.Adapter<RecAdapterListHistoryBarang.RecAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecAdapterViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_data,parent,false)
        return RecAdapterViewHolder(itemView)
    }
    class RecAdapterViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val imageData : ImageView = itemView.findViewById(R.id.ivSmallData)
        val imageQR: ImageView = itemView.findViewById(R.id.ivQrCodeData)
        val namaData : TextView = itemView.findViewById(R.id.tvNamaData)
        val btnDetailData : Button = itemView.findViewById(R.id.btnDetail)
        val btnShowQR:Button = itemView.findViewById(R.id.btnShowQR)
    }
    override fun onBindViewHolder(holder: RecAdapterViewHolder, position: Int) {
        val barang: BarangTitipan = listData[position]
        holder.imageQR.visibility=View.GONE
        val storageRef =  FirebaseStorage.getInstance().reference.child("Foto/"+barang.namapenitip+"/"+barang.uniqueid)
        val localfile = File.createTempFile("tempImage","jpg")
        Log.d("hal",storageRef.toString())
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            holder.imageData.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Log.d("gk bisa",it.toString())
        }
        //QRGET
        val storageRefQR =  FirebaseStorage.getInstance().reference.child("QRCODE/"+barang.namapenitip+"/"+barang.uniqueid)
        val localfileQR = File.createTempFile("tempImage","jpg")
        storageRefQR.getFile(localfileQR).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfileQR.absolutePath)
            holder.imageQR.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Log.d("gk bisa QR",it.toString())
        }

        holder.namaData.text = barang.namapenitip
        holder.btnDetailData.setOnClickListener {
            val bundle = Bundle()
            barang.long?.let { it1 -> bundle.putDouble("long", it1) }
            barang.lat?.let { it1 -> bundle.putDouble("lat  ", it1) }
            it.findNavController().navigate(R.id.mapsBarangTitipan,bundle)
        }
        holder.btnShowQR.setOnClickListener {
            if (holder.imageQR.visibility == View.VISIBLE){

                holder.imageQR.visibility = View.GONE
                holder.imageData.visibility=View.VISIBLE
            }else{
                holder.imageQR.visibility = View.VISIBLE
                holder.imageData.visibility=View.GONE
            }
        }

    }




    override fun getItemCount(): Int {
        return listData.size
    }
}
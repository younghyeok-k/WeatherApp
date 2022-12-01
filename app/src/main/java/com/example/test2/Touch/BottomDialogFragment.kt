package com.example.test2.Touch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test2.Adapter.WeatherAdapter
import com.example.test2.Adapter.wad
import com.example.test2.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomDialogFragment(var adapter: wad,var add: String) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottomsheet,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
        view.findViewById<TextView>(R.id.daddress).text= add
    }
}
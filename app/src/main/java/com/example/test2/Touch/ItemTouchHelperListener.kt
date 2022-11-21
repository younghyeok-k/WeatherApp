package com.example.test2.Touch

import androidx.recyclerview.widget.RecyclerView

interface ItemTouchHelperListener {
    fun onItemMove(from_position: Int, to_position: Int): Boolean
    fun onItemSwipe(position: Int)

    fun onRightClick(position: Int, viewHolder: RecyclerView.ViewHolder?)
}
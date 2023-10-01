package com.ligo.feature.home

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.IdRes
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView

internal object BottomMenuHelper {

    fun showBadge(
        context: Context,
        bottomNavigationView: BottomNavigationView,
        @IdRes itemId: Int,
        value: String
    ) {
        val itemView = bottomNavigationView.findViewById<BottomNavigationItemView>(itemId)
        val badge = LayoutInflater.from(context)
            .inflate(R.layout.layout_unread_chat_badge, bottomNavigationView, false)
        val text = badge.findViewById<TextView>(R.id.badgeTextView)
        text.text = value
        itemView.addView(badge)
    }

    fun removeBadge(bottomNavigationView: BottomNavigationView, @IdRes itemId: Int) {
        val itemView = bottomNavigationView.findViewById<BottomNavigationItemView>(itemId)
        if (itemView.childCount == 3) {
            itemView.removeViewAt(2)
        }
    }
}
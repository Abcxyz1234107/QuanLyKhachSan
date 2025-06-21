package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.fragment.app.FragmentManager
import com.example.quanlykhachsan.view.book.BookFragment
import com.example.quanlykhachsan.view.chart.ChartFragment
import com.example.quanlykhachsan.view.dashboard.DashboardFragment
import com.example.quanlykhachsan.view.room.RoomFragment
import com.example.quanlykhachsan.view.roomtype.RoomTypeFragment
import com.example.quanlykhachsan.view.staff.StaffFragment

class MainViewModel : ViewModel() {
    fun openDashboard(fm: FragmentManager, containerId: Int) {
        val tag = "dashboard"
        if (fm.findFragmentByTag(tag) == null) {
            fm.beginTransaction()
                .replace(containerId, DashboardFragment(), tag)
                .commit()
        }
    }

    fun openRoomTypeManager(fm: FragmentManager, containerId: Int) {
        val tag = "room type"
        if (fm.findFragmentByTag(tag) == null) {
            fm.beginTransaction()
                .replace(containerId, RoomTypeFragment(), tag)
                .commit()
        }
    }

    fun openStaffManager(fm: FragmentManager, containerId: Int) {
        val tag = "staff"
        if (fm.findFragmentByTag(tag) == null) {
            fm.beginTransaction()
                .replace(containerId, StaffFragment(), tag)
                .commit()
        }
    }

    fun openChart(fm: FragmentManager, containerId: Int) {
        val tag = "chart"
        if (fm.findFragmentByTag(tag) == null) {
            fm.beginTransaction()
                .replace(containerId, ChartFragment(), tag)
                .commit()
        }
    }

    fun openRoomManager(fm: FragmentManager, containerId: Int) {
        val tag = "room"
        if (fm.findFragmentByTag(tag) == null) {
            fm.beginTransaction()
                .replace(containerId, RoomFragment(), tag)
                .commit()
        }
    }

    fun openBookRoom(fm: FragmentManager, containerId: Int) {
        val tag = "book"
        if (fm.findFragmentByTag(tag) == null) {
            fm.beginTransaction()
                .replace(containerId, BookFragment(), tag)
                .commit()
        }
    }
}
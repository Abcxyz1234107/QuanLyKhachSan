package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.fragment.app.FragmentManager
import com.example.quanlykhachsan.view.dashboard.DashboardActivity

class MainViewModel : ViewModel() {
    fun openDashboard(fm: FragmentManager, containerId: Int) {
        val tag = "dashboard"
        if (fm.findFragmentByTag(tag) == null) {
            fm.beginTransaction()
                .replace(containerId, DashboardActivity(), tag)
                .commit()
        }
    }
}
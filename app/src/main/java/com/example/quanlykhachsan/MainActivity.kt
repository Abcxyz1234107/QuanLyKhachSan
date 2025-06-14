package com.example.quanlykhachsan

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.quanlykhachsan.viewmodel.MainViewModel
import android.view.Menu
import android.view.MenuInflater

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.topBar))
        supportActionBar?.setDisplayShowTitleEnabled(false) // Ẩn tiêu đề “QuanLyKhachSan”

        findViewById<LinearLayout>(R.id.sideNav).children // Gắn sự kiện cho từng icon
            .filterIsInstance<ImageButton>()
            .forEach { btn ->
                btn.setOnClickListener { onNavItemSelected(btn.id) }
            }

        if (savedInstanceState == null) {
            onNavItemSelected(R.id.itemDashboard)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.top_bar_menu, menu)
        return true
    }

    private fun onNavItemSelected(viewId: Int) { // Điều hướng theo id nút
        when (viewId) {
            /*R.id.itemDashboard -> viewModel.openDashboard(supportFragmentManager, R.id.container)
            R.id.itemStatistic -> viewModel.openStatistic(supportFragmentManager, R.id.container)
            R.id.itemBook      -> viewModel.openBookRoom(supportFragmentManager, R.id.container)
            R.id.itemCheckout  -> viewModel.openCheckOut(supportFragmentManager, R.id.container)
            R.id.itemRoom      -> viewModel.openRoomManager(supportFragmentManager, R.id.container)
            R.id.itemStaff     -> viewModel.openStaffManager(supportFragmentManager, R.id.container)*/
        }
    }
}

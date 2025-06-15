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
import android.view.View
import androidx.core.view.updateLayoutParams

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isSideNavExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.topBar))
        supportActionBar?.setDisplayShowTitleEnabled(false) // Ẩn tiêu đề “QuanLyKhachSan”

        val sideNav   = findViewById<LinearLayout>(R.id.sideNav)
        val btnMenu = findViewById<ImageButton>(R.id.itemMenu)

        /* Gán click riêng cho nút menu manage */
        btnMenu.setOnClickListener { toggleSideNav(sideNav, btnMenu) }

        /* Gắn click cho các icon điều hướng còn lại */
        listOf(
            R.id.itemDashboard,
            R.id.itemStatistic,
            R.id.itemBook,
            R.id.itemCheckout,
            R.id.itemRoom,
            R.id.itemRoomType,
            R.id.itemStaff
        ).forEach { id ->
            findViewById<ImageButton>(id).setOnClickListener { onNavItemSelected(id) }
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
    private fun toggleSideNav(side: LinearLayout, menuBtn: ImageButton) {
        if (isSideNavExpanded) {
            side.updateLayoutParams {
                width = resources.getDimensionPixelSize(R.dimen.side_nav_collapsed)
            }
            menuBtn.setImageResource(R.drawable.ic_dashboard_manage)
        } else {
            side.updateLayoutParams {
                width = resources.getDimensionPixelSize(R.dimen.side_nav_expanded)
            }
            menuBtn.setImageResource(R.drawable.ic_arrow_back)
        }
        isSideNavExpanded = !isSideNavExpanded
    }
}

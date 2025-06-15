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
import com.google.android.material.button.MaterialButton

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
        val navBtnIds = listOf(
            R.id.navDashboard,  R.id.navStatistic,
            R.id.navBook,       R.id.navCheckout,
            R.id.navRoom,       R.id.navRoomType,
            R.id.navStaff
        )

        var currentSelected: View? = null
        navBtnIds.forEach { id ->
            findViewById<MaterialButton>(id)?.apply {
                isCheckable = true
                setOnClickListener {
                    if (currentSelected == this) {
                        isSelected = false
                        isChecked = false
                        currentSelected = null
                    } else {
                        currentSelected?.apply {
                            isSelected = false
                            isChecked = false
                        }
                        isSelected = true
                        isChecked = true
                        currentSelected = this
                        onNavItemSelected(id)
                    }
                }
            }
        }


        if (savedInstanceState == null) {
            onNavItemSelected(R.id.itemMenu)
        }
        findViewById<MaterialButton>(R.id.navDashboard)?.performClick()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.top_bar_menu, menu)
        return true
    }

    private fun onNavItemSelected(viewId: Int) {
        /*when (viewId) {
            R.id.navDashboard        -> viewModel.openDashboard(supportFragmentManager, R.id.container)
            R.id.navStatistic        -> viewModel.openStatistic(supportFragmentManager, R.id.container)
            R.id.navBook             -> viewModel.openBookRoom(supportFragmentManager, R.id.container)
            R.id.navCheckout         -> viewModel.openCheckOut(supportFragmentManager, R.id.container)
            R.id.navRoom             -> viewModel.openRoomManager(supportFragmentManager, R.id.container)
            R.id.navRoomType         -> viewModel.openRoomTypeManager(supportFragmentManager, R.id.container)
            R.id.navStaff            -> viewModel.openStaffManager(supportFragmentManager, R.id.container)
        }*/
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

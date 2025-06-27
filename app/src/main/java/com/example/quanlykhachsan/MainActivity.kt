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
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isDropDownVisible = false
    private lateinit var toolbar: MaterialToolbar
    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ── 1. Ẩn thanh trạng thái (toàn màn hình) ──────────────────────────────
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setSupportActionBar(findViewById(R.id.topBar))

        toolbar = findViewById<MaterialToolbar>(R.id.topBar)
        val dropDownNav = findViewById<LinearLayout>(R.id.dropDownNav)

        setSupportActionBar(toolbar)
        isDropDownVisible = true
        dropDownNav.visibility = View.VISIBLE
        toolbar.navigationIcon =
            ContextCompat.getDrawable(this, R.drawable.ic_arrow_drop_up)

        toolbar.setNavigationOnClickListener {
            if (isDropDownVisible) {
                dropDownNav.visibility = View.GONE
                toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_drop_down)
            } else {
                dropDownNav.visibility = View.VISIBLE
                toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_drop_up)
            }
            isDropDownVisible = !isDropDownVisible
        }

        // 2) Gắn listener cho các button trong dropDownNav
        listOf(
            R.id.navDashboard, R.id.navChart,
            R.id.navBook, R.id.navCheckout,
            R.id.navRoom, R.id.navRoomType,
            R.id.navStaff
        ).forEach { id ->
            findViewById<MaterialButton>(id)?.setOnClickListener {
                onNavItemSelected(id)
                // ẩn drop-down sau khi chọn
                dropDownNav.visibility = View.GONE
                toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_drop_down)
                isDropDownVisible = false
            }
        }


        /* Gắn click cho các icon điều hướng còn lại */
        val navBtnIds = listOf(
            R.id.navDashboard,  R.id.navChart,
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
                        viewModel.openDashboard(supportFragmentManager, R.id.container)
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
            onNavItemSelected(R.id.navDashboard)
        }
        findViewById<MaterialButton>(R.id.navDashboard)?.performClick()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.top_bar_menu, menu)
        return true
    }

    private fun onNavItemSelected(viewId: Int) {
        when (viewId) {
            R.id.navDashboard -> {
                toolbar.title = getString(R.string.nav_home)         // tiêu đề
                viewModel.openDashboard(supportFragmentManager, R.id.container)
            }
            R.id.navChart -> {
                toolbar.title = getString(R.string.nav_stat)
                viewModel.openChart(supportFragmentManager, R.id.container)
            }
            R.id.navCheckout -> {
                toolbar.title = getString(R.string.nav_checkout)
                viewModel.openPayment(supportFragmentManager, R.id.container)
            }
            R.id.navBook -> {
                toolbar.title = getString(R.string.nav_book)
                viewModel.openBookRoom(supportFragmentManager, R.id.container)
            }
            R.id.navRoom -> {
                toolbar.title = getString(R.string.nav_room)
                viewModel.openRoomManager(supportFragmentManager, R.id.container)
            }
            R.id.navRoomType -> {
                toolbar.title = getString(R.string.nav_room_type)
                viewModel.openRoomTypeManager(supportFragmentManager, R.id.container)
            }
            R.id.navStaff -> {
                toolbar.title = getString(R.string.nav_staff)
                viewModel.openStaffManager(supportFragmentManager, R.id.container)
            }
        }
    }
}

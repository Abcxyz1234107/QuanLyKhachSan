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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.Intent
import android.provider.DocumentsContract
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.quanlykhachsan.util.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isDropDownVisible = false
    private lateinit var toolbar: MaterialToolbar
    private lateinit var drawer: DrawerLayout

    private var pendingWorkbook: XSSFWorkbook? = null

    private val createFile = registerForActivityResult(
        ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        pendingWorkbook?.let { wb ->
            // Chạy trên IO để tránh block UI
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use {
                        ExportUtils.writeWorkbook(wb, it)
                    }
                    // thông báo thành công
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Xuất Excel thành công", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Lỗi ghi Excel", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity,
                            "Lỗi khi ghi file: ${e.message}",
                            Toast.LENGTH_LONG).show()
                    }
                } finally {
                    pendingWorkbook = null
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu); return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_export) {
            showExportDialog(); return true
        }
        return super.onOptionsItemSelected(item)
    }


    /*──────── hộp thoại chọn bảng ────────*/
    private fun showExportDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_export, null)

        val cbDatPhong  = view.findViewById<CheckBox>(R.id.cbDatPhong)
        val cbTraPhong  = view.findViewById<CheckBox>(R.id.cbTraPhong)
        val cbPhong     = view.findViewById<CheckBox>(R.id.cbPhong)
        val cbLoaiPhong = view.findViewById<CheckBox>(R.id.cbLoaiPhong)
        val cbNhanVien  = view.findViewById<CheckBox>(R.id.cbNhanVien)

        val dlg = MaterialAlertDialogBuilder(this)
            .setTitle("Chọn bảng cần xuất")
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dlg.dismiss() }

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {

            val tables = mutableListOf<String>()
            if (cbDatPhong.isChecked)  tables += "DatPhong"
            if (cbTraPhong.isChecked)  tables += "TraPhong"
            if (cbPhong.isChecked)     tables += "Phong"
            if (cbLoaiPhong.isChecked) tables += "LoaiPhong"
            if (cbNhanVien.isChecked)  tables += "NhanVien"

            if (tables.isNotEmpty()) {
                lifecycleScope.launch {
                    /* 1. Tạo workbook */
                    pendingWorkbook = viewModel.buildWorkbookSuspend(tables)

                    /* 2. Gọi SAF => khi user chọn xong callback ghi file */
                    createFile.launch("QuanLyKhachSan_${System.currentTimeMillis()}.xlsx")
                }
            }
            dlg.dismiss()
        }
        dlg.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ẩn thanh điều hướng
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller?.isAppearanceLightNavigationBars = true // (tùy chọn: dùng theme sáng)
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.navigationBars())

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

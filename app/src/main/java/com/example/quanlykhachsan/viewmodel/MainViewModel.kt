package com.example.quanlykhachsan.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import com.example.quanlykhachsan.data.repository.export.QlksRepository
import com.example.quanlykhachsan.util.ExportUtils
import com.example.quanlykhachsan.util.appendDataSheet
import com.example.quanlykhachsan.view.book.BookFragment
import com.example.quanlykhachsan.view.chart.ChartFragment
import com.example.quanlykhachsan.view.dashboard.DashboardFragment
import com.example.quanlykhachsan.view.payment.PaymentFragment
import com.example.quanlykhachsan.view.room.RoomFragment
import com.example.quanlykhachsan.view.roomtype.RoomTypeFragment
import com.example.quanlykhachsan.view.staff.StaffFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import kotlin.jvm.javaClass


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: QlksRepository
) : ViewModel() {
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

    fun openPayment(fm: FragmentManager, containerId: Int) {
        val tag = "payment"
        if (fm.findFragmentByTag(tag) == null) {
            fm.beginTransaction()
                .replace(containerId, PaymentFragment(), tag)
                .commit()
        }
    }

    /*──────────── EXPORT EXCEL ────────────*/
    private lateinit var workbook: XSSFWorkbook

    suspend fun buildWorkbookSuspend(tables: List<String>): XSSFWorkbook =
        withContext(Dispatchers.IO) {

            val wb = XSSFWorkbook()

            if ("DatPhong"  in tables) wb.appendDataSheet("DatPhong",  repo.allDatPhong())
            if ("TraPhong"  in tables) wb.appendDataSheet("TraPhong",  repo.allTraPhong())
            if ("Phong"     in tables) wb.appendDataSheet("Phong",     repo.allPhong())
            if ("LoaiPhong" in tables) wb.appendDataSheet("LoaiPhong", repo.allLoaiPhong())
            if ("NhanVien"  in tables) wb.appendDataSheet("NhanVien",  repo.allNhanVien())

            wb                       // return workbook
        }

    /** Ghi workbook xuống Uri */
    fun writeWorkbook(cr: ContentResolver, uri: Uri, wb: XSSFWorkbook) {
        viewModelScope.launch(Dispatchers.IO) {
            cr.openOutputStream(uri)?.use { ExportUtils.writeWorkbook(wb, it) }
        }
    }

    fun writeExcelToUri(cr: ContentResolver, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            cr.openOutputStream(uri)?.use { ExportUtils.writeWorkbook(workbook, it) }
        }
    }
}
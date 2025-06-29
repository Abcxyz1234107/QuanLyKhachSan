package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import com.example.quanlykhachsan.data.local.database.AppDatabase
import com.example.quanlykhachsan.data.local.entity.DatPhong
import com.example.quanlykhachsan.data.local.entity.Phong
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class PhongUI(
    val tenPhong: String,
    val trangThai: String,
    val tenLoai: String?
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val db: AppDatabase
) : ViewModel() {

    private val _danhSachPhong = MutableLiveData<List<PhongUI>>()
    val danhSachPhong: LiveData<List<PhongUI>> = _danhSachPhong

    private val _soLuongPhongText = MutableLiveData<String>()
    val soLuongPhongText: LiveData<String> = _soLuongPhongText

    private val _soLuongLoaiText = MutableLiveData<String>()
    val soLuongLoaiText: LiveData<String> = _soLuongLoaiText

    private val _thongTinKS = MutableLiveData<List<String>>()
    val thongTinKS: LiveData<List<String>> = _thongTinKS
    val lines = mutableListOf<String>()

    init { loadData() }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        val phongDao = db.phongDao()
        val loaiDao  = db.loaiPhongDao()
        val datDao   = db.datPhongDao()

        val listPhong = phongDao.getAllSync()
        val listLoai  = loaiDao.getAllSync()

        val ui = listPhong.map { p ->
            val datList = datDao.getByPhongSync(p.maPhong)
            val loai    = listLoai.firstOrNull { it.maLoaiPhong == p.maLoaiPhong }
            PhongUI(
                tenPhong = p.maPhong.toString(),
                trangThai = tinhTrangPhong(datList),
                tenLoai = loai?.tenLoaiPhong
            )
        }

        withContext(Dispatchers.Main) {
            _danhSachPhong.value = ui

            val soPhongTrong = ui.count { it.trangThai == "Trống" || it.trangThai == "Đã đặt" }
            val infoPhong = "Tổng phòng: ${ui.size}   Trống: $soPhongTrong"

            val thongKeLoai = listLoai.sortedBy { it.tenLoaiPhong }.joinToString("\n") { loai ->
                val soTrong = ui.count {
                    it.tenLoai == loai.tenLoaiPhong &&
                            (it.trangThai != "Đã nhận phòng")
                }
                "- ${loai.tenLoaiPhong}: $soTrong trống"
            }
            lines += "Tổng phòng: ${ui.size}   Trống: $soPhongTrong"
            lines += "Số loại phòng: ${listLoai.size}"
            lines += thongKeLoai.split("\n")          // đã join sẵn
            _thongTinKS.value = lines
        }
    }

    /* Tính trạng thái tại thời điểm hiện tại */
    private fun tinhTrangPhong(datPhongs: List<DatPhong>): String {
        val today = LocalDate.now()
        datPhongs.forEach { d ->
            if (d.tinhTrangDatPhong == "Đã hủy") return@forEach
            val nhan = d.ngayNhanPhong.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val tra  = d.ngayTraPhong?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            val dat  = d.ngayDatPhong.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

            if (tra != null && today in nhan..tra &&
                d.tinhTrangDatPhong in listOf("Đã nhận phòng", "Đang sử dụng")
            ) return "Đang sử dụng"

            if (tra != null && today in nhan..tra && d.tinhTrangDatPhong == "Chưa nhận phòng")
                return "Đã đặt"

            if (today >= dat && today < nhan && d.tinhTrangDatPhong != "Đã hủy đơn!") return "Đã đặt"
        }
        return "Trống"
    }
}

package com.example.quanlykhachsan.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.quanlykhachsan.data.local.database.AppDatabase
import com.example.quanlykhachsan.data.local.entity.DatPhong
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import com.example.quanlykhachsan.data.local.entity.Phong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class BookViewModel(app: Application) : AndroidViewModel(app) {

    // ───── Dao & nguồn dữ liệu ─────
    private val db              = AppDatabase.getDatabase(app)
    private val phongDao        = db.phongDao()
    private val loaiPhongDao    = db.loaiPhongDao()
    private val datPhongDao     = db.datPhongDao()

    val roomTypeNames           = loaiPhongDao.getAllNames().asLiveData()
    val bookings                = datPhongDao.getAll()          // hiển thị danh sách đặt phòng

    private val _message        = MutableLiveData<String>()
    val message : LiveData<String> = _message

    // ───── API cho Fragment ─────
    fun addBooking(
        phone: String,
        roomTypeName: String,
        dateIn: Date,
        dateOut: Date
    ) = viewModelScope.launch(Dispatchers.IO) {

        val loaiPhong: LoaiPhong? = loaiPhongDao.getAllSync()
            .firstOrNull { it.tenLoaiPhong == roomTypeName }

        if (loaiPhong == null) {
            _message.postValue("Vui lòng chọn loại phòng!")
            return@launch
        }

        val room = findAvailableRoom(loaiPhong.maLoaiPhong, dateIn, dateOut)
        if (room == null) {
            _message.postValue("Không có [$roomTypeName] trống trong khoảng thời gian được chọn!")
            return@launch
        }

        val now = Date()
        val booking = DatPhong(
            maPhong         = room.maPhong,
            tenKhach        = "Test",              // tạm cho UI demo
            soCCCD          = "",
            soDienThoai     = phone,
            ngayDatPhong    = now,
            ngayNhanPhong   = dateIn,
            ngayTraPhong    = dateOut,
            tinhTrangDatPhong = "Chưa nhận phòng",
            ghiChu          = null
        )
        datPhongDao.insert(booking)
        _message.postValue("Đặt phòng ${room.maPhong} thành công!")
    }

    /* ───────── Nhận phòng ───────── */
    /* ───────── Nhận phòng ───────── */
    fun receiveBooking(booking: DatPhong) =
        viewModelScope.launch(Dispatchers.IO) {

            // 1. Kiểm tra trạng thái
            if (booking.tinhTrangDatPhong != "Chưa nhận phòng") {
                _message.postValue("Đơn này đã nhận phòng rồi!")
                return@launch
            }

            // 2. Kiểm tra ngày hợp lệ
            val today = Date()
            if (today.before(booking.ngayNhanPhong) ||
                (booking.ngayTraPhong != null && today.after(booking.ngayTraPhong))
            ) {
                _message.postValue("Hôm nay không nằm trong khoảng nhận/trả!")
                return@launch
            }

            // 3. Cập nhật trạng thái
            val updated = booking.copy(tinhTrangDatPhong = "Đang sử dụng")
            datPhongDao.update(updated)
            _message.postValue("Nhận phòng thành công!")
        }


    /** Trả về phòng đầu tiên KHÔNG trùng lịch */
    private suspend fun findAvailableRoom(
        loaiPhongId: Int,
        start: Date,
        end:   Date
    ): Phong? {
        val rooms = phongDao.getAllSync()
            .filter { it.maLoaiPhong == loaiPhongId }

        for (room in rooms) {
            val overlaps = datPhongDao.getByPhongSync(room.maPhong).any { b ->
                b.tinhTrangDatPhong != "Đã hủy" &&
                        b.ngayNhanPhong <= end &&
                        (b.ngayTraPhong ?: end) >= start
            }
            if (!overlaps) return room        // tìm được phòng hợp lệ
        }
        return null
    }
}
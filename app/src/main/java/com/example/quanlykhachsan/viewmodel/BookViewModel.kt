package com.example.quanlykhachsan.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.quanlykhachsan.data.local.database.AppDatabase
import com.example.quanlykhachsan.data.local.entity.DatPhong
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import com.example.quanlykhachsan.data.local.entity.Phong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class BookViewModel(app: Application) : AndroidViewModel(app) {

    // ───── Dao & nguồn dữ liệu ─────
    private val db              = AppDatabase.getDatabase(app)
    private val phongDao        = db.phongDao()
    private val loaiPhongDao    = db.loaiPhongDao()
    private val datPhongDao     = db.datPhongDao()
    private val traPhongDao    = db.traPhongDao()

    val roomTypeNames           = loaiPhongDao.getAllNames().asLiveData()
    val bookings                = datPhongDao.getAll()          // hiển thị danh sách đặt phòng

    private val _message        = MutableLiveData<String>()
    val message : LiveData<String> = _message

    /* Map <maPhong, tenLoaiPhong> cho UI */
    val roomTypeByRoomId = phongDao.getRoomsWithType()           // Flow<List<PhongWithLoaiPhong>>
        .map { list -> list.associate { it.maPhong to it.tenLoaiPhong } }
        .asLiveData()

    /* ---------- Chọn / bỏ chọn ---------- */
    private val _selectedBooking = MutableLiveData<DatPhong?>()
    val selectedBooking : LiveData<DatPhong?> = _selectedBooking
    fun setSelectedBooking(dp: DatPhong?) { _selectedBooking.value = dp }

    // ───── API cho Fragment ─────
    fun addBooking(
        phone: String,
        roomTypeName: String,
        dateIn: Date,
        dateOut: Date,
        referenceRoomId: Int? = null
    ) = viewModelScope.launch(Dispatchers.IO) {

        val today = Date()
        // (1) Ngày nhận phải >= hôm nay
        if (dateIn.before(today.clearTime())) {
            _message.postValue("Ngày nhận không được trước hôm nay!")
            return@launch
        }
        // (2) Ngày trả phải > ngày nhận
        if (dateOut.before(dateIn.clearTime().minusDays(-1))) {
            _message.postValue("Ngày trả phải sau ngày nhận!")
            return@launch
        }
        // (3) Ngày trả không được <= hôm nay
        if (dateOut.before(today.clearTime().minusDays(-1))) {
            _message.postValue("Ngày trả không được trước hôm nay!")
            return@launch
        }

        val loaiPhong: LoaiPhong? = loaiPhongDao.getAllSync()
            .firstOrNull { it.tenLoaiPhong == roomTypeName }

        if (loaiPhong == null) {
            _message.postValue("Vui lòng chọn loại phòng!")
            return@launch
        }

        val room = findNearestAvailableRoom(
            loaiPhong.maLoaiPhong,
            dateIn,
            dateOut,
            referenceRoomId
        )
        if (room == null) {
            _message.postValue("Không có [$roomTypeName] trống trong khoảng thời gian được chọn!")
            return@launch
        }

        val now = Date()
        val booking = DatPhong(
            maPhong         = room.maPhong,
            tenKhach        = "",
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
    /* ───────── Hủy đơn ───────── */
    fun cancelBooking(booking: DatPhong) =
        viewModelScope.launch(Dispatchers.IO) {

            // 1. Kiểm tra trạng thái
            if (booking.tinhTrangDatPhong == "Đang sử dụng") {
                _message.postValue("Không thể hủy khi đã nhận phòng!")
                return@launch
            }
            if (booking.tinhTrangDatPhong == "Đã trả phòng") {
                _message.postValue("Không thể hủy khi đã trả phòng!")
                return@launch
            }

            // 2. Kiểm tra ngày hợp lệ
            val today = Date()
            val cal = Calendar.getInstance().apply { time = booking.ngayNhanPhong }
            cal.add(Calendar.DAY_OF_MONTH, -1)
            val dayBefore = cal.time

            if (today.after(dayBefore))
            {
                _message.postValue("Đã quá hạn hủy đơn!")
                return@launch
            }

            // 3. Cập nhật trạng thái
            val updated = booking.copy(tinhTrangDatPhong = "Đã hủy đơn!")
            datPhongDao.update(updated)
            _message.postValue("Hủy đơn đặt phòng thành công!")
        }

    /* ---------- SỬA ---------- */
    fun updateBookingSimple(
        phone: String, roomTypeName: String, dateIn: Date, dateOut: Date
    ) {
        val current = _selectedBooking.value ?: run {
            _message.value = "Chọn đơn cần sửa!"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val updated = current.copy(
                soDienThoai   = phone,
                ngayNhanPhong = dateIn,
                ngayTraPhong  = dateOut
            )
            datPhongDao.update(updated)
            _message.postValue("Đã cập nhật đơn #${current.maDatPhong}")
        }
    }
    fun updateBookingFull(dp: DatPhong) = viewModelScope.launch(Dispatchers.IO) {
        datPhongDao.update(dp)
        _message.postValue("Đã lưu chi tiết đặt phòng")
    }

    /* ---------- XOÁ ---------- */
    fun deleteBooking(dp: DatPhong) = viewModelScope.launch(Dispatchers.IO) {
        // 1. Đếm số đơn trả phòng tham chiếu tới đơn đặt này
        val count = traPhongDao.countByDatPhongId(dp.maDatPhong)
        if (count > 0) {
            _message.postValue("Không thể xoá: còn $count đơn trả phòng dùng đơn đặt phòng này")
            return@launch
        }
        if (dp.tinhTrangDatPhong == "Đang sử dụng") {
            _message.postValue("Không thể xoá khi đang sử dụng!")
            return@launch
        }

        // 2. Nếu không có trả phòng nào, cho phép xóa
        datPhongDao.delete(dp)
        _selectedBooking.postValue(null)
        _message.postValue("Đã xóa đơn đặt phòng!")
    }


    /** Trả về phòng khả dụng GẦN NHẤT với referenceRoomId (nếu có) */
    private suspend fun findNearestAvailableRoom(
        loaiPhongId: Int,
        start: Date,
        end: Date,
        referenceRoomId: Int?
    ): Phong? {
        val rooms = phongDao.getAllSync()
            .filter { it.maLoaiPhong == loaiPhongId }               // đúng loại phòng
            .sortedWith(compareBy<Phong> {
                if (referenceRoomId == null) 0 else
                    kotlin.math.abs(it.maPhong - referenceRoomId)   // độ gần
            }.thenBy { it.maPhong })                                // tie-break

        for (room in rooms) {
            val overlaps = datPhongDao.getByPhongSync(room.maPhong).any { b ->
                b.tinhTrangDatPhong != "Đã hủy" &&
                        b.ngayNhanPhong <= end &&
                        (b.ngayTraPhong ?: end) > start
            }
            if (!overlaps) return room
        }
        return null
    }
    fun Date.minusDays(days: Int): Date =
        Calendar.getInstance().apply {
            time = this@minusDays
            add(Calendar.DAY_OF_MONTH, -days)
        }.time
    private fun Date.clearTime(): Date = Calendar.getInstance().apply {
        time = this@clearTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

}
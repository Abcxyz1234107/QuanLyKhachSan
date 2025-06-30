package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.switchMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.quanlykhachsan.data.local.dao.*
import com.example.quanlykhachsan.data.local.entity.TraPhong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val traPhongDao : TraPhongDao,
    private val datPhongDao : DatPhongDao,
    private val phongDao    : PhongDao,
    private val loaiPhongDao: LoaiPhongDao
) : ViewModel() {

    /* ---------- Toast ---------- */
    private val _notification = MutableLiveData<String>()
    val notification: LiveData<String> = _notification

    /* ---------- Item hiển thị ---------- */
    data class PaymentItem(
        val traPhongId : Int,
        val maDatPhong : Int,
        val roomId     : Int,
        val total      : Double,
        val paymentType: String,
        val paymentDate: String,
        val customer   : String,
        val note       : String
    )

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _suggestedDate = MutableLiveData<String>()
    val suggestedDate: LiveData<String> = _suggestedDate

    fun loadCheckoutDate(roomIdStr: String?, bookingIdStr: String?) =
        viewModelScope.launch(Dispatchers.IO) {

            /* ----- Parse các tham số ----- */
            val roomId    = roomIdStr?.toIntOrNull()
            val bookingId = bookingIdStr?.toIntOrNull()

            if (roomId == null) {
                _suggestedDate.postValue("")
                return@launch
            }

            /* ----- Ưu tiên tìm đúng mã đặt phòng nếu có ----- */
            val booking = when (bookingId) {
                null -> datPhongDao.getByPhongSync(roomId)
                    .sortedByDescending { it.ngayNhanPhong }
                    .firstOrNull()
                else -> datPhongDao.getById(bookingId)
                    ?.takeIf { it.maPhong == roomId }
            }

            val dateStr = booking?.ngayTraPhong
                ?.let { sdf.format(it) }
                ?: ""

            _suggestedDate.postValue(dateStr)
        }

    val payments : LiveData<List<PaymentItem>> =
        traPhongDao.getAll().switchMap { list ->
            liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
                val ui = list.map { tra ->
                    val booking   = datPhongDao.getById(tra.maDatPhong)
                    val roomId    = booking?.maPhong ?: -1
                    PaymentItem(
                        traPhongId  = tra.maTraPhong,
                        maDatPhong  = tra.maDatPhong,
                        roomId      = roomId,
                        total       = tra.tongTien,
                        paymentType = tra.hinhThucThanhToan,
                        paymentDate = sdf.format(tra.ngayThanhToan),
                        customer    = booking?.tenKhach ?: "N/A",
                        note        = tra.ghiChu ?: ""
                    )
                }
                emit(ui)
            }
        }

    /* ---------- Item đang chọn ---------- */
    private val _selected = MutableLiveData<PaymentItem?>()
    val selected : LiveData<PaymentItem?> = _selected
    fun setSelected(item: PaymentItem?) { _selected.postValue(item) }

    /* ---------- Extension Date.clearTime() ---------- */
    private fun Date.clearTime() = Calendar.getInstance().apply {
        time = this@clearTime
        set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0)
        set(Calendar.SECOND,0);      set(Calendar.MILLISECOND,0)
    }.time

    /** ---------- Thêm ---------- */
    fun addPayment(roomIdStr: String, payType: String, payDate: Date) =
        viewModelScope.launch(Dispatchers.IO) {

            /* ---- Validate mã phòng ---- */
            val roomId = roomIdStr.toIntOrNull()
            if (roomId == null) {
                _notification.postValue("Mã phòng không hợp lệ")
                return@launch
            }

            /* ---- Lấy TẤT CẢ đơn đặt của phòng, mới nhất trước ---- */
            val allBookings = datPhongDao.getByPhongSync(roomId)
                .sortedByDescending { it.ngayTraPhong }

            if (allBookings.isEmpty()) {
                _notification.postValue("Không tìm thấy đặt phòng cho phòng $roomId")
                return@launch
            }

            /* ---- Tìm đơn có ngày trả TRÙNG với ngày người dùng chọn ---- */
            val booking = allBookings.firstOrNull { bk ->
                val ngayTra = bk.ngayTraPhong?.clearTime()
                ngayTra != null && ngayTra == payDate.clearTime()
            }

            if (booking == null) {
                _notification.postValue("Không có đơn đặt phòng nào khớp ngày trả ${sdf.format(payDate)}")
                return@launch
            }

            /* ---- Kiểm tra tình trạng đơn ---- */
            when (booking.tinhTrangDatPhong) {
                "Chưa nhận phòng" -> {
                    _notification.postValue("Không thể trả phòng! Khách chưa nhận phòng!")
                    return@launch
                }
                "Đã trả phòng" -> {
                    _notification.postValue("Đơn này đã trả phòng rồi!")
                    return@launch
                }
                "Đã hủy đơn!" -> {
                    _notification.postValue("Đơn này đã bị hủy!")
                    return@launch
                }
            }

            /* ---- Tránh trùng (cùng mã đặt phòng) ---- */
            if (traPhongDao.countByDatPhongId(booking.maDatPhong) > 0) {
                _notification.postValue("Đơn trả phòng này đã tồn tại!")
                return@launch
            }

            /* ---- Tính tiền ---- */
            val loaiPhong = phongDao.getById(roomId)
                ?.let { lp -> loaiPhongDao.getById(lp.maLoaiPhong) }
                ?: run {
                    _notification.postValue("Lỗi tra cứu loại phòng!")
                    return@launch
                }

            val stayDays = max(
                1,
                ((booking.ngayTraPhong?.time ?: payDate.time) - booking.ngayNhanPhong.time) / 86_400_000L
            )
            val total = stayDays * loaiPhong.gia

            /* ---- Insert ---- */
            traPhongDao.insert(
                TraPhong(
                    maDatPhong        = booking.maDatPhong,
                    tongTien          = total,
                    hinhThucThanhToan = payType,
                    ngayThanhToan     = payDate,
                    ghiChu            = "$stayDays ngày × ${loaiPhong.gia}"
                )
            )
            _notification.postValue(
                "Thêm đơn trả phòng thành công! Tổng: ${"%,d".format(total.toLong())} ₫"
            )
        }

    /** ---------- Sửa ---------- */
    fun editPayment(payType:String, payDate:Date, note: String) = viewModelScope.launch(Dispatchers.IO) {
        val item = _selected.value ?: run { _notification.postValue("Chưa chọn đơn trả phòng!"); return@launch }
        val traPhong = traPhongDao.getById(item.traPhongId)
            ?: run { _notification.postValue("Không tìm thấy đơn trả phòng!"); return@launch }

        traPhongDao.update(
            traPhong.copy(
                hinhThucThanhToan = payType,
                ngayThanhToan     = payDate,
                ghiChu            = note
            )
        )
        _notification.postValue("Đã cập nhật đơn trả phòng!")
    }

    /** ---------- Xóa, Hủy ---------- */
    fun deletePayment() = viewModelScope.launch(Dispatchers.IO) {
        val item = _selected.value ?: run {
            _notification.postValue("Chưa chọn đơn để xoá!"); return@launch
        }
        val tra = traPhongDao.getById(item.traPhongId) ?: run {
            _notification.postValue("Không tìm thấy đơn trả phòng!"); return@launch
        }
        traPhongDao.delete(tra)
        _notification.postValue("Đã xoá đơn trả phòng!")
    }
    fun cancelPayment() = viewModelScope.launch(Dispatchers.IO) {
        val item = _selected.value ?: run {
            _notification.postValue("Chưa chọn đơn để huỷ!"); return@launch
        }
        val tra = traPhongDao.getById(item.traPhongId) ?: run {
            _notification.postValue("Không tìm thấy đơn trả phòng!"); return@launch
        }
        datPhongDao.getById(tra.maDatPhong)?.let {
            datPhongDao.update(it.copy(tinhTrangDatPhong = "Đang sử dụng"))
        }
        traPhongDao.delete(tra)
        _notification.postValue("Đã huỷ đơn trả phòng và khôi phục trạng thái đặt phòng!")
    }

    /** ---------- Trả phòng ---------- */
    fun payRoom() = viewModelScope.launch(Dispatchers.IO) {
        val item = _selected.value ?: run { _notification.postValue("Chưa chọn đơn!"); return@launch }
        val booking = datPhongDao.getById(item.maDatPhong)
            ?: run { _notification.postValue("Không tìm thấy đơn đặt phòng!"); return@launch }

        val today = Date()
        if (today >= booking.ngayNhanPhong && today < booking.ngayTraPhong) {
            _notification.postValue("Chưa đến ngày trả phòng!")
            return@launch
        }
        else datPhongDao.update(booking.copy(tinhTrangDatPhong = "Đã trả phòng"))
        _notification.postValue("Trả phòng thành công!")
    }
}
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
    fun loadLatestCheckoutDate(roomIdStr: String) = viewModelScope.launch(Dispatchers.IO) {
        val roomId = roomIdStr.toIntOrNull() ?: run {
            _suggestedDate.postValue("")
            return@launch
        }
        val booking = datPhongDao.getByPhongSync(roomId)
            .sortedByDescending { it.ngayNhanPhong }
            .firstOrNull()

        val dateStr = booking?.ngayTraPhong?.let { sdf.format(it) } ?: ""
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
    fun addPayment(roomIdStr:String, payType:String, payDate:Date) =
        viewModelScope.launch(Dispatchers.IO) {

            /* ---- Validate mã phòng ---- */
            val roomId = roomIdStr.toIntOrNull()
            if (roomId == null) { _notification.postValue("Mã phòng không hợp lệ"); return@launch }

            /* ---- Lấy đơn đặt phòng MỚI NHẤT của phòng này ---- */
            val booking = datPhongDao.getByPhongSync(roomId)
                .sortedByDescending { it.ngayNhanPhong }
                .firstOrNull()
            if (booking == null) { _notification.postValue("Không tìm thấy đặt phòng cho phòng $roomId"); return@launch }

            /* ---- Khớp ngày trả phòng ---- */
            val ngayTraPhong = booking.ngayTraPhong?.clearTime()
            if (ngayTraPhong == null || ngayTraPhong != payDate.clearTime()) {
                _notification.postValue("Ngày trả phòng không khớp với đơn đặt phòng!")
                return@launch
            }

            if (booking.tinhTrangDatPhong == "Chưa nhận phòng") {
                _notification.postValue("Không thể trả phòng! Khách hàng chưa nhận phòng!")
                return@launch
            }
            if (booking.tinhTrangDatPhong == "Đã trả phòng") {
                _notification.postValue("Đơn này đã trả phòng rồi!")
                return@launch
            }
            if (booking.tinhTrangDatPhong == "Đã hủy đơn!") {
                _notification.postValue("Đơn này đã bị hủy!")
                return@launch
            }

            /* ---- Tránh trùng khoá (phòng + ngày trả đã tồn tại) ---- */
            if (traPhongDao.countByDatPhongId(booking.maDatPhong) > 0) {
                _notification.postValue("Đơn trả phòng này đã tồn tại!")
                return@launch
            }

            /* ---- Tính tiền ---- */
            val loaiPhong = phongDao.getById(roomId)?.let { lp -> loaiPhongDao.getById(lp.maLoaiPhong) }
            if (loaiPhong == null) { _notification.postValue("Lỗi tra cứu loại phòng!"); return@launch }

            val stayDays = max(1,
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
            _notification.postValue("Thêm đơn trả phòng thành công! Tổng: ${"%,d".format(total.toLong())} ₫")
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
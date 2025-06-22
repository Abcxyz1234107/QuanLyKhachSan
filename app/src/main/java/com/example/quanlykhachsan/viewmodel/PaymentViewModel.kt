package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.switchMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.quanlykhachsan.data.local.dao.*
import com.example.quanlykhachsan.data.local.entity.TraPhong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

    // LiveData cho thông báo (Toast)
    private val _notification = MutableLiveData<String>()
    val notification: LiveData<String> = _notification

    /** Dữ liệu hiển thị trên RecyclerView */
    data class PaymentItem(val roomId: Int, val total: Double, val paymentDate: String)

    /** LiveData để UI observe */
    val payments: LiveData<List<PaymentItem>> =
        traPhongDao.getAll().switchMap { list ->
            liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val uiList = list.map { tra ->
                    val datPhong   = datPhongDao.getById(tra.maDatPhong)
                    val roomId     = datPhong?.maPhong ?: -1
                    val dateStr  = sdf.format(tra.ngayThanhToan)
                    PaymentItem(roomId, tra.tongTien, dateStr)
                }
                emit(uiList)
            }
        }

    /** Hàm nút Thêm */
    fun addPayment(roomIdStr: String, payType: String, paymentDate: Date) =
        viewModelScope.launch(Dispatchers.IO) {
            val roomId = roomIdStr.toIntOrNull()
            if (roomId == null) {
                _notification.postValue("Mã phòng không hợp lệ")
                return@launch
            }

            val booking = datPhongDao.getByPhongSync(roomId).firstOrNull()
            if (booking == null) {
                _notification.postValue("Không tìm thấy đặt phòng cho phòng $roomId")
                return@launch
            }

            val room = phongDao.getById(roomId)
            if (room == null) {
                _notification.postValue("Phòng $roomId không tồn tại")
                return@launch
            }

            val lp = loaiPhongDao.getById(room.maLoaiPhong)
            if (lp == null) {
                _notification.postValue("Không tìm thấy loại phòng cho phòng $roomId")
                return@launch
            }

            val millisPerDay = 86_400_000L
            val days = max(
                1,
                ((booking.ngayTraPhong ?: Date()).time - booking.ngayNhanPhong.time) / millisPerDay
            )
            val total = days * lp.gia

            traPhongDao.insert(
                TraPhong(
                    maDatPhong        = booking.maDatPhong,
                    tongTien          = total,
                    hinhThucThanhToan = payType,
                    ngayThanhToan     = paymentDate,
                    ghiChu            = "$days ngày * ${lp.gia}"
                )
            )
            _notification.postValue("Trả phòng thành công! Tổng: ${total.toLong()} vnđ")
        }
}
package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import com.example.quanlykhachsan.data.local.dao.DatPhongDao
import com.example.quanlykhachsan.data.local.dao.LoaiPhongDao
import com.example.quanlykhachsan.data.repository.loaiphong.LoaiPhongRepository
import com.example.quanlykhachsan.data.repository.phong.PhongRepository
import com.example.quanlykhachsan.data.local.entity.Phong
import com.example.quanlykhachsan.view.room.RoomItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

data class UiState(
    val selectedId: String = "",
    val selectedType: String = ""
)
sealed class UiEvent {
    data class ShowMessage(val message: String): UiEvent()
}

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val loaiPhongRepo: LoaiPhongRepository,
    private val phongRepo: PhongRepository,
    private val loaiPhongDao: LoaiPhongDao,
    private val datPhongDao: DatPhongDao
) : ViewModel() {

    private val _eventChannel = Channel<UiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    /* ----------- Message gửi UI ----------- */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    /** ---------------------------------- Logic nút ----------------------------------*/
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    internal var current: Phong? = null
    // Gọi khi user chạm vào 1 dòng trong RecyclerView
    fun onItemSelected(item: RoomItem?) = viewModelScope.launch {
        if (item == null) {                         // bỏ chọn ⇒ reset
            current = null
            _uiState.value = UiState()
            return@launch
        }

        current = phongRepo.getById(item.id)
        current?.let {
            // Đẩy dữ liệu vào UiState để Fragment quan sát
            val typeName = loaiPhongDao.getAllSync()
                .firstOrNull { lp -> lp.maLoaiPhong == it.maLoaiPhong }
                ?.tenLoaiPhong
                .orEmpty()

            _uiState.value = UiState(
                selectedId   = it.maPhong.toString(),
                selectedType = typeName
            )
        }
    }
    // Thêm
    fun add(idText: String, typeName: CharSequence?) = viewModelScope.launch {
        try {
            val lp = loaiPhongDao.getAllSync()
                .firstOrNull { it.tenLoaiPhong == typeName.toString() }
                ?: throw IllegalArgumentException("Chưa chọn loại phòng hợp lệ")

            val newPhong = Phong(
                maPhong        = idText.toIntOrNull() ?: throw IllegalArgumentException("Mã phòng không hợp lệ"),
                maLoaiPhong    = lp.maLoaiPhong,
                trangThaiPhong = "Trống"
            )
            phongRepo.insert(newPhong)
            _message.value = "Thêm phòng thành công"
            _uiState.value = UiState()
            current = null
        } catch (e: Exception) {
            _eventChannel.send(UiEvent.ShowMessage("Lỗi khi thêm phòng: ${e.localizedMessage}"))
        }
    }

    // Sửa
    fun editCurrent(typeName: CharSequence?) = viewModelScope.launch {
        current?.let { old ->
            try {
                val lp = loaiPhongDao.getAllSync()
                    .firstOrNull { it.tenLoaiPhong == typeName.toString() }
                    ?: throw IllegalArgumentException("Chưa chọn loại phòng hợp lệ")

                val count = datPhongDao.countByRoomId(old.maPhong)
                if (count > 0) {
                    _message.value = "Không thể sửa: còn $count đơn đặt phòng dùng phòng này"
                    return@launch
                }

                val updated = old.copy(maLoaiPhong = lp.maLoaiPhong)
                phongRepo.update(updated)
                _message.value = "Sửa phòng thành công"
                _uiState.value = UiState()
                current = null
            } catch (e: Exception) {
                _eventChannel.send(UiEvent.ShowMessage("Lỗi khi cập nhật phòng: ${e.localizedMessage}"))
            }
        }
    }

    // Xóa
    fun deleteCurrent() = viewModelScope.launch {
        current?.let { old ->
            val count = datPhongDao.countByRoomId(old.maPhong)
            if (count > 0) {
                _message.value = "Không thể xoá: còn $count đơn đặt phòng dùng phòng này"
                return@launch
            }
            try {
                phongRepo.delete(old)
                _message.value = "Xoá phòng thành công"
                _uiState.value = UiState()
                current = null
            } catch (e: Exception) {
                _eventChannel.send(UiEvent.ShowMessage("Lỗi khi xóa phòng: ${e.localizedMessage}"))
            }
        }
    }

    /** ---------------------------------- Logic khác ----------------------------------*/
    private val _allRoomTypeNames = MutableStateFlow<List<String>>(emptyList())
    /**  data of comboBox  */
    private val _query = MutableStateFlow("")

    /**  Gợi ý sau khi lọc (không phân biệt hoa thường, dấu cách)  */
    val suggestions: Flow<List<String>> =
                combine(_allRoomTypeNames, _query) { all, q ->
                        val key = q.normalized()
                        val filtered = if (key.isBlank()) all
                                      else all.filter { it.normalized().contains(key) }
                        if (filtered.isEmpty()) all else filtered
                }

    init {
        //  Lấy tên loại phòng cho ComboBox
        viewModelScope.launch {
            loaiPhongRepo.getAllNames()
                .collect { _allRoomTypeNames.value = it }
        }

            //  Lấy danh sách phòng + tên loại phòng
            viewModelScope.launch {
                    phongRepo.getRoomsWithType()
                        .collect { list ->
                                _rooms.value = list.map { RoomItem(it.maPhong, it.tenLoaiPhong) }
                        }
            }
    }

    /**  Danh sách phòng hiển thị  */
    private val _rooms = MutableStateFlow<List<RoomItem>>(emptyList())
    val rooms: StateFlow<List<RoomItem>> get() = _rooms

    /** Logic lọc */
    private val _filterQuery   = MutableStateFlow("")
    private val _filterEnable  = MutableStateFlow(false)
    val filterSuggestions: Flow<List<String>> =
        combine(_allRoomTypeNames, _filterQuery) { all, q ->
            val key = q.normalized()
            val filtered = if (key.isBlank()) all
            else all.filter { it.normalized().contains(key) }
            if (filtered.isEmpty()) all else filtered
        }

    /* Danh sách hiển thị sau khi áp filter */
    val roomsToShow: StateFlow<List<RoomItem>> =
        combine(_rooms, _filterEnable, _filterQuery) { list, enable, q ->
            if (!enable) return@combine list
            val key = q.normalized()
            if (key.isBlank()) list
            else list.filter { it.typeName.normalized().contains(key) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateQuery(text: CharSequence) {_query.value = text.toString()}
    fun updateFilterQuery(text: CharSequence) { _filterQuery.value = text.toString() }
    fun setFilterEnable(enable: Boolean)       { _filterEnable.value = enable }
    /**  hàm cho comboBox: xoá khoảng trắng + lowerCase  */
    private fun String.normalized() = replace("\\s".toRegex(), "").lowercase()
}

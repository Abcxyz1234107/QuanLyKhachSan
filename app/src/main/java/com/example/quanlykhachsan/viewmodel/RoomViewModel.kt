package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import com.example.quanlykhachsan.data.local.dao.LoaiPhongDao
import com.example.quanlykhachsan.data.repository.loaiphong.LoaiPhongRepository
import com.example.quanlykhachsan.data.repository.phong.PhongRepository
import com.example.quanlykhachsan.data.local.entity.Phong
import com.example.quanlykhachsan.view.room.RoomItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val selectedId: String = "",
    val selectedType: String = ""
)


@HiltViewModel
class RoomViewModel @Inject constructor(
    private val loaiPhongRepo: LoaiPhongRepository,
    private val phongRepo: PhongRepository,
    private val loaiPhongDao: LoaiPhongDao
) : ViewModel() {

    /** ---------------------------------- Logic nút ----------------------------------*/
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private var current: Phong? = null
    // Gọi khi user chạm vào 1 dòng trong RecyclerView
    fun onItemSelected(item: RoomItem) = viewModelScope.launch {
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
        val lp = loaiPhongDao.getAllSync()
            .firstOrNull { it.tenLoaiPhong == typeName.toString() }
            ?: return@launch
        val newPhong = Phong(
            maPhong      = idText.toIntOrNull() ?: 0,
            maLoaiPhong  = lp.maLoaiPhong,
            trangThaiPhong = "Trống"
        )
        phongRepo.insert(newPhong)
    }

    // Sửa
    fun editCurrent(typeName: CharSequence?) = viewModelScope.launch {
        current?.let { old ->
            val lp = loaiPhongDao.getAllSync()
                .firstOrNull { it.tenLoaiPhong == typeName.toString() }
                ?: return@launch
            val updated = old.copy(maLoaiPhong = lp.maLoaiPhong)
            phongRepo.update(updated)
            current = null
        }
    }

    // Xóa
    fun deleteCurrent() = viewModelScope.launch {
        current?.let {
            phongRepo.delete(it)
            current = null
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

    /** Call từ Fragment khi text thay đổi  */
    fun updateQuery(text: CharSequence) {
        _query.value = text.toString()
    }

    /**  hàm cho comboBox: xoá khoảng trắng + lowerCase  */
    private fun String.normalized() = replace("\\s".toRegex(), "").lowercase()
}

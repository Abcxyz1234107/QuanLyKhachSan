package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import com.example.quanlykhachsan.data.repository.loaiphong.LoaiPhongRepository
import com.example.quanlykhachsan.data.repository.phong.PhongRepository
import com.example.quanlykhachsan.view.room.RoomItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val loaiPhongRepo: LoaiPhongRepository,
    private val phongRepo: PhongRepository
) : ViewModel() {

    private val _allRoomTypeNames = MutableStateFlow<List<String>>(emptyList())

    /**  data of comboBox  */
    private val _query = MutableStateFlow("")

    /**  Gợi ý sau khi lọc (không phân biệt hoa thường, dấu cách)  */
    val suggestions: StateFlow<List<String>> =
        combine(_allRoomTypeNames, _query.debounce(100)) { all, q ->
            val key = q.normalized()
            if (key.isBlank()) all
            else all.filter { it.normalized().contains(key) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
    fun updateQuery(text: CharSequence) = _query.tryEmit(text.toString())

    /**  hàm cho comboBox: xoá khoảng trắng + lowerCase  */
    private fun String.normalized() = replace("\\s".toRegex(), "").lowercase()
}

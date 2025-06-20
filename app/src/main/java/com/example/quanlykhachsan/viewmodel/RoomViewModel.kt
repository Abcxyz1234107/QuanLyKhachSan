package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import com.example.quanlykhachsan.data.repository.loaiphong.LoaiPhongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val loaiPhongRepo: LoaiPhongRepository
) : ViewModel() {

    private val _allRoomTypeNames = MutableStateFlow<List<String>>(emptyList())

    /**  comboBox  */
    private val _query = MutableStateFlow("")

    /**  Gợi ý sau khi lọc (không phân biệt hoa thường, dấu cách)  */
    val suggestions: StateFlow<List<String>> =
        combine(_allRoomTypeNames, _query.debounce(100)) { all, q ->
            val key = q.normalized()
            if (key.isBlank()) all
            else all.filter { it.normalized().contains(key) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loaiPhongRepo.getAllNames()
                .collect { _allRoomTypeNames.value = it }
        }
    }

    /** Call từ Fragment khi text thay đổi  */
    fun updateQuery(text: CharSequence) = _query.tryEmit(text.toString())

    /**  Tiện ích xoá khoảng trắng + lowerCase  */
    private fun String.normalized() = replace("\\s".toRegex(), "").lowercase()
}

package com.example.quanlykhachsan.view.room

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.example.quanlykhachsan.view.room.SuggestionsAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.FragmentRoomBinding
import com.example.quanlykhachsan.viewmodel.RoomViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.view.MotionEvent
import kotlinx.coroutines.flow.distinctUntilChanged
import android.widget.Toast
import kotlinx.coroutines.flow.collectLatest
import com.example.quanlykhachsan.viewmodel.UiEvent
import com.google.android.material.checkbox.MaterialCheckBox
import android.widget.CompoundButton
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle

@AndroidEntryPoint
class RoomFragment : Fragment(R.layout.fragment_room) {

    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!
    private val vm: RoomViewModel by viewModels()

    /**  Adapter cho RecyclerView hiển thị phòng  */
    private lateinit var roomAdapter: RoomAdapter
    /**  Adapter cho ComboBox loại phòng  */
    private lateinit var comboAdapter: SuggestionsAdapter
    /** adapter cho comboBox lọc */
    private lateinit var filterAdapter: SuggestionsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentRoomBinding.bind(view)

        // Fill form
        roomAdapter = RoomAdapter { item ->
            vm.onItemSelected(item)
        }

        // Thêm
        binding.btnAdd.setOnClickListener {
            vm.add(
                binding.edtId.text.toString(),
                binding.actvRoomType.text
            )
        }
        // Sửa
        binding.btnEdit.setOnClickListener {
            vm.editCurrent(binding.actvRoomType.text)
        }
        // Xóa
        binding.btnDelete.setOnClickListener {
            vm.deleteCurrent()
        }

        /* ---------- ComboBox Tên loại phòng ---------- */
        comboAdapter = SuggestionsAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    mutableListOf()
        )
        binding.actvRoomType.setAdapter(comboAdapter)

        // gõ tới đâu -> ViewModel lọc tới đó
        binding.actvRoomType.doAfterTextChanged { text ->
            vm.updateQuery(text ?: "")
        }

        // quan sát suggestions từ ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            vm.suggestions.collectLatest { list ->
                            comboAdapter.updateData(list)
                            // nếu đang focus hoặc ngay khi xóa sạch, cứ show dropdown
                            if (binding.actvRoomType.isFocused) {
                                    binding.actvRoomType.showDropDown()
                            }
            }
        }

        /* ---------- RecyclerView Danh sách phòng ---------- */
        roomAdapter = RoomAdapter { item ->
            vm.onItemSelected(item)          // thông báo lên ViewModel
        }
        binding.rvRoom.apply {
            adapter = roomAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // quan sát uiState để đổ dữ liệu – đồng thời xoá highlight khi state rỗng
        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collect { st ->
                binding.edtId.setText(st.selectedId)
                binding.actvRoomType.setText(st.selectedType, false)

                if (st.selectedId.isBlank())          // state reset → bỏ chọn
                    roomAdapter.clearSelection()
            }
        }

        // ComboBox lọc
        filterAdapter = SuggestionsAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf())
        binding.actvFilterRoomType.setAdapter(filterAdapter)

        binding.actvFilterRoomType.doAfterTextChanged { txt ->
            vm.updateFilterQuery(txt ?: "")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.filterSuggestions.collectLatest { list ->
                filterAdapter.updateData(list)
                if (binding.actvFilterRoomType.isFocused) {
                    binding.actvFilterRoomType.showDropDown()
                }
            }
        }

        // CheckBox bật/tắt lọc
        binding.cbFilter.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean -> vm.setFilterEnable(isChecked)}
        viewLifecycleOwner.lifecycleScope.launch {
            vm.roomsToShow.collectLatest { list -> roomAdapter.submitList(list) }
        }

        /** ------ Logic khác */
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.eventFlow.collect { ev ->
                    if (ev is UiEvent.ShowMessage) {
                        Snackbar.make(requireView(), ev.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        vm.message.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                android.widget.Toast
                    .makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Bỏ focus khi ấn ngoài EditText
        binding.root.setOnTouchListener { v, event ->
            v.clearFocus()
            requireActivity().currentFocus?.clearFocus()
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

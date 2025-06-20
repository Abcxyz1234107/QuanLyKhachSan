package com.example.quanlykhachsan.view.room

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
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

@AndroidEntryPoint
class RoomFragment : Fragment(R.layout.fragment_room) {

    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!
    private val vm: RoomViewModel by viewModels()

    /**  Adapter cho RecyclerView hiển thị phòng  */
    private lateinit var roomAdapter: RoomAdapter
    /**  Adapter cho ComboBox loại phòng  */
    private lateinit var comboAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentRoomBinding.bind(view)

        /* ---------- ComboBox Tên loại phòng ---------- */
        comboAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.actvRoomType.setAdapter(comboAdapter)

        // gõ tới đâu -> ViewModel lọc tới đó & luôn mở dropdown
        binding.actvRoomType.doAfterTextChanged {
            vm.updateQuery(it ?: "")
            binding.actvRoomType.showDropDown()
        }

        // quan sát suggestions từ ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            vm.suggestions.collectLatest { list ->
                comboAdapter.clear()
                comboAdapter.addAll(list)
                comboAdapter.notifyDataSetChanged()
            }
        }

        /* ---------- RecyclerView Danh sách phòng ---------- */
        roomAdapter = RoomAdapter { item ->
            // Khi chọn 1 dòng ⇒ highlight & đổ lại form
            binding.edtId.setText(item.id.toString())
            binding.actvRoomType.setText(item.typeName, false)
        }
        binding.rvRoomTypes.apply {
            adapter = roomAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // TODO: quan sát LiveData/Flow phòng từ ViewModel rồi submitList(roomAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

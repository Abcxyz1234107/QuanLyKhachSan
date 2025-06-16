package com.example.quanlykhachsan.view.roomtype

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.FragmentRoomTypeBinding
import com.example.quanlykhachsan.viewmodel.RoomTypeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoomTypeFragment : Fragment(R.layout.fragment_room_type) {

    private var _binding: FragmentRoomTypeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoomTypeViewModel by viewModels()
    private lateinit var adapter: RoomTypeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRoomTypeBinding.bind(view)

        // Khởi tạo RecyclerView & Adapter
        adapter = RoomTypeAdapter { roomType ->
            viewModel.onItemSelected(roomType)
        }
        binding.rvRoomTypes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoomTypes.adapter = adapter

        // Lắng nghe danh sách
        viewModel.roomTypes.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        // Thêm / Sửa / Xóa
        binding.btnAdd.setOnClickListener {
            val name = binding.edtName.text
            val price = binding.edtPrice.text.toString().toFloatOrNull() ?: 0f
            viewModel.add(name, price)
        }
        binding.btnEdit.setOnClickListener {
            val name = binding.edtName.text
            val price = binding.edtPrice.text.toString().toFloatOrNull() ?: 0f
            viewModel.editCurrent(name, price)
        }
        binding.btnDelete.setOnClickListener {
            viewModel.deleteCurrent()
        }

        // Bật/tắt lọc
        binding.cbFilter.setOnCheckedChangeListener { _, checked ->
            viewModel.setFilterEnabled(checked)
            if (checked) {
                val min = binding.edtMin.text.toString().toDoubleOrNull() ?: 0.0
                val max = binding.edtMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
                //viewModel.filter(min, max)
            } else {
                viewModel.reset()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

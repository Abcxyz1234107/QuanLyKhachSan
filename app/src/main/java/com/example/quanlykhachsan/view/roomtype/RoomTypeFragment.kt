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
        binding.rvRoomTypes.layoutManager = LinearLayoutManager(requireContext())

        adapter = RoomTypeAdapter { selected ->
            if (selected == null) {
                binding.edtName.text?.clear()
                binding.edtPrice.text?.clear()
                viewModel.reset()
            } else {
                binding.edtName.setText(selected.tenLoaiPhong)
                binding.edtPrice.setText(selected.gia.toString())
                viewModel.onItemSelected(selected)
            }
        }
        binding.rvRoomTypes.adapter = adapter
        binding.rvRoomTypes.layoutManager = LinearLayoutManager(requireContext())
        viewModel.roomTypes.observe(viewLifecycleOwner) { adapter.submitList(it) }

        // Thêm loại phòng
        binding.btnAdd.setOnClickListener {
            val name = binding.edtName.text
            val price = binding.edtPrice.text.toString().toFloatOrNull() ?: 0f
            viewModel.add(name, price)
        }

        // Sửa loại phòng
        binding.btnEdit.setOnClickListener {
            val name = binding.edtName.text
            val price = binding.edtPrice.text.toString().toFloatOrNull() ?: 0f
            viewModel.editCurrent(name, price)
        }

        // Xóa loại phòng
        binding.btnDelete.setOnClickListener {
            viewModel.deleteCurrent()
        }

        // Bật/tắt filter
        binding.cbFilter.setOnCheckedChangeListener { _, checked ->
            viewModel.setFilterEnabled(checked)
            if (checked) {
                val min = binding.edtMin.text.toString().toDoubleOrNull() ?: 0.0
                val max = binding.edtMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
                // viewModel.filter(min, max)
            } else {
                viewModel.reset()
            }
        }

        // Bỏ focus các ô nhập khi chạm ngoài
        binding.root.setOnClickListener {
            binding.edtName.clearFocus()
            binding.edtPrice.clearFocus()
            binding.edtMin.clearFocus()
            binding.edtMax.clearFocus()
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

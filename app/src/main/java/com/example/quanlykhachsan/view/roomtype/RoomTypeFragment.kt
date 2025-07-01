package com.example.quanlykhachsan.view.roomtype

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.data.local.entity.DatPhong
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import com.example.quanlykhachsan.databinding.FragmentRoomTypeBinding
import com.example.quanlykhachsan.viewmodel.RoomTypeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoomTypeFragment : Fragment(R.layout.fragment_room_type) {

    private var _binding: FragmentRoomTypeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoomTypeViewModel by viewModels()
    private fun priceInput(): Float =
        binding.edtPrice.text.toString().replace(",", "").toFloatOrNull() ?: 0f
    private lateinit var adapter: RoomTypeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRoomTypeBinding.bind(view)

        /* ---------- RecyclerView ---------- */
        adapter = RoomTypeAdapter { selected ->
            if (selected == null) {
                binding.edtName.text?.clear()
                binding.edtPrice.text?.clear()
                viewModel.reset()
            } else {
                binding.edtName.setText(selected.tenLoaiPhong)
                binding.edtPrice.setText(formatPrice(selected.gia))
                viewModel.onItemSelected(selected)
            }
        }
        binding.rvRoomTypes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoomTypes.adapter       = adapter
        viewModel.roomTypes.observe(viewLifecycleOwner) { adapter.submitList(it) }

        /* ---------- Message ---------- */
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg?.let { android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show() }
        }

        /* ---------- Nút ---------- */
        binding.btnAdd.setOnClickListener {
            viewModel.add(binding.edtName.text, priceInput())
            clearInputs()
        }
        binding.btnEdit.setOnClickListener {
            viewModel.editCurrent(binding.edtName.text, priceInput())
            clearInputs()
        }
        binding.btnDelete.setOnClickListener {
            confirmDelete(viewModel.current!!)
            clearInputs()
        }

        /* ---------- Filter ---------- */
        updateFilter()

        binding.cbFilter.setOnCheckedChangeListener { _, _ -> updateFilter() }
        binding.edtMin.doOnTextChanged { _, _, _, _ -> if (binding.cbFilter.isChecked) updateFilter() }
        binding.edtMax.doOnTextChanged { _, _, _, _ -> if (binding.cbFilter.isChecked) updateFilter() }

        // Bỏ focus các ô nhập khi chạm ngoài
        binding.root.setOnClickListener {
            binding.edtName.clearFocus()
            binding.edtPrice.clearFocus()
            binding.edtMin.clearFocus()
            binding.edtMax.clearFocus()
            hideKeyboard()
        }
    }

    private fun confirmDelete(dp: LoaiPhong) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa loại phòng #${dp.tenLoaiPhong}?")
            .setMessage("Bạn chắc chắn muốn xoá loại phòng này?")
            .setPositiveButton("Xóa") { _, _ -> viewModel.deleteCurrent() }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun updateFilter() = viewModel.setFilter(
        binding.cbFilter.isChecked,
        binding.edtMin.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0,
        binding.edtMax.text.toString().replace(",", "").toDoubleOrNull() ?: Double.MAX_VALUE
    )
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    private fun clearInputs() {
        binding.edtName.setText("")
        binding.edtPrice.setText("")
    }
    private fun formatPrice(v: Double) =
        java.text.DecimalFormat("#,###").format(v)
}

package com.example.quanlykhachsan.view.staff

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.FragmentStaffBinding
import com.example.quanlykhachsan.viewmodel.StaffViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@AndroidEntryPoint
class StaffFragment : Fragment(R.layout.fragment_staff) {

    private var _binding: FragmentStaffBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StaffViewModel by viewModels()
    private val adapter by lazy {
        StaffAdapter { clickedNhanVien ->
            val selected = if (viewModel.selectedNhanVien.value?.maNhanVien == clickedNhanVien.maNhanVien)
                null else clickedNhanVien

            viewModel.setSelectedNhanVien(selected)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStaffBinding.bind(view)

        binding.rvNhanVien.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        binding.rvNhanVien.adapter = adapter

        // Quan sát LiveData & nạp vào adapter
        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        // fill các ô nhập
        viewModel.selectedNhanVien.observe(viewLifecycleOwner) { nv ->
            if (nv == null) {
                binding.edtTenNhanVien.setText("")
                binding.edtSoDienThoai.setText("")
            } else {
                binding.edtTenNhanVien.setText(nv.tenNhanVien)
                binding.edtSoDienThoai.setText(nv.soDienThoai)
            }
        }

        // Bỏ focus các ô nhập khi chạm ngoài
        binding.root.setOnClickListener {
            binding.edtTenNhanVien.clearFocus()
            binding.edtSoDienThoai.clearFocus()
            hideKeyboard()
        }

        // 1. Gán click cho các nút
        binding.btnAdd?.setOnClickListener {
            viewModel.addNhanVien(
                binding.edtTenNhanVien.text.toString(),
                binding.edtSoDienThoai.text.toString()
            )
        }
        binding.btnEdit?.setOnClickListener {
            viewModel.updateNhanVien(
                binding.edtTenNhanVien.text.toString(),
                binding.edtSoDienThoai.text.toString()
            )
        }
        binding.btnDelete?.setOnClickListener { viewModel.deleteNhanVien() }

        // 2. Lắng nghe toastEvent (toast: các messagee thông báo)
        viewModel.toastEvent.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            if (msg.startsWith("Đã")) {          // thành công → clear form
                binding.edtTenNhanVien.setText("")
                binding.edtSoDienThoai.setText("")
                binding.edtTenNhanVien.clearFocus()
                binding.edtSoDienThoai.clearFocus()
                hideKeyboard()
            }
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
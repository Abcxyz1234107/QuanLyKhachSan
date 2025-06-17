package com.example.quanlykhachsan.view.staff

import android.os.Bundle
import android.util.Log
import android.view.View
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

        viewModel.staffList.observe(viewLifecycleOwner) { list ->
            Log.d("StaffFragment", "Danh sách NV size = ${list.size}")   // ← log count
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
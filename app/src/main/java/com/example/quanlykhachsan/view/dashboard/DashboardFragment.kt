package com.example.quanlykhachsan.view.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.ActivityDashboardBinding
import com.example.quanlykhachsan.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.activity_dashboard) {

    private var _binding: ActivityDashboardBinding? = null
    private val binding get() = _binding!!

    private val vm: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ActivityDashboardBinding.bind(view)

        /* RecyclerView: lưới 2 cột */
        binding.rvPhong.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = PhongAdapter()
        binding.rvPhong.adapter = adapter

        /* Observers */
        vm.danhSachPhong.observe(viewLifecycleOwner) { adapter.submitList(it) }
        vm.soLuongPhongText.observe(viewLifecycleOwner) { binding.tvSoPhong.text = it }
        vm.soLuongLoaiText.observe(viewLifecycleOwner) { binding.tvSoLoai.text = it }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

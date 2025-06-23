package com.example.quanlykhachsan.view.staff

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.FragmentStaffBinding
import com.example.quanlykhachsan.viewmodel.StaffViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.data.local.entity.NhanVien
import com.example.quanlykhachsan.databinding.DialogStaffDetailBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.text.DecimalFormat
import java.util.Calendar
import android.content.res.Resources
import android.widget.LinearLayout
import android.widget.NumberPicker

@AndroidEntryPoint
class StaffFragment : Fragment(R.layout.fragment_staff) {

    // format tiếng Việt
    private val viDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
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
        binding.btnChiTiet?.setOnClickListener {
            val nv = viewModel.selectedNhanVien.value
            if (nv == null) {
                Toast.makeText(requireContext(), "Chọn nhân viên để xem chi tiết", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showDetailDialog(nv)
        }

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

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private fun setUpDatePicker(edt: TextInputEditText) {
        edt.inputType = InputType.TYPE_NULL          // chặn bàn phím
        edt.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                R.style.ThemeOverlay_App_DatePicker_Spinner,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    edt.setText(sdf.format(cal.time))
                },
                cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
            ).apply { datePicker.calendarViewShown = false }   // ép dùng spinner
                .show()
        }
    }

    private fun showDetailDialog(nv: NhanVien) {
        val dialogBinding = DialogStaffDetailBinding.inflate(layoutInflater)
        var isSaved = false

        // 1. Fill dữ liệu mặc định
        dialogBinding.tvDetailMaNhanVien.text    = nv.maNhanVien.toString()
        dialogBinding.edtDetailTenNhanVien.setText(nv.tenNhanVien)
        dialogBinding.edtDetailSoDienThoai.setText(nv.soDienThoai)
        dialogBinding.edtDetailChucVu.setText(nv.chucVu)
        val df = DecimalFormat("#,##0")
        dialogBinding.edtDetailLuongCoBan.setText(df.format(nv.luongCoBan))
        dialogBinding.edtDetailLichLamViec.setText(nv.lichLamViec)
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dialogBinding.edtDetailNgayVaoLam.setText(viDateFormat.format(nv.ngayBatDauLamViec))

        // 2. Mở DatePickerDialog
        dialogBinding.edtDetailNgayVaoLam.inputType = InputType.TYPE_NULL
        dialogBinding.edtDetailNgayVaoLam.setOnClickListener {
            val cal = Calendar.getInstance().apply { time = nv.ngayBatDauLamViec }
            val dpd = DatePickerDialog(
                requireContext(),
                R.style.ThemeOverlay_App_DatePicker_Spinner,
                null,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.calendarViewShown = false
            dpd.datePicker.spinnersShown     = true
            dpd.setOnShowListener {
                val pickerView = dpd.datePicker
                val res = Resources.getSystem()

                val dayPicker = pickerView.findViewById<NumberPicker>(
                    res.getIdentifier("day", "id", "android"))
                val monthPicker = pickerView.findViewById<NumberPicker>(
                    res.getIdentifier("month", "id", "android"))
                val yearPicker = pickerView.findViewById<NumberPicker>(
                    res.getIdentifier("year", "id", "android"))

                /* -------- 1. Đảo thứ tự: Day – Month – Year -------- */
                (dayPicker?.parent as? LinearLayout)?.let { container ->
                    container.removeAllViews()
                    container.addView(dayPicker)
                    container.addView(monthPicker)
                    container.addView(yearPicker)
                }

                /* -------- 2. Tên tháng Tiếng Việt -------- */
                val viMonths = arrayOf(
                    "Tháng 1","Tháng 2","Tháng 3","Tháng 4",
                    "Tháng 5","Tháng 6","Tháng 7","Tháng 8",
                    "Tháng 9","Tháng 10","Tháng 11","Tháng 12"
                )
                monthPicker?.apply {
                    minValue = 0
                    maxValue = 11
                    displayedValues = viMonths
                }

                /* -------- 3. Cập-nhật EditText ngay khi cuộn -------- */
                fun updateInput() {
                    val cal = Calendar.getInstance().apply {
                        set(
                            yearPicker?.value ?: cal[Calendar.YEAR],
                            monthPicker?.value ?: cal[Calendar.MONTH],
                            dayPicker?.value ?: cal[Calendar.DAY_OF_MONTH]
                        )
                    }
                    dialogBinding.edtDetailNgayVaoLam.setText(
                        viDateFormat.format(cal.time)
                    )
                }
                dayPicker?.setOnValueChangedListener { _, _, _ -> updateInput() }
                monthPicker?.setOnValueChangedListener { _, _, _ ->
                    monthPicker.displayedValues = viMonths; updateInput() }
                yearPicker?.setOnValueChangedListener { _, _, _ -> updateInput() }

                /* Gọi 1 lần để hiển thị giá trị ban đầu */
                updateInput()
            }

            dpd.show()
        }

        // 3. Khởi tạo AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // 4. Xử lý nút Thoát
        dialogBinding.btnDetailExit.setOnClickListener {
            val currName    = dialogBinding.edtDetailTenNhanVien.text.toString()
            val currPhone   = dialogBinding.edtDetailSoDienThoai.text.toString()
            val currChucVu  = dialogBinding.edtDetailChucVu.text.toString()
            val currSalary  = dialogBinding.edtDetailLuongCoBan.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val currLich    = dialogBinding.edtDetailLichLamViec.text.toString()
            val currDateStr = dialogBinding.edtDetailNgayVaoLam.text.toString()
            val currDate    = try { viDateFormat.parse(currDateStr) } catch (_:Exception) { nv.ngayBatDauLamViec }

            // kiểm tra đã sửa hay chưa
            val changed = currName    != nv.tenNhanVien
                    || currPhone   != nv.soDienThoai
                    || currChucVu  != nv.chucVu
                    || currSalary  != nv.luongCoBan
                    || currLich    != nv.lichLamViec
                    || currDate    != nv.ngayBatDauLamViec

            if (isSaved || !changed) {
                dialog.dismiss()
            } else {
                AlertDialog.Builder(requireContext())
                    .setMessage("Chưa lưu thay đổi, đồng ý lưu?")
                    .setPositiveButton("Có") { _, _ ->
                        val updated = nv.copy(
                            tenNhanVien       = currName,
                            soDienThoai       = currPhone,
                            chucVu            = currChucVu,
                            luongCoBan        = currSalary,
                            lichLamViec       = currLich,
                            ngayBatDauLamViec = currDate
                        )
                        viewModel.updateNhanVienFull(updated)
                        isSaved = true
                        dialog.dismiss()
                    }
                    .setNegativeButton("Không") { _, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        // 5. Xử lý nút Lưu
        dialogBinding.btnDetailSave.setOnClickListener {
            val salaryRaw = dialogBinding.edtDetailLuongCoBan.text.toString().replace(",", "")
            val updated = nv.copy(
                tenNhanVien        = dialogBinding.edtDetailTenNhanVien.text.toString(),
                soDienThoai        = dialogBinding.edtDetailSoDienThoai.text.toString(),
                chucVu             = dialogBinding.edtDetailChucVu.text.toString(),
                luongCoBan         = salaryRaw.toDoubleOrNull() ?: 0.0,
                lichLamViec        = dialogBinding.edtDetailLichLamViec.text.toString(),
                ngayBatDauLamViec  = viDateFormat.parse(dialogBinding.edtDetailNgayVaoLam.text.toString()) ?: nv.ngayBatDauLamViec
            )
            viewModel.updateNhanVienFull(updated)
            isSaved = true
        }

        dialog.show()
    }
}
package com.example.quanlykhachsan.view.payment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.FragmentPaymentBinding
import com.example.quanlykhachsan.viewmodel.PaymentViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PaymentViewModel>()
    private val adapter  = PaymentAdapter()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPaymentBinding.bind(view)

        /* ---------- DatePicker spinner ---------- */
        setUpDatePicker(binding.edtDateOut)

        /* RecyclerView */
        binding.rvBook.apply {
            layoutManager = LinearLayoutManager(context)
            adapter       = this@PaymentFragment.adapter
        }

        adapter.onItemClick = { item ->
            if (item == null) {
                // Bỏ chọn: clear input
                binding.edtRoomId.setText("")
                binding.edtDateOut.setText("")
            } else {
                binding.edtRoomId.setText(item.roomId.toString())
                binding.edtDateOut.setText(item.paymentDate)
            }
        }

        /* Dropdown hình thức thanh toán */
        val payTypes = listOf("Thẻ tín dụng", "Chuyển khoản", "Tiền mặt")
        binding.edtPaymentType.setSimpleItems(payTypes.toTypedArray())

        // Observe data và notifications
        viewModel.payments.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.notification.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        /* Nút thêm */
        binding.btnAdd.setOnClickListener {
            val roomId  = binding.edtRoomId.text.toString()
            val payType = binding.edtPaymentType.text.toString()
            val payDate = sdf.parse(binding.edtDateOut.text.toString())
            if (payDate != null) {
                viewModel.addPayment(roomId, payType, payDate)
            }
        }

        /* Tự động cập nhật ngày thanh toán */
        binding.edtDateOut.setText(java.text.SimpleDateFormat("dd/MM/yyyy")
            .format(java.util.Date()))

        /** ------------------------------- Hàm bỏ focus & ẩn bàn phím ------------------------------- */
        fun clearFocusAndHideKeyboard() {
            // Form Trả phòng
            binding.edtRoomId.clearFocus()
            binding.edtPaymentType.clearFocus()
            binding.edtDateOut.clearFocus()
            // Phần Lọc
            binding.edtFilterPaymentType.clearFocus()
            binding.edtFilterTotalPayment.clearFocus()

            // Ẩn bàn phím
            val imm = context
                ?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }

        // Chạm ngoài form sẽ clear focus
        binding.formContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearFocusAndHideKeyboard()
            }
            false
        }
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearFocusAndHideKeyboard()
            }
            false
        }
    }

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

    private fun parseDate(str: String): Date? =
        try { sdf.parse(str) } catch (e: ParseException) { null }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
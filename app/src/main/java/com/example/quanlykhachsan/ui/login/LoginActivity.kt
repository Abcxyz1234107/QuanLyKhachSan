package com.example.quanlykhachsan.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.quanlykhachsan.databinding.ActivityLoginBinding
import com.example.quanlykhachsan.MainActivity        // màn hình tiếp theo
import com.example.quanlykhachsan.viewmodel.LoginActivityVM
import com.google.android.material.snackbar.Snackbar
import android.graphics.Rect
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginActivityVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Xử lý nút Đăng nhập – gom toàn bộ logic UI ở Activity */
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty().trim()
            viewModel.login(username, password)
        }

        /* Quan sát kết quả đăng nhập */
        viewModel.loginState.observe(this, Observer { state ->
            when (state) {
                is LoginActivityVM.LoginState.Success -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()  // không quay lại màn hình Login
                }
                is LoginActivityVM.LoginState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    //Bỏ chọn khi ấn vị trí khác ngoài 2 ô nhập liệu
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focusedView = currentFocus
            if (focusedView is EditText) { // Kiểm tra điểm chạm nằm ngoài EditText
                val outRect = Rect()
                focusedView.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    focusedView.clearFocus() // Bỏ focus & ẩn bàn phím
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}

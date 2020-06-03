package com.dllewellyn.common

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.dllewellyn.common.databinding.ActivityViewDataBinding
import com.dllewellyn.common.room.SingleFileEntity
import org.koin.androidx.viewmodel.ext.android.viewModel


class ViewDataActivity : AppCompatActivity() {

    private val viewDataViewModel: ViewDataViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entity = intent?.extras?.get("data") as SingleFileEntity

        viewDataViewModel.loadWithEntity(entity)


        viewDataViewModel.needPassword.observe(this, Observer {
            showPasswordPromptDialog()
        })

        DataBindingUtil.setContentView<ActivityViewDataBinding>(this, R.layout.activity_view_data)

            .apply {
                viewModel = viewDataViewModel
                lifecycleOwner = this@ViewDataActivity
            }

    }


    private fun showPasswordPromptDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Password")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { _, _ ->
            viewDataViewModel.passwordEntered(input.text.toString())
        }

        builder.show()
    }
}
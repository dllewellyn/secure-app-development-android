package com.dllewellyn.encryption_lab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.dllewellyn.common.ViewDataActivity
import com.dllewellyn.common.bindingadapters.models.DirectoryListController
import com.dllewellyn.common.models.FileTypes
import com.dllewellyn.encryption_lab.databinding.FragmentEncryptionLabBinding
import kotlinx.android.synthetic.main.fragment_encryption_lab.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.IllegalStateException

class EncryptionLabFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private val viewModel: EncryptionLabViewModel by viewModel()
    private val controller: DirectoryListController by lazy {
        DirectoryListController {
            startActivity(
                Intent(requireContext(), ViewDataActivity::class.java)
                    .apply {
                        putExtra("data", it)
                    }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = DataBindingUtil.inflate<FragmentEncryptionLabBinding>(
        inflater,
        R.layout.fragment_encryption_lab,
        container,
        false
    ).run {
        viewModel = this@EncryptionLabFragment.viewModel
        lifecycleOwner = this@EncryptionLabFragment.viewLifecycleOwner
        root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            createTypeSpinner.adapter = adapter
            createTypeSpinner.onItemSelectedListener = this
        }

        epoxyDirectoryView.setController(controller)

        viewModel.files.observe(viewLifecycleOwner, Observer {
            controller.setData(it)
        })
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        viewModel.nothingSelected()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedType = when (resources.getStringArray(R.array.options)[position]) {
            "Shared prefs" -> FileTypes.SHARED_PREFERENCES
            "Room Database" -> FileTypes.ROOM_DATABASE
            "File on disk" -> FileTypes.FILE
            else -> throw  IllegalStateException()
        }

        viewModel.selectedItem(selectedType)
    }
}
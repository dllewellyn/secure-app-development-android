package com.secure.app.dev

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dllewellyn.common.models.PluginModel
import com.secure.app.dev.ui.main.AllLabController
import kotlinx.android.synthetic.main.fragment_all_plugins.*

class AllPluginsFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controller = AllLabController(requireContext())
        epoxyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        epoxyRecyclerView.adapter = controller.adapter

        controller.setData(
            listOf(
                PluginModel(
                    "Lab one - Location, Location, Location"
                ) {
                    findNavController().navigate(
                        AllPluginsFragmentDirections.actionAllPluginsFragmentToLocationLocationLocationFragment()
                    )
                },

                PluginModel(
                    "Lab two - Encryption"
                ) {
                    findNavController().navigate(
                        AllPluginsFragmentDirections.actionAllPluginsFragmentToEncryptionLabFragment()
                    )
                }
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_plugins, container, false)
    }
}
package com.secure.app.dev.ui.main

import android.content.Context
import com.airbnb.epoxy.TypedEpoxyController
import com.dllewellyn.common.models.PluginModel
import com.secure.app.dev.tutorialListItem

class AllLabController(val context: Context) : TypedEpoxyController<List<PluginModel>>() {

    override fun buildModels(items: List<PluginModel>) {
        items.forEach { plugin ->
            tutorialListItem {
                id(plugin.title)
                tutorial(plugin)
                clickListener { _ -> plugin.clickHandler() }
            }
        }
    }
}
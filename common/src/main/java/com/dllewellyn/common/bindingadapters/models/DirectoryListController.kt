package com.dllewellyn.common.bindingadapters.models

import com.airbnb.epoxy.TypedEpoxyController
import com.dllewellyn.common.directoryLayout
import java.io.File

class DirectoryListController : TypedEpoxyController<List<File>>() {

    override fun buildModels(data: List<File>) {

        data.forEach { file ->
            directoryLayout {
                id(file.path)
                path(file.path)
            }
        }
    }
}
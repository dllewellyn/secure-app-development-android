package com.dllewellyn.common.bindingadapters.models

import com.airbnb.epoxy.TypedEpoxyController
import com.dllewellyn.common.directoryLayout
import com.dllewellyn.common.room.SingleFileEntity
import java.io.File

class DirectoryListController(val onClickListener: (SingleFileEntity) -> Unit) :
    TypedEpoxyController<List<SingleFileEntity>>() {

    override fun buildModels(data: List<SingleFileEntity>) {

        data.forEach { file ->
            directoryLayout {
                id(file.path)
                path(file.path)
                type(file.type)
                isEncrypted(
                    if (file.encrypted) {
                        requireNotNull("Encrypted: ${file.encryptionType}")
                    } else {
                        "Not encrypted"
                    }
                )
                clickListener { _, _, _, _ ->
                    onClickListener(file)
                }
            }
        }
    }
}
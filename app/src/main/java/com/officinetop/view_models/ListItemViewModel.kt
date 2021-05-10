package com.officinetop.view_models

import com.officinetop.adapter.GenericAdapter
import com.officinetop.sos.SOSActivity
import java.io.Serializable

abstract class ListItemViewModel : Serializable {
    var adapterPostion: Int = -1
    var isChecked: Boolean = false
    var onListItemViewClickListener: GenericAdapter.OnListItemViewClickListener? = null
    var sosActivityListener: SOSActivity.SOSActivityListener? = null

}
package com.officinetop.officine.view_models

import com.officinetop.officine.adapter.GenericAdapter
import com.officinetop.officine.sos.SOSActivity
import java.io.Serializable

abstract class ListItemViewModel : Serializable {
    var adapterPostion: Int = -1
    var isChecked: Boolean = false
    var onListItemViewClickListener: GenericAdapter.OnListItemViewClickListener? = null
    var sosActivityListener: SOSActivity.SOSActivityListener? = null

}
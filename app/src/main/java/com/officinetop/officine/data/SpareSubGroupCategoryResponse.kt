package com.officinetop.officine.data

import com.google.gson.annotations.SerializedName

data class SpareSubGroupCategoryResponse(

	@field:SerializedName("status_code")
	val statusCode: Int? = null,

	@field:SerializedName("data")
	val data: Any? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("data_set")
	val dataSetSubGroupCat: MutableList<DataSetSubGroupCatItem?>? = null
)
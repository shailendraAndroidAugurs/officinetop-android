package com.officinetop.officine.data

import com.google.gson.annotations.SerializedName

data class SpareSubCategoriesResponse(

	@field:SerializedName("status_code")
	val statusCode: Int? = null,

	@field:SerializedName("data")
	val data: Any? = null,

	@field:SerializedName("data_set")
	val dataSet: MutableList<DataSetItem?>? = null,

	@field:SerializedName("message")
	val message: String? = null
)
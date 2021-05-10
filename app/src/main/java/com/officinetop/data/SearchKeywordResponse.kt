package com.officinetop.data

import com.google.gson.annotations.SerializedName

data class SearchKeywordResponse(

        @field:SerializedName("status_code")
        val statusCode: Int? = null,

        @field:SerializedName("data")
        val data: SearchData? = null,

        @field:SerializedName("data_set")
        val dataSet: Any? = null,

        @field:SerializedName("message")
        val message: String? = null
)

data class SearchData(

        @field:SerializedName("all")
        val all: ArrayList<AllItem?>? = null,

        @field:SerializedName("self")
        val self: ArrayList<SelfItem?>? = null
)

data class SelfItem(

        @field:SerializedName("keyword")
        val keyword: String? = null
)

data class AllItem(

        @field:SerializedName("keyword")
        val keyword: String? = null
)

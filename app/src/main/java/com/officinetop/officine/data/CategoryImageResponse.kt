package com.officinetop.officine.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CategoryImageResponse(
        @SerializedName("id")
        var id: String = "",
        @SerializedName("image_name")
        var imageName: String = "",
        @SerializedName("image_url")
        var imageUrl: String = "") : Serializable
package com.officinetop.data

import com.google.gson.annotations.SerializedName

data class TyreMeasurementResponse(

        @field:SerializedName("data_set")
        val tyreDataSet: Any? = null,

        @field:SerializedName("status_code")
        val statusCode: Int? = null,

        @field:SerializedName("data")
        val tyreData: TyreData? = null,

        @field:SerializedName("message")
        val message: Any? = null
)

data class TyreData(

        @field:SerializedName("measurment")
        val measurment: List<MeasurmentItem?>? = null,

        @field:SerializedName("model_response")
        val modelResponse: String? = null,

        @field:SerializedName("maker_response")
        val makerResponse: String? = null,

        @field:SerializedName("year")
        val year: Int? = null,

        @field:SerializedName("generations")
        val generations: String? = null,

        @field:SerializedName("rims")
        val rims: String? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("language")
        val language: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("maker_slug")
        val makerSlug: String? = null,

        @field:SerializedName("model_slug")
        val modelSlug: String? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("year_response")
        val yearResponse: String? = null,

        @field:SerializedName("tires")
        val tires: String? = null,

        @field:SerializedName("image")
        val alertImageURL: String? = "",

        @field:SerializedName("discription")
        val alertDescription: String? = ""
)

data class MeasurmentItem(

        @field:SerializedName("aspect_ratio")
        val aspectRatio: Int? = null,

        @field:SerializedName("unique_id")
        val uniqueId: String? = null,

        @field:SerializedName("count")
        val count: Int? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("priority")
        val priority: Any? = null,

        @field:SerializedName("deleted_at")
        val deletedAt: Any? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("width")
        val width: Int? = null,

        @field:SerializedName("users_id")
        val usersId: Int? = null,

        @field:SerializedName("rim_diameter")
        val rimDiameter: Int? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("tire")
        val tire: String? = null,

        @field:SerializedName("status")
        val status: String? = null
)

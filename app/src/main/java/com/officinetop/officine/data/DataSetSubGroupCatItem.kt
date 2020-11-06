package com.officinetop.officine.data

import com.google.gson.annotations.SerializedName

data class DataSetSubGroupCatItem(

        @field:SerializedName("org_description")
        val orgDescription: String? = null,

        @field:SerializedName("item")
        val item: String? = null,

        @field:SerializedName("images")
        val images: ArrayList<CategoryImageResponse> = ArrayList(),

        @field:SerializedName("unique_id")
        val uniqueId: String? = null,

        @field:SerializedName("item_id")
        val itemId: Int? = null,

        @field:SerializedName("org_priority")
        val orgPriority: Int? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("n2_kromeda_group_id")
        val n2KromedaGroupId: Int? = null,

        @field:SerializedName("language")
        val language: String? = null,

        @field:SerializedName("type")
        val type: Int? = null,

        @field:SerializedName("priority")
        val priority: Int? = null,

        @field:SerializedName("deleted_at")
        val deletedAt: Any? = null,

        @field:SerializedName("products_groups_id")
        val productsGroupsId: Int? = null,

        @field:SerializedName("our_description")
        val ourDescription: Any? = null,

        @field:SerializedName("front_rear")
        val frontRear: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("users_id")
        val usersId: Int? = null,

        @field:SerializedName("n1_kromeda_group_id")
        val n1KromedaGroupId: Int? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("left_right")
        val leftRight: String? = null,

        @field:SerializedName("status")
        val status: String? = null,
        @field:SerializedName("our_category_name")
        val ourCategoryName: String? = null

)


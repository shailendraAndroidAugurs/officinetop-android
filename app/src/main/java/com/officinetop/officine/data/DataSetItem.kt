package com.officinetop.officine.data

import com.google.gson.annotations.SerializedName

data class DataSetItem(

        @field:SerializedName("car_model")
        val carModel: String? = null,

        @field:SerializedName("org_description")
        val orgDescription: Any? = null,

        @field:SerializedName("images")
        val images: ArrayList<CategoryImageResponse> = ArrayList(),

        @field:SerializedName("group_name")
        val groupName: String? = null,

        @field:SerializedName("org_priority")
        val orgPriority: Int? = null,

        @field:SerializedName("description")
        val description: Any? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("language")
        val language: String? = null,

        @field:SerializedName("type")
        val type: Int? = null,

        @field:SerializedName("group_unique_id")
        val groupUniqueId: String? = null,

        @field:SerializedName("priority")
        val priority: Any? = null,

        @field:SerializedName("deleted_at")
        val deletedAt: Any? = null,

        @field:SerializedName("car_makers")
        val carMakers: String? = null,

        @field:SerializedName("car_version")
        val carVersion: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("group_id")
        val groupId: Int? = null,

        @field:SerializedName("parent_id")
        val parentId: Int? = null,

        @field:SerializedName("products_groups_group_id")
        val productsGroupsGroupId: Int? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("status")
        val status: String? = null
)
package com.officinetop.officine.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RevisionServicesResponse(

        @field:SerializedName("status_code")
        val statusCode: Int? = null,

        @field:SerializedName("data_set")
        val revDataSet: MutableList<RevDataSetItem?>? = null,

        @field:SerializedName("data")
        val revData: Any? = null,

        @field:SerializedName("message")
        val message: String? = null
) : Serializable

data class RevDataSetItem(

        @field:SerializedName("images")
        val images: List<ImagesItem?>? = null,

        @field:SerializedName("category_name")
        val categoryName: String? = null,

        @field:SerializedName("cat_images")
        val catImages: String? = null,

        @field:SerializedName("cat_image_url")
        val catImageUrl: String? = null,

        @field:SerializedName("category_type")
        val categoryType: String? = null,

        @field:SerializedName("parent_cat_id")
        val parentCatId: Any? = null,

        @field:SerializedName("description")
        val description: String? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("priority")
        val priority: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("min_price")
        val minPrice: String? = null,

        @field:SerializedName("price")
        val price: Any? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("time")
        val time: Any? = null,

        @field:SerializedName("status")
        val status: Int? = null
) : Serializable

data class ImagesItem(

        @field:SerializedName("primary_image")
        val primaryImage: Any? = null,

        @field:SerializedName("services_id")
        val servicesId: Any? = null,

        @field:SerializedName("image_url")
        val imageUrl: String? = null,

        @field:SerializedName("feedback_id")
        val feedbackId: Any? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("products_groups_items_id")
        val productsGroupsItemsId: Int? = null,

        @field:SerializedName("workshops_id")
        val workshopsId: Any? = null,

        @field:SerializedName("type_status")
        val typeStatus: String? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("deleted_at")
        val deletedAt: Any? = null,

        @field:SerializedName("main_category_id")
        val mainCategoryId: Int? = null,

        @field:SerializedName("user_details_id")
        val userDetailsId: Any? = null,

        @field:SerializedName("image_name")
        val imageName: String? = null,

        @field:SerializedName("category_id")
        val categoryId: Int? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("group_id")
        val groupId: Any? = null,

        @field:SerializedName("products_groups_items_item_id")
        val productsGroupsItemsItemId: Int? = null,

        @field:SerializedName("product_sub_group_group_id")
        val productSubGroupGroupId: Any? = null,

        @field:SerializedName("users_id")
        val usersId: Int? = null,

        @field:SerializedName("product_group_group_id")
        val productGroupGroupId: Int? = null,

        @field:SerializedName("id")
        val id: Int? = null
) : Serializable
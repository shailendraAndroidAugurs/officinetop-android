package com.officinetop.officine.car_parts

interface PartCategoryInterface {

    fun onCategoryClicked(categoryID: Int)

    fun onSubCategoryClicked(subCategoryID: Int)

    fun onGroupCategoryClicked(groupCategoryID: Int)
}

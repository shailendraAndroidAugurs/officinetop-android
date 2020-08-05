package com.officinetop.officine.car_parts

import java.text.FieldPosition

interface PartCategoryInterface {

    fun onCategoryClicked(categoryID: Int)

    fun onSubCategoryClicked(subCategoryID: Int)

    fun onGroupCategoryClicked(groupCategoryID: Int)
}

package com.officinetop.officine.utils

import com.officinetop.officine.data.Models


interface OnGetLoginUserDetail {


    fun getUserDetailData(ApiRespoinse: Models.UserDetailData?, ApiRespoinsewallet: Models.UserWallet?)

}
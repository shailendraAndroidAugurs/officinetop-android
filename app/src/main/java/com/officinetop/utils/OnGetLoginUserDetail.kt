package com.officinetop.utils

import com.officinetop.data.Models


interface OnGetLoginUserDetail {


    fun getUserDetailData(ApiRespoinse: Models.UserDetailData?, ApiRespoinsewallet: Models.UserWallet?)

}
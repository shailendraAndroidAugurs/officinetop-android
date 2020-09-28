@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.officinetop.officine.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.convertToJson
import org.json.JSONArray
import org.json.JSONObject

inline fun Context.storeToken(token: String, email: String?) {
    val pref = getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
    pref.edit().putString(Constant.Key.token, token)
            .putString("email", email)
            .apply()
}

inline fun Context.getTokenFromJSON(bodyString: String): String {
    val tokenJSON = JSONObject(bodyString)
    return JSONObject(tokenJSON.getString(Constant.Fields.data)).getString("token")
}

inline fun Context.getUserIdFromJSON(bodyString: String): String {
    val userId = JSONObject(bodyString)
    return JSONObject(userId.getString(Constant.Fields.data)).getString("user_id")
}

inline fun Context.storeUserId(userId: String) {
    val user_id = getSharedPreferences(Constant.Key.userID, Context.MODE_PRIVATE)
    user_id.edit().putString(Constant.Key.userID, userId).apply()
    //Log.d("user_id: ",user_id.getString(Constant.Key.userID,""))
}

inline fun Context.getUserId(): String {
    val userId = getSharedPreferences(Constant.Key.userID, Context.MODE_PRIVATE)
    return userId.getString(Constant.Key.userID, "") ?: ""
}


inline fun Context.storeUserFCMToken(Token: String) {
    val token = getSharedPreferences(Constant.Key.Token, Context.MODE_PRIVATE)
    token.edit().putString(Constant.Key.Token, Token).apply()
    //Log.d("user_id: ",user_id.getString(Constant.Key.userID,""))
}

inline fun Context.getFCMToken(): String {
    val token = getSharedPreferences(Constant.Key.Token, Context.MODE_PRIVATE)
    return token.getString(Constant.Key.Token, "") ?: ""
}


inline fun Context.storeDeeplink(Token: String) {
    val deeplink = getSharedPreferences(Constant.Key.deeplink, Context.MODE_PRIVATE)
    deeplink.edit().putString(Constant.Key.deeplink, Token).apply()
    //Log.d("user_id: ",user_id.getString(Constant.Key.userID,""))
}

inline fun Context.getDeeplink(): String {
    val deeplink = getSharedPreferences(Constant.Key.deeplink, Context.MODE_PRIVATE)
    return deeplink.getString(Constant.Key.deeplink, "") ?: ""
}


inline fun Context.saveOrderId(orderId: String) {
    val order_id = getSharedPreferences(Constant.Key.orderId, Context.MODE_PRIVATE)
    order_id.edit().putString(Constant.Key.orderId, orderId).apply()
    Log.d("booking order id: ", order_id.getString(Constant.Key.orderId, ""))
}

inline fun Context.saveIsAvailableDataInCart(isavailable: Boolean) {
    val isdataStored = getSharedPreferences(Constant.Key.cart, Context.MODE_PRIVATE)
    isdataStored.edit().putBoolean(Constant.Key.isCartDataAvailable, isavailable).apply()
    Log.d("cart data booking: ", isavailable.toString())
}

inline fun Context.getIsAvailableDataInCart(): Boolean {
    val iscartDataStore = getSharedPreferences(Constant.Key.cart, Context.MODE_PRIVATE)
    return iscartDataStore.getBoolean(Constant.Key.isCartDataAvailable, false)
}

inline fun Context.getOrderId(): String {
    val order_id = getSharedPreferences(Constant.Key.orderId, Context.MODE_PRIVATE)
    return order_id.getString(Constant.Key.orderId, "") ?: ""
}


inline fun getStatusCode(bodyString: String): String {
    if (bodyString.isEmpty())
        return "0"

    return try {
        JSONObject(bodyString).getString("status_code")
    } catch (e: Exception) {
        "0"
    }
}

inline fun isStatusCodeValid(bodyString: String?): Boolean {
    return try {

        bodyString?.let {
            getStatusCode(it)
        } == "1"
    } catch (e: Exception) {
        false
    }
}

inline fun getMessageFromJSON(bodyString: String?): String {
    try {
        bodyString?.let { return JSONObject(it).getString("message") }
    } catch (e: Exception) {
        return "Unspecified Error"
    }
    return "Unspecified Error"
}

inline fun Context.removeToken() {
    val pref = getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
    pref.edit().remove(Constant.Key.token).apply()
}

inline fun Context.removeUserId() {
    val pref = getSharedPreferences(Constant.Key.userID, Context.MODE_PRIVATE)
    pref.edit().remove(Constant.Key.userID).apply()
}

inline fun Context.getDataSetArrayFromResponse(body: String?): JSONArray {
    try {
        body?.let { return JSONObject(body).getJSONArray(Constant.Fields.dataset) }
    } catch (e: Exception) {
    }
    return JSONArray()

}

inline fun Context.getDataFromResponse(body: String?): JSONObject {
    try {
        body?.let {
            return JSONObject(body).getJSONObject(Constant.Fields.data)
        }
    } catch (e: java.lang.Exception) {
    }
    return JSONObject()
}

inline fun Context.getStoredToken(): String? = getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).getString(Constant.Key.token, "")

inline fun Context.getStoredEmail(): String? = getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).getString("email", "")


inline fun Context.getBearerToken(): String? {

    return if (!getStoredToken().isNullOrEmpty())
        "Bearer " + getStoredToken()
    else null

}

inline fun Context.isLoggedIn(): Boolean = getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
        .contains(Constant.Key.token)


inline fun Context.saveVehicleDetailsLocally(brandId: String, brand: String, codeList: String, carModelID: String, carModel: String, carModelYear: String
                                             , carVersion: String, carDal: String, carCV: String, carKW: String, caridVeicolo: String) {

    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .edit()
            .putString(Constant.localStorageKeys.brandID, brandId)
            .putString(Constant.localStorageKeys.brand, brand)
            .putString(Constant.localStorageKeys.codeList, codeList)

            .putString(Constant.localStorageKeys.carModelID, carModelID)
            .putString(Constant.localStorageKeys.carModel, carModel)
            .putString(Constant.localStorageKeys.carModelYear, carModelYear)

            .putString(Constant.localStorageKeys.carVersion, carVersion)
            .putString(Constant.localStorageKeys.carDal, carDal)
            .putString(Constant.localStorageKeys.carCV, carCV)
            .putString(Constant.localStorageKeys.carKW, carKW)
            .putString(Constant.localStorageKeys.caridVeicolo, caridVeicolo).apply()
}


inline fun Context.getLocalSavedCarJSON(): String {
    return getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .getString(Constant.Key.saved_car_json, "")!!
}


inline fun Context.hasLocalSavedCar(): Boolean {
    return getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .contains(Constant.Key.saved_car_json)
}

inline fun Context.removeLocalSavedCar() {
    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .edit().remove(Constant.Key.saved_car_json).apply()
}

inline fun Context.saveLocalCarInJSON(json: String) {

    Log.d("Pref", "saveLocalCarInJSON: $json")

    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .edit()
            .putString(Constant.Key.saved_car_json, json).apply()
}

inline fun Context.getVehicleDetailsLocally(key: String): String {
    return getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .getString(key, "") ?: ""
}

inline fun Context.saveSelectedCar(selectedCarJson: String) {
    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .edit()
            .putString(Constant.Key.selectedCar, selectedCarJson).apply()
}

inline fun Context.getSelectedCar(): Models.MyCarDataSet? {
    val data = getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .getString(Constant.Key.selectedCar, "") ?: ""
    if (data.isNotEmpty()) {
        return Gson().fromJson(data, Models.MyCarDataSet::class.java)
    }
    return null
}

inline fun Context.saveAllAdvertisement(allAdvertisment: String) {
    getSharedPreferences(Constant.all_advertisement, Context.MODE_PRIVATE)
            .edit()
            .putString(Constant.Key.advertisements, allAdvertisment).apply()
}

inline fun Context.getAllAdvertisment(): MutableList<Models.AllAdvertisment> {
    val data = getSharedPreferences(Constant.all_advertisement, Context.MODE_PRIVATE)
            .getString(Constant.Key.advertisements, "") ?: ""
    if (data.isNotEmpty()) {
        val data_set = JSONArray(data)
        val advertisment: MutableList<Models.AllAdvertisment> = ArrayList()
        for (i in 0 until data_set.length()) {
            val dataItem = Gson().fromJson<Models.AllAdvertisment>(data_set.get(i).toString(), Models.AllAdvertisment::class.java)
            advertisment.add(dataItem)
        }
        return advertisment
    }
    return ArrayList()
}

inline fun Context.getSavedSelectedVehicleID(): String {
    return getSelectedCar()?.id ?: ""
}

inline fun Context.removeSelectedCar() {
    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .edit().remove(Constant.Key.carId).remove(Constant.Key.carSize).remove(Constant.Key.selectedCar)
            .remove(Constant.Key.carVersionID).apply()
}

inline fun Context.shouldShowAddCarInfoDialog(): Boolean {
    return getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .getBoolean(Constant.Key.show_add_car_dialog, true)
}

inline fun Context.shouldShowAddCarInfoDialog(shouldShow: Boolean) {
    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(Constant.Key.show_add_car_dialog, shouldShow)
            .apply()
}

inline fun Context.removeUserDetail() {
    removeToken()
    removeSelectedCar()
    shouldShowAddCarInfoDialog(true)
    removeUserId()
    saveIsAvailableDataInCart(false)
}

inline fun Context.setFirstRun(status: Boolean) {
    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .edit().putBoolean("first_run", status).apply()
}

inline fun Context.isFirstRun() = getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).getBoolean("first_run", true)

inline fun Context.setCartItem(cartItem: Models.CartItem) {

    val productID = (getCartItems().size + 1).toString()
    cartItem.id = productID

    val json = Gson().toJson(cartItem)
    /*Log.e("Prefff", "setCartItem: adding item = $productID ${cartItem.id} ")
    Log.e("Preffff", "setCartItem: adding item = ${getCartProducts().size} ")
    Log.e("Prefffff", "setCartItem: adding item = ${getCartItems().size}")
    Log.e("Prefffff", "setCartItem: adding item = $json")*/

    getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE)
            .edit()
            .putString(productID, json).apply()
}

inline fun Context.replaceCartProduct(productID: String, productDetailString: String) {
    getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE)
            .edit()
            .putString(productID, productDetailString).apply()
}

inline fun Context.updateCartItem(cartItem: Models.CartItem) {

    val list = getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE).all

    list.values.forEachIndexed { index, it ->

        val item = Gson().fromJson<Models.CartItem>(it.toString(), Models.CartItem::class.java)

        if (item.id == cartItem.id) {
            val itemID = list.keys.elementAt(index)
            Log.d("Utils", "updateCartItem: $item")
            getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE)
                    .edit().putString(itemID, convertToJson(cartItem)).apply()
        }
    }

    if (true)
        return


    var key = ""
    getCartProducts().forEach {
        val item = Gson().fromJson<Models.CartItem>(convertToJson(it), Models.CartItem::class.java)
        Log.d("Utils", "updateCartItem: $item")
        item?.let { it2 ->
            if (item.id == cartItem.id)
                key = it.key
        }
    }
    if (key.isNotEmpty()) {
        replaceCartProduct(key, convertToJson(cartItem))
        Log.d("Utils", "updateCartItem: item updated")
    }
}


inline fun Context.getCartProducts(): MutableMap<String, *> = getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE).all

inline fun Context.getCartItems(): MutableList<HashMap<String, Models.CartItem>> {

    val list: MutableList<HashMap<String, Models.CartItem>> = ArrayList()
    getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE).all.forEach {
        val map: HashMap<String, Models.CartItem> = HashMap()
        map[it.key] = Gson().fromJson<Models.CartItem>(it.value.toString(), Models.CartItem::class.java)
        list.add(map)
    }
    return list
}


inline fun Context.hasCartItem(cartItem: Models.CartItem) = getCartItems().any { it.containsValue(cartItem) }

inline fun Context.clearCartProducts() {
    getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE)
            .edit().clear().apply()
}

inline fun Context.removeCartItemData(productData: String) {

    val list = getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE).all

    list.values.forEachIndexed { index, it ->

        if (it.toString() == productData) {
            val itemID = list.keys.elementAt(index)
            Log.d("", "removeCartItemData: removed itemID = $itemID")
            getSharedPreferences(Constant.file_cart, Context.MODE_PRIVATE)
                    .edit().remove(itemID).apply()
        }
    }
}

inline fun Context.setTyreDetail(tyreDetail: Models.TyreDetail) {
    /* val tyreID = (getStoredTyres().size + 1 ).toString()
     tyreDetail.id = tyreID
 */
    val json = Gson().toJson(tyreDetail)
    Log.d("Inline", "setTyreDetail: adding tyre = $json")

    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).edit().putString("tyre_detail", convertToJson(tyreDetail)).apply()
}

inline fun Context.getTyreDetail(): Models.TyreDetail? {
    return try {
        Gson().fromJson(getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).getString("tyre_detail", ""), Models.TyreDetail::class.java)
    } catch (e: Exception) {
        null
    }
}

inline fun Context.clearTyreDetail() {
    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).edit().remove("tyre_detail").apply()

}


inline fun Context.setSelectedTyreDetails(tyreDetail: MeasurementDataSetItem) {
    getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).edit()
            .putString("selected_tyre", convertToJson(tyreDetail)).apply()
}

inline fun Context.getSelectedTyre(): MeasurementDataSetItem? {
    return try {
        Gson().fromJson(getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE).getString("selected_tyre", ""),
                MeasurementDataSetItem::class.java)
    } catch (e: Exception) {
        null
    }
}

inline fun Context.getStoredTyres(): MutableMap<String, *> = getSharedPreferences(Constant.file_tyre, Context.MODE_PRIVATE).all

/*
inline fun Context.setSelectedCar(): Models.MyCarDataSet? {
    val data = getSharedPreferences(Constant.file_pref, Context.MODE_PRIVATE)
            .getString(Constant.Key.selectedCar, "") ?: ""
    if (data.isNotEmpty()) {
        return Gson().fromJson(data, Models.MyCarDataSet::class.java)
    }
    return null
}*/
inline fun Context.saveMotCarKM(MotCarKm: String) {
    val MotCarKM = getSharedPreferences("MotCarKM", Context.MODE_PRIVATE)
    MotCarKM.edit().putString("MotCarKM", MotCarKm).apply()

}


inline fun Context.SaveUserWallet(WalletAmount: String) {
    val ueserWalletPref = getSharedPreferences("UserWallet", Context.MODE_PRIVATE)
    ueserWalletPref.edit().putString(Constant.Path.user_WalletAmount, WalletAmount).apply()

}


inline fun Context.getMotKm() = getSharedPreferences("MotCarKM", Context.MODE_PRIVATE).getString("MotCarKM", "")

inline fun Context.saveAddress_ContactForShipping(Address: String, AddressId: String) {
    val userData = getSharedPreferences("ShippingContact_Address", Context.MODE_PRIVATE)
    val editor = userData.edit()

    editor.putString("Address", Address)
    editor.putString("AddressId", AddressId)
    editor.apply()

}

inline fun Context.saveContact_ContactForShipping(contactNo: String, contactId: String) {
    val userData = getSharedPreferences("ShippingContact_Address", Context.MODE_PRIVATE)
    val editor = userData.edit()

    editor.putString("contactNo", contactNo)
    editor.putString("contactId", contactId)
    editor.apply()

}

inline fun Context.saveServicesType(servicestype: String) {
    val userData = getSharedPreferences("Service", Context.MODE_PRIVATE)
    val editor = userData.edit()
    editor.putString("ServicesType", servicestype)
    editor.apply()

}


inline fun Context.saveCartPricesData(TotalVat: String, TotalDiscount: String, TotalPFU: String) {
    val cartData = getSharedPreferences("Cart", Context.MODE_PRIVATE)
    val editor = cartData.edit()
    editor.putString("TotalVat", TotalVat)
    editor.putString("TotalDiscount", TotalDiscount)
    editor.putString("TotalPFU", TotalPFU)
    editor.apply()

}

inline fun Context.getServicesType(): String {
    val order_id = getSharedPreferences("Service", Context.MODE_PRIVATE)
    return order_id.getString("ServicesType", "") ?: ""
}

inline fun Context.storeLangLocale(language: String) {
    val langCode = getSharedPreferences(Constant.Key.language_locale, Context.MODE_PRIVATE)
    langCode.edit().putString(Constant.Key.language_locale, language).apply()
}

inline fun Context.getLangLocale(): String {
    val langCode = getSharedPreferences(Constant.Key.language_locale, Context.MODE_PRIVATE)
    return langCode.getString(Constant.Key.language_locale, "") ?: ""
}


inline fun Context.storeLatLong(Lat: Double, Long: Double) {
    val sharedPreferences_current_latlong = getSharedPreferences(Constant.Key.currentLatLong, Context.MODE_PRIVATE)
    sharedPreferences_current_latlong.edit().putString(Constant.Path.latitude, Lat.toString()).putString(Constant.Path.longitude, Long.toString()).apply()
}

inline fun Context.UserAddressLatLong(Lat: Double, Long: Double) {
    val sharedPreferences_UserStore_latlong = getSharedPreferences(Constant.Key.usertLatLong, Context.MODE_PRIVATE)
    sharedPreferences_UserStore_latlong.edit().putString(Constant.Path.latitude, Lat.toString()).putString(Constant.Path.longitude, Long.toString()).apply()
}

inline fun Context.getLat(): String {
    val sharedPreferences = getSharedPreferences(Constant.Key.currentLatLong, Context.MODE_PRIVATE)
    return sharedPreferences.getString(Constant.Path.latitude, "0.0") ?: "0.0"
}

inline fun Context.getLong(): String {
    val langCode = getSharedPreferences(Constant.Key.currentLatLong, Context.MODE_PRIVATE)
    return langCode.getString(Constant.Path.longitude, "0.0") ?: "0.0"
}

inline fun Context.clearStoreLatLong() {
    val langCode = getSharedPreferences(Constant.Key.usertLatLong, Context.MODE_PRIVATE)
    langCode.edit().clear().apply()
}


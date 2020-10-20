package com.officinetop.officine.utils

object Constant {

    const val GALLERY = 1
    const val CAMERA = 2
    const val RP_STORAGE = 102
    const val REQUEST_PERMISSIONS_LOCATION = 34
    val sample_array = arrayListOf("item1", "item2", "item3")
    const val file_pref = "session"
    const val file_cart = "cart"
    const val file_tyre = "tyre"
    const val user_id = "user_id"
    const val all_advertisement = "all_advertisement"

    const val baseUrl = "https://services.officinetop.com/api/"//https://officine.augurstech.com/officineTop/api/

    //  const val baseUrl = "http://18.223.98.226/api/"
    const val headerJSON = "Accept: application/json"
    const val domainBaseURL = "https://services.officinetop.com/"//https://officine.augurstech.com/officineTop/

    const val imageBaseURL = domainBaseURL + "public/carlogo/"
    const val linkedInURL = "https://api.linkedin.com/v2/"

    const val itemImageBaseURL = domainBaseURL + "public/storage/category/"
    const val profileBaseUrl = domainBaseURL + "public/storage/profile_image/"

    const val itemProductGroupImageUrl = domainBaseURL + "storage/group_image/"

    ///http:\/\/127.0.0.1:8000\/storage\/group_image
    const val pref_selected_car = "selected_car"
    const val pref_login_from = "login_from"

    const val dateformat_workshop = "yyyy-MM-dd"

    const val connection_failed_dialog = "Connection Failed, Tap to Retry"

    const val type_product = "product"
    const val type_workshop = "workshop"


    object UrlEndPoints {
        const val register = "register"
        const val login = "login"
        const val detail = "details"
        const val logout = "logout"
        const val tyre = "tyre24/get_tyre_list"
        const val resetPassword = "resetPassword"
        const val changePassword = "changePassword"
        const val manufacturer = "getCarMakers"
        const val carModels = "getCarModels"
        const val carVersion = "getCarVersion"
        const val addCar = "addCar"
        const val editCar = "editCar"
        const val searchPlate = "getSearchPlate"
        const val addCarFromPlate = "addSearchCar"
        const val myCarList = "carList"
        const val sparePartGroup = "get_spare_group"
        const val sparePartSubGroup = "get_spare_sub_group"
        const val spareN3Groups = "get_spare_n3_groups"

        const val deleteCar = "deleteCar"
        const val uploadCarImage = "upload_car_pic"
        const val deleteCarImage = "garage/remove_car_image"
        const val getCategory = "get_car_wash_category/{${Path.categoryNumber}}/{${Path.carSize}}/{${Path.userLat}}/{${Path.userLong}}/{${Path.distanceRange}}"

        const val getQuotesWorkshop = "request_quotes_workshop"
        const val getWorkshop = "get_workshop"
        const val getTyreWorkshop = "get_tyre_workshop"
        const val getTyreDetails = "get_tyre_details"
        const val getWorkshopRevision = "get_workshop_revision_facility"
        const val getCalendarPrice = "get_calendar_with_price"
        const val getRevisionCalendar = "get_next_thirty_days_min_price"
        const val getTyreCalendar = "get_next_seven_days_min_price_for_tyre"
        const val getAssemblyCalendarPrice = "assemble_workshop_calendar_with_price"
        const val getQuotesCalendarPrice = "next_seven_days_request_quotes"
        const val getCarMaintenanceCalendarPrice = "get_next_seven_days_min_price_for_car_maintenance"
        const val getSOSAppointmentCalendarPrice = "next_thirty_days_for_sos"

        const val getWorkshopPackageDetail = "get_workshop_service_package/{${Path.workshopUsersId}}/{${Path.categoryId}}/{${Path.workshopFilterSelectedDate}}/{${Path.carSize}}/{${Path.selectedCarId}}/{${Path.userid}}"
        const val getWorkshopPackageDetailNew = "get_workshop_service_package_new"
        const val getCarRevisionPackageDetail = "car_revision_workshop_package"
        const val getAssembleWorkshopPackageNew = "get_assemble_workshop_package_new"
        const val getTyreWorkshopPackageDetail = "get_tyre_workshop_package"
        const val getSOSWorkshopPackageDetail = "sos_workshop_packages"
        const val getSOSWorkshopPackageDetailEmergency = "sos_workshop_packages_for_emergency"
        const val getCarMaintenancePackageDetail = "car_maintenance_services_package"
        const val getQuotesPackageDetail = "workshop_package_for_service_quotes"
        const val getCarMaintenanceWorkshop = "car_maintenance_workshop"
        const val getWorkshopPackageDetailWithProductID = "get_workshop_service_package/{${Path.workshopUsersId}}/{${Path.categoryId}}/{${Path.workshopFilterSelectedDate}}"
        const val get_products = "get_productsnew"
        const val get_product_detail = "get_products_details"
        const val serviceBooking = "service_booking"
        const val revisionServiceBooking = "car_revision_service_booking"
        const val carWashServiceBooking = "car_wash_service_booking"
        const val serviceAssemblyBooking = "assemble_service_booking"
        const val checkServiceBooking = "check_service_booking"
        const val tyreServiceBooking = "tyre_service_booking"
        const val sosServiceBooking = "sos_service_booking"
        const val serviceBookingForCarMaintenance = "service_booking_for_car_maintenance"
        const val service_booking_request_quotes = "service_booking_request_quotes"

        const val productBrandList = "get_product_brand_list"
        const val getUserDetails = "get_user_profile"

        // search product list
        const val getSearchKeywords = "search_key/"

        const val getAllAdvertising = "get_all_advertising"
        const val bestSeller = "best_seller_product"
        const val bestSelleingProduct_home = "best_selling_product_for_home"


        const val getTyreSpecification = "get_tyre_specification"

        const val saveUserTyreDetails = "save_user_tyre_details"
        const val getUserTyreDetails = "get_user_tyre_details"

        const val revisionServices = "get_revision_services"

        const val workshopAddressInfo = "get_workshop_address_info"
        const val emergencySOServiceBooking = "emergency_sos_service_booking"
        const val wrackerServices = "get_wrackerservices"
        const val allWrackerServices = "get_all_wracker_workshop_services"
        const val saveUserLocation = "save_users_address"
        const val removeTyreDetail = "remove_tyre_detail"
        const val getSpecialCondition = "get_special_condition"
        const val serviceQuotes = "service_quotes"
        const val updatecoustmerprofile = "update_coustmer_profile"
        const val getCategoryQuotes = "get_main_category"
        const val supporttype = "support_type"
        const val getCarMaintenanceServices = "car_maintenance_services"
        const val getAllCoupons = "get_all_coupon"
        const val getOrderlist = "get_user_order_details"
        const val getTicketlist = "support_ticket"
        const val getNotificationlist = "show_notification"
        const val getMOT = "get_mot_service"
        const val getMotServiceDetail = "get_mot_service_operation"
        const val getMotCalendar = "get_next_seven_days_min_price_for_mot_service"
        const val getMotWorkshop = "get_workshop_for_mot_service"
        const val service_booking_request_mot = "mot_service_booking"
        const val getMotServicePackageDetail = "mot_services_package"
        const val addFeedback = "add_feedback"
        const val feedbackList = "get_workshop_ratings"
        const val changepassword = "update_coustmer_change_password"
        const val addUserContactList = "add_user_contact_list"
        const val updateContact = "update_coustmer_contact"
        const val updatenotification = "add_notification_detail"
        const val deleteaccountforcustomer = "delete_account_for_customer"
        const val getbrandcarmaintenance = "get_brand_car_maintenance"
        const val getPartForMotReplacement = "load_mot_spare_parts_for_replace"

        const val addUserAddress = "add_user_address_list"
        const val updateAddress = "update_coustmer_address"

        const val deleteContact = "delete_user_contact_list"

        const val deleteAddress = "delete_user_address"

        const val getCartList = "get_cart_list"
        const val sosWorkshopListForAppontment = "sos_workshop_list_for_appontment"
        const val sosWorkshopListForEmergency = "sos_workshop_list_for_emergency"
        const val SimilarPartProduct = "similar_for_parts"
        const val highRatingFeedback = "feedback_list_for_workshop_product"


        const val updateProductQuantity = "update_product_quantity"
        const val deleteCartItem = "delete_user_add_item"
        const val addToCart = "add_to_cart"
        const val updatePaymentStatus = "update_payment_status"
        const val addToFavorite = "add_user_wish_list"
        const val removeFromFavorite = "delete_user_wish_list"

        const val generatesupportticket = "generate_support_ticket"
        const val userWishList = "get_user_wish_list"
        const val getnotificationdetail = "get_notification_detail"
        const val removeCartItems = "remove_cart_items"
        const val checkUserCartItems = "check_user_cart_items"
        const val generateInvoice = "generate_invoice_xml"
        const val return_policy = "customer_return_policy"
        const val get_bonus_detail = "get_bonus_detail"
        const val loadKromedaParts = "load_car_main_kromeda_parts"
        const val customerInvoicePolicy = "customer_invoice_policy"
        const val getFAQ = "get_faq"
        const val getUserSavedAddress = "customer_address"
        const val kromedaCall = "save_maintenance_kromeda_call"
        const val motServiceSchedule = "mot_service_schedule"
        const val carMaintenanceCriteria = "car_maintenance_criteria"
        const val carMaintenanceParts = "car_maintenance_parts"


        const val carSpareKromedaCall = "save_spare_kromeda_call"
        const val carMOTKromedaCall = "save_mot_kromeda_call"

        const val addCarByPlateNumber = "addCarByPlateNumber"
        const val editTyreMeasurementdetails = "edit_user_tyre_details"
        const val searchPartAutocomplete = "searchPartsAutocomplete"
        const val selectedCar = "selectedCar"
        const val getSparePartDetail = "spare_part_detail"

    }

    object Key {
        const val id = "id"
        const val selectedCar = "selectedCar"
        const val carSize = "car_size"
        const val token = "token"
        const val userID = "userID"
        const val Token = "Token"
        const val deeplink = "deeplink"
        const val brand = "carModelName"
        const val version = "carVersion"
        const val carVersionID = "carVersionID"
        const val advertisements = "advertisements"
        const val carId = "carId"
        const val myCar = "myCar"
        const val partItemID = "partItemID"
        const val partCategory = "partCategory"
        const val searchedKeyword = "SearchedKeyword"
        const val searchedCategoryType = "SearchedCategoryType"
        const val orderId = "orderId"
        const val saved_car_json = "saved_car_json"
        const val show_add_car_dialog = "show_add_car_dialog"
        const val is_quotes = "is_quotes"

        const val is_workshop = "is_workshop"
        const val is_revision = "is_revision"
        const val is_tyre = "is_tyre"
        const val is_best_selling = "is_best_selling"
        const val is_assembly_service = "is_assembly_service"
        const val is_sos_service = "is_sos_service"
        const val is_sos_service_emergency = "is_sos_service_emergency"
        const val is_car_maintenance_service = "is_car_maintenance_service"

        const val productDetail = "productDetail"
        const val workshopCategoryDetail = "workshopCategoryDetail"
        const val workshopAssembledDetail = "workshopAssembledDetail"

        const val workshopCalendarPrice = "workshopCalendarPrice"

        const val PartIdMap = "partIdmap"
        const val MotPartIdMap = "motpartIdmap"

        const val cartItem = "carItem"
        const val mot_type = "mot_type"

        const val is_sub_group = "is_sub_group"
        const val is_sub_n3_group = "is_n3_group"
        const val is_motService = "is_motService"

        const val address = "address"

        const val zip_code = "zip_code"
        const val lat = "lat"
        const val log = "log"
        const val addresstype = "address_type"
        const val wishList = "wishList"
        const val language_locale = "language_locale"
        const val isCartDataAvailable = "isCartDataAvailable"
        const val cart = "cart"
        const val is_car_wash = "is_car_wash"
        const val currentLatLong = "currentLatLong"
        const val usertLatLong = "usertLatLong"
    }

    object Fields {
        const val brandID = "idMarca"
        const val brand = "Marca"
        const val codeList = "CodiceListino"
        const val data = "data"
        const val dataset = "data_set"
        const val authorization = "Authorization"


        //car model
        const val carModelID = "idModello"
        const val carModel = "Modello"
        const val carModelYear = "ModelloAnno"

    }

    object Path {
        const val serviceID = "service_id"

        const val PartID = "part_id"
        const val Motpartdata = "Motpart_data"

        const val categoryNumber = "categoryNumber"
        const val washServiceDetails = "washServiceDetails"
        const val revisionServiceDetails = "revisionServiceDetails"
        const val workshopUsersId = "workshop_id"
        const val workshopUserId = "workshop_user_id"
        const val serviceid = "service_id"
        const val categoryId = "category_id"
        const val categoryType = "category_type"
        const val workshopUserDaysId = "workshop_user_days_id"
        const val carSize = "car_size"
        const val userLat = "user_lat"
        const val userLong = "user_long"
        const val distanceRange = "distance_range"
        const val productId = "products_id"
        const val productid = "product_id"
        const val productqty = "product_quantity"
        const val pfutax = "pfu_tax"
        const val sellerId = "seller_id"
        const val comments = "comments"
        const val selectedCarId = "selected_car_id"
        const val userid = "user_id"
        const val couponId = "coupon_id"
        const val couponList = "couponList"
        const val quantity = "quantity"
        const val workshopId = "workshop_id"
        const val motservicetype = "mot_service_type"
        const val service_average_time = "service_average_time"

        const val maxAppointment = "max_appointment"
        const val slotEndTime = "slot_end_time"
        const val slotStartTime = "slot_start_time"

        const val specialConditionId = "special_condition_id"

        const val couponid = "coupon_id"

        const val partid = "parts"
        const val workshopWreckerId = "workshop_wrecker_id"
        const val orderId = "order_id"
        const val service_specification = "service_specification"
        const val addressId = "address_id"
        const val latitude = "latitude"
        const val longitude = "longitude"
        const val discountPrice = "discount_price"
        const val discountType = "discount_type"

        const val workshopFilterSelectedDate = "selected_date"
        const val filterRating = "rating"
        const val ratings = "ratings"
        const val filterPriceRange = "price_range"

        const val sortPrice = "price_level"
        const val mainCategoryId = "main_category_id"
        const val mainCategoryIdCar_wash = "mainCategoryIdCar_wash"
        const val serviceQuotesInsertedId = "service_quotes_inserted_id"
        const val price = "price"
        const val workShopType = "type"
        const val packageId = "package_id"

        const val productDetails = "product_details"

        const val productType = "product_type"
        const val orderid = "order_id"
        const val type = "type"
        const val model = "model"

        const val ProductOrWorkshopName = "ProductOrWorkshopName"


        const val language = "language"
        const val car_version_id = "car_version_id"
        const val version_id = "version_id"
        const val version = "version"
        const val total_price = "total_price"
        const val discount = "discount"
        const val forOrderType = "for_order_type"
        const val start_time = "start_time"
        const val end_time = "end_time"
        const val mot_id = "mot_id"
        const val motservices_time = "service_average_time"
        const val servicekm = "service_km"
        const val editStatus = "edit_status"

        const val old_password = "old_password"
        const val new_password = "new_password"
        const val confirm_password = "confirm_password"
        const val totalAmount = "totalAmount"
        const val transactionId = "transaction_id"
        const val paymentMode = "payment_mode"
        const val productOrderId = "product_order_id"
        const val message = "message"
        const val ticket_id = "ticket_id"
        const val ticket_type = "ticket_type"
        const val productName = "product_name"
        const val productDescription = "product_description"

        const val mainCategoryIdTyre = "mainCategoryIdTyre"

        const val productitem = "product_item"
        const val N3Service_id = "n3_service_id"

        const val lang = "lang"
        const val notification_setting = "notification_setting"
        const val notification_for_offer = "notification_for_offer"
        const val notification_for_revision = "notification_for_revision"
        const val contactId = "contact_id"
        const val amount = "amount"
        const val totalDiscount = "total_discount"
        const val totalItemAmount = "totalItemAmount"
        const val totalPfu = "total_pfu"
        const val totalVat = "total_vat"
        const val finalprice = "final_order_price"
        const val user_WalletAmount = "user_WalletAmount"
        const val payableAmount = "payable_amount"
        const val walletAmount = "used_wallet_amount"
        const val withoutPurchase = "without_purchase"
        const val favorite = "favorite"
        const val couponFilter = "coupon_filter"
        const val versionCriteria = "version_criteria"
        const val plateNumber = "plateNumber"
        const val scheduleId = "schedule_id"
        const val keyword = "keyword"
        const val maker = "maker"


    }

    object RC {
        const val onCarAdded = 101
        const val onCarEdited = 102
    }

    object localStorageKeys {
        const val brandID = "localCarbrandID"
        const val brand = "localCarbrand"
        const val codeList = "localCarcodeList"

        const val carModelID = "localCarcarModelID"
        const val carModel = "localCarcarModel"
        const val carModelYear = "localCarcarModelYear"

        const val carVersion = "localCarVersion"
        const val carDal = "localCarDal"
        const val carCV = "localCarCV"
        const val carKW = "localCarKW"
        const val caridVeicolo = "localCaridVeicolo"

    }
}
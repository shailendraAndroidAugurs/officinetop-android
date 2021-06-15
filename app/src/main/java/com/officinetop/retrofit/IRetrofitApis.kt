package com.officinetop.retrofit

import com.officinetop.data.*
import com.officinetop.utils.Constant
import com.officinetop.utils.Constant.defaultDistance
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.http.*

interface IRetrofitApis {

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.login)
    fun login(@Field("email") email: String,
              @Field("device_token_id") device_token_id: String,
              @Field("password") password: String): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.register)
    fun register(@Field("email") email: String,
                 @Field("password") password: String,
                 @Field("confirm_password") confirm_password: String,
                 @Field("f_name") first_name: String,
                 @Field("l_name") last_name: String,
                 @Field("mobile_number") mobileNumber: String,
                 @Field("device_token_id") device_token_id: String,
                 @Field("referral_code") referral_code: String,
                 @Field("is_social") isSocial: String,
                 @Field("licence_plate") licencePlate: String,
                 @Field("device_id") deviceId: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.register)
    fun registerWithSocial(@Field("f_name") first_name: String,
                           @Field("l_name") last_name: String,
                           @Field("email") email: String,
                           @Field("mobile_number") mobileNumber: String,
                           @Field("is_social") isSocial: String,
                           @Field("provider_name") provider_name: String,
                           @Field("device_token_id") device_token_id: String,
                           @Field("referral_code") referral_code: String,
                           @Field("provider_id") provider_id: String,
                           @Field("licence_plate") licencePlate: String,
                           @Field("device_id") deviceId: String): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.resetPassword)
    fun resetPassword(@Field("email") email: String): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.changePassword)
    fun changePassword(@Header("Authorization") authToken: String,
                       @Header("accept") accept: String = "application/json",
                       @Field("new_password") newPassword: String): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.detail)
    fun details(@Header("Authorization") authToken: String): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.logout)
    fun logout(@Header("Authorization") authToken: String): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.manufacturer)
    fun carManufacturer(): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.rim_car_brands)
    fun rimCarBrands(): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.rim_car_model_by_brandId)
    fun rimcarModels(@Query("brand_id") brandID: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.rim_car_type_by_modelId)
    fun rimcarversionlist(@Query("model_id") modelID: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.alloy_rim_attachments_by_type)
    fun rimavailablelist(@Query("car_type_id") modelID: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.alloy_rim_details)
    fun rimdetails(@Query("front_rim_id") front_rim_id: String,@Query("rear_rim_id") rear_rim_id: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.alloy_rim_search_list)
    fun rimsearch(@Query("front_width") front_width: String,
                  @Query("front_diameter") front_diameter: String,
                  @Query("front_rim_et") front_rim_et: String,
                  @Query("front_number_of_holes") front_number_of_holes: String,
                  @Query("front_bolts_distance") front_bolts_distance: String,
                  @Query("front_rear_same") front_rear_same : Int,
                  @Query("rear_width") rear_width: String,
                  @Query("rear_diameter") rear_diameter: String,
                  @Query("rear_rim_et") rear_rim_et: String,
                  @Query("rear_number_of_holes") rear_number_of_holes: String,
                  @Query("rear_bolts_distance") rear_bolts_distance: String
    ): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.carModels + "/{brandID}")
    fun carModels(@Path("brandID") brandID: String): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.carVersion + "/{modelID}/{year}")
    fun carVersion(@Path("modelID") modelID: String, @Path("year") year: String): Call<ResponseBody>


    /**Allow wheel accept 0,1 value 0 - false, 1 - true*/

    @FormUrlEncoded
    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.addCar)
    fun addCar(@Field("carMakeName") brandID: String,
               @Field("carModelName") model: String,
               @Field("carVersion") carVersion: String,
               @Field("carBody") carBody: String,
               @Field("lang") language: String = "ENG",
               @Field(Constant.Path.versionCriteria) versionCriteria: String,
               @Field(Constant.Path.scheduleId) scheduleId: String,
               @Header(Constant.Fields.authorization) authToken: String,
               @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    @FormUrlEncoded
    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.editCar)
    fun editCar(@Field("carId") carID: String,
                @Field("carMakeName") brandID: String,
                @Field("carModelName") model: String,
                @Field("carVersion") carVersion: String,
                @Field("km_of_cars") carTotalKm: String,
                @Field("km_traveled_annually") carTotalKmAnnual: String,
                @Field("revision_date_km") carRevisionDate: String,
                @Field("revesion_km") carRevisionDateOnKm: String,
                @Field("alloy_wheels") alloyWheels: String,
                @Field("fueltype") fuelType: String,
                @Field("carBody") carBody: String,
                @Header(Constant.Fields.authorization) authToken: String,
                @Field(Constant.Path.versionCriteria) versionCriteria: String,
                @Field(Constant.Path.scheduleId) scheduleId: String,

                @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    /**
    Pass "ita" as lang if italian needed, default is "eng" no need to pass it
     */
    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.searchPlate + "/{plateNumber}/{language}")
    fun searchCarPlate(@Path("plateNumber") plateNumber: String,
                       @Path("language") language: String = "eng"): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.addCarFromPlate + "/{plateNumber}/{language}")
    fun addCarFromPlate(@Path("plateNumber") plateNumber: String,
                        @Path("language") language: String = "eng",
                        @Header(Constant.Fields.authorization) authToken: String,
                        @Header("accept") accept: String = "application/json"): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.myCarList)
    fun myCars(@Header(Constant.Fields.authorization) authToken: String,
               @Header("accept") accept: String = "application/json"): Call<Models.MyCar>

    @GET(Constant.UrlEndPoints.sparePartGroup + "/{car_version_id}/{lang}")
    fun sparePartsGroup(@Path("car_version_id") carVersionId: String,
                        @Path("lang") language: String = "ENG"): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.sparePartSubGroup + "/{id}")
    fun sparePartsSubGroup(@Path("id") id: Int): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.spareN3Groups + "/{id}")
    fun spareN3Groups(@Path("id") id: Int): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.sparePartSubGroup + "/{id}")
    fun sparePartsSubGroupUpdated(@Path("id") id: Int): Call<SpareSubCategoriesResponse>

    @GET(Constant.UrlEndPoints.spareN3Groups + "/{id}")
    fun spareN3GroupsUpdated(@Path("id") id: Int): Call<SpareSubGroupCategoryResponse>

    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.getSearchKeywords)
    fun getSearchKeyWords(@Header(Constant.Fields.authorization) authToken: String,
                          @Header("accept") accept: String = "application/json"): Call<SearchKeywordResponse>

    @Headers(Constant.headerJSON)
    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.deleteCar)
    fun deleteCar(@Field("carId") carID: String, @Header(Constant.Fields.authorization) authToken: String,
                  @Header("accept") accept: String = "application/json"): Call<Models.MyCar>


    @Headers(Constant.headerJSON)
    @GET("clientAwareMemberHandles?q=members&projection=(elements*(primary,type,handle~))")
    fun linkedInGetEmail(@Header(Constant.Fields.authorization) accessToken: String,
                         @Header("cache-control") cacheControl: String = "no-cache",
                         @Header("X-Restli-Protocol-Version") protocol: String = "2.0.0"): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @GET("me")
    fun linkedInGetDetail(@Header(Constant.Fields.authorization) accessToken: String,
                          @Header("cache-control") cacheControl: String = "no-cache",
                          @Header("X-Restli-Protocol-Version") protocol: String = "2.0.0"): Call<ResponseBody>


    @Multipart
    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.uploadCarImage)
    fun uploadCarImage(@Part("users_details_id") id: RequestBody,
                       @Part image: MultipartBody.Part,
                       @Part("default_image") default_image: RequestBody,
                       @Header(Constant.Fields.authorization) authToken: String,
                       @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.deleteCarImage)
    fun deleteCarImage(@Query("image_row_id") image_row_id: String,
                       @Header(Constant.Fields.authorization) authToken: String,
                       @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getCategory)
    fun getServiceCategory(@Path(Constant.Path.categoryNumber) categoryNumber: Int = 2,
                           @Path(Constant.Path.carSize) carSize: String? = "2",
                           @Path(Constant.Path.userLat) user_lat: String? = "0",
                           @Path(Constant.Path.userLong) user_long: String = "0",
                           @Path(Constant.Path.distanceRange) distance_range: String? = defaultDistance
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getWorkshop)

    fun getWorkshops(@Query(Constant.Path.categoryId) categoryId: Int,
                     @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                     @Query(Constant.Path.filterRating) rating: String,
                     @Query(Constant.Path.filterPriceRange) priceRange: String,
                     @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                     @Query(Constant.Path.workShopType) workShopType: Int,
                     @Query(Constant.Path.carSize) carSize: String,
                     @Query("user_id") user_id: String,
                     @Query(Constant.Path.version_id) version_id: String,
                     @Query(Constant.Path.selectedCarId) selectedCarId: String,
                     @Query("user_lat") user_lat: String,
                     @Query("user_long") user_long: String,
                     @Query("distance_range") distance_range: String,
                     @Query(Constant.Path.favorite) favorite: String,
                     @Query(Constant.Path.couponFilter) couponfilter: String,
                     @Query(Constant.Path.mainCategoryId) mainCategoryId:String,
                     @Query(Constant.Path.sort_by_distance) sort_by_distance: Int
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getWorkshopRevision)
    fun getRevisionWorkshop(@Query(Constant.Path.serviceID) serviceId: Int,
                            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                            @Query(Constant.Path.filterRating) rating: String,
                            @Query(Constant.Path.filterPriceRange) priceRange: String,
                            @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                            @Query("user_id") user_id: String,
                            @Query(Constant.Path.selectedCarId) selectedCarId: String,
                            @Query(Constant.Path.version_id) version_id: String,

                            @Query("user_lat") user_lat: String,
                            @Query("user_long") user_long: String,
                            @Query("distance_range") distance_range: String,
                            @Query(Constant.Path.favorite) favorite: String,
                            @Query(Constant.Path.couponFilter) couponfilter: String,
                            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                            @Query(Constant.Path.sort_by_distance) sort_by_distance: Int
    ): Call<ResponseBody>



    @GET(Constant.UrlEndPoints.alloy_rim_workshop_list)
    fun getRimWorkshop(@Query(Constant.Path.serviceID) serviceId: Int,
                            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                            @Query(Constant.Path.filterRating) rating: String,
                            @Query(Constant.Path.filterPriceRange) priceRange: String,
                            @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                            @Query("user_id") user_id: String,
                            @Query(Constant.Path.selectedCarId) selectedCarId: String,
                            @Query(Constant.Path.version_id) version_id: String,

                            @Query("user_lat") user_lat: String,
                            @Query("user_long") user_long: String,
                            @Query("distance_range") distance_range: String,
                            @Query(Constant.Path.favorite) favorite: String,
                            @Query(Constant.Path.couponFilter) couponfilter: String,
                            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                            @Query(Constant.Path.sort_by_distance) sort_by_distance: Int
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.assembleServiceWorkshop)
    fun getAssemblyWorkshops(@Query(Constant.Path.productId) categoryId: Int,
                             @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                             @Query(Constant.Path.filterRating) rating: String,
                             @Query(Constant.Path.filterPriceRange) priceRange: String,
                             @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                             @Query(Constant.Path.workShopType) workShopType: Int,
                             @Query(Constant.Path.carSize) carSize: String,
                             @Query("user_id") user_id: String,
                             @Query(Constant.Path.version_id) version_id: String,
                             @Query(Constant.Path.selectedCarId) selectedCarId: String,
                             @Query(Constant.Path.productqty) productqty: String,
                             @Query("user_lat") user_lat: String,
                             @Query("user_long") user_long: String,
                             @Query("distance_range") distance_range: String,
                             @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                             @Query(Constant.Path.servicesAverageTime) servicesAverageTime: String,
                             @Query(Constant.Path.serviceid) serviceId: String,
                             @Query(Constant.Path.sort_by_distance) sort_by_distance: Int


    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.alloy_rim_calender)
    fun getRimCalender(@Query(Constant.Path.serviceID) serviceId: Int,
                            @Query(Constant.Path.version_id) versionId: String,
                            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                            @Query("user_lat") user_lat: String,
                            @Query("user_long") user_long: String,
                            @Query("distance_range") distance_range: String,
                            @Query("main_category_id") mainCategoryId: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getRevisionCalendar)
    fun getRevisionCalendar(@Query(Constant.Path.serviceID) serviceId: Int,
                            @Query(Constant.Path.version_id) versionId: String,
                            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                            @Query("user_lat") user_lat: String,
                            @Query("user_long") user_long: String,
                            @Query("distance_range") distance_range: String,
                            @Query("main_category_id") mainCategoryId: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getTyreCalendar)
    fun getTyreCalendar(@Query(Constant.Path.productId) serviceId: Int,
                        @Query(Constant.Path.version_id) versionId: String,
                        @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                        @Query(Constant.Path.productqty) productqty: String,
                        @Query("user_lat") user_lat: String,
                        @Query("user_long") user_long: String,
                        @Query("distance_range") distance_range: String,
                        @Query("main_category_id") mainCategoryId: String,
                        @Query(Constant.Path.service_average_time) service_average_time: String,
                        @Query(Constant.Path.serviceID) serviceID: String


    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getCalendarPrice)
    fun getCalendarMinPrice(@Query(Constant.Path.categoryId) categoryId: Int,
                            @Query(Constant.Path.productId) productId: Int,
                            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                            @Query(Constant.Path.filterRating) rating: String,
                            @Query(Constant.Path.filterPriceRange) priceRange: String,
                            @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                            @Query(Constant.Path.workShopType) workShopType: Int,
                            @Query(Constant.Path.carSize) carSize: String,
                            @Query("user_lat") user_lat: String,
                            @Query("user_long") user_long: String,
                            @Query("distance_range") distance_range: String,
                            @Query("main_category_id") mainCategoryId: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getMotCalendar)
    fun getMotCalendar(@Query(Constant.Path.serviceID) serviceId: Int,
                       @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                       @Query(Constant.Path.type) type: String,
                       @Query("user_lat") user_lat: String,
                       @Query("user_long") user_long: String,
                       @Query("distance_range") distance_range: String,
                       @Query(Constant.Path.version) version_id: String,
                       @Query(Constant.Path.userid) userId: String,
                       @Query(Constant.Path.service_average_time) service_average_time: String,
                       @Query(Constant.Path.mainCategoryId) mainCategoryId: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getAssemblyCalendarPrice)
    fun getAssemblyCalendarPrice(@Query(Constant.Path.categoryId) categoryId: Int,
                                 @Query(Constant.Path.productId) productId: Int,
                                 @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                                 @Query(Constant.Path.filterRating) rating: String,
                                 @Query(Constant.Path.filterPriceRange) priceRange: String,
                                 @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                                 @Query(Constant.Path.workShopType) workShopType: Int,
                                 @Query(Constant.Path.carSize) carSize: String,
                                 @Query(Constant.Path.version_id) version_id: String,
                                 @Query(Constant.Path.productqty) productqty: String,
                                 @Query(Constant.Path.userLat) user_lat: String,
                                 @Query(Constant.Path.userLong) user_long: String,
                                 @Query(Constant.Path.distanceRange) distance_range: String,
                                 @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                                 @Query(Constant.Path.servicesAverageTime) servicesAverageTime: String,
                                 @Query(Constant.Path.serviceid) serviceId: String


    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getWorkshopPackageDetailNew)
    fun getWorkshopPackageDetailNew(@Query(Constant.Path.workshopUsersId) workshopUserId: Int,
                                    @Query(Constant.Path.categoryId) categoryId: Int,
                                    @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                                    @Query(Constant.Path.carSize) carSize: String? = "1",
                                    @Query(Constant.Path.userid) userid: String? = "1",
                                    @Query(Constant.Path.selectedCarId) selectedCarId: String,
                                    @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                                    @Query(Constant.Path.version_id) versionId: String,
                                    @Query(Constant.Path.service_average_time) averagetime: String,
                                    @Query(Constant.Path.max_appointment) maxappointment: String,
                                    @Query(Constant.Path.hourly_rate) hourlyrate: String,
                                    @Query(Constant.Path.maker) maker: String,
                                    @Query(Constant.Path.model) model: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getCarRevisionPackageDetail)
    fun getCarRevisionPackageDetail(
            @Query(Constant.Path.workshopUsersId) workshop_id: Int,
            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
            @Query(Constant.Path.version_id) version_id: String,
            @Query(Constant.Path.serviceID) service_id: String,
            @Query(Constant.Path.mainCategoryId) main_category_id: String,
            @Query(Constant.Path.userid) userid: String,
            @Query(Constant.Path.service_average_time) service_average_time: String,

            @Header(Constant.Fields.authorization) authToken: String


    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getAssembleWorkshopPackageNew)
    fun getAssemblyWorkshopPackageDetail(@Query(Constant.Path.workshopUsersId) workshopUserId: Int,
                                         @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                                         @Query(Constant.Path.productid) productId: String,
                                         @Query(Constant.Path.selectedCarId) selectedCarId: String,
                                         @Query(Constant.Path.userid) userid: String,
                                         @Query(Constant.Path.serviceid) categoryId: String,
                                         @Query(Constant.Path.service_average_time) service_average_time: String,
                                         @Query(Constant.Path.version_id) version_id: String,
                                         @Query(Constant.Path.mainCategoryId) mainCategoryId: String


    ): Call<ResponseBody>


    /** Brands must be comma seperated*/
    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.get_products)
    fun getSpareParts(@Field(Constant.Path.car_version_id) carVersionID: String,
                      @Field(Constant.Path.categoryId) categoryId: Int,
                      @Field(Constant.Path.filterPriceRange) priceRange: String,
                      @Field(Constant.Path.sortPrice) priceSortLevel: Int,
                      @Field(Constant.Path.carSize) carSize: String,
                      @Field("language") language: String = "ENG",
                      @Field("brand") brands: String? = "",
                      @Field("category_type") categoryType: String?,
                      @Field("product_keyword") productKeyword: String? = "",
                      @Field("product_type") product_type: String,
                      @Field("user_id") user_id: String,
                      @Field("rating_level") ratingLevel: String,
                      @Field("rating_range") ratingRange: String,
                      @Field("favorite") favorite: String,
                      @Field("coupon") coupon: String,
                      @Field("model") model: String,
                      @Field("limit") limit: String


    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.serviceBooking)
    fun serviceBooking(@Field("category_id") categoryID: String,
                       @Field("package_id") packageID: Int,
                       @Field("start_time") startTime: String,
                       @Field("end_time") endTime: String,
                       @Field("price") price: String,
                       @Field(Constant.Path.carSize) carSize: String,
                       @Field("selected_date") selectedDate: String,
                       @Field("version_id") versionId: String,
                       @Field(Constant.Path.orderId) orderId: String,
                       @Header(Constant.Fields.authorization) authToken: String,
                       @Field(Constant.Path.selectedCarId) selectedCarId: String,
                       @Field(Constant.Path.workshopId) workshopId: Int,
                       @Field(Constant.Path.couponId) couponId: String,
                       @Field(Constant.Path.specialConditionId) specialConditionId: String,

                       @Field("temp_slot_id") temp_slot_id: String,
                       @Field(Constant.Path.discountType) discountType: String,
                       @Header("accept") accept: String = "application/json"): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.revisionServiceBooking)
    fun revisionServiceBooking(@Field("service_id") serviceID: String,
                               @Field("package_id") packageID: Int,
                               @Field("start_time") startTime: String,
                               @Field("selected_date") selectedDate: String,
                               @Field("price") price: String,
                               @Field("main_category_id") mainCategoryId: String,
                               @Header(Constant.Fields.authorization) authToken: String,
                               @Field("special_condition_id") specialId: String? = "",
                               @Field("version_id") versionId: String,
                               @Field(Constant.Path.workshopId) workshopId: Int,
                               @Field(Constant.Path.selectedCarId) selectedCarId: String,
                               @Field(Constant.Path.couponId) couponId: String,
                               @Field(Constant.Path.orderId) orderId: String,
                               @Field(Constant.Path.end_time) endTime: String,
                               @Field(Constant.Path.discountPrice) discountPrice: String,
                               @Field(Constant.Path.discountType) discountType: String,
                               @Field("temp_slot_id") temp_slot_id: String,
                               @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.carWashServiceBooking)
    fun carWashServiceBooking(
            @Field(Constant.Path.packageId) packageID: Int,
            @Field(Constant.Path.start_time) startTime: String,
            @Field(Constant.Path.end_time) endTime: String,
            @Field(Constant.Path.price) price: String,
            @Field(Constant.Path.workshopFilterSelectedDate) selectedDate: String,
            @Field(Constant.Path.carSize) carSize: String,
            @Field(Constant.Path.categoryId) categoryId: String,
            @Field(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Field(Constant.Path.selectedCarId) selectedCarId: String,
            @Field(Constant.Path.couponId) couponId: String,
            @Field(Constant.Path.orderId) orderId: String,
            @Field("version_id") versionId: String,
            @Field("temp_slot_id") temp_slot_id: String,
            @Field(Constant.Path.specialConditionId) specialConditionId: String,
            @Field(Constant.Path.workshopId) workshopId: String,
            @Field(Constant.Path.discountType) discountType: String,


            @Header("Authorization") authToken: String,
            @Header("accept") accept: String = "application/json"): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.serviceAssemblyBooking)
    fun serviceAssemblyBooking(@Field("product_id") productId: String,
                               @Field("package_id") packageID: Int,
                               @Field("start_time") startTime: String,
                               @Field("end_time") endTime: String,
                               @Field("price") price: String,
                               @Field("selected_date") selectedDate: String,
                               @Field("main_category_id") mainCategoryID: String,
                               @Header(Constant.Fields.authorization) authToken: String,
                               @Field("special_id") specialId: String? = "",
                               @Field("version_id") versionId: String,
                               @Field(Constant.Path.selectedCarId) selectedCarId: String,
                               @Field(Constant.Path.couponId) couponId: String,
                               @Field(Constant.Path.quantity) quantity: String,
                               @Field(Constant.Path.orderId) orderId: String,
                               @Field(Constant.Path.productName) product_name: String,
                               @Field(Constant.Path.productDescription) product_description: String,
                               @Field(Constant.Path.discount) discount: String,
                               @Field(Constant.Path.pfutax) pfuTax: String,
                               @Field("product_total_price") productTotalPrice: String,
                               @Field("product_price") productPrice: String,
                               @Field("seller_id") seller_id: String,
                               @Field("products_coupon_id") products_coupon_id: String,
                               @Field(Constant.Path.specialConditionId) specialConditionId: String,
                               @Field("temp_slot_id") temp_slot_id: String,
                               @Field(Constant.Path.workshopId) workshopId: String,
                               @Field(Constant.Path.discountType) discountType: String,
                               @Field(Constant.Path.serviceid) serviceid: String,
                               @Header("accept") accept: String = "application/json"): Call<ResponseBody>

    @GET("selected_car/{selected_car_id}")
    fun selectCar(@Path("selected_car_id") car_id: String,
                  @Header(Constant.Fields.authorization) authToken: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.productBrandList)
    fun getProductBrandList(@Query("type") type: String): Call<ResponseBody>

    @POST("search_key/{query}")
    fun addSearchQuery(@Path("query") query: String,
                       @Header(Constant.Fields.authorization) authToken: String): Call<ResponseBody>

    @DELETE("clear_key")
    fun clearSearchKeyWords(@Header(Constant.Fields.authorization) authToken: String): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getTyreSpecification)
    fun getTyreSpecification(
            @Query(Constant.Path.selectedCarId) selectedCarId: String,
            @Query("search_string") search_string: String,
            @Query(Constant.Path.userid) userId: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.bestSeller)
    fun bestSeller(@Query(Constant.Path.filterPriceRange) priceRange: String,
                   @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                   @Query(Constant.Path.version_id) version_id: String,
                   @Query("brand") brands: String? = "",
                   @Query(Constant.Path.userid) userid: String? = ""


                   ): Call<ResponseBody>





    @GET(Constant.UrlEndPoints.bestSelleingProduct_home)
    fun bestSellingProductHome(@Query(Constant.Path.filterPriceRange) priceRange: String,
                               @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                               @Query(Constant.Path.version_id) version_id: String,
                               @Query("brand") brands: String? = ""): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getAllAdvertising)
    fun getAllAdvertising(
            @Header(Constant.Fields.authorization) authToken: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.saveUserTyreDetails)
    fun saveUserTyreDetails(@Field("user_id") user_id: String,
                            @Field("vehicle_type") vehicle_type: String,
                            @Field("season") season: String,
                            @Field("width") width: Int,
                            @Field("speedindex") speed_index: String,
                            @Field("run_flat") run_flat: Int,
                            @Field("reinforced") reinforced: Int,
                            @Field("execute") execute: Int,
                            @Field("aspect_ratio") aspectRatio: String,
                            @Field("rim_diameter") rimDiameter: String,
                            @Field("car_version_id") carVersionId: String,
                            @Field("load_index") load_index: String


    ): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.getUserTyreDetails)
    fun getUserTyreDetails(@Query("user_id") user_id: String,
                           @Query("car_version_id") car_version_id: String): Call<UserTyreMeasurementResponse>

    @GET(Constant.UrlEndPoints.revisionServices)
    fun getRevisionServices(@Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                            @Query(Constant.Path.version_id) version_id: String,
                            @Query(Constant.Path.userLat) user_lat: String,
                            @Query(Constant.Path.userLong) user_long: String,
                            @Query(Constant.Path.distanceRange) distance_range: String): Call<RevisionServicesResponse>


    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.tyre)
    fun tyreList(@Query("vehicle_tyre_type") tyre_Type: String,
                 @Query("search_string") search_string: String,
                 @Query("brand") brand: String,
                 @Query("season_type") season_type: String,
                 @Query("speed_index") speed_index: String,
                 @Query("limit") limit: String? = "",
                 @Query("favourite") favourite: String? = "",
                 @Query("offer_coupon") offer_coupon: String? = "",
                 @Query("rein_forced") rein_forced: String? = "",
                 @Query("run_flat") run_flat: String,
                 @Query("price_level") price_level: String,
                 @Query("price_range") price_range: String,
            /*     @Query("AlphabeticalOrder") AlphabeticalOrder: String,*/

                 @Query("rating") rating: String,
                 @Query("product_type") product_type: String,
                 @Query("user_id") user_id: String,
                 @Query("load_index") load_index: String



    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getTyreWorkshop)

    fun getTyreWorkshops(@Query(Constant.Path.productId) categoryId: Int,
                         @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                         @Query(Constant.Path.filterRating) rating: String,
                         @Query(Constant.Path.filterPriceRange) priceRange: String,
                         @Query(Constant.Path.sortPrice) priceSortLevel: Int,
                         @Query("user_id") user_id: String,
                         @Query(Constant.Path.version_id) versionId: String,
                         @Query("user_lat") user_lat: String,
                         @Query("user_long") user_long: String,
                         @Query("distance_range") distance_range: String,
                         @Query(Constant.Path.productqty) productqty: String,
                         @Query(Constant.Path.favorite) favorite: String,
                         @Query(Constant.Path.couponFilter) couponfilter: String,
                         @Query(Constant.Path.service_average_time) service_average_time: String,
                         @Query(Constant.Path.serviceID) serviceID: String,
                         @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                         @Query(Constant.Path.sort_by_distance) sort_by_distance: Int


    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getMotWorkshop)
    fun getMotWorkshops(@Query(Constant.Path.serviceID) serviceId: Int,
                        @Query(Constant.Path.type) type: String,
                        @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                        @Query(Constant.Path.filterRating) filterRating: String,
                        @Query(Constant.Path.filterPriceRange) filterPriceRange: String,
                        @Query(Constant.Path.sortPrice) sortPrice: Int,
                        @Query("user_id") user_id: String,
                        @Query(Constant.Path.version) selectedCarId: String?,
                        @Query(Constant.Path.motservices_time) motservicestime: String?,
                        @Query("user_lat") user_lat: String,
                        @Query("user_long") user_long: String,
                        @Query("distance_range") distance_range: String,
                        @Query(Constant.Path.favorite) favorite: String,
                        @Query(Constant.Path.couponFilter) couponfilter: String,
                        @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                        @Query(Constant.Path.sort_by_distance) sort_by_distance: Int
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getTyreWorkshopPackageDetail)

    fun getTyrePackageDetail(@Query(Constant.Path.workshopId) workshopId: Int,
                             @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                             @Query(Constant.Path.serviceID) serviceID: Int,
                             @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                             @Query(Constant.Path.productId) productId: String,
                             @Query(Constant.Path.quantity) quantity: String,
                             @Query(Constant.Path.couponId) couponId: String,
                             @Query(Constant.user_id) userId: String,
                             @Query(Constant.Path.version_id) versionId: String,
                             @Query(Constant.Path.productqty) productqty: String,
                             @Query(Constant.Path.service_average_time) service_average_time: String


    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getSOSWorkshopPackageDetail)
    fun getSOSPackageDetail(@Query(Constant.Path.workshopId) workshopUserId: String,
                            @Query(Constant.Path.serviceID) serviceID: String,
                            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                            @Query(Constant.Path.latitude) latitude: String,
                            @Query(Constant.Path.longitude) longitude: String,
                            @Query(Constant.Path.selectedCarId) selectedCarId: String?,
                            @Query(Constant.Path.addressId) addressId: String?,
                            @Query(Constant.Path.workshopWreckerId) workshopWreckerId: String,
                            @Query(Constant.Path.userid) userid: String,
                            @Query(Constant.Path.version_id) version_id: String,
                            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                            @Query(Constant.Path.wrecker_service_type) wrecker_service_type: String,
                            @Query(Constant.Path.service_average_time) service_average_time: String,
                            @Query(Constant.Path.servicesPrice) servicesPrice: String,
                            @Query(Constant.Path.max_appointment) max_appointment: String,

            @Query(Constant.Path.hourly_rate) hourlyrate: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getSOSWorkshopPackageDetailEmergency)
    fun getSOSPackageDetailEmergency(@Query(Constant.Path.workshopId) workshopUserId: String,
                                     @Query(Constant.Path.serviceID) serviceID: String,
                                     @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                                     @Query(Constant.Path.latitude) latitude: String,
                                     @Query(Constant.Path.longitude) longitude: String,
                                     @Query(Constant.Path.selectedCarId) selectedCarId: String?,
                                     @Query(Constant.Path.addressId) addressId: String,
                                     @Query(Constant.Path.start_time) startTime: String,
                                     @Query(Constant.Path.userid) userid: String,
                                     @Query(Constant.Path.wrecker_service_type) wrecker_service_type: String,
                                     @Query(Constant.Path.version_id) version_id: String,
                                     @Query(Constant.Path.service_average_time) service_average_time: String,
                                     @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
                                     @Query(Constant.Path.servicesPrice) servicesPrice: String



    ): Call<ResponseBody>

    @POST(Constant.UrlEndPoints.getCarMaintenancePackageDetail)
    fun getCarMaintenacePackageDetail(
            @Query(Constant.Path.version_id) versionId: String,
            @Query(Constant.Path.workshopFilterSelectedDate) selected_date: String,
            @Query(Constant.Path.userid) userid: String,
            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Query(Constant.Path.workshopId) workshopUsersId: String,
            @Query(Constant.Path.services) services: JSONArray,
            @Query(Constant.Path.servicesPrice) services_price: String,
            @Query(Constant.Path.service_average_time) service_average_time: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getQuotesPackageDetail)
    fun getServiceQuotesPackageDetail(
            @Query("service_id") categoryType: String,
            @Query(Constant.Path.workshopUserDaysId) workshopUserDaysId: String,
            @Query(Constant.Path.workshopFilterSelectedDate) selected_date: String,
            @Query(Constant.Path.serviceQuotesInsertedId) serviceQuotesInsertedId: String,
            @Query(Constant.Path.selectedCarId) selectedCarId: String,
            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Query(Constant.Path.workshopId) workshopUsersId: String,
            @Query(Constant.Path.couponId) couponId: String,
            @Query(Constant.Path.userid) userid: String,
            @Query(Constant.Path.version_id) versionId: String,
            @Query(Constant.Path.service_average_time) ServicesAvarageTime: String,
            @Query(Constant.Path.maxAppointment) maxAppointment: String


    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.tyreServiceBooking)
    fun tyreServiceBooking(
            @Field("products_id") productId: String,
            @Field(Constant.Path.quantity) quantity: String,
            @Field("package_id") packageID: Int,
            @Field("start_time") startTime: String,
            @Field("end_time") endTime: String,
            @Field("selected_date") selectedDate: String,
            @Field("price") price: String,
            @Field("category_id") categoryId: String,
            @Header(Constant.Fields.authorization) authToken: String,
            @Field("version_id") versionId: String,
            @Field(Constant.Path.selectedCarId) selectedCarId: String,
            @Field(Constant.Path.couponId) couponId: String,
            @Field(Constant.Path.orderId) orderId: String,
            @Field("discount") discount: String,
            @Field("pfu_tax") pfuTax: String,
            @Field("product_total_price") totalPrice: String,
            @Field("product_price") productPrice: String,
            @Field(Constant.Path.productName) product_name: String,
            @Field(Constant.Path.productDescription) product_description: String,
            @Field(Constant.Path.serviceID) service_id: String,
            @Field("products_coupon_id") products_coupon_id: String,
            @Field("seller_id") seller_id: String,
            @Field("main_category_id") maincategoryId: String,
            @Field(Constant.Path.specialConditionId) specialConditionId: String,
            @Field(Constant.Path.slotStartTime) slotStartTime: String,
            @Field(Constant.Path.slotEndTime) slotEndTime: String,
            @Field(Constant.Path.maxAppointment) maxAppointment: String,
            @Field(Constant.Path.service_average_time) service_average_time: String,
            @Field(Constant.Path.workshopUsersId) workshopUsersId: String,
            @Field(Constant.Path.discountPrice) discountPrice: String,
            @Field(Constant.Path.slot_id) slot_id: String,
            @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.sosServiceBooking)
    fun sosServiceBooking(
            @Field(Constant.Path.packageId) packageId: String,
            @Field(Constant.Path.workshopId) workshopId: String,
            @Field(Constant.Path.start_time) startTime: String,
            @Field(Constant.Path.end_time) endTime: String,
            @Field(Constant.Path.workshopFilterSelectedDate) selectedDate: String,
            @Field(Constant.Path.latitude) latitude: String,
            @Field(Constant.Path.longitude) longitude: String,
            @Field(Constant.Path.selectedCarId) selectedCarId: Int?,
            @Field(Constant.Path.price) price: String,
            @Field(Constant.Path.addressId) addressId: String?,
            @Field(Constant.Path.serviceID) serviceId: String,
            @Field(Constant.Path.workshopWreckerId) workshopWrackerId: String?,
            @Field(Constant.Path.orderId) orderId: String,
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.couponId) couponId: String,
            @Field(Constant.Path.specialConditionId) specialConditionId: String,
            @Field("temp_slot_id") temp_slot_id: String,
            @Field(Constant.Path.discountType) discountType: String,
            @Field(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Field(Constant.Path.version_id) versionId: String,
            @Field(Constant.Path.service_average_time) service_average_time: String,
            @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.serviceBookingForCarMaintenance)
    fun serviceBookingCarMaintenance(
            @Field(Constant.Path.start_time) startTime: String,
            @Field(Constant.Path.end_time) endTime: String,
            @Field(Constant.Path.workshopFilterSelectedDate) selectedDate: String,
            @Field(Constant.Path.price) price: String,
            @Field(Constant.Path.version_id) versionId: String,
            @Field(Constant.Path.serviceID) serviceId: String,
            @Field(Constant.Path.workshopId) workshopId: Int?,
            @Field(Constant.Path.packageId) packageID: Int?,
            @Field(Constant.Path.orderId) orderId: String,
            @Field(Constant.Path.service_specification) serviceSpecification: JSONArray,
            @Field(Constant.Path.selectedCarId) selectedCarId: String?,
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.couponId) couponId: String,
            @Field(Constant.Path.specialConditionId) specialConditionId: String,
            @Field("temp_slot_id") temp_slot_id: String,
            @Field(Constant.Path.discountType) discountType: String,
            @Field(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Field(Constant.Path.services) services: JSONArray,
            @Header("accept") accept: String = "application/json"): Call<ResponseBody>

    @Multipart
    @POST(Constant.UrlEndPoints.service_booking_request_quotes)
    fun serviceQuotesBooking(
            @Part("service_id") categoryType: RequestBody,
            @Part(Constant.Path.workshopFilterSelectedDate) selectedDate: RequestBody,
            @Part(Constant.Path.serviceQuotesInsertedId) serviceQuotesInsertedId: RequestBody,
            @Part(Constant.Path.mainCategoryId) mainCategoryId: RequestBody,
            @Part(Constant.Path.selectedCarId) selectedCarId: RequestBody?,
            @Part(Constant.Path.workshopId) workshopId: RequestBody?,
            @Part(Constant.Path.start_time) startTime: RequestBody,
            @Part(Constant.Path.packageId) packageId: RequestBody,
            @Part(Constant.Path.orderId) orderId: RequestBody,
            @Header(Constant.Fields.authorization) authToken: String,
            @Part(Constant.Path.couponId) couponId: RequestBody,
            @Part(Constant.Path.end_time) endTime: RequestBody,
            @Part(Constant.Path.version_id) versionId: RequestBody,
            @Part(Constant.Path.specialConditionId) specialConditionId: RequestBody,
            @Part("temp_slot_id") temp_slot_id: RequestBody,
            @Part(Constant.Path.discountType) discountType: RequestBody,
            @Part("text") description: RequestBody,
            @Part images: List<MultipartBody.Part?>,
            @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.emergencySOServiceBooking)
    fun emergencySosServiceBooking(
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.packageId) packageID: String,
            @Field(Constant.Path.workshopId) workshopId: String,
            @Field(Constant.Path.start_time) startTime: String,
            @Field(Constant.Path.end_time) endTime: String,
            @Field(Constant.Path.workshopFilterSelectedDate) selectedDate: String,
            @Field(Constant.Path.latitude) latitude: String,
            @Field(Constant.Path.longitude) longitude: String,
            @Field(Constant.Path.selectedCarId) selectedCarId: String,
            @Field(Constant.Path.price) price: String,
            @Field(Constant.Path.addressId) addressId: String,
            @Field(Constant.Path.serviceID) serviceId: String,
            @Field(Constant.Path.orderId) orderId: String,
            @Field(Constant.Path.specialConditionId) specialConditionId: String,
            @Field("temp_slot_id") temp_slot_id: String,
            @Field(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Field(Constant.Path.service_average_time) service_average_time: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.wrackerServices)
    fun getWrackerServices(
            @Query("address_id") id: String,
            @Query("workshop_id") workshopUserId: String,
            @Query("selected_car_id") selectedCarId: Int?): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.allWrackerServices)
    fun getAllWrackerServices(
            @Query("latitude") latitude: String,
            @Query("longitude") longitude: String,
            @Query("selected_date") selectedDate: String,
            @Query("type") type: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getTyreDetails)
    fun getTyreDetails(
            @Query("seller_id") sellerId: String,
            @Query("tyre_id") tyreId: String,
            @Query("selected_date") selectedDate: String,
            @Query("version_id") versionId: String,
            @Query(Constant.Path.userLat) user_lat: String,
            @Query(Constant.Path.userLong) user_long: String,
            @Query(Constant.Path.distanceRange) distance_range: String,
            @Query(Constant.Path.userid) userId: String
    ): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @GET(Constant.UrlEndPoints.getUserDetails)
    fun getUserDetails(@Header("Authorization") authToken: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.saveUserLocation)
    fun saveUserLocation(
            @Header(Constant.Fields.authorization) authToken: String,
            @Field("latitude") latitude: String?,
            @Field("longitude") longitude: String?,
            @Field("address") address: String?,
            @Field("address_1") address1: String?,
            @Field("address_2") address2: String?,
            @Field("address_3") address3: String,
            @Field("landmark") landmark: String?,
            @Field("zip_code") zipCode: String?,
            @Field("country_name") countryName: String?,
            @Field("state_name") stateName: String?,
            @Field("city_name") cityName: String?
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.removeTyreDetail + "/{user_id}")
    fun removeTyreDetail(
            @Header(Constant.Fields.authorization) authToken: String,
            @Path("user_id") id: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getSpecialCondition)
    fun getSpecialCondition(
            @Query("main_category_id") mainCategoryId: String,
            @Query("workshop_id") workshopId: Int
    ): Call<ResponseBody>


    @Multipart
    @POST(Constant.UrlEndPoints.serviceQuotes)
    fun serviceQuotes(
            @Header(Constant.Fields.authorization) authToken: String,
            @Part("users_id") user_id: RequestBody,
            @Part("category_type") categoryType: RequestBody,
            @Part("text") description: RequestBody,
            @Part images: List<MultipartBody.Part?>,
            @Part("selected_date") selectedDate: RequestBody,
            @Part("car_size") carSize: RequestBody,
            @Part("button_type") buttonType: RequestBody
    ): Call<ResponseBody>


    @Multipart
    @POST(Constant.UrlEndPoints.updatecoustmerprofile)
    fun updateprofile(
            @Header(Constant.Fields.authorization) authToken: String,
            @Part("email") email: RequestBody,
            @Part("mobile") mobile: RequestBody,
            @Part profile_pic: MultipartBody.Part?,
            @Part("first_name") User_name: RequestBody,
            @Part("last_name") last_name: RequestBody

    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getCategoryQuotes)
    fun getQuotesCategory(
            @Header(Constant.Fields.authorization) authToken: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.supporttype)
    fun getComplaintCategory(
            @Header(Constant.Fields.authorization) authToken: String
    ): Call<ResponseBody>


    @POST(Constant.UrlEndPoints.getCarMaintenanceServices)
    fun getCarMaintenanceService(
            @Header(Constant.Fields.authorization) authToken: String,
            @Query("version_id") versionId: String,
            @Query("language") language: String,
            @Query("front_rear") frontRear: String,
            @Query("left_right") leftRight: String,
            @Query(Constant.Path.filterPriceRange) priceRange: String,
            @Query(Constant.Path.sortPrice) priceSortLevel: Int,
            @Query("user_id") user_id: String,
            @Query(Constant.Path.limit) limit: Int,
            @Query(Constant.Path.search) search: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getCarMaintenanceServices)
    fun getCarMaintenanceServiceSearch(
            @Header(Constant.Fields.authorization) authToken: String,
            @Query("version_id") versionId: String,
            @Query("language") language: String,
            @Query("front_rear") frontRear: String,
            @Query("left_right") leftRight: String,
            @Query(Constant.Path.filterPriceRange) priceRange: String,
            @Query(Constant.Path.sortPrice) priceSortLevel: Int,
            @Query("user_id") user_id: String,
            @Query(Constant.Path.limit) limit: Int,
            @Query(Constant.Path.search) search: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getQuotesCalendarPrice)
    fun getQuotesCalendar(
            @Query(Constant.Path.categoryType) categoryId: Int,
            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
            @Query(Constant.Path.filterRating) rating: String,
            @Query(Constant.Path.filterPriceRange) priceRange: String,
            @Query(Constant.Path.sortPrice) priceSortLevel: Int,
            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Query(Constant.Path.serviceQuotesInsertedId) serviceQuotesInsertedId: String,
            @Query(Constant.Path.version_id) versionId: String,
            @Query("user_lat") user_lat: String,
            @Query("user_long") user_long: String,
            @Query("distance_range") distance_range: String

    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.getCarMaintenanceCalendarPrice)
    fun getCarMaintenanceCalendar(
            @Field(Constant.Path.serviceID) serviceID: String,
            @Field(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
            @Field(Constant.Path.filterRating) rating: String,
            @Field(Constant.Path.filterPriceRange) priceRange: String,
            @Field(Constant.Path.sortPrice) priceSortLevel: Int,
            @Field(Constant.Path.type) type: Int,
            @Field("user_lat") user_lat: String,
            @Field("user_long") user_long: String,
            @Field("distance_range") distance_range: String,
            @Field(Constant.Path.mainCategoryId) mainCategoryId: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getSOSAppointmentCalendarPrice)
    fun getSosAppointmentCalendar(
            @Query(Constant.Path.workshopId) workshopId: String,
            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
            @Query(Constant.Path.latitude) latitude: String,
            @Query(Constant.Path.longitude) longitude: String,
            @Query(Constant.Path.serviceID) serviceID: String,
            @Query(Constant.Path.selectedCarId) selectedCarId: String,
            @Query("distance_range") distance_range: String,
            @Query(Constant.Path.version_id) version_id: String,
            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Query(Constant.Path.wrecker_service_type) wrecker_service_type: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getQuotesWorkshop)
    fun getQuotesWorkshops(
            @Header(Constant.Fields.authorization) authToken: String,

            @Query("service_id") categoryType: Int,
            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
            @Query(Constant.Path.filterRating) rating: String,
            @Query(Constant.Path.filterPriceRange) priceRange: String,
            @Query(Constant.Path.sortPrice) priceSortLevel: Int,
            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Query(Constant.Path.serviceQuotesInsertedId) serviceQuotesInsertedId: String,
            @Query(Constant.Path.version_id) versionId: String,
            @Query("user_lat") user_lat: String,
            @Query("user_long") user_long: String,
            @Query("distance_range") distance_range: String,
            @Query(Constant.Path.favorite) favorite: String,
            @Query(Constant.Path.couponFilter) couponfilter: String,
            @Query(Constant.Path.sort_by_distance) sort_by_distance: Int

    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.getCarMaintenanceWorkshop)
    fun getCarMaintenanceWorkshop(@Field(Constant.Path.version_id) versionId: String?,
                                  @Field(Constant.Path.language) language: String,
                                  @Field(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                                  @Field(Constant.Path.serviceID) serviceID: String,
                                  @Field(Constant.Path.filterRating) rating: String,
                                  @Field(Constant.Path.filterPriceRange) priceRange: String,
                                  @Field(Constant.Path.sortPrice) priceLevel: Int,
                                  @Field(Constant.Path.selectedCarId) selectedCarId: String,
                                  @Field("user_id") user_id: String,
                                  @Field("user_lat") user_lat: String,
                                  @Field("user_long") user_long: String,
                                  @Field("distance_range") distance_range: String,
                                  @Field(Constant.Path.favorite) favorite: String,
                                  @Field(Constant.Path.couponFilter) couponfilter: String,
                                  @Field(Constant.Path.mainCategoryId) mainCategoryId: String,
                                  @Field(Constant.Path.sort_by_distance) sort_by_distance: Int


    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getOrderlist)
    fun getOrderlist(
            @Header(Constant.Fields.authorization) authToken: String,
            @Query(Constant.Path.start) start:Int
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getTicketlist)
    fun getTicketlist(
            @Header(Constant.Fields.authorization) authToken: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getNotificationlist)
    fun getNotification(
            @Header(Constant.Fields.authorization) authToken: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getMOT)
    fun getMOT(
            @Query(Constant.Path.selectedCarId) selectedCarId: String,
            @Query(Constant.Path.servicekm) servicekm: String,
            @Query(Constant.Path.editStatus) edit_status: String,
            @Query(Constant.Path.language) language: String,

            @Query(Constant.Path.version_id) version_id: String,
            @Query("schedule_id") schedule_id: String


    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getMotServiceDetail)
    fun getmotserviceDetail(
            @Query(Constant.Path.mot_id) mot_id: String, @Query(Constant.Path.type) type: String, @Query(Constant.Path.version) version: String, @Query("user_id") user_id: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getMotServicePackageDetail)
    fun getMotServicePackageDetail(@Query(Constant.Path.workshopUsersId) workshopUserId: Int,
                                   @Query(Constant.Path.serviceid) categoryId: Int,
                                   @Query(Constant.Path.type) type: String,
                                   @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
                                   @Query(Constant.Path.selectedCarId) selectedCarId: String,
                                   @Query(Constant.Path.userid) userid: String,
                                   @Query(Constant.Path.service_average_time) averagetime: String,
                                   @Query(Constant.Path.couponid) couponid: String,
                                   @Query(Constant.Path.mainCategoryId) mainCategoryId: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.service_booking_request_mot)
    fun serviceMotBooking(
            @Field(Constant.Path.packageId) packageId: String,
            @Field(Constant.Path.start_time) startTime: String,
            @Field(Constant.Path.end_time) endTime: String,
            @Field(Constant.Path.workshopFilterSelectedDate) price: String,
            @Field(Constant.Path.serviceID) categoryType: String,
            @Field(Constant.Path.price) selectedDate: String,
            @Field(Constant.Path.orderId) serviceQuotesInsertedId: String,
            @Field(Constant.Path.selectedCarId) selectedCarId: String,
            @Field(Constant.Path.couponId) couponId: String?,
            @Field(Constant.Path.workshopId) workshopId: String?,
            @Field(Constant.Path.motservicetype) motservicetype: String?,
            @Field(Constant.Path.partid) parts: JSONArray,
            @Field(Constant.Path.specialConditionId) specialConditionId: String,
            @Field(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Field("temp_slot_id") temp_slot_id: String,
            @Field(Constant.Path.discountType) discountType: String,
            @Field(Constant.Path.version_id) version_id: String,
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.feedbackList)
    fun getfeedbackList(
            @Header(Constant.Fields.authorization) authToken: String,
            @Query(Constant.Path.workshopId) workshopId: String,
            @Query(Constant.Path.productid) productId: String,
            @Query(Constant.Path.type) type: String,
            @Query(Constant.Path.productType) productType: String,
            @Query("users_id") users_id: String
    ): Call<ResponseBody>

    @Multipart
    @POST(Constant.UrlEndPoints.addFeedback)
    fun addFeedback(
            @Header(Constant.Fields.authorization) authToken: String,
            @Part(Constant.Path.workshopId) workshopId: RequestBody,
            @Part(Constant.Path.ratings) ratings: RequestBody,
            @Part(Constant.Path.productId) productId: RequestBody,
            @Part images: ArrayList<MultipartBody.Part?>,
            @Part(Constant.Path.sellerId) sellerId: RequestBody,
            @Part(Constant.Path.comments) comments: RequestBody,
            @Part(Constant.Path.productType) productType: RequestBody,
            @Part(Constant.Path.mainCategoryId) mainCategoryId: RequestBody,
            @Part(Constant.Path.serviceID) serviceID: RequestBody,
            @Part(Constant.Path.type) type: RequestBody,
            @Part(Constant.Path.orderid) orderid: RequestBody,
            @Part(Constant.Path.motservicetype) motservicetype: RequestBody,
            @Part(Constant.Path.withoutPurchase) withoutPurchase: RequestBody


    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.changepassword)
    fun changepassword(
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.old_password) old_password: String,
            @Field(Constant.Path.new_password) new_password: String,
            @Field(Constant.Path.confirm_password) confirm_password: String
    ): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.addUserContactList)
    fun addUserContactList(@Header("Authorization") authToken: String?,
                           @Query("mobile_no") mobile_no: String): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.updateContact)
    fun updateContact(@Header("Authorization") authToken: String?, @Query("contact_id") ContactId: String,
                      @Query("mobile_no") mobile_no: String): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.updatenotification)
    fun updatesettings(@Header(Constant.Fields.authorization) authToken: String,
                       @Query(Constant.Path.lang) lang: String,
                       @Query(Constant.Path.notification_setting) notification_setting: String,
                       @Query(Constant.Path.notification_for_offer) notification_for_offer: String,
                       @Query(Constant.Path.notification_for_revision) notification_for_revision: String
    ): Call<ResponseBody>

    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.addUserAddress)
    fun addUserAddress(@Header("Authorization") authToken: String?,
                       @Query(Constant.Key.address) address: String,
                       @Query(Constant.Key.zip_code) zip_code: String,
                       @Query(Constant.Key.lat) lat: String,
                       @Query(Constant.Key.log) log: String, @Query(Constant.Key.addresstype) addresstype: String): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.updateAddress)
    fun updateAddress(@Header("Authorization") authToken: String?,
                      @Query(Constant.Key.address) address: String,
                      @Query(Constant.Key.zip_code) zip_code: String,
                      @Query(Constant.Key.lat) lat: String,
                      @Query(Constant.Key.log) log: String, @Query("address_id") ContactId: String, @Query(Constant.Key.addresstype) addresstype: String): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.deleteContact)
    fun deleteContact(@Header("Authorization") authToken: String?, @Query("contact_id") ContactId: String): Call<ResponseBody>


    @Headers(Constant.headerJSON)
    @POST(Constant.UrlEndPoints.deleteAddress)
    fun deleteAddress(@Header("Authorization") authToken: String?, @Query("address_id") ContactId: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getCartList)
    fun getCartList(@Header(Constant.Fields.authorization) authToken: String, @Query(Constant.Path.orderId) orderId: String): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.sosWorkshopListForAppontment)
    fun getSOSWorkshopListforAppointment(
            @Header(Constant.Fields.authorization) authToken: String,
            @Query(Constant.Path.selectedCarId) selectedCarId: String,
            @Query(Constant.Path.workshopFilterSelectedDate) selectedDate: String,
            @Query(Constant.Path.serviceID) serviceId: String,
            @Query(Constant.Path.userLat) latitude: String,
            @Query(Constant.Path.userLong) longitude: String,
            @Query("distance_range") distance_range: String,
            @Query(Constant.Path.favorite) favorite: String,
            @Query(Constant.Path.couponFilter) couponfilter: String,
            @Query(Constant.Path.filterRating) rating: String,
            @Query(Constant.Path.filterPriceRange) priceRange: String,
            @Query(Constant.Path.sortPrice) priceLevel: Int,
            @Query(Constant.Path.version_id) versionId: String,
            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Query(Constant.Path.wrecker_service_type) wrecker_service_type: String,
            @Query(Constant.Path.userid) userid: String,
            @Query(Constant.Path.sort_by_distance) sort_by_distance: Int

    ): Call<ResponseBody>


    @POST(Constant.UrlEndPoints.updateProductQuantity)
    fun updateCartProductQuantity(@Header("Authorization") authToken: String?,
                                  @Query(Constant.Path.quantity) quantity: String,
                                  @Query(Constant.Path.productid) productid: String,
                                  @Query(Constant.Path.price) price: String,
                                  @Query(Constant.Path.total_price) total_price: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.sosWorkshopListForEmergency)
    fun getSOSWorkshopListforEmergency(
            @Header(Constant.Fields.authorization) authToken: String,
            @Query(Constant.Path.selectedCarId) selectedCarId: String,
            @Query(Constant.Path.workshopFilterSelectedDate) selectedDate: String,
            @Query(Constant.Path.serviceID) serviceId: String,
            @Query(Constant.Path.latitude) latitude: String,
            @Query(Constant.Path.longitude) longitude: String,
            @Query(Constant.Path.start_time) startTime: String,
            @Query("distance_range") distance_range: String,
            @Query(Constant.Path.favorite) favorite: String,
            @Query(Constant.Path.couponFilter) couponfilter: String,
            @Query(Constant.Path.filterRating) rating: String,
            @Query(Constant.Path.filterPriceRange) priceRange: String,
            @Query(Constant.Path.sortPrice) priceLevel: Int,
            @Query(Constant.Path.version_id) versionId: String,
            @Query(Constant.Path.mainCategoryId) mainCategoryId: String,
            @Query(Constant.Path.wrecker_service_type) wrecker_service_type: String,
            @Query(Constant.Path.userid) userid: String,
            @Query(Constant.Path.sort_by_distance) sort_by_distance: Int

    ): Call<ResponseBody>


    @POST(Constant.UrlEndPoints.deleteCartItem)
    fun deleteCartItem(@Header("Authorization") authToken: String?,
                       @Query("book_id") book_id: String,
                       @Query(Constant.Path.total_price) total_price: String,
                       @Query(Constant.Path.type) type: String): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.addToCart)
    fun addToCartProduct(
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.productid) productId: String,
            @Field(Constant.Path.productqty) productQuantity: String,
            @Field(Constant.Path.pfutax) pfutax: String,
            @Field(Constant.Path.couponId) couponId: String?,
            @Field(Constant.Path.price) price: String,
            @Field(Constant.Path.total_price) totalPrice: String,
            @Field(Constant.Path.discount) discount: String,
            @Field(Constant.Path.forOrderType) forOrderType: String,
            @Field(Constant.Path.orderId) orderId: String,
            @Field(Constant.Path.workshopId) workshopId: String,
            @Field(Constant.Path.sellerId) sellerId: String,
            @Field(Constant.Path.productName) product_name: String,
            @Field(Constant.Path.productDescription) product_description: String,
            @Field(Constant.Path.selectedCarId) selectedCarId: String,
            @Field(Constant.Path.version_id) versionId: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.updatePaymentStatus)
    fun updatePaymentStatus(
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.transactionId) transactionId: String,
            @Field(Constant.Path.paymentMode) paymentMode: String,
            @Field(Constant.Path.productOrderId) productOrderId: String,
            @Field(Constant.Path.finalprice) finalprice: String,
            @Field(Constant.Path.total_price) total_price: String,
            @Field(Constant.Path.totalDiscount) totalDiscount: String,
            @Field(Constant.Path.totalVat) totalVat: String,
            @Field(Constant.Path.totalPfu) totalPfu: String,
            @Field(Constant.Path.payableAmount) payableAmount: String,
            @Field(Constant.Path.walletAmount) walletAmount: String,
            @Field(Constant.Path.bankDetailId) bankDetailId: String,

            @Header("accept") accept: String = "application/json"


    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.addToFavorite)
    fun addToFavorite(
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.productid) productid: String,
            @Field(Constant.Path.productType) productType: String,
            @Field(Constant.Path.workshopId) workshopId: String,
            @Field(Constant.Path.version_id) version_id: String,

            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.removeFromFavorite)
    fun removeFromFavorite(
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.productid) productid: String,
            @Field(Constant.Path.workshopId) workshopId: String,
            @Field(Constant.Path.productType) product_type: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.generatesupportticket)
    fun generatesupportticket(
            @Header(Constant.Fields.authorization) authToken: String,
            @Field(Constant.Path.message) message: String,
            @Field(Constant.Path.ticket_id) ticketid: String,
            @Field(Constant.Path.ticket_type) tickettype: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.userWishList)
    fun getUserWishList(
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getnotificationdetail)
    fun getUpdatesettings(
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.deleteaccountforcustomer)
    fun deleteAccount(
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getPartForMotReplacement)
    fun partListForMotReplacement(@Query(Constant.Path.N3Service_id) N3Service_id: String,
                                  @Query(Constant.Path.version_id) version_id: String,
                                  @Query(Constant.Path.motservicetype) motservicetype: String,
                                  @Query("user_id") user_id: String,
                                  @Query("limit") limit: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.removeCartItems)
    fun removeCart(
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>


    @POST(Constant.UrlEndPoints.checkUserCartItems)
    fun checkUserCartItems(
            @Query(Constant.Path.productOrderId) productOrderId: String,
            @Query(Constant.Path.addressId) shippingAddressId: String,
            @Query(Constant.Path.contactId) contactId: String,
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.generateInvoice)
    fun downloadInvoice(
            @Query(Constant.Path.orderId) orderId: String,
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.return_policy)
    fun returnOrder(
            @Query(Constant.Path.orderId) orderId: String,
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.customerInvoicePolicy)
    fun customerInvoicePolicy(
            @Query(Constant.Path.orderId) orderId: String,
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.get_bonus_detail)
    fun getInviteFrndsSummaryAPI(): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.loadKromedaParts)
    fun kromedaParts(
            @Header(Constant.Fields.authorization) authToken: String,
            @Query("id") id: String,
            @Query("user_id") user_id: String,
            @Query("limit") limit: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.SimilarPartProduct)
    fun getSimilarProduct(
            @Header(Constant.Fields.authorization) authToken: String,
            @Query(Constant.Path.version_id) versionId: String,
            @Query(Constant.Path.productType) productType: String,
            @Query(Constant.Path.productid) productid: String,
            @Query(Constant.Path.userid) userid: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.highRatingFeedback)
    fun getHighRatingFeedback(
            @Query(Constant.Path.type) type: String,
            @Query(Constant.Path.limit) limit: Int
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getFAQ)
    fun getFAQ_Data(
            @Header(Constant.Fields.authorization) authToken: String,
            @Header("accept") accept: String = "application/json"
    ): Call<ResponseBody>

    // get user saved address
    @GET(Constant.UrlEndPoints.getUserSavedAddress)
    fun getUserSavedAddress(
            @Header(Constant.Fields.authorization) authToken: String
    ): Call<ResponseBody>

    // call kromeda part
    @GET(Constant.UrlEndPoints.kromedaCall)
    fun kromedaCall(
            @Query(Constant.Path.version) versionId: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.motServiceSchedule)
    fun getMotServiceSchedule(
            @Query(Constant.Path.version_id) versionId: String
    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.carMaintenanceCriteria)
    fun getCarMaintenanceCriteria(
            @Query(Constant.Path.version_id) versionId: String,
            @Query(Constant.Path.language) language: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.carMaintenanceParts)
    fun getCarMaintenancePart(
            @Query(Constant.Path.version_id) versionId: String,
            @Query(Constant.Path.serviceid) serviceid: String,
            @Query(Constant.Path.userid) userid: String,
            @Query("limit") limit: String
    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.carMOTKromedaCall + "/{versionId}")
    fun getCarMOTKromedaCall(
            @Path("versionId") versionId: String

    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.carSpareKromedaCall + "/{versionId}" + "/{callingFrom}")
    fun getCarSpareKromedaCall(
            @Path("versionId") versionId: String,
            @Path("callingFrom") callingFrom: String

    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.addCarByPlateNumber)
    fun getAddCarByPlateNumber(@Field(Constant.Path.plateNumber) plateNumber: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.editTyreMeasurementdetails)
    fun editUserTyreDetails(@Field("user_id") user_id: String,
                            @Field("vehicle_type") vehicle_type: String,
                            @Field("season") season: String,
                            @Field("width") width: Int,
                            @Field("speedindex") speed_index: String,
                            @Field("run_flat") run_flat: Int,
                            @Field("reinforced") reinforced: Int,
                            @Field("execute") execute: Int,
                            @Field("aspect_ratio") aspectRatio: String,
                            @Field("rim_diameter") rimDiameter: String,
                            @Field("car_version_id") carVersionId: String,
                            @Field("load_index") load_index: String,
                            @Field("editid") editMeasurementId: String


    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.searchPartAutocomplete)
    fun getSearchPartAutocomplete(
            @Field(Constant.Path.keyword) search_keyword: String,
            @Field(Constant.Path.version_id) versionId: String


    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.selectedCar)
    fun getselectedUserCar(
            @Header(Constant.Fields.authorization) authToken: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getSparePartDetail)
    fun getSparePartDetail(
            @Query(Constant.Path.language) language: String,
            @Query(Constant.Path.productid) productId: String,
            @Query(Constant.Path.model) model: String,
            @Query(Constant.Path.car_version_id) versionId: String,
            @Query(Constant.Path.maker) search_keyword: String,
            @Query("user_id") user_id: String,
            @Query(Constant.Path.userLat) user_lat: String,
            @Query(Constant.Path.userLong) user_long: String,
            @Query(Constant.Path.distanceRange) distance_range: String,
            @Query(Constant.Path.item_id) item_id: String

    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.getSearchSparePartsBykeywords)
    fun getSearchSparePartsBykeywords(
            @Field(Constant.Path.keyword) keyword: String,
            @Field(Constant.Path.version_id) version_id: String,
            @Field(Constant.Path.type) type: String,
            @Field(Constant.Path.coupon) coupon: String,
            @Field("limit") limit: String,
            @Field("favorite") favorite: String,
            @Field("user_id") user_id: String,
            @Field(Constant.Path.filterPriceRange) priceRange: String,
            @Field(Constant.Path.sortPrice) priceSortLevel: Int,
            @Field("brand") brands: String? = "",
            @Field("rating_level") ratingLevel: String,
            @Field("rating_range") ratingRange: String

    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.getCarMakerslogo)
    fun getcarLogo(
            @Field(Constant.Path.makerName) brandName: String


    ): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.minPriceForMotServices)
    fun getminPriceForMotServicesl(
            @Query(Constant.Path.serviceid) mot_id: String,
            @Query(Constant.Path.type) type: String,
            @Query(Constant.Path.version) version: String,
            @Query(Constant.user_id) user_id: String,
            @Query(Constant.Path.service_average_time) service_average_time: String,
            @Query(Constant.Path.distanceRange) distanceRange: String,
            @Query(Constant.Path.workshopFilterSelectedDate) selected_date: String,
            @Query(Constant.Path.userLat) userLat: String,
            @Query(Constant.Path.userLong) userLong: String,
            @Query(Constant.Path.mainCategoryId) mainCategoryId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constant.UrlEndPoints.minPricesForMaintenanceServices)
    fun getminPriceForMaintenanceServices(
            @Field(Constant.Path.serviceid) mot_id: String,
            @Field(Constant.Path.version_id) version: String,
            @Field(Constant.user_id) user_id: String,
            @Field(Constant.Path.language) language: String,
            @Field(Constant.Path.distanceRange) distanceRange: String,
            @Field(Constant.Path.workshopFilterSelectedDate) selected_date: String,
            @Field(Constant.Path.userLat) userLat: String,
            @Field(Constant.Path.userLong) userLong: String

    ): Call<ResponseBody>


    @GET(Constant.UrlEndPoints.getbank_paymentInformation)
    fun getBankPaymentInformation(
            @Header(Constant.Fields.authorization) authToken: String): Call<ResponseBody>


    //  https://services.officinetop.com/api/workshopCalendarPrice?users_id=490&category_id=188&hourly_rate=2&service_average_time=0.29&selected_date=2020-12-21


    @GET(Constant.UrlEndPoints.getSelectedWorkshopCalendarPrice)
    fun getSelectedWorkshopCalendarPrice(
            @Query(Constant.Path.workshopUsersId) workshop_id: String,
            @Query(Constant.Path.userid) userid: String,
            @Query(Constant.Path.categoryId) categoryId: String,
            @Query(Constant.Path.hourly_rate) hourly_rate: String,
            @Query(Constant.Path.service_average_time) serviceAverageTime: String,
            @Query(Constant.Path.workshopFilterSelectedDate) selectedDate: String,
            @Query(Constant.Path.mainCategoryId) main_category_id: String
    ): Call<ResponseBody>

    @POST(Constant.UrlEndPoints.getworkshopCalendarPriceMaintenance)
    fun getSelectedWorkshopCalendarPriceMaintence(
            @Query(Constant.Path.workshopUsersId) workshop_id: String,
            @Query(Constant.Path.userid) userid: String,
            @Query(Constant.Path.categoryId) categoryId: String,
            @Query(Constant.Path.hourly_rate) hourly_rate: String,
            @Query(Constant.Path.service_average_time) serviceAverageTime: String,
            @Query(Constant.Path.workshopFilterSelectedDate) selectedDate: String,
            @Query(Constant.Path.mainCategoryId) main_category_id: String,
            @Query(Constant.Path.version_id) version_id: String,
            @Query(Constant.Path.services) services: JSONArray,
            @Query(Constant.Path.servicesPrice) services_price: String): Call<ResponseBody>

    @GET(Constant.UrlEndPoints.getSosoworkshopCalendarPrice)
    fun getSelectedWorkshopCalendarPriceSOs(
            @Query(Constant.Path.workshopUsersId) workshop_id: String,
            @Query(Constant.Path.workshopFilterSelectedDate) workshopFilterSelectedDate: String,
            @Query(Constant.Path.wrecker_service_type) wrecker_service_type: String,
            @Query(Constant.Path.serviceid) hourly_rate: String,
            @Query(Constant.Path.servicesPrice) services_price: String,
            @Query(Constant.Path.service_average_time) serviceAverageTime: String,
            @Query(Constant.Path.max_appointment) selectedDate: String,
            @Query(Constant.Path.mainCategoryId) main_category_id: String): Call<ResponseBody>




    @GET(Constant.UrlEndPoints.save_n3_id)
    fun saveN3id(
            @Query(Constant.Path.n3_Id) n3_Id: Int) : Call<ResponseBody>


}


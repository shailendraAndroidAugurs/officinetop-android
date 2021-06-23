package com.officinetop.data

import com.google.gson.annotations.SerializedName
import com.officinetop.utils.Constant
import com.officinetop.view_models.ListItemViewModel
import java.io.Serializable

object Models {
    data class AllAdvertisment(
            @SerializedName("add_location")
            val addLocation: String,
            @SerializedName("advertising_images")
            val advertisingImages: List<AdvertisingImages>

    ) : Serializable

    data class AdvertisingImages(
            @SerializedName("image_url")
            val imageUrl: String?,
            @SerializedName("main_cat_id")
            val MainCatId: String?,
            @SerializedName("url")
            val url: String?,
            @SerializedName("type")
            val type: String?
    )

    data class Manufacturer(@SerializedName(Constant.Fields.brandID) val brandID: String = "",
                            @SerializedName(Constant.Fields.brand) val brand: String = "",
                            @SerializedName(Constant.Fields.codeList) val codeList: String = "") : Serializable

    data class CarModels(@SerializedName(Constant.Fields.carModelID) val modelID: String = "",
                         @SerializedName(Constant.Fields.carModel) val model: String = "",
                         @SerializedName(Constant.Fields.carModelYear) val modelYear: String = "") : Serializable

    data class RimBrandlist(@SerializedName(Constant.Fields.tyrebrandid) val tyrebrandid: String = "",
                            @SerializedName(Constant.Fields.rimbrand) val rimbrand: String = "",
                            @SerializedName(Constant.Fields.id) val id: String = "") : Serializable

    data class RimCaModels(@SerializedName(Constant.Fields.tyre24ModelId) val tyre24ModelId: String = "",
                         @SerializedName(Constant.Fields.model) val model: String = "",
                           @SerializedName(Constant.Fields.id) val id: String = "") : Serializable

    data class CarVersion(

            @SerializedName("Alimentazione")
            val alimentazione: String = "",
            @SerializedName("Body")
            val body: String = "",
            @SerializedName("car_size")
            val carSize: Int = 2,

            @SerializedName("Cm3")
            val cm3: String = "",
            @SerializedName("Cv")
            val cv: String = "",
            @SerializedName("Dal")
            val dal: String = "",
            @SerializedName("idVeicolo")
            val idVehicle: String = "",
            @SerializedName("Kw")
            val kw: String = "",
            @SerializedName("ModelloCodice")
            val modelloCodice: String = "",
            @SerializedName("Motore")
            val motore: String = "",


            @SerializedName("fuel_type")
            val fueltype: String = "",

            @SerializedName("Versione")
            val version: String = ""

            //Versione, Dal, Cv, Kw, idVeicolo
    ) : Serializable


    data class MyCar(
            @SerializedName("data")
            val data: Any = Any(),
            @SerializedName("data_set")
            val dataSet: List<MyCarDataSet> = listOf(),
            @SerializedName("message")
            val message: String = "",
            @SerializedName("status_code")
            val statusCode: Int = 0
    )


    data class MyCarDataSet(
            @SerializedName("carMakeName")
            val carMakeName: String = "",
            @SerializedName("carModelName")
            val carModelName: String = "",
            @SerializedName("carVersion")
            val carVersion: String = "",

            @SerializedName("carMake")
            val carMakeModel: Manufacturer = Manufacturer(),

            @SerializedName("carVers")
            val carVersionModel: CarVersion = CarVersion(),


            @SerializedName("carModel")
            val carModel: CarModels = CarModels(),

            @SerializedName("criteriaResponse")
            val carCriteria: CarCriteria = CarCriteria(),

            @SerializedName("scheduleResponse")
            val carConditionMotSchedule: MotSchedule = MotSchedule(),
            @SerializedName("km_of_cars") val km_of_cars: String = "",
            @SerializedName("km_traveled_annually") val km_traveled_annually: String = "",
            @SerializedName("revision_date_km") val carRevisionDate: String = "",
            @SerializedName("revesion_km") val carRevisionDateOnKm: String = "",
            @SerializedName("alloy_wheels") val alloy_wheels: String = "",
            @SerializedName("fuel_type") val fuelType: String = "",///*fueltype*/


            @SerializedName("id")
            val id: String = "0",

            @SerializedName("image")
            val carDefaultImage: String? = "",
            @SerializedName("images")
            val carImages: ArrayList<CarImages> = ArrayList(),
            @SerializedName("car_size")
            val carSize: String = "",
            @SerializedName("selected")
            val selected: String = "0",
            @SerializedName("number_plate")
            val numberPlate: String = "",
            @SerializedName("created_at")
            val created_at: String = ""

    ) : Serializable

    data class CarImages(
            @SerializedName("id")
            var id: String = "",
            @SerializedName("image_name")
            var imageName: String = "") : Serializable


    object LinkedIn {

        data class LinkedInResponse(
                @SerializedName("elements")
                val elements: List<Element> = listOf()
        )

        data class Element(
                @SerializedName("handle")
                val handle: String = "",
                @SerializedName("handle~")
                val handleObject: Handle = Handle(),
                @SerializedName("primary")
                val primary: Boolean = false,
                @SerializedName("type")
                val type: String = ""
        )


        data class Handle(
                @SerializedName("emailAddress")
                val emailAddress: String = ""
        )
    }


    data class ServiceCategory(
            val id: Int?,
            val category_name: String?,
            val description: String?,
            val cat_image_url: String?,
            val services_price: String?,
            @SerializedName("vat_percentage_set_by_admin")
            val vat_Admin: String,
            @SerializedName("images")
            val itemImages: ArrayList<ItemImages>,
            val main_category_id: String? = ""
    ) : Serializable


    data class ItemImages(

            @SerializedName("image_name")
            var imageName: String = "",
            @SerializedName("image_url")
            var image_url: String = ""
    ) : Serializable


    //"date":"2019-06-07","price":100
    data class CalendarPrice(
            @SerializedName("date")
            var date: String = "",
            @SerializedName("price")
            var minPrice: String = "0.0"
    ) : Serializable


    data class Carversionlist(
            @SerializedName("Id")
            var Id: String = "",
            @SerializedName("IdModel")
            var IdModel: String = "",
            @SerializedName("Tyre24CarId")
            var Tyre24CarId: String = "",
            @SerializedName("CarType")
            var CarType: String = "",
            @SerializedName("CarTypeDesc")
            var CarTypeDesc: String = "",
            @SerializedName("ManufactureYear")
            var ManufactureYear: String = "",
            @SerializedName("CarComment")
            var CarComment: String = "",
            @SerializedName("CarPic")
            var CarPic: String = "",
            @SerializedName("HoleNumber")
            var HoleNumber: String = "",
            @SerializedName("HoleCircle")
            var HoleCircle: String = "",
            @SerializedName("HubHole")
            var HubHole: String = "",
            @SerializedName("PermitNumber")
            var PermitNumber: String = ""
    ) : Serializable


    data class rimProductDetails(
            @SerializedName("Id")
            var Id: String = "",
            @SerializedName("Id_Diameter")
            var Id_Diameter: String = "",
            @SerializedName("Id_Width")
            var Id_Width: String = "",
            @SerializedName("AlloyRimPrice")
            var AlloyRimPrice: String = "",
            @SerializedName("AlloyRimPriceEC")
            var AlloyRimPriceEC: String = "",
            @SerializedName("AlloyRimStock")
            var AlloyRimStock: String = "",
            @SerializedName("AlloyRimFrontID")
            var AlloyRimFrontID: String = "",
            @SerializedName("AlloyRimRearID")
            var AlloyRimRearID: String = "",
            @SerializedName("AlloyRimDescription")
            var AlloyRimDescription: String = "",
            @SerializedName("AlloyRimLochzahl")
            var AlloyRimLochzahl: String = "",
            @SerializedName("AlloyRimET")
            var AlloyRimET: String = "",
            @SerializedName("AlloyRimLochkreis")
            var AlloyRimLochkreis: String = "",
            @SerializedName("AlloyRimHubHole")
            var AlloyRimHubHole: String = "",
            @SerializedName("AlloyRimHubHoleType")
            var AlloyRimHubHoleType: String = "",
            @SerializedName("AlloyRimOwnStock")
            var AlloyRimOwnStock: String = "",
            @SerializedName("AlloyRimWholesalerArticleNo")
            var AlloyRimWholesalerArticleNo: String = "",
            @SerializedName("Manufacturer")
            var Manufacturer: String = "",
            @SerializedName("Type")
            var Type: String = "",
            @SerializedName("TypeName")
            var TypeName: String = "",
            @SerializedName("TypeColor")
            var TypeColor: String = "",
            @SerializedName("TypeConstruction")
            var TypeConstruction: String = "",
            @SerializedName("ImageUrl")
            var ImageUrl: String = "",
            @SerializedName("alloyIsPromo")
            var alloyIsPromo: String = "",
            @SerializedName("assemble_time")
            var assemble_time: String = "",
            @SerializedName("rim_anteriore")
            var rim_anteriore: String = "",
            @SerializedName("front_width")
            var front_width: String = "",
            @SerializedName("front_diameter")
            var front_diameter: String = "",
            @SerializedName("rim_posteriore")
            var rim_posteriore: String = "",
            @SerializedName("rear_width")
            var rear_width: String = "",
            @SerializedName("rear_diameter")
            var rear_diameter: String = "",
            @SerializedName("winter")
            var winter: String = ""
    ) : Serializable

    data class Availablecartypelist(
            @SerializedName("Id")
            var Id: String = "",
            @SerializedName("IdCarType")
            var IdCarType: String = "",
            @SerializedName("IdFrontDiameter")
            var IdFrontDiameter: String = "",
            @SerializedName("IdRearDiameter")
            var IdRearDiameter: String = "",
            @SerializedName("IdFrontWidth")
            var IdFrontWidth: String = "",
            @SerializedName("IdRearWidth")
            var IdRearWidth: String = "",
            @SerializedName("front_diameter")
            var front_diameter: String = "",
            @SerializedName("rear_diameter")
            var rear_diameter: String = "",
            @SerializedName("total_quantity")
            var total_quantity: String = "",
            @SerializedName("measures")
            var measures: List<Measures>

    ) : Serializable



    data class Measures(
            @SerializedName("Id")
            var Id: String = "",
            @SerializedName("IdCarType")
            var IdCarType: String = "",
            @SerializedName("IdFrontDiameter")
            var IdFrontDiameter: String = "",
            @SerializedName("IdRearDiameter")
            var IdRearDiameter: String = "",
            @SerializedName("IdFrontWidth")
            var IdFrontWidth: String = "",
            @SerializedName("IdRearWidth")
            var IdRearWidth: String = "",
            @SerializedName("front_diameter")
            var front_diameter: String = "",
            @SerializedName("rear_diameter")
            var rear_diameter: String = "",
            @SerializedName("quantity")
            var quantity: String = "",
            @SerializedName("front_width")
            var front_width: String = "",
            @SerializedName("rear_width")
            var rear_width: String = ""

    ) : Serializable

    data class ProductDetail(
            @SerializedName("p_name")
            var productName: String,
            @SerializedName("p_description")
            var Productdescription: String,

            @SerializedName("assemble_status")
            val assembleStatus: String,

            @SerializedName("brand_image")
            val brandImage: String,

            @SerializedName("coupon_list")
            val couponList: List<Coupon>,

            var selectedProductCouponId: String,
            @SerializedName("for_pair")
            val forPair: String,

            @SerializedName("id")
            val id: String,

            @SerializedName("products_name")
            val productsName: String,
            @SerializedName("products_quantiuty")
            val productsQuantiuty: String,
            @SerializedName("seller_price")
            val sellerPrice: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("users_id")
            val usersId: String,

            @SerializedName("service_average_time")
            val serviceAverageTime: String,
            @SerializedName("main_category_id")
            val mainCategoryId: String,
            @SerializedName("service_id")
            val serviceId: String,
            @SerializedName("wish_list")
            var wishlist: String = ""

    ) : Serializable


    data class CartItem(var id: String = "",
                        var name: String = "",
                        var description: String? = "",
                        var price: Double = 0.0,
                        var quantity: Int = 1,
                        var data: String? = "",
                        var type: String? = "",
                        var productDetail: ProductDetail? = null,
                        var tyreDetail: TyreDetailData? = null,
                        var additionalPrice: Double = 0.00,
                        var finalPrice: Double = (price * quantity) + additionalPrice,
                        val addedOn: Long = System.currentTimeMillis(),
                        val reverseTimeStamp: Long = addedOn * -1,
                        var pfu_tax: String = "",
                        var tyretotalPrice: String = "",
                        var Deliverydays: String = "",
                        var servicesAverageTime: String = "",
                        var serviceId: String = "",
                        var mainCategoryId: String = ""
    ) : Serializable

    data class TyreDetail(var id: String = "",
                          var vehicleType: String = "",
                          var aspectRatio: String = "",
                          var width: Float = 0f,
                          var diameter: Float = 0f,
                          var rimWidth: Float = 0f,

                          var runFlat: Boolean = false,
                          var reinforced: Boolean = false,
                          val addedOn: Long = System.currentTimeMillis(),
                          val reverseTimeStamp: Long = addedOn * -1,
                          var vehicleTypeName: String = "",
                          var priceLevel: String = "0",
                          var priceRange: String = "",
                          var brands: String = "",
                          var offerOrCoupon: Boolean = false,
                          var onlyFav: Boolean = false,
                          var Rating: String = "",
                          var AlphabeticalOrder: String = "0",
                          var filter_brandsId: String = "",
                          var seasonId: String = "",
                          var speedIndexId: String = "",
                          var seasonName: String = "",
                          var speedIndexName: String = "",
                          var speed_load_index: String = "",
                          var speed_load_indexDesc: String = "",
                          var cust_speedIndexId: String = "",
                          var cust_seasonId: String = "",
                          var cust_speedLoad_indexName: String = "",
                          var cust_seasonName: String = "",
                          var cust_speed_indexName: String = "",
                          var cust_runflat: Boolean = false,
                          var cust_reinforced: Boolean = false,
                          var cust_speedLoad_indexDesc: String = ""

    )


    data class TypeSpecification(var name: String, var code: String)
    data class TypeSpecificationForSeason(var name: String, var id: String, var CodeName: String)
    data class TypeSpecificationForVehicalType(var name: String, var id: String, var vhicle_type_arr: ArrayList<String>?)

    data class Rating(
            @SerializedName("num_of_product")
            val numOfProduct: Int,
            @SerializedName("rating")
            val rating: String
    ) : Serializable

    data class tyrePfu(
            var price: String

    ) : Serializable


    data class TyreImage(
            var image_url: String? = ""
    ) : Serializable

    data class AllWrackerWorkshops(
            @SerializedName("address_1")
            val address1: String? = null,
            @SerializedName("address_2")
            val address2: String? = null,
            @SerializedName("address_3")
            val address3: String? = null,
            @SerializedName("id")
            val id: Int,

            @SerializedName("latitude")
            val latitude: String? = null,
            @SerializedName("longitude")
            val longitude: String? = null,
            @SerializedName("mobile_number")
            val mobileNumber: Long? = null,
            @SerializedName("users_id")
            val usersId: Int,
            @SerializedName("workshop_name")
            val WorkshopName: String

    ) : ListItemViewModel()

    data class SOSWorkshop(
            val address: List<Addres>,
            val business_name: String,
            val registered_office: String

    ) : ListItemViewModel()

    data class Addres(
            val latitude: Any,
            val longitude: Any
    ) : ListItemViewModel()

    data class WrackerServices(
            @SerializedName("address_id")
            val addressId: String,
            @SerializedName("call_cost")
            val callCost: String,
            @SerializedName("cost_per_km")
            val costPerKm: String,
            @SerializedName("hourly_cost")
            val hourlyCost: String,
            @SerializedName("id")
            val id: Int,
            @SerializedName("selected_car_id")
            val selectedCarId: String,
            @SerializedName("service_image_url")
            val serviceImageUrl: String,
            @SerializedName("services_name")
            val servicesName: String,
            @SerializedName("total_time_arrives")
            val totalTimeArrives: String,

            @SerializedName("workshop_id")
            val workshopId: Int,
            @SerializedName("workshop_wrecker_id")
            val workshopWreckerId: Int,
            @SerializedName("main_category_id")
            val main_category_id: String,
            @SerializedName("description")
            val description: String

    ) : ListItemViewModel()

    data class Image(
            val id: Int,
            @SerializedName("image_name")
            val imageName: String,
            @SerializedName("image_url")
            val imageUrl: String


    ) : ListItemViewModel()

    data class TyreDetailItem(
            val brand_image: String? = "",
            val description: String? = "",
            val ean_number: String? = "",
            val noiseDb: String? = "",
            val id: Int?,
            val imageUrl: String? = "",
            val images: List<TyreImage>? = null,
            val tyre_label_images: List<TyreImage>? = null,
            val price: String? = "",
            val manufacturer_description: String? = "",
            val max_aspect_ratio: String? = "",
            val max_diameter: String? = "",
            val max_price: String? = "",
            val max_width: String? = "",
            val min_price: String? = "",
            val our_description: Any?,
            val pair: String?,
            val pr_description: String? = "",
            val quantity: String?,
            val rollingResistance: String? = "",
            val seller_price: String? = "",
            val speed_index: String? = "",
            val type: String? = "",
            val tyre_response: String? = "",
            val user_id: Int?,
            val wetGrip: String? = "",
            val wholesalerArticleNo: String? = "",
            val is3PMSF: String? = "",
            val season_tyre_type: String? = "",
            var wish_list: String? = "0",
            @SerializedName("rating")
            val rating: Rating?,
            @SerializedName("rating_count")
            val ratingCount: String,
            @SerializedName("rating_star")
            val ratingStar: String,
            @SerializedName("coupon_list")
            val couponList: List<Coupon>?,
            @SerializedName("load_speed_index")
            val load_speed_index: String = "",
            @SerializedName("assemble_status")
            val assemblestatus: String,
            @SerializedName("tyre_season_image")
            val tyreSeasonImageURL: String = "",
            @SerializedName("tyre_type_image")
            val tyreImageURL: String,
            @SerializedName("pfu")
            val tyrePfu: tyrePfu?,
            var SelectedTyreCouponId: String? = "",
            @SerializedName("season_desc")
            val seasonName: String?,

            @SerializedName("type_status")
            val typeStatus: String?

    ) : Serializable


    data class SpecialCondition(
            @SerializedName("id")
            val id: String = "",
            @SerializedName("item")
            val item: String = ""
    ) : Serializable


    /*quotes main category*/
    data class MainCategory(
            @SerializedName("service_id")
            val serviceId: String = "",
            @SerializedName("main_cat_name")
            val mainCatName: String = "",
            @SerializedName("main_category_id")
            val Qutoes_mainCategoryId: String = ""

    ) : ListItemViewModel()

    data class CompCategory(
            @SerializedName("code")
            val code: String,
            @SerializedName("name")
            val name: String
    ) : ListItemViewModel()


    /*get car maintenance services*/
    data class CarMaintenanceServices(
            @SerializedName("action_description")
            val actionDescription: String = "",
            @SerializedName("front_rear")
            val frontRear: String = "",
            @SerializedName("id")
            var id: String = "",

            @SerializedName("item")
            val item: String = "",
            @SerializedName("left_right")
            val leftRight: String = "",
            @SerializedName("our_description")
            val ourDescription: String = "",
            @SerializedName("part_list")
            var parts: ArrayList<Part>,
            @SerializedName("type")
            val type: String = "",
            @SerializedName("users_id")
            var usersId: String = "",
            @SerializedName("price")
            val price: String = "",
            @SerializedName("listino")
            var listino: String = "",
            @SerializedName("descrizione")
            var descrizione: String = "",
            @SerializedName("seller_price")
            var seller_price: String = "",
            @SerializedName("product_image_url")
            var product_image_url: String = "",
            @SerializedName("brand_image_url")
            var brandImageURL: String = "",
            @SerializedName("for_pair")
            var forPair: String = "",
            @SerializedName("product_id")
            var productId: String = "",
            @SerializedName("version_id")
            var versionId: String = "",
            var CouponTitle: String = "",
            var CouponId: String = "",
            @SerializedName("coupon_list")
            var couponList: ArrayList<Coupon> = ArrayList<Coupon>(),
            @SerializedName("p_name")
            var productName: String,
            @SerializedName("rating_star")
            var rating_star: String = "",
            @SerializedName("rating_count")
            var rating_count: String = "",
            @SerializedName("wish_list")
            var wishlist: String = "",
            @SerializedName("main_category_id")
            var mainCategoryId: String = "",
            var searchKey : String = ""


    ) : ListItemViewModel()

    data class Part(
            @SerializedName("p_name")
            var productName: String,
            @SerializedName("p_description")
            var Productdescription: String,
            @SerializedName("descrizione")
            val descrizione: String = "",

            @SerializedName("for_pair")
            val forPair: String = "",

            @SerializedName("id")
            var id: String = "",
            @SerializedName("listino")
            val listino: String = "",
            @SerializedName("price")
            val price: String = "",


            @SerializedName("seller_price")
            val sellerPrice: String,

            @SerializedName("product_image_url")
            var product_image_url: String = "",

            @SerializedName("users_id")
            val usersId: String = "",


            @SerializedName("image_url")
            val imageUrl: String?,

            @SerializedName("product_id")
            val productId: String = "",
            @SerializedName("product_type")
            val productType: String = "",
            @SerializedName("rating_star")
            var rating_star: String = "",
            @SerializedName("rating_count")
            var rating_count: String = "",
            @SerializedName("brand_image")
            val brandImage: String = "",
            @SerializedName("brand_image_url")
            var brandImageURL: String = "",
            @SerializedName("images")
            val images: List<Image>,
            var partimage: String = "",
            @SerializedName("coupon_list")
            var couponList: ArrayList<Coupon> = ArrayList<Coupon>(),

            @SerializedName("mot_type")
            val mot_type: String = "",
            @SerializedName("version_id")
            val version_id: String = "",

            @SerializedName("number_of_delivery_days")
            val numberOfDeliveryDays: String = "",
            @SerializedName("n3_service_id")
            val n3_service_id: String = "",
            @SerializedName("wish_list")
            var wishlist: String = "",
            var couponTitle: String = "",
            var couponId: String = ""
    ) : ListItemViewModel()


    data class Coupon(
            @SerializedName("amount")
            var amount: String?,
            @SerializedName("coupon_image")
            val couponImage: Any?,
            @SerializedName("coupon_quantity")
            var couponQuantity: String?,
            @SerializedName("coupon_title")
            var couponTitle: String,
            @SerializedName("coupon_type")
            val couponType: Int,
            @SerializedName("id")
            var id: String,
            @SerializedName("offer_type")
            val offerType: String
    ) : ListItemViewModel()


    data class MotServicesList(
            @SerializedName("id")
            val id: Int,
            @SerializedName("interval_description_for_kms")
            val intervalDescriptionForKms: String,

            @SerializedName("service_advisory_message")
            val serviceAdvisoryMessage: Any?,
            @SerializedName("service_description")
            val serviceDescription: String,
            @SerializedName("service_interval_id")
            val serviceIntervalId: Any?,

            @SerializedName("service_kms")
            val serviceKms: String,

            @SerializedName("service_name")
            val serviceName: String,

            @SerializedName("type")
            val type: Int,

            @SerializedName("min_price")
            val min_price: String,

            @SerializedName("main_category_id")
            val main_category_id: Int
    ) : ListItemViewModel()


    data class MotDetail(
            @SerializedName("data")
            val data: Data,
            @SerializedName("data_set")
            val dataSet: Any?,
            @SerializedName("message")
            val message: Any?,
            @SerializedName("status_code")
            val statusCode: Int
    ) : Serializable

    data class Data(

            @SerializedName("interval_description_for_kms")
            val intervalDescriptionForKms: String,
            @SerializedName("k_partList")
            val kPartList: List<Part>,
            @SerializedName("operations")
            val operations: List<Operation>,


            @SerializedName("service_average_time")
            var serviceaveragetime: String
    )


    data class Operation(
            @SerializedName("group_name")
            val groupName: String,
            @SerializedName("id")
            val id: Int,
            @SerializedName("operation_description")
            val operationDescription: String,
            @SerializedName("operation_action")
            val opretionAction: String
    ) : ListItemViewModel()


    data class FeedbacksList(
            @SerializedName("avg_ratings")
            var avgRatings: String = "",
            @SerializedName("comments")
            var comments: String = "",
            @SerializedName("created_at")
            var createdAt: String = "",
            @SerializedName("f_name")
            var fName: String = "",
            @SerializedName("customer_profile_image")
            var profile_image: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("images")
            var images: List<FeedbackImage> = ArrayList(),

            @SerializedName("l_name")
            val lName: String = "",
            @SerializedName("no_of_people")
            val noOfPeople: String? = "",

            @SerializedName("rating")
            val rating: String = "",
            @SerializedName("seller_id")
            val sellerId: String = "",
            @SerializedName("service_id")
            val serviceId: String = "",

            @SerializedName("users_id")
            val usersId: String = "",
            @SerializedName("workshop_id")
            val workshopId: String = "",
            var feedbackType: String = "",
            @SerializedName("productorWorkshopName")
            var ProductOrWorkshopName: String = "",
            var product_type: String = "",

            @SerializedName("without_purchase")
            var withoutPurchase: String = ""


    ) : ListItemViewModel()


    data class UserDetailData(
            @SerializedName("user_address")
            val userAddress: List<UserAddres>,
            @SerializedName("user_contact")
            val userContact: List<UserContact>,
            @SerializedName("user_details")
            val userDetails: List<UserDetail>


    )


    data class FeedbackImage(
            @SerializedName("image_name")
            val imageName: String,
            @SerializedName("image_url")
            val imageUrl: String,
            @SerializedName("type")
            val type: String
    ) : ListItemViewModel()


    data class UserAddres(
            @SerializedName("address_1")
            val address1: String,
            @SerializedName("address_2")
            val address2: String?,
            @SerializedName("address_3")
            val address3: Any?,
            @SerializedName("address_type")
            val addressType: String?,

            @SerializedName("city_name")
            val cityName: String?,
            @SerializedName("country_id")
            val countryId: Any?,
            @SerializedName("country_name")
            val countryName: String?,

            @SerializedName("distance")
            val distance: Any?,
            @SerializedName("id")
            val id: Int,

            @SerializedName("landmark")
            val landmark: String?,
            @SerializedName("latitude")
            val latitude: String,
            @SerializedName("longitude")
            val longitude: String,
            @SerializedName("state_id")
            val stateId: Any?,
            @SerializedName("state_name")
            val stateName: String?,
            @SerializedName("status")
            val status: Int,

            @SerializedName("zip_code")
            val zipCode: String?


    )


    data class UserWallet(
            @SerializedName("amount")
            val amount: String = "100",
            @SerializedName("created_at")
            val createdat: String?
    )


    data class UserContact(

            @SerializedName("id")
            val id: Int,
            @SerializedName("mobile")
            var mobile: String
    ) : ListItemViewModel()

    data class UserDetail(

            @SerializedName("email")
            val email: String,

            @SerializedName("f_name")
            val fName: String = "",
            @SerializedName("id")
            val id: Int,
            @SerializedName("l_name")
            val lName: String = "",
            @SerializedName("mobile_number")
            val mobileNumber: String,
            @SerializedName("password")
            val password: String,
            @SerializedName("profile_image")
            val profileImage: String,
            @SerializedName("provider")
            val provider: Any?,
            @SerializedName("provider_id")
            val providerId: Any?,
            @SerializedName("own_referal_code")
            val ownreferalcode: String


    )


    data class CartItemList(
            @SerializedName("id")
            val id: String,

            @SerializedName("order_id")
            val orderId: String,

            @SerializedName("order_date")
            val orderDate: String,

            @SerializedName("payment_status")
            val paymentStatus: String,


            @SerializedName("service_product_description")
            val serviceProductDescription: List<ServiceProductDescription>,

            @SerializedName("spare_product_description")
            val spareProductDescription: List<SpareProductDescription>,
            @SerializedName("status")
            val status: String,

            @SerializedName("tyre_product_description")
            val tyreProductDescription: List<TyreProductDescription>,


            @SerializedName("return_request_status")
            var returnRequest: String,

            @SerializedName("address")
            val address: Address,
            @SerializedName("tracking_details")
            val orderTracking: OrderTracking,
            @SerializedName("final_order_price")
            val finalOrderPrice: String


    ) : ListItemViewModel()


    data class Address(
            @SerializedName("address_1")
            val address_1: String,
            @SerializedName("zip_code")
            val zip_code: String?
    )


    data class OrderTracking(
            @SerializedName("id")
            val id: String,
            @SerializedName("tracking_by")
            val tracking_by: String?,
            @SerializedName("tracking_url")
            val trackingUrl: String,
            @SerializedName("sample_tracking_id")
            val sample_tracking_id: String,

            @SerializedName("created_at")
            val created_at: String,

            @SerializedName("tracking_id")
            val trackingId: String

    )


    data class ServiceProductDescription(
            @SerializedName("after_discount_price")
            val afterDiscountPrice: String,
            @SerializedName("price")
            val price: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("booking_date")
            val bookingDate: String,
            @SerializedName("end_time")
            val endTime: String,
            @SerializedName("assembly_service_product_description")
            val serviceAssemblyProductDescription: ServiceAssemblyProductDescription,
            @SerializedName("service_detail")
            val serviceDetail: ServiceDetail,
            @SerializedName("part_details")
            val partDetails: List<Part>,
            @SerializedName("start_time")
            val startTime: String,
            @SerializedName("order_status")
            val status: String,

            @SerializedName("workshop_details")
            val workshopDetails: WorkshopDetails,

            @SerializedName("mot_service_type")
            val motServiceType: String?,
            @SerializedName("coupons_id")
            val couponId: String?,
            @SerializedName("coupon_title")
            val couponTitle: String?,
            @SerializedName("coupon_type")
            val couponType: String,
            @SerializedName("coupon_price")
            val CouponPrices: String


    ) : ListItemViewModel()


    data class Feedbackdetail(
            @SerializedName("comments")
            val comments: String,
            @SerializedName("rating")
            val rating: String?
    ) : ListItemViewModel()


    data class ServiceAssemblyProductDescription(
            @SerializedName("discount")
            val discount: String,
            @SerializedName("for_order_type")
            val forOrderType: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("number_of_delivery_days")
            val deliveryDays: String,
            @SerializedName("pfu_tax")
            val pfuTax: String,
            @SerializedName("pfu_desc")
            val pfuDesc: String,
            @SerializedName("price")
            val price: String,
            @SerializedName("product_description")
            val productDescription: String?,
            @SerializedName("product_image")
            val productImage: String?,
            @SerializedName("product_image_url")
            val productImageUrl: String?,
            @SerializedName("product_name")
            val productName: String?,
            @SerializedName("product_quantity")
            val productQuantity: String,
            @SerializedName("products_id")
            val productsId: String,
            @SerializedName("total_price")
            val totalPrice: String?,
            @SerializedName("users_id")
            val usersId: String,
            @SerializedName("seller_id")
            val sellerId: String,
            @SerializedName("max_products_quantity")
            val maxProductQuantity: String = "",
            @SerializedName("vat")
            val product_vat: String,
            @SerializedName("final_order_price")
            val finalOrderPrice: String,
            @SerializedName("feedback_status")
            val feedbackstatus: String?,
            @SerializedName("feedback_detail")
            val feedbackdetail: Feedbackdetail,
            @SerializedName("pair")
            val IsProductPair: String?,
            @SerializedName("coupons_id")
            val couponId: String?,
            @SerializedName("coupon_title")
            val couponTitle: String?,
            @SerializedName("coupon_type")
            val couponType: String,
            @SerializedName("coupon_price")
            val CouponPrices: String

    ) : ListItemViewModel()

    data class ServiceDetail(
            @SerializedName("cat_image_url")
            val catImageUrl: String,
            @SerializedName("price")
            val price: String?,
            @SerializedName("service_name")
            val serviceName: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("service_description")
            val serviceDescription: String,
            @SerializedName("main_category_id")
            var mainCategoryId: String?,
            @SerializedName("service_id")
            val serviceId: String,
            @SerializedName("feedback_status")
            val feedbackstatus: String?,
            @SerializedName("feedback_detail")
            val feedbackdetail: Feedbackdetail

    ) : ListItemViewModel()

    data class WorkshopDetails(

            @SerializedName("business_name")
            val businessName: String,

            @SerializedName("id")
            val id: String,

            @SerializedName("langitude")
            val langitude: Double,
            @SerializedName("latitude")
            val latitude: Double,


            @SerializedName("registered_office")
            val registeredOffice: String,


            @SerializedName("profile_image")
            val Profile_image: String,
            @SerializedName("profile_image_url")
            val workShopImageUrl: String,
            @SerializedName("rating")
            val rating: Rating,
            @SerializedName("rating_count")
            val ratingCount: Int,
            @SerializedName("rating_star")
            val ratingStar: String,
            @SerializedName("company_name")
            val companyName: String,
            @SerializedName("mobile_number")
            val mobileNumber: String,
            @SerializedName("coupon_list")
            var couponList: List<Coupon>
    ) : ListItemViewModel()

    /*------------------------------------------------ DataCustomize----------------------------*/
    data class SpareProductDescription(

            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("deleted_at")
            val deletedAt: String?,
            @SerializedName("discount")
            val discount: String,
            @SerializedName("for_assemble_service")
            val forAssembleService: String,
            @SerializedName("for_order_type")
            val forOrderType: String,
            @SerializedName("number_of_delivery_days")
            val deliveryDays: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("pfu_tax")
            val pfuTax: String,
            @SerializedName("price")
            val price: String,
            @SerializedName("product_description")
            val productDescription: String,
            @SerializedName("product_image")
            val productImage: String?,
            @SerializedName("product_image_url")
            val productImageUrl: String?,
            @SerializedName("product_name")
            val productName: String,
            @SerializedName("product_quantity")
            val productQuantity: String,
            @SerializedName("products_id")
            val productsId: String,
            @SerializedName("products_orders_id")
            val productsOrdersId: String,
            @SerializedName("service_booking_id")
            val serviceBookingId: String,
            @SerializedName("order_status")
            val status: String,
            @SerializedName("total_price")
            val totalPrice: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("users_id")
            val usersId: String,
            @SerializedName("seller_id")
            val sellerId: String,
            @SerializedName("max_products_quantity")
            val maxProductQuantity: String,
            @SerializedName("final_order_price")
            val finalOrderPrice: String,
            @SerializedName("vat")
            val product_vat: String,
            @SerializedName("avilability")
            val avilability: String?,
            @SerializedName("feedback_status")
            val feedbackstatus: String?,
            @SerializedName("feedback_detail")
            val feedbackdetail: Feedbackdetail,
            @SerializedName("pair")
            val IsProductPair: String?,

            @SerializedName("coupons_id")
            val couponsId: String?,
            @SerializedName("coupon_title")
            val couponTitle: String?,
            @SerializedName("coupon_type")
            val couponType: String,
            @SerializedName("coupon_price")
            val CouponPrices: String

    ) : ListItemViewModel()

    data class TyreProductDescription(
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("deleted_at")
            val deletedAt: String?,
            @SerializedName("discount")
            val discount: String,

            @SerializedName("number_of_delivery_days")
            val deliveryDays: String,


            @SerializedName("for_assemble_service")
            val forAssembleService: String,
            @SerializedName("for_order_type")
            val forOrderType: String,
            @SerializedName("pfu_desc")
            val pfuDesc: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("pfu_tax")
            val pfuTax: String,
            @SerializedName("price")
            val price: String,
            @SerializedName("product_description")
            val productDescription: String?,
            @SerializedName("product_image")
            val productImage: String?,
            @SerializedName("product_image_url")
            val productImageUrl: String?,
            @SerializedName("product_name")
            val productName: String?,
            @SerializedName("product_quantity")
            val productQuantity: String,
            @SerializedName("products_id")
            val productsId: String,
            @SerializedName("products_orders_id")
            val productsOrdersId: String,
            @SerializedName("service_booking_id")
            val serviceBookingId: String,
            @SerializedName("order_status")
            val status: String,
            @SerializedName("total_price")
            val totalPrice: String?,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("users_id")
            val usersId: String,
            @SerializedName("seller_id")
            val sellerId: String,
            @SerializedName("max_products_quantity")
            val maxProductQuantity: String,
            @SerializedName("final_order_price")
            val finalOrderPrice: String
            ,
            @SerializedName("vat")
            val product_vat: String,
            @SerializedName("avilability")
            val avilability: String?,
            @SerializedName("feedback_status")
            val feedbackstatus: String?,
            @SerializedName("feedback_detail")
            val feedbackdetail: Feedbackdetail,
            @SerializedName("pair")
            val IsProductPair: String?,
            @SerializedName("coupons_id")
            val couponId: String?,
            @SerializedName("coupon_title")
            val couponTitle: String?,
            @SerializedName("coupon_type")
            val couponType: String,
            @SerializedName("coupon_price")
            val CouponPrices: String


    ) : ListItemViewModel()


    data class CartData(
            @SerializedName("address_type")
            val addressType: String,
            @SerializedName("courier_id")
            val courierId: String,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("deleted_at")
            val deletedAt: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("delivery_price")
            val deliveryPrice: String,
            @SerializedName("no_of_products")
            val noOfProducts: String,
            @SerializedName("order_date")
            val orderDate: String,
            @SerializedName("payment_mode")
            val paymentMode: String,
            @SerializedName("payment_status")
            val paymentStatus: String,
            @SerializedName("seller_id")
            val sellerId: String,

            @SerializedName("CartDataList")
            var CartDataList: ArrayList<CartDataList> = ArrayList(),

            @SerializedName("shipping_address_id")
            val shippingAddressId: String,
            @SerializedName("status")
            val status: String,
            @SerializedName("total_discount")
            val totalDiscount: String,
            @SerializedName("total_price")
            val totalPrice: String,
            @SerializedName("tracking_id")
            val trackingId: String,
            @SerializedName("transaction_id")
            val transactionId: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("user_details_id")
            val userDetailsId: String,
            @SerializedName("users_id")
            val usersId: String,
            @SerializedName("user_wallet")
            val userWallet: UserWallet,
            @SerializedName("vat_percentage_set_by_admin")
            val vat_Admin: String

    ) : ListItemViewModel()


    data class CartDataList(
            @SerializedName("discount")
            val discount: String,
            @SerializedName("for_order_type")
            val forOrderType: String,
            @SerializedName("pfu_tax")
            val pfuTax: String,
            @SerializedName("pfu_desc")
            val pfuDesc: String,
            @SerializedName("number_of_delivery_days")
            val deliveryDays: String,
            @SerializedName("assembly_service_product_description")
            val serviceAssemblyProductDescription: ServiceAssemblyProductDescription,
            @SerializedName("product_description")
            val productDescription: String?,
            @SerializedName("product_name")
            val productName: String,
            @SerializedName("product_quantity")
            var productQuantity: String,
            @SerializedName("products_id")
            val productsId: String,
            @SerializedName("products_orders_id")
            val productsOrdersId: String,
            @SerializedName("product_image_url")
            val productImageUrl: String,
            @SerializedName("total_price")
            var totalPrice: String,
            @SerializedName("after_discount_price")
            val afterDiscountPrice: String,
            @SerializedName("booking_date")
            val bookingDate: String,
            @SerializedName("car_size")
            val carSize: String,
            @SerializedName("coupon_id")
            val couponId: String,
            @SerializedName("coupons_id")
            val couponsId: String,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("deleted_at")
            val deletedAt: String,
            @SerializedName("end_time")
            val endTime: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("price")
            val price: String,
            @SerializedName("product_id")
            val productId: String,
            @SerializedName("product_order_id")
            val productOrderId: String,
            @SerializedName("service_detail")
            val serviceDetail: ServiceDetail,
            @SerializedName("servicequotes_id")
            val servicequotesId: String,
            @SerializedName("services_id")
            val servicesId: String,
            @SerializedName("special_condition_id")
            val specialConditionId: String,
            @SerializedName("start_time")
            val startTime: String,
            @SerializedName("status")
            val status: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("users_id")
            val usersId: String,
            @SerializedName("users_latitude")
            val usersLatitude: String,
            @SerializedName("users_longitude")
            val usersLongitude: String,
            @SerializedName("workshop_address_id")
            val workshopAddressId: String,
            @SerializedName("workshop_user_day_timings_id")
            val workshopUserDayTimingsId: String,
            @SerializedName("workshop_user_days_id")
            val workshopUserDaysId: String,
            @SerializedName("workshop_user_id")
            val workshopUserId: String,
            @SerializedName("wrecker_service_type")
            val wreckerServiceType: String,
            @SerializedName("workshop_details")
            val workshopDetail: WorkshopDetails,
            @SerializedName("CartType")
            var CartType: String,
            @SerializedName("max_products_quantity")
            var maxProductQuantity: String,
            @SerializedName("final_order_price")
            val finalOrderPrice: String,
            @SerializedName("vat")
            val ProductVat: String,
            @SerializedName("service_vat")
            val serviceVat: String,
            @SerializedName("avilability")
            var avilability: String?,
            @SerializedName("part_details")
            val partDetails: List<Part>,
            @SerializedName("pair")
            val IsProductPair: String?,
            @SerializedName("coupon_title")
            val couponTitle: String?,
            @SerializedName("coupon_type")
            val couponType: String,
            @SerializedName("coupon_price")
            val CouponPrices: String

    ) : ListItemViewModel()


    data class NotificationList(
            @SerializedName("id")
            val messageId: String,
            @SerializedName("title")
            val title: String,
            @SerializedName("subject")
            val subject: String,
            @SerializedName("content")
            val content: String,
            @SerializedName("file_url")
            val fileUrl: String,
            @SerializedName("file")
            val file: String,
            @SerializedName("created_at")
            val createdAt: String

    ) : ListItemViewModel()


    data class SparePartVisibility(
            @SerializedName("id")
            val orderIdPart: String

    ) : ListItemViewModel()


    data class TicketList(
            @SerializedName("id")
            val ticketId: String,
            @SerializedName("token")
            val token: String,
            @SerializedName("ticket_type")
            val ticket_type: String,
            @SerializedName("created_at")
            val created_at: String,
            @SerializedName("status")
            val status: String,
            @SerializedName("ticket_type_name")
            val tickettypename: String,
            @SerializedName("messages")
            val messages: List<Messages>

    ) : ListItemViewModel()

    data class Messages(
            @SerializedName("id")
            val ticketId: String,
            @SerializedName("sender_id")
            val sender_id: String,
            @SerializedName("messages")
            val messages: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("created_at")
            val created_at: String
    ) : ListItemViewModel()


    data class WishList(
            @SerializedName("id")
            val id: Int,
            @SerializedName("product_id")
            val productId: Int,
            @SerializedName("product_type")
            val productType: Int,
            @SerializedName("spare_product_detail")
            val spareProductDetail: Part?,
            @SerializedName("tyre_product_detail")
            val tyreProductDetail: WishListTyreProductDetail?,
            @SerializedName("wishlist_type")
            val wishlistType: Int,
            @SerializedName("workshop_detail")
            val workshopDetails: WorkshopDetails?,
            @SerializedName("workshop_id")
            val workshopId: Int
    )


    data class WishListTyreProductDetail(
            @SerializedName("brand_image")
            val brandImage: String,
            @SerializedName("brands")
            val brands: String,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("deleted_at")
            val deletedAt: String?,
            @SerializedName("delivery_days")
            val deliveryDays: String?,
            @SerializedName("description")
            val description: String,
            @SerializedName("discount")
            val discount: String?,
            @SerializedName("ean_number")
            val eanNumber: String,
            @SerializedName("extRollingNoiseDb")
            val extRollingNoiseDb: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("imageUrl")
            val imageUrl: String,
            @SerializedName("images")
            val images: List<Image>,
            @SerializedName("itemId")
            val itemId: String,
            @SerializedName("load_speed_index")
            val loadpeedIndex: String?,
            @SerializedName("manufacturer_description")
            val manufacturerDescription: String,
            @SerializedName("max_aspect_ratio")
            val maxAspectRatio: String,
            @SerializedName("max_diameter")
            val maxDiameter: String,
            @SerializedName("max_width")
            val maxWidth: String,
            @SerializedName("meta_key_title")
            val metaKeyTitle: String?,
            @SerializedName("meta_key_word")
            val metaKeyWord: String?,
            @SerializedName("our_description")
            val ourDescription: String?,
            @SerializedName("PFU")
            val pFU: String?,
            @SerializedName("pair")
            val pair: String?,
            @SerializedName("peak_mountain_snowflake")
            val peakMountainSnowflake: String?,
            @SerializedName("pr_description")
            val prDescription: String,
            @SerializedName("price")
            val price: String,
            @SerializedName("product_id")
            val productId: Int,
            @SerializedName("product_type")
            val productType: Int,
            @SerializedName("quantity")
            val quantity: String?,
            @SerializedName("reinforced")
            val reinforced: String?,
            @SerializedName("rollingResistance")
            val rollingResistance: String,
            @SerializedName("runflat")
            val runflat: String?,
            @SerializedName("season_name")
            val seasonName: String,
            @SerializedName("season_tyre_type")
            val seasonTyreType: String,
            @SerializedName("seller_price")
            val sellerPrice: String,
            @SerializedName("short_notation_description")
            val shortNotationDescription: List<String>,
            @SerializedName("speed_index")
            val speedIndex: String,
            @SerializedName("status")
            val status: String,
            @SerializedName("stock_warning")
            val stockWarning: String?,
            @SerializedName("substract_stock")
            val substractStock: String,
            @SerializedName("tax")
            val tax: String?,
            @SerializedName("tax_value")
            val taxValue: String?,
            @SerializedName("type")
            val type: String,
            @SerializedName("type_status")
            val typeStatus: String,
            @SerializedName("tyre_detail_response")
            val tyreDetailResponse: String,
            @SerializedName("tyreLabelUrl")
            val tyreLabelUrl: String,
            @SerializedName("tyre_max_size")
            val tyreMaxSize: String,
            @SerializedName("tyre_response")
            val tyreResponse: String,
            @SerializedName("unique_id")
            val uniqueId: String,
            @SerializedName("unit")
            val unit: String?,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("user_id")
            val userId: String,
            @SerializedName("vehicle_tyre_type")
            val vehicleTyreType: String,
            @SerializedName("vhicle_type")
            val vhicleType: String,
            @SerializedName("wetGrip")
            val wetGrip: String,
            @SerializedName("wholesalerArticleNo")
            val wholesalerArticleNo: String,
            @SerializedName("wish_list")
            val wishList: String,
            @SerializedName("wishlist_type")
            val wishlistType: String,
            @SerializedName("workshop_id")
            val workshopId: String,
            @SerializedName("rating_star")
            val rating_star: String,
            @SerializedName("rating_count")
            val rating_count: String, @SerializedName("tyre_season_image")
            val tyreSeasonImageURL: String = "",
            @SerializedName("tyre_type_image")
            val tyreImageURL: String,
            @SerializedName("pfu")
            val tyrePfu: tyrePfu?,
            var SelectedTyreCouponId: String? = "",
            @SerializedName("coupon_list")
            val couponList: List<Coupon>?,

            @SerializedName("assemble_status")
            val assemblestatus: String?,
            val tyre_label_images: List<TyreImage>? = null
    )

    data class ProductOrWorkshopList(
            @SerializedName("main_category_id")
            val main_category_id: String,
            @SerializedName("item_id")
            val item_id: String,
            @SerializedName("p_name")
            val productName: String,
            @SerializedName("p_description")
            val Productdescription: String,
            @SerializedName("assemble_kromeda_time")
            val assembleKromedaTime: String,
            @SerializedName("assemble_status")
            val assembleStatus: String,
            @SerializedName("assemble_time")
            val assembleTime: String?,
            @SerializedName("bar_code")
            val barCode: String?,
            @SerializedName("brand_image")
            val brandImage: String,
            @SerializedName("brand_image_url")
            val brandImageurl: String,
            @SerializedName("CodiceArticolo")
            val codiceArticolo: String,
            @SerializedName("CodiceListino")
            val codice: String,
            @SerializedName("coupon_list")
            val couponList: List<Coupon>,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("cs")
            val cs: String,
            @SerializedName("deleted_at")
            val deletedAt: String?,
            @SerializedName("descrizione")
            val descrizione: String,
            @SerializedName("F")
            val f: String?,
            @SerializedName("for_pair")
            val forPair: String?,
            @SerializedName("Foto")
            val foto: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("images")
            val images: List<Image>?,


            @SerializedName("kromeda_description")
            val kromedaDescription: String,
            @SerializedName("kromeda_products_id")
            val kromedaProductsId: String,
            @SerializedName("listino")
            val listino: String,
            @SerializedName("max_price")
            val maxPrice: String,
            @SerializedName("meta_key_title")
            val metaKeyTitle: String?,
            @SerializedName("meta_key_words")
            val metaKeyWords: String?,
            @SerializedName("min_price")
            val minPrice: String?,
            @SerializedName("minimum_quantity")
            val minimumQuantity: String?,
            @SerializedName("n")
            val n: String,
            @SerializedName("our_products_description")
            val ourProductsDescription: Any?,
            @SerializedName("out_of_stock_status")
            val outOfStockStatus: String,
            @SerializedName("price")
            val price: String,
            @SerializedName("products_groups_group_id")
            val productsGroupsGroupId: String?,
            @SerializedName("products_groups_id")
            val productsGroupsId: String,
            @SerializedName("products_groups_items_id")
            val productsGroupsItemsId: String,
            @SerializedName("products_groups_items_item_id")
            val productsGroupsItemsItemId: String,
            @SerializedName("products_item_numbers_id")
            val productsItemNumbersId: String,
            @SerializedName("products_json")
            val productsJson: String?,
            @SerializedName("products_name")
            val productsName: String,
            @SerializedName("products_quantiuty")
            val productsQuantiuty: String?,
            @SerializedName("products_status")
            val productsStatus: String,
            @SerializedName("S")
            val s: String,
            @SerializedName("seller_price")
            val sellerPrice: String?,
            @SerializedName("substract_stock")
            val substractStock: String,
            @SerializedName("tax")
            val tax: String?,
            @SerializedName("tax_value")
            val taxValue: String?,
            @SerializedName("tipo")
            val tipo: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("unique_id")
            val uniqueId: String,
            @SerializedName("unit")
            val unit: String?,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("users_id")
            val usersId: String,
            @SerializedName("v")
            val v: String,
            @SerializedName("wish_list")
            var wish_list: String,
            ////// Added
            @SerializedName("about_business")
            val aboutBusiness: String,
            @SerializedName("about_services")
            val aboutServices: String?,
            @SerializedName("available_status")
            val availableStatus: String,
            @SerializedName("business_name")
            val businessName: String,
            @SerializedName("category_id")
            val categoryId: String?,
            @SerializedName("company_name")


            val companyName: String,


            @SerializedName("service_id")


            val service_id: String,


            @SerializedName("f_name")
            val fName: String,
            @SerializedName("l_name")
            val lName: String,
            @SerializedName("mobile_number")
            val mobileNumber: String,
            @SerializedName("owner_name")
            val ownerName: String,
            @SerializedName("profile_image")
            val profileImage: String,
            @SerializedName("profile_image_url")
            val profileImageUrl: String,
            @SerializedName("rating")
            val rating: Rating,
            @SerializedName("rating_count")
            val ratingCount: String,
            @SerializedName("rating_star")
            val ratingStar: String,
            @SerializedName("registered_office")
            val registeredOffice: String,
            @SerializedName("service_average_time")
            val serviceAverageTime: String,
            @SerializedName("service_images")
            val serviceImages: Any?,
            @SerializedName("services_price")
            val servicesPrice: String,
            @SerializedName("average_time_in_min")
            val average_time_in_min: String,
            @SerializedName("latitude")
            val latitude: String,
            @SerializedName("longitude")
            val longitude: String,
            @SerializedName("workshop_user_days_id")
            val workshop_user_days_id: String,
            @SerializedName("product_image_url")
            val product_image_url: String,
            @SerializedName("max_appointment")
            val max_appointment: String,
            @SerializedName("hourly_rate")
            val hourly_rate: String,
            @SerializedName("temp_slot_id")
            val temp_slot_id: String,
            @SerializedName("service_time_list")
            val service_time_list: String


    ) : ListItemViewModel()


    data class brand(
            @SerializedName("id")
            val id: String?,
            @SerializedName("brand_name")
            val brandName: String?,
            @SerializedName("ischecked")
            var isBrandchecked: Boolean = false
    )


    data class servicesCouponData(
            @SerializedName("service_partcouponID")
            val service_partcouponID: String = "",
            @SerializedName("part_id")
            val service_partID: String = "",
            @SerializedName("seller_id")
            val seller_ID: String = "",
            @SerializedName("quantity")
            val partQty: String = "",
            @SerializedName("serviceId")
            val servicesid: String = ""
    ) : Serializable

    data class MotservicesCouponData(
            val service_partcouponID: ArrayList<String> = ArrayList(),
            val service_partID: ArrayList<String> = ArrayList(),
            val seller_ID: ArrayList<String> = ArrayList()

    ) : Serializable


    data class HighRatingfeedback(
            val comments: String,
            val created_at: String,
            val feedback_image: List<FeedbackImage>,
            val first_name: String,
            val id: String,
            val last_name: String,
            val main_category_id: String,
            val order_id: String,
            @SerializedName("product_detail")
            val ProductFeedbackDetail: ProductFeedbackDetail,
            val product_type: String,
            val products_id: String,
            val profile_image: String,
            val rating: String,
            val seller_id: String,
            val service_detail: serviceDetailFeedback,
            val service_id: String,
            val status: String,
            val type: String,
            val users_id: String,
            val workshop_details: WorkshopfeedbackDetails,
            val workshop_id: String
    ) : Serializable


    data class ProductFeedbackDetail(
            val description: String,
            val product_image_url: String,
            val product_name: String
    ) : Serializable

    data class serviceDetailFeedback(
            val service_name: String
    ) : Serializable

    data class WorkshopfeedbackDetails(
            val about_business: String,
            val address_2: String,
            val address_3: String,
            val business_name: String,
            val company_name: String,
            val f_name: String,
            val id: String,
            val l_name: String,
            val mobile_number: String,
            val owner_name: String,
            val profile_image: String,
            val registered_office: String
    ) : Serializable

    data class TyreDetailData(
            val brand_image: String? = "",
            val description: String? = "",
            val ean_number: String? = "",
            val noiseDb: String? = "",
            val id: Int?,
            val imageUrl: String? = "",
            val images: List<TyreImage>? = null,
            val tyre_label_images: List<TyreImage>? = null,
            val price: String? = "",
            val manufacturer_description: String? = "",
            val max_aspect_ratio: String? = "",
            val max_diameter: String? = "",
            val max_price: String? = "",
            val max_width: String? = "",
            @SerializedName("min_prices")
            val min_price: String? = "",
            val our_description: Any?,
            val pair: String?,
            val pr_description: String? = "",
            val quantity: String?,
            val rollingResistance: String? = "",
            val seller_price: String? = "",
            val speed_index: String? = "",
            val type: String? = "",
            val tyre_response: String? = "",
            val user_id: Int?,
            val wetGrip: String? = "",
            val wholesalerArticleNo: String? = "",
            val is3PMSF: String? = "",
            val season_tyre_type: String? = "",
            var wish_list: String? = "0",
            @SerializedName("rating")
            val rating: Rating?,
            @SerializedName("rating_count")
            val ratingCount: String,
            @SerializedName("rating_star")
            val ratingStar: String,
            @SerializedName("coupon_list")
            val couponList: List<Coupon>?,
            @SerializedName("load_speed_index")
            val load_speed_index: String = "",
            @SerializedName("assemble_status")
            val assemblestatus: String,
            @SerializedName("tyre_season_image")
            val tyreSeasonImageURL: String = "",
            @SerializedName("tyre_type_image")
            val tyreImageURL: String,
            @SerializedName("pfu")
            val tyrePfu: tyrePfu?,
            var SelectedTyreCouponId: String? = "",
            val noice_db_arr: NoiceDbArr,
            val rolling_resistance_arr: RollingResistanceArr,
            val wet_grip_arr: WetGripArr,
            @SerializedName("number_of_delivery_days")
            val delivery_days: String,
            @SerializedName("main_category_id")
            val tyre_mainCategory_id: String,
            @SerializedName("tyre_dot")
            val tyreDot: String,
            @SerializedName("service_id")
            val serviceId: String,
            @SerializedName("service_average_time")
            val servicesAverageTime: String,
            @SerializedName("type_status")
            val typeStatus: String?,
            @SerializedName("season_desc")
            val seasonName: String?,
            @SerializedName("load_speed_index_desc")
            val loadIndexDesc: String?,
            @SerializedName("speed_index_desc")
            val speedIndexDesc: String?
    ) : Serializable


    data class NoiceDbArr(
            val description: String,
            val icon: String,
            val name: String,
            val graphical_image: String,
            @SerializedName("name_2")
            val title: String
    ) : Serializable


    data class RollingResistanceArr(
            val description: String,
            val icon: String,
            val name: String,
            val graphical_image: String,
            @SerializedName("name_2")
            val title: String

    ) : Serializable


    data class WetGripArr(
            val description: String,
            val icon: String,
            val name: String,
            val graphical_image: String,
            @SerializedName("name_2")
            val title: String
    ) : Serializable


    data class FAQ_Question_Answer(
            @SerializedName("created_at")
            val created_at: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("is_approve")
            val is_approve: String,
            @SerializedName("terms_conditions_detail")
            val terms_conditions_detail: String,
            @SerializedName("title")
            val title: String,
            @SerializedName("updated_at")
            val updated_at: String
    )

    data class Coupon1(
            @SerializedName("amount")
            var amount: Any?,
            @SerializedName("coupon_quantity")
            var couponQuantity: Any?,
            @SerializedName("coupon_title")
            var couponTitle: String,
            @SerializedName("id")
            var id: String,
            @SerializedName("users_id")
            val usersId: String

    ) : ListItemViewModel()

    data class BestSellingProduct_home(

            @SerializedName("spare_parts")
            val spareProductDetail: ProductOrWorkshopList?,
            @SerializedName("tyre_parts")
            val tyreProductDetail: TyreDetailItem)

    data class MotSchedule(
            val id: String = "",
            val schedule_id: String = "",
            val service_schedule_description: String = "",
            val service_schedule_id: String = "",
            val sort_order: String = "",
            val version_id: String = ""
    ) : Serializable

    data class CarCriteria(
            val created_at: String = "",
            val cron_executed_status: String = "",
            val id: String = "",
            val language: String = "",
            val repair_times_description: String = "",
            val repair_times_id: String = "",
            val unique_id: String = "",
            val updated_at: String = "",
            val users_id: String = "",
            val version_id: String = ""
    ) : Serializable


    data class SparePartSearchData(
            @SerializedName("n3response")
            val n3response: ArrayList<Search_N3response>,
            @SerializedName("oeResponse")
            val oeResponse: ArrayList<Search_OenPart>,
            @SerializedName("spareParts")
            val spareParts: ArrayList<Search_SparePart>,
            @SerializedName("ourn3response")
            val ourn3response: ArrayList<Search_N3response>
    )

    data class Search_N3response(
            @SerializedName("name")
            val name: String,
            @SerializedName("search_type")
            val type: String
    )

    data class Search_SparePart(
            @SerializedName("name")
            val name: String,
            @SerializedName("search_type")
            val type: String,
            @SerializedName("productid")
            val productid: String,
            @SerializedName("products_name")
            val productsName: String

    )
    data class Search_SparePart_Category(
            @SerializedName("our_category_name")
            val our_category_name: String,
            @SerializedName("item")
            val item: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("version_id")
            val version_id: String

    )

    data class Search_OenPart(
            @SerializedName("name")
            val name: String,
            @SerializedName("search_type")
            val type: String


    )

    data class BankPaymentInfo(
            @SerializedName("account_number")
            val accountNumber: String,
            @SerializedName("bank_name")
            val bankName: String,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("deleted_at")
            val deletedAt: Any?,
            @SerializedName("description")
            val description: String,
            @SerializedName("email")
            val email: String,
            @SerializedName("iban")
            val iban: String,
            @SerializedName("id")
            val id: String,
            @SerializedName("instatario")
            val instatario: String,
            @SerializedName("title")
            val title: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("payment_name")
            val payment_name: String,
            @SerializedName("payment_description")
            val payment_description: String
    )

}


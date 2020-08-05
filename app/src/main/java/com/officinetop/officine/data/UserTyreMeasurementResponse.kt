package com.officinetop.officine.data

import com.google.gson.annotations.SerializedName

data class UserTyreMeasurementResponse(

        @field:SerializedName("status_code")
        val statusCode: Int? = null,

        @field:SerializedName("data")
        val mData: Any? = null,

        @field:SerializedName("message")
        val message: Any? = null,

        @field:SerializedName("data_set")
        val mDataSet: MutableList<MeasurementDataSetItem?>? = null
)

data class GetTyreDetail(

        @field:SerializedName("idVeicolo")
        val idVeicolo: Int? = null,

        @field:SerializedName("Status")
        val status: String? = null,

        @field:SerializedName("Versione")
        val versione: String? = null,

        @field:SerializedName("Motore")
        val motore: String? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("Al")
        val al: String? = null,

        @field:SerializedName("Kw")
        val kw: String? = null,

        @field:SerializedName("Dal")
        val dal: String? = null,

        @field:SerializedName("ModelloCodice")
        val modelloCodice: String? = null,

        @field:SerializedName("Cv")
        val cv: String? = null,

        @field:SerializedName("Cilindri")
        val cilindri: String? = null,

        @field:SerializedName("Valvole")
        val valvole: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("unnique_key")
        val unniqueKey: String? = null,

        @field:SerializedName("Alimentazione")
        val alimentazione: String? = null,

        @field:SerializedName("model")
        val model: String? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("Body")
        val body: String? = null,

        @field:SerializedName("PorteComm")
        val porteComm: String? = null
)

data class MeasurementDataSetItem(

        @field:SerializedName("car_version_id")
        val carVersionId: Int? = null,

        @field:SerializedName("aspect_ratio")
        val aspectRatio: Any? = null,

        @field:SerializedName("vehicle_type")
        val vehicleType: String? = null,

        @field:SerializedName("vehicle_name")
        val vehicleName: String? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("reinforced")
        val reinforced: String? = null,

        @field:SerializedName("execute")
        val execute: Int? = null,

        @field:SerializedName("deleted_at")
        val deletedAt: Any? = null,

        @field:SerializedName("speedindex")
        val speedindex: String? = null,

        @field:SerializedName("run_flat")
        val runFlat: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("user_id")
        val userId: Int? = null,

        @field:SerializedName("width")
        val width: Int? = null,

        @field:SerializedName("season")
        val season: String? = null,

        @field:SerializedName("season_name")
        val season_name: String? = null,
        @field:SerializedName("rim_diameter")
        val rimDiameter: Any? = null,
        @field:SerializedName("get_tyre_detail")
        val getTyreDetail: GetTyreDetail? = null,
        @field:SerializedName("id")
        val id: Int? = null,
        @field:SerializedName("measurment")
        val measurment: Measurment? = null,
        @SerializedName("season_status")
        val seasonStatus: String,
        @SerializedName("speedindex_status")
        val speedindexStatus: String,
        @SerializedName("vehicle_type_status")
        val vehicleTypeStatus: String

)


data class Measurment(
        @field:SerializedName("max_aspect_ratio")
        val max_aspect_ratio: String,
        @field:SerializedName("max_diameter")
        val max_diameter: String,
        @field:SerializedName("max_width")
        val max_width: String,
        @field:SerializedName("min_aspect_ratio")
        val min_aspect_ratio: String,
        @field:SerializedName("min_diameter")
        val min_diameter: String,
        @field:SerializedName("min_width")
        val min_width: String
)


data class mea(
        @SerializedName("aspect_ratio")
        val aspectRatio: String,
        @SerializedName("car_version_id")
        val carVersionId: Int,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("deleted_at")
        val deletedAt: Any?,
        @SerializedName("execute")
        val execute: Int,
        @SerializedName("id")
        val id: Int,
        @SerializedName("reinforced")
        val reinforced: String,
        @SerializedName("rim_diameter")
        val rimDiameter: String,
        @SerializedName("run_flat")
        val runFlat: String,
        @SerializedName("season")
        val season: Int,
        @SerializedName("season_status")
        val seasonStatus: String,
        @SerializedName("speedindex")
        val speedindex: Int,
        @SerializedName("speedindex_status")
        val speedindexStatus: String,
        @SerializedName("updated_at")
        val updatedAt: String,
        @SerializedName("user_id")
        val userId: Int,
        @SerializedName("vehicle_type")
        val vehicleType: Int,
        @SerializedName("vehicle_type_status")
        val vehicleTypeStatus: String,
        @SerializedName("width")
        val width: Int
)
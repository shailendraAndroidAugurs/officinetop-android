package com.officinetop.officine.retrofit

import com.officinetop.officine.BuildConfig
import com.officinetop.officine.utils.Constant
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


object  RetrofitClient {


    val client: IRetrofitApis
        get() = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constant.baseUrl)
                .client(OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .readTimeout(100, TimeUnit.SECONDS)
                        .writeTimeout(100, TimeUnit.SECONDS)
                        .addInterceptor {
                            val request = it.request().newBuilder()
                                    .addHeader("Accept", "application/json")
                                    .build()
                            it.proceed(request)
                        }
                        .addInterceptor(HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE))
                        .build())
                .build().create(IRetrofitApis::class.java)


    val linkedInClient: IRetrofitApis
        get() = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constant.linkedInURL)
                .build().create(IRetrofitApis::class.java)


//    val clientWithEmptyBaseURL : IRetrofitApis
//    get() = Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .baseUrl(Constant.domainBaseURL)
//            .build().create(IRetrofitApis::class.java)

    abstract class networkInterceptor : Interceptor {

        abstract val isInternetAvailable: Boolean

        abstract fun onInternetUnavailable()

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (!isInternetAvailable) {
                onInternetUnavailable()
            }
            return chain.proceed(request)
        }
    }


}
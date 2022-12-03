package de.wbh.storageboxmanager.retrofit

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.wbh.storageboxmanager.*
import de.wbh.storageboxmanager.model.Item
import de.wbh.storageboxmanager.model.ItemPost
import de.wbh.storageboxmanager.model.StorageBox
import de.wbh.storageboxmanager.model.StorageBoxPost
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://192.168.178.42:8080"

// Erzeugen vom Moshi Objekt mit Kotlin Adapter Factory
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Retrofit Objekt mit Moshi Converter
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ApiService {

    // Calls für Boxen
    @GET("/boxes")
    suspend fun getBoxes() : List<StorageBox>

    @GET("/itemsinbox/{boxID}")
    suspend fun getItemsInBox(@Path("boxID") boxID: Long) : List<Item>

    @DELETE("/boxes/{boxID}")
    suspend fun deleteBox(@Path("boxID") boxID: Long)

    @POST("/boxes")
    suspend fun addBox(@Body box: StorageBoxPost)

    @PUT("/boxes/{boxID}")
    suspend fun modBox(@Path("boxID") boxID: Long, @Body box: StorageBoxPost)

    // Calls für Items
    @POST("/items/{boxID}")
    suspend fun addItem(@Path("boxID") boxID: Long, @Body item: ItemPost)

    @DELETE("/items/{itemID}")
    suspend fun deleteItem(@Path("itemID") itemID: Long)

    @PUT("/items/{boxID}")
    suspend fun modItem(@Path("boxID") boxID: Long, @Body item: Item)

    @GET("/itemssearch/{boxID}")
    suspend fun searchItemsInBox(@Path("boxID") boxID: Long, @Query("name") name: String,
                                @Query("desc") desc: String) : List<Item>
}

object Api {
    val retrofitService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}



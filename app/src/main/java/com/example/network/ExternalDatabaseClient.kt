package com.example.network

import com.example.data.Booking
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ExternalSyncResponse(
    val success: Boolean,
    val message: String?,
    val data: List<Booking>?
)

object ExternalDatabaseClient {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Test connection to the remote custom server base URL
     */
    suspend fun pingServer(baseUrl: String): Boolean = withContext(Dispatchers.IO) {
        val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        try {
            val request = Request.Builder()
                .url(cleanUrl)
                .head()
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 404 // 404 is fine as long as server responded
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Post all current local bookings to sync with the remote server database.
     */
    suspend fun uploadBookings(baseUrl: String, bookings: List<Booking>): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val finalUrl = "${cleanUrl}bookings/sync"

        try {
            val listAdapter = moshi.adapter(List::class.java)
            // Convert list to dynamic serializable map list
            val serializedList = bookings.map {
                mapOf(
                    "productName" to it.productName,
                    "productPrice" to it.productPrice,
                    "customerName" to it.customerName,
                    "customerPhone" to it.customerPhone,
                    "bookingDate" to it.bookingDate,
                    "notes" to it.notes,
                    "status" to it.status
                )
            }
            val jsonString = listAdapter.toJson(serializedList)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("Đồng bộ liên kết thành công! Đã tải ${bookings.size} lịch hẹn lên cơ sở dữ liệu server.")
                } else {
                    Result.failure(Exception("Lỗi máy chủ phản hồi: mã lỗi ${response.code}"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Không thể kết nối đến máy chủ bên ngoài: ${e.localizedMessage}"))
        }
    }

    /**
     * Pull remote items to update our local database state.
     */
    suspend fun downloadBookings(baseUrl: String): Result<List<Booking>> = withContext(Dispatchers.IO) {
        val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val finalUrl = "${cleanUrl}bookings"

        try {
            val request = Request.Builder()
                .url(finalUrl)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val rawBody = response.body?.string() ?: ""
                    // Parse raw list of bookings
                    val jsonAdapter = moshi.adapter(Array<BookingRemoteModel>::class.java)
                    val parsed = jsonAdapter.fromJson(rawBody)
                    if (parsed != null) {
                        val converted = parsed.map {
                            Booking(
                                productName = it.productName ?: "Sản phẩm không rõ",
                                productPrice = it.productPrice ?: 0.0,
                                customerName = it.customerName ?: "Khách hàng mới",
                                customerPhone = it.customerPhone ?: "",
                                bookingDate = it.bookingDate ?: "2026-05-24",
                                notes = it.notes ?: "Nhập từ Remote DB Server.",
                                status = it.status ?: "Chờ xác nhận"
                            )
                        }
                        Result.success(converted)
                    } else {
                        Result.failure(Exception("Dữ liệu phản hồi lỗi định dạng."))
                    }
                } else {
                    Result.failure(Exception("Lỗi máy chủ từ xa: mã lỗi ${response.code}"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Không thể tải dữ liệu từ server: ${e.localizedMessage}"))
        }
    }
}

@JsonClass(generateAdapter = true)
data class BookingRemoteModel(
    val productName: String?,
    val productPrice: Double?,
    val customerName: String?,
    val customerPhone: String?,
    val bookingDate: String?,
    val notes: String?,
    val status: String?
)

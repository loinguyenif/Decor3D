package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = 0.7f,
    val maxOutputTokens: Int? = 1000
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiService = retrofit.create(GeminiService::class.java)

    /**
     * Executes the Gemini request with proper system instructions
     */
    suspend fun getFurnitureAdvice(history: List<Content>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Chào bạn! Tôi là Trợ lý Thiết kế Decor3D AI. Hiện tại API Key chưa được bổ sung vào cài đặt bí mật (Secrets panel) của Google AI Studio. Vui lòng liên hệ quản trị viên hoặc thiết lập khóa để trò chuyện với AI. Tuy nhiên, bạn vẫn có thể sử dụng đầy đủ tính năng thiết kế 3D và Đặt hàng bình thường!"
        }

        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = """
                    Bạn là Trợ Lý Thiết Kế & Tư Vấn Nội Thất VIP của Decor3D (Decor3D Smart Advisor). Nhiệm vụ của bạn là tư vấn tận tâm và chuyên nghiệp cho khách hàng bằng Tiếng Việt.
                    Hãy trả lời các câu hỏi về: phong cách thiết kế nội thất (Scandinavian, hiện đại, tối giản, cổ điển, Đông Dương), cách bố trí tối ưu diện tích phòng, phối màu, lựa chọn vật liệu tốt phù hợp và dự toán chi phí.
                    
                    Dưới đây là danh sách các sản phẩm thực tế của Decor3D để bạn có thể gợi ý trực tiếp kèm báo giá phù hợp tài chính của khách:
                    1. Sofa Da Scandinavian Luxury (Bản da cao cấp cực sang, khung Gỗ óc chó) - Giá: 15.500.000đ
                    2. Bàn Ăn Gỗ Nguyên Khối Rustic (Gỗ Óc Chó nguyên tấm, Vân tự nhiên uốn lượn u sầu ấm cúng) - Giá: 8.900.000đ
                    3. Giường Ngủ Velvet Cozy (Bọc vải nhung cực êm mềm, đầu giường lót foam dày) - Giá: 12.200.000đ
                    4. Ghế Làm Việc Ergonomic Elite (Ghế công thái học bảo vệ cột sống, lưới 3D nâng đỡ êm ái) - Giá: 4.500.000đ
                    5. Tủ Sách Gỗ Walnut Minimalist (Chất liệu gỗ Walnut ghép sấy, phối kệ lệch tầng phá cách phong cách Nhật) - Giá: 7.800.000đ

                    Nguyên tắc trả lời:
                    - Luôn dùng Tiếng Việt, giữ tông giọng nhã nhặn, sang trọng, lịch sự.
                    - Gợi ý chi tiết sản phẩm thuộc Catalog Decor3D ở trên khi thấy cơ hội.
                    - Hướng dẫn khách hàng nếu muốn mua hoặc đặt tư vấn khảo sát mặt bằng tại nhà thì hãy bấm nút "Mua Ngay" hoặc chuyển sang tab "Đặt Hàng" ngay trong ứng dụng để nhập thông tin đặt mua giữ lịch nhanh chóng nhất.
                    - Định dạng câu trả lời sử dụng Markdown sạch sẽ (gạch đầu dòng, tô đậm tên sản phẩm, vẽ bảng báo giá nếu cần).
                    """.trimIndent()
                )
            )
        )

        return try {
            val response = service.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = history,
                    systemInstruction = systemInstruction,
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )
            )
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            replyText ?: "Xin lỗi bạn, tôi không thể phân tích nội dung tư vấn ngay lúc này. Hãy thử hỏi lại nhé!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Đã gặp lỗi khi kết nối với máy chủ Decor3D AI: ${e.localizedMessage}. Vui lòng kiểm tra cổng mạng hoặc kết nối internet."
        }
    }
}

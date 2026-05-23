package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Booking
import com.example.data.ChatMessage
import com.example.data.DecorRepository
import com.example.data.Product
import com.example.data.ProductCatalog
import com.example.network.Content
import com.example.network.GeminiApiClient
import com.example.network.Part
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    Catalog,
    Design3D,
    Chat,
    Bookings
}

class DecorViewModel(private val repository: DecorRepository) : ViewModel() {

    // Primary UI Screens View state
    var currentScreen by mutableStateOf(Screen.Catalog)

    // Catalog filtering and detail focus state
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("Tất cả")
    var selectedProduct by mutableStateOf<Product?>(null)

    // Interactive 3D Studio specifications
    var selected3DProduct by mutableStateOf(ProductCatalog.items[0])
    var active3DColorIndex by mutableIntStateOf(0)
    var selectedMaterialIn3D by mutableStateOf("Gỗ Tự Nhiên")
    var arSimulationEnabled by mutableStateOf(false)
    var roomLayoutType by mutableStateOf("Phòng Mẫu Studio") // Floor decoration layouts

    // Pitch & Yaw Rotations for Camera angle adjustment (radians)
    var angleY by mutableFloatStateOf(0.5f) // Horizontal orbit rotation
    var angleX by mutableFloatStateOf(-0.3f) // Vertical elevation rotation

    // Local DB Observables
    val bookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val chatMessages: StateFlow<List<ChatMessage>> = repository.allChatMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Gemini network consultation busy tracking
    var isAiLoading by mutableStateOf(false)
        private set

    // Internal active conversation memory block for API context
    private val rawChatHistory = mutableListOf<Content>()

    init {
        // Hydrate conversation memory directly from pre-existing DB cache on initialization
        viewModelScope.launch {
            val dbChats = repository.allChatMessages.first()
            if (dbChats.isEmpty()) {
                // Populate warm welcomes on the first execution
                val welcomeText = "Chào bạn! Tôi là Trợ lý Thiết kế Decor3D AI. Tôi có kiến thức sâu rộng về thiết kế nội thất Bắc Âu, vật liệu Gỗ Óc Chó, phối màu sắc và các giải pháp tối ưu không gian nhà hẹp. Hãy gửi câu hỏi bất kỳ, ví dụ như: 'Làm thế nào để decor phòng khách nhỏ phong cách tủ kệ hiện đại?'"
                repository.insertChatMessage(ChatMessage(sender = "assistant", content = welcomeText))
                rawChatHistory.add(Content(parts = listOf(Part(welcomeText))))
            } else {
                dbChats.forEach { msg ->
                    // Map db records to REST model format
                    val role = if (msg.sender == "user") "user" else "model"
                    rawChatHistory.add(Content(parts = listOf(Part(msg.content))))
                }
            }
        }
    }

    /**
     * Propagates prompt input safely, stores in local DB, and queries Gemini client.
     */
    fun sendUserMessage(content: String) {
        if (content.isBlank() || isAiLoading) return

        viewModelScope.launch {
            // Write user inquiry to SQLite
            repository.insertChatMessage(ChatMessage(sender = "user", content = content))
            
            // Mirror translation
            val currentTurn = Content(parts = listOf(Part(content)))
            rawChatHistory.add(currentTurn)

            // Engage busy UI spinner
            isAiLoading = true

            // REST callout
            val aiResponse = GeminiApiClient.getFurnitureAdvice(rawChatHistory)

            // Write agent reply to SQLite
            repository.insertChatMessage(ChatMessage(sender = "assistant", content = aiResponse))
            rawChatHistory.add(Content(parts = listOf(Part(aiResponse))))

            isAiLoading = false
        }
    }

    /**
     * Books a custom furniture configuration (measurements / design consult) online.
     */
    fun placeFurnitureBooking(
        productName: String,
        price: Double,
        customerName: String,
        customerPhone: String,
        bookingDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val info = Booking(
                productName = productName,
                productPrice = price,
                customerName = customerName,
                customerPhone = customerPhone,
                bookingDate = bookingDate,
                notes = notes, 
                status = "Chờ xác nhận"
            )
            repository.insertBooking(info)
        }
    }

    /**
     * Deletes a recorded booking.
     */
    fun removeBooking(booking: Booking) {
        viewModelScope.launch {
            repository.deleteBooking(booking)
        }
    }

    /**
     * Adjusts active reservation items status dynamically.
     */
    fun adjustBookingStatus(booking: Booking, newStatus: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(booking.id, newStatus)
        }
    }

    /**
     * Resets active AI consultation sessions back to starting prompts.
     */
    fun wipeConversationThreads() {
        viewModelScope.launch {
            repository.clearChatHistory()
            rawChatHistory.clear()
            val restartWelcome = "Cuộc trò chuyện đã được làm mới! Cho tôi biết bạn đang tìm kiếm ý tưởng thiết kế nội thất nào nhé."
            repository.insertChatMessage(ChatMessage(sender = "assistant", content = restartWelcome))
            rawChatHistory.add(Content(parts = listOf(Part(restartWelcome))))
        }
    }
}

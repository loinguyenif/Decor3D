package com.example.data

import kotlinx.coroutines.flow.Flow

class DecorRepository(
    private val bookingDao: BookingDao,
    private val chatMessageDao: ChatMessageDao
) {
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()
    val allChatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    suspend fun insertBooking(booking: Booking) {
        bookingDao.insertBooking(booking)
    }

    suspend fun deleteBooking(booking: Booking) {
        bookingDao.deleteBooking(booking)
    }

    suspend fun updateBookingStatus(id: Long, status: String) {
        bookingDao.updateBookingStatus(id, status)
    }

    suspend fun insertChatMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearHistory()
    }
}

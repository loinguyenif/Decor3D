package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val productPrice: Double,
    val customerName: String,
    val customerPhone: String,
    val bookingDate: String,
    val status: String, // "Pending", "Confirmed", "Completed"
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

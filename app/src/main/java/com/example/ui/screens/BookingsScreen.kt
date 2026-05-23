package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Booking
import com.example.ui.DecorViewModel
import com.example.ui.Screen
import java.text.DecimalFormat

@Composable
fun BookingsScreen(
    viewModel: DecorViewModel,
    modifier: Modifier = Modifier
) {
    val bookingList by viewModel.bookings.collectAsState()
    val df = DecimalFormat("#,###đ")

    val totalBudget = remember(bookingList) {
        bookingList.sumOf { it.productPrice }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "LỊCH HẸN & ĐƠN HÀNG",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Quản lý giữ chỗ khảo sát đo đạc thực tế & đơn hàng của bạn",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (bookingList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(36.dp))
                                .background(Color.LightGray.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = "Empty list",
                                modifier = Modifier.size(36.dp),
                                tint = Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Danh sách trống!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quý khách chưa đặt mua tủ ghế hoặc đặt chỗ đo đạc nào. Hãy lựa chọn sản phẩm quý khách ưng ý ở tab Cửa Hàng hoặc Trình Xem 3D nhé!",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.currentScreen = Screen.Catalog },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Storefront, "Store")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Xem Bộ Sưu Tập Ngay", fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // Statistics Summary Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Tổng Lịch Đặt", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                "${bookingList.size} dịch vụ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1.5f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Tổng Giá Trị Đơn", fontSize = 11.sp, color = Color.LightGray.copy(alpha = 0.7f))
                            Text(
                                df.format(totalBudget),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFB923C)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable List of Reservations/Bookings
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(bookingList, key = { it.id }) { booking ->
                        BookingItemRow(
                            booking = booking,
                            priceFormatted = df.format(booking.productPrice),
                            onDelete = { viewModel.removeBooking(booking) },
                            onUpdateStatus = { nextStatus ->
                                viewModel.adjustBookingStatus(booking, nextStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItemRow(
    booking: Booking,
    priceFormatted: String,
    onDelete: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Product name + Delete Icon
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Khách hàng: ${booking.customerName} - ${booking.customerPhone}",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hủy",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details section of date and price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hẹn ngày: ${booking.bookingDate}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }

                Text(
                    text = priceFormatted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFC2410C)
                )
            }

            // Client specifications notes
            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Yêu cầu đo đạc: ${booking.notes}",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(10.dp))

            // Status Indicator & Interactive confirmation toggles!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status design badge
                val badgeColor = when (booking.status) {
                    "Chờ xác nhận" -> Color(0xFFFBBF24) // Yellow
                    "Đã duyệt" -> Color(0xFF10B981) // Green
                    "Đã hoàn thành" -> Color(0xFF3B82F6) // Blue
                    else -> Color.Gray
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = booking.status,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interactive approval buttons so user can manipulate demo status
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (booking.status == "Chờ xác nhận") {
                        TextButton(
                            onClick = { onUpdateStatus("Đã duyệt") },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Duyệt Hẹn", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (booking.status == "Đã duyệt") {
                        TextButton(
                            onClick = { onUpdateStatus("Đã hoàn thành") },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Bàn Giao Xong", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

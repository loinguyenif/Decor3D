package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.data.ProductCatalog
import com.example.ui.DecorViewModel
import com.example.ui.Screen
import com.example.ui.components.Studio3DCanvas
import java.text.DecimalFormat

@Composable
fun Design3DScreen(
    viewModel: DecorViewModel,
    modifier: Modifier = Modifier
) {
    var showBookingSheetFrom3D by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val df = DecimalFormat("#,###đ")

    val currentProduct = viewModel.selected3DProduct
    val currentSelectedColorHex = currentProduct.colorValues[viewModel.active3DColorIndex]
    val currentMaterial = viewModel.selectedMaterialIn3D

    val floorStyles = listOf("Phòng Mẫu Studio", "Nhà Phố Minimalist", "Căn Hộ Scandinavian")
    val materials = listOf("Vải Nhung Cao Cấp", "Da Bò Tự Nhiên", "Gỗ Óc Chó Lau Sơn Dầu", "Nỉ Mịn Kháng Nước")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Header
        Column {
            Text(
                text = "TRÌNH XEM THIẾT KẾ 3D",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )
            Text(
                text = "Xoay đa góc nhìn, tinh chỉnh vật liệu & sắc độ nội thất trực quan",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // 1. Core 3D Studio Canvas Wrapper
        Studio3DCanvas(
            product = currentProduct,
            selectedColorHex = currentSelectedColorHex,
            materialType = currentMaterial,
            angleX = viewModel.angleX,
            angleY = viewModel.angleY,
            onAnglesChanged = { ax, ay ->
                viewModel.angleX = ax
                viewModel.angleY = ay
            },
            roomLayoutType = viewModel.roomLayoutType,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Select Model Row
        Column {
            Text(
                text = "Bước 1: Chọn mô hình sản phẩm",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(ProductCatalog.items) { index, product ->
                    val isSelected = currentProduct.id == product.id
                    Card(
                        onClick = {
                            viewModel.selected3DProduct = product
                            viewModel.active3DColorIndex = 0
                            viewModel.selectedMaterialIn3D = product.colors.firstOrNull() ?: "Tự Nhiên"
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (product.modelType) {
                                    "sofa" -> Icons.Default.Weekend
                                    "table" -> Icons.Default.TableRestaurant
                                    "bed" -> Icons.Default.Bed
                                    "chair" -> Icons.Default.Chair
                                    else -> Icons.Default.Inventory
                                },
                                contentDescription = "icon",
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. Choice of Colors with Round Indicators
        Column {
            Text(
                text = "Bước 2: Chọn sắc độ (Sơn / Da nỉ)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentProduct.colors.forEachIndexed { colorIdx, colorLabel ->
                    val valueHex = currentProduct.colorValues[colorIdx]
                    val isSelected = viewModel.active3DColorIndex == colorIdx

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.active3DColorIndex = colorIdx }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        // Colored Circle
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(valueHex))
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = colorLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                        )
                    }
                }
            }
        }

        // 4. Material Texture customization
        Column {
            Text(
                text = "Bước 3: Chọn chất liệu sản xuất",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(materials) { index, mat ->
                    val isSelected = currentMaterial == mat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedMaterialIn3D = mat },
                        label = { Text(mat, fontSize = 11.sp) }
                    )
                }
            }
        }

        // 5. Environmental layouts
        Column {
            Text(
                text = "Bước 4: Thay đổi không gian mẫu",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                floorStyles.forEach { style ->
                    val isSelected = viewModel.roomLayoutType == style
                    Button(
                        onClick = { viewModel.roomLayoutType = style },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else Color.LightGray.copy(alpha = 0.25f),
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(style, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        // Pricing and booking trigger
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Khác biệt phối màu 3D",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = df.format(currentProduct.price),
                        color = Color(0xFFC2410C),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { showBookingSheetFrom3D = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, "Shopping", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Đặt thiết kế này", fontSize = 13.sp)
                }
            }
        }
    }

    // Modal Booking Sheet triggered from 3D studio
    if (showBookingSheetFrom3D) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("2026-05-24") }
        val finalNotes = "Phối màu 3D: ${currentProduct.colors[viewModel.active3DColorIndex]}. Vật liệu: $currentMaterial. Phong cảnh: ${viewModel.roomLayoutType}."
        var showError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBookingSheetFrom3D = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank()) {
                            showError = "Vui lòng nhập Tên và Số điện thoại liên lạc!"
                        } else {
                            viewModel.placeFurnitureBooking(
                                productName = "${currentProduct.name} (Bản Phối 3D)",
                                price = currentProduct.price,
                                customerName = name,
                                customerPhone = phone,
                                bookingDate = date,
                                notes = finalNotes
                            )
                            showBookingSheetFrom3D = false
                            viewModel.currentScreen = Screen.Bookings
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C))
                ) {
                    Text("Gửi Thiết Kế Lên Hệ Thống")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingSheetFrom3D = false }) {
                    Text("Đóng")
                }
            },
            title = {
                Text("ĐẶT SẢN PHẨM KHỚP BẢN VẼ 3D", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Cấu hình sản phẩm: ${currentProduct.name}",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Vật liệu lựa chọn: $currentMaterial",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Sắc màu đính kèm: ${currentProduct.colors[viewModel.active3DColorIndex]}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; showError = "" },
                        label = { Text("Tên khách hàng") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; showError = "" },
                        label = { Text("Số điện thoại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Ngày khảo sát & bàn giao bản vẽ mẫu") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = finalNotes,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Cấu hình thiết kế ghi chú") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(showError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        )
    }
}

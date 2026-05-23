package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: DecorViewModel,
    modifier: Modifier = Modifier
) {
    var showBookingSheet by remember { mutableStateOf(false) }
    var bookingProduct by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("Tất cả", "Sofa", "Bàn", "Giường", "Ghế", "Tủ")
    val df = DecimalFormat("#,###đ")

    val filteredProducts = remember(viewModel.searchQuery, viewModel.selectedCategory) {
        ProductCatalog.items.filter {
            (viewModel.selectedCategory == "Tất cả" || it.category.equals(viewModel.selectedCategory, ignoreCase = true)) &&
            (viewModel.searchQuery.isBlank() || it.name.contains(viewModel.searchQuery, ignoreCase = true) || it.description.contains(viewModel.searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "BỘ SƯU TẬP NỘI THẤT",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Lựa chọn các mẫu thiết kế tinh xảo và sang trọng bậc nhất",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Search Box
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = { Text("Tìm sofa, bàn nguyên khối, tủ gỗ...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, "Search") },
                    trailingIcon = {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Categories Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.size) { index ->
                        val cat = categories[index]
                        val isSelected = viewModel.selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedCategory = cat },
                            label = { Text(cat, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chair,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Không tìm thấy sản phẩm phù hợp",
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Thử dùng từ khóa khác hoặc lọc theo danh mục khác nhé rảnh",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        ProductItemCard(
                            product = product,
                            priceFormatted = df.format(product.price),
                            onClick = { viewModel.selectedProduct = product }
                        )
                    }
                }
            }
        }
    }

    // Modal Details Sheet/Dialog
    viewModel.selectedProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { viewModel.selectedProduct = null },
            confirmButton = {},
            dismissButton = {},
            title = null,
            text = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        // Illustrated Background Header representing 3D CAD draft
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0F172A),
                                            Color(product.baseColorHex)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = when (product.modelType) {
                                        "sofa" -> Icons.Default.Weekend
                                        "table" -> Icons.Default.TableRestaurant
                                        "bed" -> Icons.Default.Bed
                                        "chair" -> Icons.Default.Chair
                                        else -> Icons.Default.Inventory
                                    },
                                    contentDescription = product.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "BẢN PHÁC THẢO 3D",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = product.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = product.category,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Attributes table
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Chất liệu", fontSize = 11.sp, color = Color.Gray)
                                Text(product.material, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Kích thước", fontSize = 11.sp, color = Color.Gray)
                                Text(product.size, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(2.dp))
                            Text(product.rating.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(
                                text = df.format(product.price),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action rows
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Load directly in 3D studio, turn off details dialog, swap tab
                                    viewModel.selected3DProduct = product
                                    viewModel.active3DColorIndex = 0
                                    viewModel.selectedMaterialIn3D = product.material
                                    viewModel.currentScreen = Screen.Design3D
                                    viewModel.selectedProduct = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ViewInAr, "3D", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trực quan 3D", fontSize = 12.sp, maxLines = 1)
                            }

                            Button(
                                onClick = {
                                    bookingProduct = product
                                    showBookingSheet = true
                                    viewModel.selectedProduct = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ShoppingCart, "Shopping", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Đặt Mua", fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        )
    }

    // Modern Booking Form Sheet Dialog
    if (showBookingSheet && bookingProduct != null) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("2026-05-24") }
        var notes by remember { mutableStateOf("Tư vấn khảo sát kích thước trực tiếp tại chung cư.") }
        var showError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBookingSheet = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank()) {
                            showError = "Vui lòng điền đầy đủ Tên và Số Điện Thoại liên hệ!"
                        } else {
                            viewModel.placeFurnitureBooking(
                                productName = bookingProduct!!.name,
                                price = bookingProduct!!.price,
                                customerName = name,
                                customerPhone = phone,
                                bookingDate = date,
                                notes = notes
                            )
                            showBookingSheet = false
                            // Swap automatically to status tracker to see purchase being loaded!
                            viewModel.currentScreen = Screen.Bookings
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("Xác Nhận Đặt Lịch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingSheet = false }) {
                    Text("Đóng")
                }
            },
            title = {
                Text("ĐẶT MUA & ĐẶT LỊCH KHẢO SÁT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Sản phẩm: ${bookingProduct!!.name}",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Đơn giá dự kiến: ${df.format(bookingProduct!!.price)}",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC2410C)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; showError = "" },
                        label = { Text("Họ & Tên khách hàng") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; showError = "" },
                        label = { Text("Số điện thoại liên hệ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Ngày khảo sát đo đạc (yyyy-mm-dd)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Ghi chú yêu cầu riêng") },
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

@Composable
fun ProductItemCard(
    product: Product,
    priceFormatted: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Illustrated Box matching product's color theme & category
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E293B), // slate
                                Color(product.baseColorHex).copy(alpha = 0.82f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Large styled category vector icons inside a clean bubble
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (product.modelType) {
                            "sofa" -> Icons.Default.Weekend
                            "table" -> Icons.Default.TableRestaurant
                            "bed" -> Icons.Default.Bed
                            "chair" -> Icons.Default.Chair
                            else -> Icons.Default.Inventory
                        },
                        contentDescription = product.name,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = product.material,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = priceFormatted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            "Star",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            text = product.rating.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

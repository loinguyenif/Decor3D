package com.example.data

data class Product(
    val id: String,
    val name: String,
    val category: String, // "Sofa", "Bàn", "Giường", "Ghế", "Tủ"
    val price: Double,
    val description: String,
    val size: String,
    val material: String,
    val rating: Float,
    val colors: List<String>, // List of color names / options
    val colorValues: List<Long>, // List of hex color longs
    val modelType: String, // "sofa", "table", "bed", "chair", "cabinet"
    val baseColorHex: Long // Primary default color hex long
)

object ProductCatalog {
    val items = listOf(
        Product(
            id = "p1",
            name = "Sofa Da Scandinavian Luxury",
            category = "Sofa",
            price = 15500000.0,
            description = "Sofa bọc da tự nhiên cao cấp phong cách Bắc Âu tối giản. Đường chỉ may tinh sảo, đệm mút lông vũ siêu êm ái, chân khung gỗ óc chó vững chãi.",
            size = "220x95x85 cm",
            material = "Da Bò Tự Nhiên, Gỗ Óc Chó",
            rating = 4.9f,
            colors = listOf("Nâu Da Bò", "Xám Graphite", "Kem Vanilla"),
            colorValues = listOf(0xFF8B4513, 0xFF424242, 0xFFF5F5DC),
            modelType = "sofa",
            baseColorHex = 0xFF8B4513
        ),
        Product(
            id = "p2",
            name = "Bàn Ăn Gỗ Nguyên Khối Rustic",
            category = "Bàn",
            price = 8900000.0,
            description = "Bàn ăn chế tác thủ công hoàn toàn từ gỗ Óc Chó nguyên khối quý hiếm. Vân gỗ tự nhiên uốn lượn nghệ thuật được phủ sơn dầu lau thẩm mỹ cao.",
            size = "180x90x76 cm",
            material = "Gỗ Óc Chó Tự Nhiên",
            rating = 4.8f,
            colors = listOf("Gỗ Óc Chó Tự Nhiên", "Gỗ Sồi Ấm", "Sơn Đen Mờ"),
            colorValues = listOf(0xFF5C4033, 0xFFC39B78, 0xFF1C1C1C),
            modelType = "table",
            baseColorHex = 0xFF5C4033
        ),
        Product(
            id = "p3",
            name = "Giường Ngủ Velvet Cozy",
            category = "Giường",
            price = 12200000.0,
            description = "Giường ngủ bọc vải nhung (Velvet) cao cấp màu sắc cá tính, phong cách hiện đại. Đầu giường được lót foam cực dày thích hợp để tựa lưng đọc sách.",
            size = "180x200x120 cm",
            material = "Vải Nhung, Khung Kim Loại",
            rating = 4.7f,
            colors = listOf("Xanh Emerald", "Hồng Khói", "Xám Kim Loại"),
            colorValues = listOf(0xFF097969, 0xFFE0B0FF, 0xFF708090),
            modelType = "bed",
            baseColorHex = 0xFF097969
        ),
        Product(
            id = "p4",
            name = "Ghế Làm Việc Ergonomic Elite",
            category = "Ghế",
            price = 4500000.0,
            description = "Ghế xoay công thái học bảo vệ cột sống tối đa. Thiết kế lưới 3D thoáng khí chống ẩm mốc kết hợp các khớp tinh chỉnh đỡ cổ, lưng dưới và bệ tỳ tay 3D.",
            size = "65x65x125 cm",
            material = "Nhựa Gia Cường, Lưới Thoáng Khí",
            rating = 4.9f,
            colors = listOf("Đen Stealth", "Xám Sương Mù", "Trắng Office"),
            colorValues = listOf(0xFF121212, 0xFFD3D3D3, 0xFFF8F9FA),
            modelType = "chair",
            baseColorHex = 0xFF121212
        ),
        Product(
            id = "p5",
            name = "Tủ Sách Gỗ Walnut Minimalist",
            category = "Tủ",
            price = 7800000.0,
            description = "Tủ sách gỗ mang đậm ngôn ngữ thiết kế tối giản Nhật Bản. Tường ngăn lệch tầng thẩm mỹ cao cùng các hộc kéo âm lịch thiệp giúp lưu trữ đồ dùng gọn gàng.",
            size = "120x35x185 cm",
            material = "Gỗ Walnut Ghép Sấy",
            rating = 4.6f,
            colors = listOf("Vàng Walnut Sáng", "Walnut Trầm", "Sơn Trắng Sứ"),
            colorValues = listOf(0xFFD2B48C, 0xFF4A3B32, 0xFFF2F3F4),
            modelType = "cabinet",
            baseColorHex = 0xFF4A3B32
        )
    )
}

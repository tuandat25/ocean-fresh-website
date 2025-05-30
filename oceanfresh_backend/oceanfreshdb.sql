-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th5 30, 2025 lúc 05:33 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `oceanfreshdb`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `attributes`
--

CREATE TABLE `attributes` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL COMMENT 'Tên thuộc tính (VD: Kích cỡ, Trọng lượng)',
  `code` varchar(50) NOT NULL COMMENT 'Mã thuộc tính (VD: SIZE, WEIGHT)',
  `description` text DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `attributes`
--

INSERT INTO `attributes` (`id`, `name`, `code`, `description`, `created_at`, `updated_at`) VALUES
(1, 'Kích cỡ', 'SIZE', 'Kích cỡ của sản phẩm (Lớn, Vừa, Nhỏ)', '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(2, 'Trọng lượng', 'WEIGHT', 'Trọng lượng tịnh của sản phẩm', '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(3, 'Quy cách', 'PACKAGING', 'Quy cách đóng gói (Nguyên con, Fillet, Cắt khúc)', '2025-05-17 03:04:04', '2025-05-17 03:04:04');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `attribute_values`
--

CREATE TABLE `attribute_values` (
  `id` int(11) NOT NULL,
  `attribute_id` int(11) NOT NULL,
  `value` varchar(255) NOT NULL COMMENT 'Giá trị (VD: L, M, 500g, 1kg)',
  `display_order` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `attribute_values`
--

INSERT INTO `attribute_values` (`id`, `attribute_id`, `value`, `display_order`, `created_at`, `updated_at`) VALUES
(1, 1, 'Lớn (L)', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(2, 1, 'Vừa (M)', 2, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(3, 1, 'Nhỏ (S)', 3, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(4, 2, '500g', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(5, 2, '1kg', 2, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(6, 2, '2kg', 3, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(7, 3, 'Nguyên con', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(8, 3, 'Fillet', 2, '2025-05-17 03:04:04', '2025-05-17 03:04:04');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `cart`
--

CREATE TABLE `cart` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'Null nếu là khách chưa đăng nhập',
  `session_id` varchar(100) DEFAULT NULL COMMENT 'Dùng cho khách chưa đăng nhập',
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `cart`
--

INSERT INTO `cart` (`id`, `user_id`, `session_id`, `created_at`, `updated_at`) VALUES
(1, 1, NULL, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(2, NULL, 'sess_abc123xyz789', '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(3, 2, NULL, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(4, NULL, 'sess_def456uvw012', '2025-05-17 03:04:04', '2025-05-17 03:04:04');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `cart_items`
--

CREATE TABLE `cart_items` (
  `id` int(11) NOT NULL,
  `cart_id` int(11) NOT NULL,
  `product_variant_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `price_at_addition` decimal(15,2) NOT NULL COMMENT 'Giá tại thời điểm thêm vào giỏ',
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `cart_items`
--

INSERT INTO `cart_items` (`id`, `cart_id`, `product_variant_id`, `quantity`, `price_at_addition`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 2, 350000.00, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(2, 1, 3, 1, 450000.00, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(3, 2, 2, 3, 250000.00, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(4, 3, 4, 1, 600000.00, '2025-05-17 03:04:04', '2025-05-17 03:04:04');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `categories`
--

INSERT INTO `categories` (`id`, `name`, `parent_id`, `created_at`, `updated_at`) VALUES
(1, 'Cá Tươi Sống', NULL, '2025-05-17 03:01:19', '2025-05-17 03:01:19'),
(2, 'Tôm Các Loại', NULL, '2025-05-17 03:01:19', '2025-05-17 03:01:19'),
(3, 'Cua Và Ghẹ', NULL, '2025-05-17 03:01:19', '2025-05-17 03:01:19'),
(4, 'Hải Sản Khác', NULL, '2025-05-17 03:01:19', '2025-05-17 03:01:19'),
(5, 'Cá Biển Sâu Tươi', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(6, 'Cá Sông & Hồ Tự Nhiên', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(7, 'Cá Fillet & Cắt Khúc Tiện Lợi', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(8, 'Cá Nhập Khẩu Nguyên Con Chất Lượng', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(9, 'Tôm Hùm Sống & Tôm Hùm Đông Lạnh', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(10, 'Tôm Càng & Tôm Sú Size Lớn', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(11, 'Tôm Thẻ & Tôm Đất Tươi Ngon', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(12, 'Tôm Đông Lạnh Các Loại (Bóc Nõn, Nguyên Vỏ)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(13, 'Cua Hoàng Đế (King Crab) & Cua Tuyết', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(14, 'Cua Biển Thịt & Cua Biển Gạch', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(15, 'Ghẹ Xanh, Ghẹ Hoa & Ghẹ Ba Chấm', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(16, 'Ốc Hương & Ốc Mỡ Các Loại', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(17, 'Ốc Móng Tay & Ốc Len Tự Nhiên', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(18, 'Sò Điệp Nhật & Sò Dương Khổng Lồ', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(19, 'Sò Huyết & Sò Lông Biển', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(20, 'Nghêu Sạch & Ngao Trắng Tươi', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(21, 'Hàu Sữa Sống & Hàu Chế Biến', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(22, 'Bào Ngư & Vi Cá Thượng Hạng', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(23, 'Mực Nang Tươi & Mực Lá Khổng Lồ', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(24, 'Mực Ống & Bạch Tuộc Các Kích Cỡ', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(25, 'Hải Sâm & Cầu Gai (Nhím Biển)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(26, 'Hải Sản Khô Cao Cấp (Tôm, Cá, Mực)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(27, 'Tôm Khô & Cá Khô Đặc Sản Vùng Miền', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(28, 'Mực Khô & Mực Một Nắng Dai Ngọt', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(29, 'Chả Mực Giã Tay & Chả Cá Thác Lác', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(30, 'Nem Hải Sản & Chả Ram Tôm Đất', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(31, 'Hải Sản Tẩm Ướp Gia Vị Sẵn', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(32, 'Hải Sản Đông Lạnh Sơ Chế Tiện Lợi', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(33, 'Hải Sản Chế Biến Sẵn (Ready-to-Eat/Cook)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(34, 'Nguyên Liệu Sashimi & Sushi Grade A', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(35, 'Trứng Cá Hồi & Trứng Cá Chuồn Nhật Bản', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(36, 'Caviar & Trứng Cá Tầm Cao Cấp', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(37, 'Hải Sản Xông Khói (Cá Hồi, Cá Thu...)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(38, 'Đồ Hộp Cá Ngừ & Cá Mòi Chất Lượng', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(39, 'Mắm Tôm, Mắm Ruốc & Mắm Nêm', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(40, 'Nước Mắm Truyền Thống (Phú Quốc, Phan Thiết)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(41, 'Gia Vị Ướp & Sốt Chấm Hải Sản Đặc Biệt', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(42, 'Set Nguyên Liệu Lẩu Hải Sản Đầy Đủ', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(43, 'Combo Nướng Hải Sản BBQ Gia Đình', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(44, 'Hải Sản Cho Bé (Không Xương, Dinh Dưỡng)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(45, 'Hải Sản Dành Cho Bà Bầu & Mẹ Sau Sinh', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(46, 'Hải Sản Vùng Miền (Đặc Sản Địa Phương Độc Đáo)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(47, 'Snack Hải Sản Sấy Giòn Ăn Liền', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(48, 'Rong Biển Khô & Tảo Biển Tươi', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(49, 'Hải Sản Hữu Cơ (Organic Seafood)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(50, 'Hải Sản Đánh Bắt Bền Vững (Sustainable Seafood)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(51, 'Hải Sản Ít Calo (Low-Calorie Seafood)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(52, 'Hải Sản Giàu Omega-3 & DHA', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(53, 'Hải Sản Theo Mùa (Tươi Ngon Đúng Vụ)', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52'),
(54, 'Quà Tặng Hải Sản Sang Trọng & Ý Nghĩa', NULL, '2025-05-22 01:40:52', '2025-05-22 01:40:52');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `coupons`
--

CREATE TABLE `coupons` (
  `id` int(11) NOT NULL,
  `code` varchar(50) NOT NULL,
  `description` text DEFAULT NULL,
  `discount_type` enum('percentage','fixed_amount') NOT NULL,
  `discount_value` decimal(15,2) NOT NULL,
  `max_discount_amount` decimal(15,2) DEFAULT NULL COMMENT 'Số tiền giảm tối đa nếu là percentage',
  `minimum_order_value` decimal(15,2) DEFAULT 0.00,
  `max_usage_per_user` int(11) DEFAULT 1,
  `max_total_usage` int(11) DEFAULT NULL,
  `current_usage_count` int(11) DEFAULT 0,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `coupons`
--

INSERT INTO `coupons` (`id`, `code`, `description`, `discount_type`, `discount_value`, `max_discount_amount`, `minimum_order_value`, `max_usage_per_user`, `max_total_usage`, `current_usage_count`, `start_date`, `end_date`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'WELCOME20', 'Giảm giá 20% cho đơn hàng đầu tiên', 'percentage', 20.00, 100000.00, 300000.00, 1, 1000, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(2, 'FREESHIP50K', 'Miễn phí vận chuyển tối đa 50K', 'fixed_amount', 50000.00, NULL, 500000.00, 5, NULL, 0, '2025-05-01 00:00:00', '2025-06-30 23:59:59', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(3, 'SUMMERDEAL', 'Giảm 100K cho đơn từ 1 triệu', 'fixed_amount', 100000.00, NULL, 1000000.00, 1, 500, 0, '2025-06-01 00:00:00', '2025-08-31 23:59:59', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `favorites`
--

CREATE TABLE `favorites` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL COMMENT 'Thường là yêu thích sản phẩm gốc',
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `favorites`
--

INSERT INTO `favorites` (`id`, `user_id`, `product_id`, `created_at`) VALUES
(1, 1, 1, '2025-05-17 03:04:05'),
(2, 1, 2, '2025-05-17 03:04:05'),
(3, 2, 3, '2025-05-17 03:04:05'),
(4, 2, 1, '2025-05-17 03:04:05');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `flyway_schema_history`
--

CREATE TABLE `flyway_schema_history` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `orders`
--

CREATE TABLE `orders` (
  `id` int(11) NOT NULL,
  `order_code` varchar(20) NOT NULL COMMENT 'Mã đơn hàng hiển thị cho người dùng, tự sinh',
  `user_id` int(11) DEFAULT NULL COMMENT 'Null nếu khách không đăng nhập đặt hàng',
  `fullname` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `shipping_address` varchar(250) NOT NULL,
  `note` text DEFAULT NULL,
  `order_date` datetime DEFAULT current_timestamp(),
  `status` enum('PENDING','PROCESSING','SHIPPED','DELIVERED','CANCELLED_BY_CUSTOMER','CANCELLED_BY_ADMIN','RETURNED') NOT NULL DEFAULT 'PENDING',
  `subtotal_amount` decimal(15,2) NOT NULL COMMENT 'Tổng tiền hàng trước giảm giá, thuế',
  `shipping_fee` decimal(15,2) DEFAULT 0.00,
  `discount_amount` decimal(15,2) DEFAULT 0.00,
  `total_amount` decimal(15,2) NOT NULL COMMENT 'Tổng tiền cuối cùng phải thanh toán',
  `shipping_method` varchar(100) DEFAULT NULL,
  `shipping_date_expected` date DEFAULT NULL,
  `actual_shipping_date` datetime DEFAULT NULL,
  `tracking_number` varchar(100) DEFAULT NULL,
  `payment_method` varchar(100) NOT NULL,
  `payment_status` enum('UNPAID','PAID','FAILED','REFUNDED') NOT NULL DEFAULT 'UNPAID',
  `coupon_id` int(11) DEFAULT NULL,
  `vnp_txn_ref` varchar(255) DEFAULT NULL COMMENT 'Mã giao dịch từ VNPay hoặc cổng thanh toán khác',
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `orders`
--

INSERT INTO `orders` (`id`, `order_code`, `user_id`, `fullname`, `email`, `phone_number`, `shipping_address`, `note`, `order_date`, `status`, `subtotal_amount`, `shipping_fee`, `discount_amount`, `total_amount`, `shipping_method`, `shipping_date_expected`, `actual_shipping_date`, `tracking_number`, `payment_method`, `payment_status`, `coupon_id`, `vnp_txn_ref`, `created_at`, `updated_at`) VALUES
(1, 'OF20250516001', 1, 'Nguyễn Văn An', 'nguyenvana@example.com', '0901234567', '123 Đường ABC, Quận 1, TP.HCM', 'Giao hàng giờ hành chính', '2025-05-17 03:04:04', 'PENDING', 1150000.00, 30000.00, 100000.00, 1080000.00, 'Giao hàng nhanh', NULL, NULL, NULL, 'COD', 'UNPAID', 1, NULL, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(2, 'OF20250517002', 2, 'Trần Thị Bích', 'tranthibich@example.com', '0907654321', '456 Đường XYZ, Quận Ba Đình, Hà Nội', NULL, '2025-05-17 03:04:04', 'PROCESSING', 600000.00, 0.00, 0.00, 600000.00, 'Giao hàng tiêu chuẩn', NULL, NULL, NULL, 'VNPay', 'PAID', NULL, NULL, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(3, 'OF20250517003', NULL, 'Khách Vãng Lai A', 'guestA@example.com', '0911223344', '777 Đường DEF, Quận 3, TP.HCM', 'Gọi trước khi giao', '2025-05-17 03:04:04', 'DELIVERED', 750000.00, 25000.00, 50000.00, 725000.00, 'Grab Express', NULL, NULL, NULL, 'Momo', 'PAID', 2, NULL, '2025-05-17 03:04:04', '2025-05-17 03:04:04');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `order_details`
--

CREATE TABLE `order_details` (
  `id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `product_variant_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `price_at_order` decimal(15,2) NOT NULL COMMENT 'Giá của variant tại thời điểm đặt hàng',
  `total_line_amount` decimal(15,2) NOT NULL COMMENT 'quantity * price_at_order'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `order_details`
--

INSERT INTO `order_details` (`id`, `order_id`, `product_variant_id`, `quantity`, `price_at_order`, `total_line_amount`) VALUES
(1, 1, 1, 2, 350000.00, 700000.00),
(2, 1, 3, 1, 450000.00, 450000.00),
(3, 2, 4, 1, 600000.00, 600000.00),
(4, 3, 2, 3, 250000.00, 750000.00);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `products`
--

CREATE TABLE `products` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL COMMENT 'Tên dòng sản phẩm',
  `slug` varchar(280) NOT NULL,
  `description` text DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `origin` varchar(100) DEFAULT NULL,
  `main_image_url` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1 COMMENT 'Dòng sản phẩm này có được bán không',
  `is_featured` tinyint(1) DEFAULT 0 COMMENT 'Sản phẩm nổi bật',
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `products`
--

INSERT INTO `products` (`id`, `name`, `slug`, `description`, `category_id`, `brand`, `origin`, `main_image_url`, `is_active`, `is_featured`, `created_at`, `updated_at`) VALUES
(1, 'Cá Hồi Na Uy Nguyên Con', 'ca-hoi-na-uy-nguyen-con', 'Cá hồi tươi ngon nhập khẩu trực tiếp từ Na Uy, giữ nguyên con.', 1, 'SalMar ASA', 'Na Uy', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:04:04', '2025-05-29 02:33:10'),
(2, 'Tôm Càng Xanh Sống Size Lớn', 'tom-cang-xanh-song-size-lon', 'Tôm càng xanh sống, size lớn, thịt chắc ngọt, thích hợp cho các món lẩu, nướng.', 2, 'CP Foods', 'Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:04:04', '2025-05-29 02:33:10'),
(3, 'Cua Thịt Cà Mau Loại 1', 'cua-thit-ca-mau-loai-1', 'Cua thịt Cà Mau chính gốc, loại 1, dây buộc nhỏ, nhiều thịt, chắc nịch.', 3, 'Vua Cua', 'Cà Mau, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:04:04', '2025-05-29 02:33:10'),
(4, 'Nghêu Thái Bình Dương', 'ngheu-thai-binh-duong', 'Nghêu sống size vừa, tươi ngon, xuất xứ Thái Bình Dương.', 4, 'Ocean Foods', 'Thái Bình Dương', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:04:04', '2025-05-29 02:33:10'),
(5, 'Cá Basa Fillet Đông Lạnh', 'ca-basa-fillet-dong-lanh', 'Cá basa fillet không da, không xương, đã được làm sạch và cấp đông tiêu chuẩn.', 1, 'Vinh Hoan Corp', 'Đồng Tháp, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(6, 'Cá Thu Phấn Nguyên Con', 'ca-thu-phan-nguyen-con', 'Cá thu phấn tươi ngon, nguyên con, thích hợp chế biến nhiều món ăn.', 1, 'SeaHarvest', 'Phan Thiết, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(7, 'Cá Diêu Hồng Sông Tươi', 'ca-dieu-hong-song-tuoi', 'Cá diêu hồng nuôi sông, thịt ngọt, còn bơi khỏe.', 1, 'LocalCatch', 'Tiền Giang, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(8, 'Cá Trích Ép Trứng Nhật Bản', 'ca-trich-ep-trung-nhat-ban', 'Cá trích ép trứng vàng óng, món ăn đặc trưng của Nhật Bản, giàu dinh dưỡng.', 1, 'PremiumSeafood', 'Nhập khẩu Nhật Bản', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(9, 'Cá Ngừ Đại Dương Saku AA', 'ca-ngu-dai-duong-saku-aa', 'Thăn cá ngừ đại dương cắt Saku, chất lượng AA, chuyên dùng cho sashimi.', 1, 'OceanDelight', 'Bình Định, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(10, 'Cá Tuyết Fillet Atlantic', 'ca-tuyet-fillet-atlantic', 'Fillet cá tuyết từ vùng biển Atlantic, thịt trắng, mềm và béo ngậy.', 1, 'NorthSea Treasures', 'Nhập khẩu Na Uy', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(11, 'Cá Saba Nhật Nướng Teriyaki', 'ca-saba-nhat-nuong-teriyaki', 'Cá Saba Nhật đã được tẩm ướp và nướng sẵn với sốt Teriyaki thơm lừng.', 1, 'JapanSeafood Co.', 'Nhập khẩu Nhật Bản', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(12, 'Cá Chim Trắng Biển', 'ca-chim-trang-bien', 'Cá chim trắng biển tươi, thịt dai ngon, ít xương.', 1, 'LocalCatch', 'Vũng Tàu, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(13, 'Cá Lóc Đồng Tự Nhiên', 'ca-loc-dong-tu-nhien', 'Cá lóc đồng tự nhiên, thịt chắc, thơm ngon, tốt cho sức khỏe.', 1, 'MekongHarvest', 'Đồng Tháp Mười, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(14, 'Cá Bớp Nguyên Con Làm Sạch', 'ca-bop-nguyen-con-lam-sach', 'Cá bớp biển tươi, đã được làm sạch ruột, đánh vảy.', 1, 'SeaFresh', 'Phú Quốc, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(15, 'Tôm Sú Biển Tươi Loại 1', 'tom-su-bien-tuoi-loai-1', 'Tôm sú biển thiên nhiên, size lớn, thịt săn chắc, vị ngọt đậm đà.', 2, 'OceanFresh', 'Cà Mau, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(16, 'Tôm Hùm Alaska Sống Size Lớn', 'tom-hum-alaska-song-size-lon', 'Tôm hùm Alaska nhập khẩu sống, size lớn, thịt đầy và chắc.', 2, 'Alaska Kings', 'Nhập khẩu Alaska', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(17, 'Tôm Thẻ Bóc Nõn Hấp Sơ', 'tom-the-boc-non-hap-so', 'Tôm thẻ đã được bóc vỏ, bỏ đầu, hấp sơ, tiện lợi chế biến.', 2, 'CP Foods', 'Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(18, 'Tôm Tích (Bề Bề) Sống Size Lớn', 'tom-tich-be-be-song-size-lon', 'Tôm tích hay còn gọi là bề bề, sống, size lớn, nhiều gạch.', 2, 'QuangNinh Seafood', 'Quảng Ninh, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(19, 'Tôm Mũ Ni Đỏ Tươi Sống', 'tom-mu-ni-do-tuoi-song', 'Tôm mũ ni đỏ, hàng hiếm, thịt ngon đặc biệt.', 2, 'PremiumSeafood', 'Nha Trang, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(20, 'Tôm Đất Tự Nhiên Cà Mau', 'tom-dat-tu-nhien-ca-mau', 'Tôm đất sống tự nhiên từ vùng Cà Mau, vỏ mỏng, thịt ngọt.', 2, 'LocalCatch', 'Cà Mau, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(21, 'Tôm Càng Sen Tươi Sống', 'tom-cang-sen-tuoi-song', 'Tôm càng sen tươi, thịt dai và ngọt, kích thước vừa phải.', 2, 'MekongHarvest', 'Đồng Bằng Sông Cửu Long', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(22, 'Tôm Sắt Biển Tươi Ngon', 'tom-sat-bien-tuoi-ngon', 'Tôm sắt biển, tuy nhỏ nhưng thịt rất chắc và đậm đà hương vị biển.', 2, 'CoastalDelights', 'Bình Thuận, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(23, 'Tôm Rảo Tự Nhiên', 'tom-rao-tu-nhien', 'Tôm rảo tự nhiên, vỏ mỏng, thịt ngọt, thích hợp làm các món rim, rang.', 2, 'NorthernSea', 'Nam Định, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(24, 'Tôm Hùm Xanh Sống Size Vừa', 'tom-hum-xanh-song-size-vua', 'Tôm hùm xanh (tôm hùm bông) sống, size vừa, chất lượng cao.', 2, 'IndoPacific Gems', 'Nhập khẩu Úc', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(25, 'Ghẹ Xanh Sống Ba Con/kg', 'ghe-xanh-song-ba-con-kg', 'Ghẹ xanh tươi sống, loại 3 con/kg, chắc thịt, không bị óp.', 3, 'PhuQuocSeafood', 'Phú Quốc, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(26, 'Cua Huỳnh Đế Tươi Sống', 'cua-huynh-de-tuoi-song', 'Cua Huỳnh Đế, \"vua của các loại cua\", thịt ngon hiếm có.', 3, 'RoyalCrabs', 'Phú Yên, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(27, 'Ốc Hương Tự Nhiên Size Cồ', 'oc-huong-tu-nhien-size-co', 'Ốc hương tự nhiên, size lớn (cồ), thịt giòn, thơm nức.', 4, 'NhaTrangBest', 'Nha Trang, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(28, 'Sò Điệp Nhật Tách Vỏ', 'so-diep-nhat-tach-vo', 'Cồi sò điệp Nhật Bản, đã tách vỏ, trắng nõn, ngọt thanh.', 4, 'HokkaidoScallops', 'Nhập khẩu Nhật Bản', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(29, 'Hàu Sữa Pháp Fine de Claire', 'hau-sua-phap-fine-de-claire', 'Hàu sữa Fine de Claire từ Pháp, vị mặn nhẹ, béo ngậy đặc trưng.', 4, 'FrenchOysters Ltd.', 'Nhập khẩu Pháp', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(30, 'Vẹm Xanh New Zealand Nguyên Vỏ', 'vem-xanh-new-zealand-nguyen-vo', 'Vẹm xanh New Zealand, còn nguyên vỏ, thịt đầy, giàu omega-3.', 4, 'NZGreenShell', 'Nhập khẩu New Zealand', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(31, 'Bào Ngư Hàn Quốc Tươi Sống', 'bao-ngu-han-quoc-tuoi-song', 'Bào ngư Hàn Quốc tươi sống, bổ dưỡng, size vừa.', 4, 'KoreaAbalone', 'Nhập khẩu Hàn Quốc', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(32, 'Sò Lông Biển Tươi', 'so-long-bien-tuoi', 'Sò lông biển tươi sống, thịt ngọt, có thể chế biến nhiều món.', 4, 'VietnamShellfish', 'Kiên Giang, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(33, 'Ốc Móng Tay Chúa Size Lớn', 'oc-mong-tay-chua-size-lon', 'Ốc móng tay chúa, size lớn, thịt dày và ngọt.', 4, 'CoastalHarvest', 'Bến Tre, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(34, 'Ngao Hai Cồi (Ngao Trắng) Tươi', 'ngao-hai-coi-ngao-trang-tuoi', 'Ngao hai cồi, còn gọi là ngao trắng, tươi sống, ngọt nước.', 4, 'DeltaClams', 'Thái Bình, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(35, 'Mực Nang Tươi Làm Sạch', 'muc-nang-tuoi-lam-sach', 'Mực nang tươi, đã được làm sạch da và ruột, thịt dày, giòn.', 4, 'OceanFresh', 'Phan Thiết, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(36, 'Mực Ống Nguyên Con Tươi', 'muc-ong-nguyen-con-tuoi', 'Mực ống tươi, còn nguyên con, thích hợp nhồi thịt, hấp, xào.', 4, 'SquidWorld', 'Bình Thuận, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(37, 'Bạch Tuộc Baby Đông Lạnh', 'bach-tuoc-baby-dong-lanh', 'Bạch tuộc baby (kích thước nhỏ), đã làm sạch, cấp đông.', 4, 'OctoTreats', 'Nhập khẩu Thái Lan', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(38, 'Chả Mực Hạ Long Giã Tay Loại Đặc Biệt', 'cha-muc-ha-long-gia-tay-loai-dac-biet', 'Chả mực Hạ Long làm thủ công, giã tay, miếng mực giòn sần sật.', 4, 'HaLongDelicacy', 'Quảng Ninh, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(39, 'Surimi Thanh Cua Nhật Bản', 'surimi-thanh-cua-nhat-ban', 'Thanh cua surimi chất lượng cao, nhập khẩu từ Nhật Bản, dùng cho lẩu, salad.', 4, 'KaniSupreme', 'Nhập khẩu Nhật Bản', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(40, 'Lườn Cá Hồi Xông Khói Na Uy', 'luon-ca-hoi-xong-khoi-na-uy', 'Lườn cá hồi Na Uy được xông khói tự nhiên, thơm ngon, béo ngậy.', 1, 'NorwaySmoked', 'Nhập khẩu Na Uy', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(41, 'Trứng Cá Tầm Caviar Osetra', 'trung-ca-tam-caviar-osetra', 'Trứng cá tầm Osetra thượng hạng, hạt đều, vị tinh tế.', 4, 'ImperialCaviar', 'Nhập khẩu Nga', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(42, 'Cá Bò Khô Rim Me Đặc Sản', 'ca-bo-kho-rim-me-dac-san', 'Cá bò khô tẩm gia vị rim me, món ăn vặt hấp dẫn.', 4, 'VietSnacks', 'Nha Trang, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(43, 'Tôm Khô Đất Cà Mau Loại 1', 'tom-kho-dat-ca-mau-loai-1', 'Tôm đất khô Cà Mau, được phơi tự nhiên, màu đỏ đẹp, ngọt thịt.', 2, 'DatMui DriedShrimp', 'Cà Mau, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(44, 'Mực Khô Cô Tô Câu Tay', 'muc-kho-co-to-cau-tay', 'Mực khô Cô Tô được câu và phơi thủ công, thịt dày, ngọt.', 4, 'CoToSquid', 'Cô Tô, Quảng Ninh', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(45, 'Cá Cam Nhật Nguyên Con (Hamachi)', 'ca-cam-nhat-nguyen-con-hamachi', 'Cá cam Nhật (Hamachi) tươi sống nguyên con, chuyên dùng cho sashimi và sushi.', 1, 'JapanFisheries', 'Nhập khẩu Nhật Bản', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(46, 'Cá Đuối Sao Tươi Sống', 'ca-duoi-sao-tuoi-song', 'Cá đuối sao, thịt dai, ngọt, thường dùng để nấu lẩu, nướng.', 1, 'CoastalCatch', 'Long Hải, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(47, 'Sá Sùng Khô Quan Lạn Cao Cấp', 'sa-sung-kho-quan-lan-cao-cap', 'Sá sùng khô từ Quan Lạn, đặc sản quý hiếm, giàu dinh dưỡng.', 4, 'QuanLanTreasures', 'Quan Lạn, Quảng Ninh', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(48, 'Hải Sâm Đen Tự Nhiên', 'hai-sam-den-tu-nhien', 'Hải sâm đen (đồn đột) tự nhiên, đã sơ chế, tốt cho sức khỏe.', 4, 'SeaCucumberWorld', 'Trường Sa, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(49, 'Sò Huyết Cồ Đầm Ô Loan', 'so-huyet-co-dam-o-loan', 'Sò huyết size cồ từ đầm Ô Loan, Phú Yên, béo ngậy, bổ máu.', 4, 'OLoanShells', 'Phú Yên, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(50, 'Cầu Gai (Nhím Biển) Tươi', 'cau-gai-nhim-bien-tuoi', 'Cầu gai tươi sống, còn gọi là nhím biển, ăn sống với mù tạt hoặc nấu cháo.', 4, 'IslandDelicacies', 'Lý Sơn, Quảng Ngãi', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(51, 'Cá Mao Tiên (Cá Sư Tử)', 'ca-mao-tien-ca-su-tu', 'Cá mao tiên, hay cá sư tử, vẻ ngoài độc đáo, thịt ngon lạ miệng (cần chế biến cẩn thận).', 1, 'ExoticFish Co.', 'Vịnh Nha Trang', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(52, 'Ghẹ Ba Chấm (Ghẹ Mặt Trăng)', 'ghe-ba-cham-ghe-mat-trang', 'Ghẹ ba chấm, còn gọi là ghẹ mặt trăng, thịt ngọt và thơm.', 3, 'MoonCrabs', 'Bình Thuận, Việt Nam', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(53, 'Tôm Càng Đỏ Úc Sống (Red Claw)', 'tom-cang-do-uc-song-red-claw', 'Tôm càng đỏ Úc (Red Claw Crayfish), thịt ngon, càng to.', 2, 'AussieClaws', 'Nhập khẩu Úc', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 1, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(54, 'Mực Lá Đại Dương Khổng Lồ', 'muc-la-dai-duong-khong-lo', 'Mực lá đại dương, size khổng lồ, thịt dày, giòn ngọt.', 4, 'GiantSquids Inc.', 'Biển Đông', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 03:12:40', '2025-05-29 02:33:10'),
(55, 'Cá Hồi Fillet Tươi Sống', 'ca-hoi-fillet-tuoi-song-nhap-khau', 'Cá hồi fillet nhập khẩu từ Na Uy, đảm bảo tươi ngon và giàu dinh dưỡng. Thích hợp cho nhiều món ăn.', NULL, 'NorwaySeafood', 'Na Uy', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-16 20:26:19', '2025-05-29 02:33:10'),
(56, 'Cá Hồi Fillet Tươi Sống 2', 'ca-hoi-fillet-tuoi-song-2', 'Cá hồi fillet nhập khẩu từ Na Uy, đảm bảo tươi ngon và giàu dinh dưỡng. Thích hợp cho nhiều món ăn.', 1, 'NorwaySeafood', 'Na Uy', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-17 06:05:09', '2025-05-29 02:33:10'),
(57, 'Cá Hồi Fillet Na Uy 3', 'ca-hoi-fillet-na-uy', 'Cá hồi fillet Na Uy tươi ngon, giàu omega-3, thích hợp cho các món nướng, hấp và salad.', 1, 'OceanFresh', 'Na Uy', 'https://file.hstatic.net/1000175970/file/ca-chep-gion-bhfood-1_a48870a3b72e4921b1a3796df87d80a2_grande.jpg', 1, 0, '2025-05-19 16:14:22', '2025-05-29 02:33:10');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_images`
--

CREATE TABLE `product_images` (
  `id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `product_images`
--

INSERT INTO `product_images` (`id`, `product_id`, `image_url`, `created_at`) VALUES
(5, 1, 'https://example.com/images/ca-hoi-nauy-1.jpg', '2025-05-17 03:04:04'),
(6, 1, 'https://example.com/images/ca-hoi-nauy-2.jpg', '2025-05-17 03:04:04'),
(7, 2, 'https://example.com/images/tom-cang-xanh-1.jpg', '2025-05-17 03:04:04'),
(8, 3, 'https://example.com/images/cua-thit-camau-1.jpg', '2025-05-17 03:04:04');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_variants`
--

CREATE TABLE `product_variants` (
  `id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `sku` varchar(100) NOT NULL COMMENT 'Stock Keeping Unit',
  `variant_name` varchar(255) DEFAULT NULL COMMENT 'Tên mô tả biến thể (VD: Tôm Càng Xanh - Size L - 500g)',
  `price` decimal(15,2) NOT NULL,
  `old_price` decimal(15,2) DEFAULT NULL,
  `quantity_in_stock` int(11) NOT NULL DEFAULT 0,
  `sold_quantity` int(11) NOT NULL DEFAULT 0,
  `thumbnail_url` varchar(255) DEFAULT NULL COMMENT 'Ảnh riêng cho biến thể',
  `is_active` tinyint(1) DEFAULT 1 COMMENT 'Biến thể này có được bán không',
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `product_variants`
--

INSERT INTO `product_variants` (`id`, `product_id`, `sku`, `variant_name`, `price`, `old_price`, `quantity_in_stock`, `sold_quantity`, `thumbnail_url`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 1, 'SALMON-NORWAY-WH-1KG', 'Cá Hồi Na Uy - Nguyên Con - Khoảng 1KG', 350000.00, 380000.00, 50, 120, 'https://example.com/images/ca-hoi-nauy-1kg.jpg', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(2, 1, 'SALMON-NORWAY-FILLET-500G', 'Cá Hồi Na Uy - Fillet - 500g', 250000.00, NULL, 100, 300, 'https://example.com/images/ca-hoi-fillet-500g.jpg', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(3, 2, 'PRAWN-BLUE-LIVE-L-500G', 'Tôm Càng Xanh Sống - Size Lớn - 500g', 450000.00, 480000.00, 30, 80, 'https://example.com/images/tom-cang-xanh-500g.jpg', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(4, 3, 'CRAB-MUD-CM-1-1KG', 'Cua Thịt Cà Mau Loại 1 - 1 Con (khoảng 1kg)', 600000.00, NULL, 20, 50, 'https://example.com/images/cua-thit-1kg.jpg', 1, '2025-05-17 03:04:04', '2025-05-17 03:04:04'),
(6, 57, 'CAHOI-1KG', 'Cá Hồi Fillet Na Uy 3 - Lớn (L)', 475000.00, 520000.00, 25, 0, NULL, 1, '2025-05-20 08:29:21', '2025-05-20 08:29:21');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_variant_attribute_values`
--

CREATE TABLE `product_variant_attribute_values` (
  `product_variant_id` int(11) NOT NULL,
  `attribute_value_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `product_variant_attribute_values`
--

INSERT INTO `product_variant_attribute_values` (`product_variant_id`, `attribute_value_id`) VALUES
(1, 5),
(1, 7),
(2, 4),
(2, 8),
(3, 1),
(3, 4),
(6, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `reviews`
--

CREATE TABLE `reviews` (
  `id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL COMMENT 'Đánh giá cho sản phẩm gốc',
  `user_id` int(11) NOT NULL,
  `order_detail_id` int(11) DEFAULT NULL COMMENT 'Liên kết với chi tiết đơn hàng để xác minh người mua (tùy chọn)',
  `rating` tinyint(4) NOT NULL COMMENT 'Từ 1 đến 5 sao',
  `title` varchar(255) DEFAULT NULL,
  `content` text DEFAULT NULL,
  `is_approved` tinyint(1) DEFAULT 1 COMMENT 'Admin có thể cần duyệt',
  `parent_review_id` int(11) DEFAULT NULL COMMENT 'Để trả lời review',
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `reviews`
--

INSERT INTO `reviews` (`id`, `product_id`, `user_id`, `order_detail_id`, `rating`, `title`, `content`, `is_approved`, `parent_review_id`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 1, 5, 'Cá hồi rất tươi!', 'Tôi rất hài lòng với chất lượng cá hồi, sẽ ủng hộ shop dài dài.', 1, NULL, '2025-05-17 03:04:05', '2025-05-17 03:04:05'),
(2, 2, 2, NULL, 4, 'Tôm càng xanh ổn', 'Tôm to, chắc thịt nhưng có vài con hơi nhỏ hơn quảng cáo.', 1, NULL, '2025-05-17 03:04:05', '2025-05-17 03:04:05'),
(3, 1, 1, NULL, 5, 'Tuyệt vời', 'Sản phẩm chất lượng cao, giao hàng nhanh.', 1, NULL, '2025-05-17 03:04:05', '2025-05-17 03:04:05'),
(4, 3, 2, 3, 5, 'Cua ngon, nhiều thịt', 'Cua Cà Mau đúng chuẩn, thịt rất chắc và ngọt. Sẽ mua lại.', 1, NULL, '2025-05-17 03:04:05', '2025-05-17 03:04:05');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `roles`
--

CREATE TABLE `roles` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL COMMENT 'Tên quyền (VD: CUSTOMER, ADMIN, MANAGER)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `roles`
--

INSERT INTO `roles` (`id`, `name`) VALUES
(2, 'ADMIN'),
(1, 'CUSTOMER'),
(3, 'STAFF');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `social_accounts`
--

CREATE TABLE `social_accounts` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `provider` varchar(50) NOT NULL COMMENT 'Tên nhà cung cấp (google, facebook)',
  `provider_user_id` varchar(255) NOT NULL COMMENT 'ID người dùng từ nhà cung cấp',
  `email` varchar(150) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `avatar_url_provider` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `social_accounts`
--

INSERT INTO `social_accounts` (`id`, `user_id`, `provider`, `provider_user_id`, `email`, `name`, `avatar_url_provider`, `created_at`, `updated_at`) VALUES
(1, 1, 'google', 'google_user_id_12345', 'nguyenvana.google@example.com', 'Nguyễn Văn An Google', 'https://lh3.googleusercontent.com/a-/examplean', '2025-05-17 02:51:25', '2025-05-17 02:51:25'),
(2, 2, 'facebook', 'facebook_user_id_67890', 'tranthibich.fb@example.com', 'Trần Thị Bích Facebook', 'https://graph.facebook.com/67890/picture?type=large', '2025-05-17 02:51:25', '2025-05-17 02:51:25'),
(3, 1, 'facebook', 'facebook_user_id_11223', 'nguyenvana.fb@example.com', 'Nguyễn Văn An Facebook', 'https://graph.facebook.com/11223/picture?type=large', '2025-05-17 02:51:25', '2025-05-17 02:51:25');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `tokens`
--

CREATE TABLE `tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `token` varchar(500) NOT NULL,
  `token_type` varchar(50) NOT NULL COMMENT 'REFRESH_TOKEN, EMAIL_VERIFICATION, PASSWORD_RESET',
  `expiration_date` datetime NOT NULL,
  `is_revoked` tinyint(1) DEFAULT 0,
  `is_used` tinyint(1) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `tokens`
--

INSERT INTO `tokens` (`id`, `user_id`, `token`, `token_type`, `expiration_date`, `is_revoked`, `is_used`, `created_at`) VALUES
(1, 1, 'refresh_token_example_user1', 'REFRESH_TOKEN', '2025-06-16 02:51:25', 0, 0, '2025-05-17 02:51:25'),
(2, 2, 'email_verification_token_user2', 'EMAIL_VERIFICATION', '2025-05-18 02:51:25', 0, 0, '2025-05-17 02:51:25'),
(3, 3, 'password_reset_token_user3', 'PASSWORD_RESET', '2025-05-17 03:51:25', 0, 0, '2025-05-17 02:51:25'),
(4, 1, 'another_refresh_token_user1', 'REFRESH_TOKEN', '2025-07-16 02:51:25', 0, 0, '2025-05-17 02:51:25');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `fullname` varchar(100) DEFAULT '',
  `email` varchar(150) NOT NULL COMMENT 'Email dùng để đăng nhập, là duy nhất',
  `phone_number` varchar(15) DEFAULT NULL,
  `address` varchar(250) DEFAULT '',
  `password` varchar(255) DEFAULT NULL COMMENT 'NULL nếu đăng nhập qua social',
  `avatar_url` varchar(255) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `role_id` int(11) NOT NULL,
  `is_active` tinyint(1) DEFAULT 1 COMMENT 'Tài khoản có hoạt động không',
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `fullname`, `email`, `phone_number`, `address`, `password`, `avatar_url`, `date_of_birth`, `role_id`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'Nguyễn Văn An', 'nguyenvana@example.com', '0901234567', '123 Đường ABC, Quận 1, TP.HCM', '$2a$10$abcdefghijklmnopqrstuv', 'https://example.com/avatar/an.jpg', '1990-01-15', 1, 1, '2025-05-17 02:51:25', '2025-05-17 02:51:25'),
(2, 'Trần Thị Bích', 'tranthibich@example.com', '0907654321', '456 Đường XYZ, Quận Ba Đình, Hà Nội', '$2a$10$wxyzabcdefghijklmnopqr', 'https://example.com/avatar/bich.jpg', '1995-05-20', 1, 1, '2025-05-17 02:51:25', '2025-05-17 02:51:25'),
(3, 'Lê Minh Cường (Admin)', 'leminhcuong.admin@example.com', '0912345678', '789 Đường KLM, Quận Hải Châu, Đà Nẵng', '$2a$10$stuvwxyzabcdefghijklmno', 'https://example.com/avatar/cuong.jpg', '1985-11-01', 2, 1, '2025-05-17 02:51:25', '2025-05-17 02:51:25'),
(4, 'Phạm Thị Dung (Staff)', 'phamthidung.staff@example.com', '0987654321', '101 Đường NOP, Quận Cầu Giấy, Hà Nội', '$2a$10$pqrstuvwxyzabcdefghijkl', 'https://example.com/avatar/dung.jpg', '1992-07-10', 3, 1, '2025-05-17 02:51:25', '2025-05-17 02:51:25');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `attributes`
--
ALTER TABLE `attributes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`),
  ADD UNIQUE KEY `uk_attributes_name` (`name`);

--
-- Chỉ mục cho bảng `attribute_values`
--
ALTER TABLE `attribute_values`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_attribute_value_pair` (`attribute_id`,`value`);

--
-- Chỉ mục cho bảng `cart`
--
ALTER TABLE `cart`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_cart_user` (`user_id`),
  ADD UNIQUE KEY `uk_cart_session` (`session_id`);

--
-- Chỉ mục cho bảng `cart_items`
--
ALTER TABLE `cart_items`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_cart_item_variant` (`cart_id`,`product_variant_id`),
  ADD KEY `fk_cart_items_variant` (`product_variant_id`);

--
-- Chỉ mục cho bảng `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_categories_name` (`name`);

--
-- Chỉ mục cho bảng `coupons`
--
ALTER TABLE `coupons`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`);

--
-- Chỉ mục cho bảng `favorites`
--
ALTER TABLE `favorites`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_favorite_user_product` (`user_id`,`product_id`),
  ADD KEY `fk_favorites_product` (`product_id`);

--
-- Chỉ mục cho bảng `flyway_schema_history`
--
ALTER TABLE `flyway_schema_history`
  ADD PRIMARY KEY (`installed_rank`),
  ADD KEY `flyway_schema_history_s_idx` (`success`);

--
-- Chỉ mục cho bảng `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `order_code` (`order_code`),
  ADD KEY `fk_orders_coupon` (`coupon_id`),
  ADD KEY `idx_orders_user_id` (`user_id`),
  ADD KEY `idx_orders_status` (`status`),
  ADD KEY `idx_orders_order_date` (`order_date`);

--
-- Chỉ mục cho bảng `order_details`
--
ALTER TABLE `order_details`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_order_details_order` (`order_id`),
  ADD KEY `fk_order_details_variant` (`product_variant_id`);

--
-- Chỉ mục cho bảng `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `slug` (`slug`),
  ADD KEY `idx_products_category_id` (`category_id`),
  ADD KEY `idx_products_is_active` (`is_active`);

--
-- Chỉ mục cho bảng `product_images`
--
ALTER TABLE `product_images`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_product_images_product` (`product_id`);

--
-- Chỉ mục cho bảng `product_variants`
--
ALTER TABLE `product_variants`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `sku` (`sku`),
  ADD KEY `idx_product_variants_product_id` (`product_id`),
  ADD KEY `idx_product_variants_is_active` (`is_active`);

--
-- Chỉ mục cho bảng `product_variant_attribute_values`
--
ALTER TABLE `product_variant_attribute_values`
  ADD PRIMARY KEY (`product_variant_id`,`attribute_value_id`),
  ADD KEY `fk_pvav_attribute_value` (`attribute_value_id`);

--
-- Chỉ mục cho bảng `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_reviews_parent` (`parent_review_id`),
  ADD KEY `fk_reviews_order_detail` (`order_detail_id`),
  ADD KEY `idx_reviews_product_id` (`product_id`),
  ADD KEY `idx_reviews_user_id` (`user_id`);

--
-- Chỉ mục cho bảng `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Chỉ mục cho bảng `social_accounts`
--
ALTER TABLE `social_accounts`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_social_provider_id` (`provider`,`provider_user_id`),
  ADD KEY `fk_social_accounts_user` (`user_id`);

--
-- Chỉ mục cho bảng `tokens`
--
ALTER TABLE `tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `fk_tokens_user` (`user_id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phone_number` (`phone_number`),
  ADD KEY `fk_users_role` (`role_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `attributes`
--
ALTER TABLE `attributes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `attribute_values`
--
ALTER TABLE `attribute_values`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT cho bảng `cart`
--
ALTER TABLE `cart`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=55;

--
-- AUTO_INCREMENT cho bảng `coupons`
--
ALTER TABLE `coupons`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `favorites`
--
ALTER TABLE `favorites`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `orders`
--
ALTER TABLE `orders`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `order_details`
--
ALTER TABLE `order_details`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `products`
--
ALTER TABLE `products`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=58;

--
-- AUTO_INCREMENT cho bảng `product_images`
--
ALTER TABLE `product_images`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT cho bảng `product_variants`
--
ALTER TABLE `product_variants`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT cho bảng `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `roles`
--
ALTER TABLE `roles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `social_accounts`
--
ALTER TABLE `social_accounts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `tokens`
--
ALTER TABLE `tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `attribute_values`
--
ALTER TABLE `attribute_values`
  ADD CONSTRAINT `fk_attribute_values_attribute` FOREIGN KEY (`attribute_id`) REFERENCES `attributes` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `cart`
--
ALTER TABLE `cart`
  ADD CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `fk_cart_items_cart` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_cart_items_variant` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `favorites`
--
ALTER TABLE `favorites`
  ADD CONSTRAINT `fk_favorites_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_orders_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Các ràng buộc cho bảng `order_details`
--
ALTER TABLE `order_details`
  ADD CONSTRAINT `fk_order_details_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_order_details_variant` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`);

--
-- Các ràng buộc cho bảng `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL;

--
-- Các ràng buộc cho bảng `product_images`
--
ALTER TABLE `product_images`
  ADD CONSTRAINT `fk_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `product_variants`
--
ALTER TABLE `product_variants`
  ADD CONSTRAINT `fk_product_variants_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `product_variant_attribute_values`
--
ALTER TABLE `product_variant_attribute_values`
  ADD CONSTRAINT `fk_pvav_attribute_value` FOREIGN KEY (`attribute_value_id`) REFERENCES `attribute_values` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_pvav_variant` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `fk_reviews_order_detail` FOREIGN KEY (`order_detail_id`) REFERENCES `order_details` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_reviews_parent` FOREIGN KEY (`parent_review_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_reviews_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `social_accounts`
--
ALTER TABLE `social_accounts`
  ADD CONSTRAINT `fk_social_accounts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `tokens`
--
ALTER TABLE `tokens`
  ADD CONSTRAINT `fk_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
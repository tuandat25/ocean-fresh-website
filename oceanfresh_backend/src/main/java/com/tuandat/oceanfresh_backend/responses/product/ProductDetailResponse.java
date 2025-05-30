// package com.tuandat.oceanfresh_backend.responses.product;


// import com.fasterxml.jackson.annotation.JsonFormat;
// import com.tuandat.oceanfresh_backend.dtos.product.ProductImageDTO;
// import com.tuandat.oceanfresh_backend.models.Product;
// import com.tuandat.oceanfresh_backend.models.ProductImage;
// import com.tuandat.oceanfresh_backend.responses.CategoryResponse;
// import lombok.Builder;
// import lombok.Data;
// import java.time.LocalDateTime;
// import java.util.Collections;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Set;
// import java.util.stream.Collectors;


// @Data
// @Builder
// public class ProductDetailResponse { // Đổi tên để rõ ràng hơn
//     private Long id;
//     private String name;
//     private String slug;
//     private String description;
//     private CategoryResponse category;
//     private String brand;
//     private String origin;
//     private String mainImageUrl;
//     private List<ProductImageDTO> additionalImages; // Sử dụng List<ProductImageDTO>
//     private boolean isActive;
//     private Set<ProductVariantResponse> variants; // Sử dụng ProductVariantResponse
//     private LocalDateTime createdAt;
//     private LocalDateTime updatedAt;

//     // Constructor fromEntity cần nhận Product và danh sách ProductImage liên quan đến Product đó
//     public static ProductDetailResponse fromEntity(Product product, List<ProductImage> productImages) {
//         if (product == null) {
//             return null;
//         }

//         List<ProductImageDTO> imageResponses = Collections.emptyList();
//         if (productImages != null) {
//             imageResponses = productImages.stream()
//                                 .filter(img -> img.getProduct() != null && img.getProduct().getId().equals(product.getId())) // Chỉ lấy ảnh của product gốc
//                                 .sorted(Comparator.comparingInt(ProductImage::getDisplayOrder))
//                                 .map(ProductImageDTO::fromEntity)
//                                 .collect(Collectors.toList());
//         }

//         return ProductDetailResponse.builder()
//                 .id(product.getId())
//                 .name(product.getName())
//                 .slug(product.getSlug())
//                 .description(product.getDescription())
//                 .category(product.getCategory() != null ? CategoryResponse.fromEntity(product.getCategory()) : null)
//                 .brand(product.getBrand())
//                 .origin(product.getOrigin())
//                 .mainImageUrl(product.getMainImageUrl())
//                 .additionalImages(imageResponses)
//                 .isActive(product.isActive())
//                 .variants(
//                     product.getVariants() != null ?
//                     product.getVariants().stream()
//                         // Để map ProductVariant sang ProductVariantResponse, bạn cũng cần danh sách ảnh của từng variant
//                         // Điều này có thể yêu cầu logic query phức tạp hơn hoặc Service phải chuẩn bị sẵn.
//                         // Cách đơn giản là ProductVariantResponse.fromEntity(variant) sẽ tự xử lý ảnh của nó nếu có @OneToMany
//                         .map(variant -> {
//                             // Giả sử ProductVariant Entity có @OneToMany List<ProductImage> images;
//                             // và nó được fetch (ví dụ qua EntityGraph trong ProductRepository khi lấy Product)
//                             List<ProductImage> variantSpecificImages = variant.getImages() != null ?
//                                     new java.util.ArrayList<>(variant.getImages()) : Collections.emptyList();
//                             return ProductVariantResponse.fromEntity(variant, variantSpecificImages);
//                         })
//                         .collect(Collectors.toSet()) : Collections.emptySet()
//                 )
//                 .createdAt(product.getCreatedAt())
//                 .updatedAt(product.getUpdatedAt())
//                 .build();
//     }
// }
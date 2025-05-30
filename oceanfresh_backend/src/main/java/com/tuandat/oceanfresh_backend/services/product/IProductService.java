package com.tuandat.oceanfresh_backend.services.product;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tuandat.oceanfresh_backend.dtos.product.ProductCreateDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductDetailDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductImageDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductVariantDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductVariantRequestDTO;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.product.ProductBaseResponse;

public interface IProductService {
    ProductDetailDTO createProduct(ProductCreateDTO productCreateDTO);
    ProductDetailDTO getProductById(Long productId);
    ProductDetailDTO getProductBySlug(String slug);
    Page<ProductBaseResponse> getAllProducts(Pageable pageable); // Phân trang
    ProductDetailDTO updateProduct(Long productId, ProductDetailDTO productDetailDTO);
    void deleteProduct(Long productId);

    ProductVariantDTO addVariantToProduct(Long productId, ProductVariantRequestDTO variantRequestDTO);
    ProductVariantDTO updateProductVariant(Long variantId, ProductVariantRequestDTO variantRequestDTO);
    void deleteProductVariant(Long variantId);
    ProductVariantDTO getProductVariantById(Long variantId); // Lấy chi tiết biến thể
    ProductVariantDTO getProductVariantBySku(String sku);
    List<ProductVariantDTO> getVariantsByProductId(Long productId); // Lấy danh sách biến thể của 1 sản phẩm
    ProductVariantDTO findActiveVariantByProductAndAttributes(Long productId, Set<Long> attributeValueIds);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception;
}

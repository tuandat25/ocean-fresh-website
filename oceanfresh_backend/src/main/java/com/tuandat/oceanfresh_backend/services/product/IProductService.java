package com.tuandat.oceanfresh_backend.services.product;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.tuandat.oceanfresh_backend.dtos.product.ProductCreateDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductDetailDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductImageDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductUpdateDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductVariantDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductVariantRequestDTO;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.product.ProductBaseResponse;

public interface IProductService {
    ProductDetailDTO createProduct(ProductCreateDTO productCreateDTO);

    ProductDetailDTO getProductById(Long productId);

    ProductDetailDTO getProductBySlug(String slug);

    Page<ProductBaseResponse> getAllProducts(Pageable pageable); // Phân trang

    Page<ProductBaseResponse> getAllProductsIsActive(Pageable pageable); // Phân trang

    
    Page<ProductBaseResponse> searchProducts(String keyword, Pageable pageable); // Search tất cả sản phẩm (admin)
    
    Page<ProductBaseResponse> searchActiveProducts(String keyword, Pageable pageable); // Search sản phẩm active (user)

    Page<ProductBaseResponse> getProductsByCategory(Long categoryId, Pageable pageable); // Lấy sản phẩm theo danh mục
                                                                                         // với phân trang

    Page<ProductBaseResponse> getActiveProductsByCategory(Long categoryId, Pageable pageable); // Lấy sản phẩm active
                                                                                               // theo danh mục với phân
                                                                                               // trang

    ProductDetailDTO updateProduct(Long productId, ProductUpdateDTO productUpdateDTO);

    void deleteProduct(Long productId);

    ProductVariantDTO addVariantToProduct(Long productId, ProductVariantRequestDTO variantRequestDTO);

    ProductVariantDTO updateProductVariant(Long variantId, ProductVariantRequestDTO variantRequestDTO);

    void deleteProductVariant(Long variantId);

    ProductVariantDTO getProductVariantById(Long variantId); // Lấy chi tiết biến thể

    ProductVariantDTO getProductVariantBySku(String sku);

    List<ProductVariantDTO> getVariantsByProductId(Long productId); // Lấy danh sách biến thể của 1 sản phẩm

    ProductVariantDTO findActiveVariantByProductAndAttributes(Long productId, Set<Long> attributeValueIds);

    ProductDetailDTO uploadMainImage(Long productId, String imageName) throws Exception;

    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception;

    ProductDetailDTO createProductWithMainImage(
            ProductCreateDTO productCreateDTO,
            MultipartFile mainImage) throws Exception;
}

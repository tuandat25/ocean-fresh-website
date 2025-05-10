package com.tuandat.oceanfresh_backend.services.product;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import com.tuandat.oceanfresh_backend.dtos.ProductDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductImageDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductUpdateDTO;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.responses.product.ProductResponse;

public interface IProductService {
    Product createProduct(ProductDTO productDTO) throws Exception;
    Product getProductById(long id) throws Exception;
    public Page<ProductResponse> getAllProducts(String keyword,
                                                Long categoryId, PageRequest pageRequest);
    Product updateProduct(long id, ProductDTO productDTO) throws Exception;
    void deleteProduct(long id);
    boolean existsByName(String name);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception;

    List<Product> findProductsByIds(List<Long> productIds);
    String storeFile(MultipartFile file) throws IOException;    void deleteFile(String filename) throws IOException;
      // Thêm các phương thức làm việc với thuộc tính sản phẩm
    Product addProductAttribute(Long productId, Long attributeId, String value) throws Exception;
    Product updateProductAttribute(Long productId, Long attributeId, String value) throws Exception;
    Product removeProductAttribute(Long productId, Long attributeId) throws Exception;    Product updateProductWithAttributes(Long productId, ProductDTO productDTO, List<Long> attributeIds, List<String> attributeValues) throws Exception;
    
    // Cập nhật sản phẩm với các thuộc tính động theo tên
    Product updateProductWithDynamicAttributes(Long productId, ProductUpdateDTO productUpdateDTO) throws Exception;
    }

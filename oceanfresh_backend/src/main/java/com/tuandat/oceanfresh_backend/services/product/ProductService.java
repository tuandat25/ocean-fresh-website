package com.tuandat.oceanfresh_backend.services.product;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tuandat.oceanfresh_backend.dtos.ProductDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductImageDTO;
import com.tuandat.oceanfresh_backend.dtos.ProductUpdateDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.exceptions.InvalidParamException;
import com.tuandat.oceanfresh_backend.models.Attribute;
import com.tuandat.oceanfresh_backend.models.Category;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductAttributeValue;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.repositories.AttributeRepository;
import com.tuandat.oceanfresh_backend.repositories.CategoryRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductAttributeValueRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductImageRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductRepository;
import com.tuandat.oceanfresh_backend.responses.ProductResponse;

import lombok.RequiredArgsConstructor;

/**
 * Service để quản lý các sản phẩm và thuộc tính của sản phẩm.
 * Sử dụng Entity-Attribute-Value pattern để lưu trữ thuộc tính động.
 */
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final AttributeRepository attributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    // Removed: không còn sử dụng Kafka
    // private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String UPLOADS_FOLDER = "uploads";    @Override
    @Transactional
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
        Category existingCategory = categoryRepository
                .findById(productDTO.getCategoryId())
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find category with id: "+productDTO.getCategoryId()));

        // Tạo sản phẩm mới với thông tin từ ProductDTO
        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice() != null ? productDTO.getPrice() : 0f)
                .thumbnail(productDTO.getThumbnail())
                .description(productDTO.getDescription())
                .category(existingCategory)
                .quantity(productDTO.getQuantity() != null ? productDTO.getQuantity() : 0L)
                .soldQuantity(productDTO.getSoldQuantity() != null ? productDTO.getSoldQuantity() : 0L)
                .build();
        return productRepository.save(newProduct);
    }

    @Override
    public Product getProductById(long productId) throws Exception {
        Optional<Product> optionalProduct = productRepository.getDetailProduct(productId);
        if(optionalProduct.isPresent()) {
            return optionalProduct.get();
        }
        throw new DataNotFoundException("Cannot find product with id =" + productId);
    }
    @Override
    public List<Product> findProductsByIds(List<Long> productIds) {
        return productRepository.findProductsByIds(productIds);
    }

    @Override
    public Page<ProductResponse> getAllProducts(String keyword,
                                                Long categoryId, PageRequest pageRequest) {
        // Lấy danh sách sản phẩm theo trang (page), giới hạn (limit), và categoryId (nếu có)
        Page<Product> productsPage;
        productsPage = productRepository.searchProducts(categoryId, keyword, pageRequest);
        return productsPage.map(ProductResponse::fromProduct);
    }
    @Override
    @Transactional
    public Product updateProduct(
            long id,
            ProductDTO productDTO
    )
            throws Exception {
        Product existingProduct = getProductById(id);
        if(existingProduct != null) {
            //copy các thuộc tính từ DTO -> Product
            //Có thể sử dụng ModelMapper
            Category existingCategory = categoryRepository
                    .findById(productDTO.getCategoryId())
                    .orElseThrow(() ->
                            new DataNotFoundException(
                                    "Cannot find category with id: "+productDTO.getCategoryId()));
            if(productDTO.getName() != null && !productDTO.getName().isEmpty()) {
                existingProduct.setName(productDTO.getName());
            }

            existingProduct.setCategory(existingCategory);
            if(productDTO.getPrice() != null && productDTO.getPrice() >= 0) {
                existingProduct.setPrice(productDTO.getPrice());
            }
            if(productDTO.getDescription() != null &&
                    !productDTO.getDescription().isEmpty()) {
                existingProduct.setDescription(productDTO.getDescription());
            }
            if(productDTO.getThumbnail() != null &&
                    !productDTO.getThumbnail().isEmpty()) {
                existingProduct.setThumbnail(productDTO.getThumbnail());
            }
            if(productDTO.getQuantity() != null) {
                existingProduct.setQuantity(productDTO.getQuantity());
            }
            if(productDTO.getSoldQuantity() != null) {
                existingProduct.setSoldQuantity(productDTO.getSoldQuantity());
            }
            return productRepository.save(existingProduct);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteProduct(long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        optionalProduct.ifPresent(productRepository::delete);
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
    @Override
    @Transactional
    public ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception {
        Product existingProduct = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find product with id: "+productImageDTO.getProductId()));
        ProductImage newProductImage = ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        //Ko cho insert quá 5 ảnh cho 1 sản phẩm
        int size = productImageRepository.findByProductId(productId).size();
        if(size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new InvalidParamException(
                    "Number of images must be <= "
                    +ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        if (existingProduct.getThumbnail() == null ) {
            existingProduct.setThumbnail(newProductImage.getImageUrl());
        }
        productRepository.save(existingProduct);
        return productImageRepository.save(newProductImage);
    }
    @Override
    public void deleteFile(String filename) throws IOException {
        // Đường dẫn đến thư mục chứa file
        java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);
        // Đường dẫn đầy đủ đến file cần xóa
        java.nio.file.Path filePath = uploadDir.resolve(filename);

        // Kiểm tra xem file tồn tại hay không
        if (Files.exists(filePath)) {
            // Xóa file
            Files.delete(filePath);
        } else {
            throw new FileNotFoundException("File not found: " + filename);
        }
    }
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        
        // Đường dẫn đến thư mục lưu file
        java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);
        
        // Kiểm tra và tạo thư mục nếu không tồn tại
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // Đường dẫn đầy đủ đến file đích
        java.nio.file.Path destinationPath = Paths.get(uploadDir.toString(), uniqueFilename);
        
        // Sao chép file vào thư mục đích
        Files.copy(file.getInputStream(), destinationPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        return uniqueFilename;
    }

    @Override
    @Transactional
    public Product addProductAttribute(Long productId, Long attributeId, String value) throws Exception {
        // Lấy sản phẩm từ database
        Product product = getProductById(productId);
        
        // Lấy thuộc tính từ database
        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new DataNotFoundException("Cannot find attribute with id: " + attributeId));
        
        // Kiểm tra xem thuộc tính này đã được gán cho sản phẩm chưa
        ProductAttributeValue existingValue = productAttributeValueRepository
                .findByProductIdAndAttributeId(productId, attributeId);
        
        if (existingValue != null) {
            // Cập nhật giá trị nếu thuộc tính đã tồn tại
            existingValue.setValue(value);
            existingValue.setUpdatedAt(LocalDateTime.now());
            productAttributeValueRepository.save(existingValue);
        } else {
            // Tạo mới thuộc tính cho sản phẩm
            ProductAttributeValue newValue = new ProductAttributeValue();
            newValue.setProduct(product);
            newValue.setAttribute(attribute);
            newValue.setValue(value);
            newValue.setCreatedAt(LocalDateTime.now());
            newValue.setUpdatedAt(LocalDateTime.now());
            
            productAttributeValueRepository.save(newValue);
        }
        
        return product;
    }
    
    @Override
    @Transactional
    public Product updateProductAttribute(Long productId, Long attributeId, String value) throws Exception {
        // Kiểm tra xem thuộc tính này có tồn tại cho sản phẩm không
        ProductAttributeValue existingValue = productAttributeValueRepository
                .findByProductIdAndAttributeId(productId, attributeId);
        
        if (existingValue == null) {
            throw new DataNotFoundException("Attribute with id " + attributeId + 
                                           " does not exist for product with id " + productId);
        }
        
        // Cập nhật giá trị
        existingValue.setValue(value);
        existingValue.setUpdatedAt(LocalDateTime.now());
        productAttributeValueRepository.save(existingValue);
        
        return getProductById(productId);
    }
    
    @Override
    @Transactional
    public Product removeProductAttribute(Long productId, Long attributeId) throws Exception {
        // Kiểm tra xem thuộc tính này có tồn tại cho sản phẩm không
        ProductAttributeValue existingValue = productAttributeValueRepository
                .findByProductIdAndAttributeId(productId, attributeId);
        
        if (existingValue == null) {
            throw new DataNotFoundException("Attribute with id " + attributeId + 
                                           " does not exist for product with id " + productId);
        }
        
        // Xóa thuộc tính
        productAttributeValueRepository.delete(existingValue);
        
        return getProductById(productId);
    }
      @Override
    @Transactional
    public Product updateProductWithAttributes(Long productId, ProductDTO productDTO, 
                                               List<Long> attributeIds, List<String> attributeValues) throws Exception {
        
        // Cập nhật thông tin cơ bản của sản phẩm
        Product updatedProduct = updateProduct(productId, productDTO);
        
        // Cập nhật thuộc tính sản phẩm
        if (attributeIds != null && attributeValues != null && attributeIds.size() == attributeValues.size()) {
            // Xóa tất cả thuộc tính hiện tại
            List<ProductAttributeValue> existingValues = productAttributeValueRepository.findByProductId(productId);
            productAttributeValueRepository.deleteAll(existingValues);
            
            // Thêm lại tất cả thuộc tính mới
            for (int i = 0; i < attributeIds.size(); i++) {
                updateAttributeById(updatedProduct, attributeIds.get(i), attributeValues.get(i));
            }
        }
        
        return updatedProduct;
    }
      /**
     * Helper method để cập nhật thuộc tính bằng ID thuộc tính
     */
    private void updateAttributeById(Product product, Long attributeId, String value) throws DataNotFoundException {
        if (product == null || attributeId == null || value == null) {
            return; // Không làm gì nếu dữ liệu đầu vào không hợp lệ
        }
        
        // Kiểm tra thuộc tính tồn tại
        Attribute attribute = attributeRepository.findById(attributeId)
            .orElseThrow(() -> new DataNotFoundException("Cannot find attribute with id: " + attributeId));
        
        // Tạo thuộc tính mới
        ProductAttributeValue newValue = new ProductAttributeValue();
        newValue.setProduct(product);
        newValue.setAttribute(attribute);
        newValue.setValue(value);
        newValue.setCreatedAt(LocalDateTime.now());
        newValue.setUpdatedAt(LocalDateTime.now());
        
        productAttributeValueRepository.save(newValue);
    }// Cập nhật sản phẩm với các thuộc tính động theo tên
    // Sử dụng mô hình Entity-Attribute-Value (EAV) để lưu trữ các thuộc tính động
    @Override
    @Transactional
    public Product updateProductWithDynamicAttributes(Long productId, ProductUpdateDTO productUpdateDTO) throws Exception {
        // 1. Cập nhật thông tin cơ bản của sản phẩm
        Product existingProduct = getProductById(productId);
        if (existingProduct == null) {
            throw new DataNotFoundException("Không tìm thấy sản phẩm với id: " + productId);
        }
        
        // Chuyển đổi từ ProductUpdateDTO sang ProductDTO để sử dụng phương thức updateProduct() hiện có
        ProductDTO basicProductDTO = new ProductDTO();
        basicProductDTO.setName(productUpdateDTO.getName());
        basicProductDTO.setPrice(productUpdateDTO.getPrice());
        basicProductDTO.setDescription(productUpdateDTO.getDescription());
        basicProductDTO.setThumbnail(productUpdateDTO.getThumbnail());
        basicProductDTO.setCategoryId(productUpdateDTO.getCategoryId());
        basicProductDTO.setQuantity(productUpdateDTO.getQuantity());
        basicProductDTO.setSoldQuantity(productUpdateDTO.getSoldQuantity());
        
        // Cập nhật thông tin cơ bản sản phẩm (bảng products)
        Product updatedProduct = updateProduct(productId, basicProductDTO);
        
        // 2. Cập nhật các thuộc tính động đơn giá trị theo mô hình Entity-Attribute-Value (EAV)
        Map<String, String> attributesMap = productUpdateDTO.getAttributes();
        if (attributesMap != null && !attributesMap.isEmpty()) {
            for (Map.Entry<String, String> entry : attributesMap.entrySet()) {
                String attributeName = entry.getKey();    // Tên thuộc tính
                String attributeValue = entry.getValue(); // Giá trị thuộc tính
                
                updateSingleValueAttribute(updatedProduct, attributeName, attributeValue);
            }
        }
        
        // 3. Cập nhật các thuộc tính động đa giá trị
        Map<String, List<String>> multiValueAttributesMap = productUpdateDTO.getMultiValueAttributes();
        if (multiValueAttributesMap != null && !multiValueAttributesMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : multiValueAttributesMap.entrySet()) {
                String attributeName = entry.getKey();          // Tên thuộc tính
                List<String> attributeValues = entry.getValue(); // Danh sách giá trị thuộc tính
                
                if (attributeValues == null || attributeValues.isEmpty()) {
                    continue; // Bỏ qua nếu danh sách giá trị trống
                }
                
                updateMultiValueAttribute(updatedProduct, attributeName, attributeValues);
            }
        }
        
        return updatedProduct;
    }
      /**
     * Helper method để cập nhật thuộc tính đơn giá trị
     */
    private void updateSingleValueAttribute(Product product, String attributeName, String attributeValue) {
        // Tìm hoặc tạo thuộc tính trong bảng attributes (theo tên)
        Attribute attribute;
        if (attributeRepository.existsByName(attributeName)) {
            // Thuộc tính đã tồn tại, sử dụng thuộc tính đó
            attribute = attributeRepository.findByName(attributeName);
        } else {
            // Tạo thuộc tính mới nếu chưa tồn tại
            attribute = new Attribute();
            attribute.setName(attributeName);
            attribute = attributeRepository.save(attribute);
        }
        
        if (product != null && product.getId() != null && attribute != null && attribute.getId() != null) {
            // Kiểm tra xem thuộc tính đã tồn tại cho sản phẩm này chưa
            ProductAttributeValue existingValue = productAttributeValueRepository
                    .findByProductIdAndAttributeId(product.getId(), attribute.getId());
            
            if (existingValue != null) {
                // Nếu thuộc tính đã tồn tại cho sản phẩm, cập nhật giá trị
                existingValue.setValue(attributeValue);
                existingValue.setUpdatedAt(LocalDateTime.now());
                productAttributeValueRepository.save(existingValue);
            } else {
                // Nếu thuộc tính chưa tồn tại cho sản phẩm, thêm mới vào bảng product_attribute_values
                ProductAttributeValue newValue = new ProductAttributeValue();
                newValue.setProduct(product);
                newValue.setAttribute(attribute);
                newValue.setValue(attributeValue);
                newValue.setCreatedAt(LocalDateTime.now());
                newValue.setUpdatedAt(LocalDateTime.now());
                productAttributeValueRepository.save(newValue);
            }
        }
    }    /**
     * Helper method để cập nhật thuộc tính đa giá trị
     * Giữ lại các giá trị hiện có nếu vẫn còn trong danh sách mới,
     * chỉ xóa những giá trị không còn trong danh sách mới,
     * và chỉ thêm những giá trị mới chưa tồn tại
     */
    private void updateMultiValueAttribute(Product product, String attributeName, List<String> newAttributeValues) {
        if (product == null || attributeName == null || newAttributeValues == null) {
            return; // Không làm gì nếu dữ liệu đầu vào không hợp lệ
        }
        
        // Tìm hoặc tạo thuộc tính trong bảng attributes (theo tên)
        Attribute attribute;
        if (attributeRepository.existsByName(attributeName)) {
            // Thuộc tính đã tồn tại, sử dụng thuộc tính đó
            attribute = attributeRepository.findByName(attributeName);
        } else {
            // Tạo thuộc tính mới nếu chưa tồn tại
            attribute = new Attribute();
            attribute.setName(attributeName);
            attribute = attributeRepository.save(attribute);
        }
        
        if (attribute == null || product.getId() == null || attribute.getId() == null) {
            return; // Không thể tiếp tục nếu không có thông tin cần thiết
        }
        
        // Lấy tất cả các giá trị hiện có của thuộc tính này
        List<ProductAttributeValue> existingValues = productAttributeValueRepository
                .findAllByProductIdAndAttributeId(product.getId(), attribute.getId());
        
        // Tạo danh sách các giá trị hiện tại
        List<String> existingValuesList = new ArrayList<>();
        if (existingValues != null) {
            existingValuesList = existingValues.stream()
                    .map(ProductAttributeValue::getValue)
                    .collect(Collectors.toList());
        }
        
        // Xác định giá trị cần xóa (có trong existingValues nhưng không có trong newAttributeValues)
        List<ProductAttributeValue> valuesToDelete = new ArrayList<>();
        if (existingValues != null) {
            for (ProductAttributeValue existingValue : existingValues) {
                if (!newAttributeValues.contains(existingValue.getValue())) {
                    valuesToDelete.add(existingValue);
                }
            }
        }
        
        // Xóa các giá trị không còn trong danh sách mới
        if (!valuesToDelete.isEmpty()) {
            productAttributeValueRepository.deleteAll(valuesToDelete);
        }
        
        // Thêm các giá trị mới chưa tồn tại
        for (String newValue : newAttributeValues) {
            if (!existingValuesList.contains(newValue)) {
                ProductAttributeValue attributeValue = new ProductAttributeValue();
                attributeValue.setProduct(product);
                attributeValue.setAttribute(attribute);
                attributeValue.setValue(newValue);
                attributeValue.setCreatedAt(LocalDateTime.now());
                attributeValue.setUpdatedAt(LocalDateTime.now());
                productAttributeValueRepository.save(attributeValue);
            }
        }
    }
}


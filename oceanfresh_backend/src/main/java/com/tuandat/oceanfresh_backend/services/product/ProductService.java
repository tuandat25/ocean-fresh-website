package com.tuandat.oceanfresh_backend.services.product;


import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper; // For StringUtils.hasText
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.tuandat.oceanfresh_backend.dtos.product.AttributeValueDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductCreateDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductDetailDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductImageDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductVariantDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductVariantRequestDTO;
import com.tuandat.oceanfresh_backend.exceptions.DuplicateResourceException;
import com.tuandat.oceanfresh_backend.exceptions.InvalidParamException;
import com.tuandat.oceanfresh_backend.exceptions.ResourceNotFoundException;
import com.tuandat.oceanfresh_backend.models.AttributeValue;
import com.tuandat.oceanfresh_backend.models.Category;
import com.tuandat.oceanfresh_backend.models.Product;
import com.tuandat.oceanfresh_backend.models.ProductImage;
import com.tuandat.oceanfresh_backend.models.ProductVariant;
import com.tuandat.oceanfresh_backend.repositories.AttributeValueRepository;
import com.tuandat.oceanfresh_backend.repositories.CategoryRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductImageRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductRepository;
import com.tuandat.oceanfresh_backend.repositories.ProductVariantRepository;
import com.tuandat.oceanfresh_backend.responses.product.ProductBaseResponse;

import lombok.RequiredArgsConstructor;


/**
 * Service để quản lý các sản phẩm và thuộc tính của sản phẩm.
 * Sử dụng Entity-Attribute-Value pattern để lưu trữ thuộc tính động.
 */
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
     private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ModelMapper modelMapper;

    // --- Product Methods ---
    @Override
    @Transactional
    public ProductDetailDTO createProduct(ProductCreateDTO productCreateDTO) {
        if (!StringUtils.hasText(productCreateDTO.getSlug())) {
            productCreateDTO.setSlug(generateSlug(productCreateDTO.getName()));
        }
        if (productRepository.existsBySlug(productCreateDTO.getSlug())) {
            throw new DuplicateResourceException("Sản phẩm", "slug", productCreateDTO.getSlug());
        }

        Product product = modelMapper.map(productCreateDTO, Product.class);

        if (productCreateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", productCreateDTO.getCategoryId()));
            product.setCategory(category);
        }
        product.setVariants(new HashSet<>()); // Khởi tạo variants rỗng khi tạo product

        Product savedProduct = productRepository.save(product);
        logger.info("Sản phẩm được tạo với ID: {} và slug: {}", savedProduct.getId(), savedProduct.getSlug());
        return mapProductToDetailDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));
        return mapProductToDetailDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "slug", slug));
        // If findBySlug doesn't use EntityGraph, manually initialize if needed, or ensure findById is called.
        // For simplicity, assume findById (which has EntityGraph) is preferred for detail views.
        // Or add @EntityGraph to findBySlug as well if it's a common entry point for details.
        return getProductById(product.getId()); // Call findById to leverage EntityGraph
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductBaseResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapProductToBaseDTO);
    }

    @Override
    @Transactional
    public ProductDetailDTO updateProduct(Long productId, ProductDetailDTO productDetailDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        // Update slug if name changes and no explicit slug is provided
        if (StringUtils.hasText(productDetailDTO.getName()) && !existingProduct.getName().equals(productDetailDTO.getName()) &&
            (!StringUtils.hasText(productDetailDTO.getSlug()) || productDetailDTO.getSlug().equals(existingProduct.getSlug()))) {
            productDetailDTO.setSlug(generateSlug(productDetailDTO.getName()));
        }

        if (StringUtils.hasText(productDetailDTO.getSlug()) && !existingProduct.getSlug().equals(productDetailDTO.getSlug()) &&
            productRepository.existsBySlugAndIdNot(productDetailDTO.getSlug(), productId)) {
            throw new DuplicateResourceException("Sản phẩm", "slug", productDetailDTO.getSlug());
        }

        // Manual mapping to control updates
        existingProduct.setName(productDetailDTO.getName());
        existingProduct.setSlug(productDetailDTO.getSlug());
        existingProduct.setDescription(productDetailDTO.getDescription());
        existingProduct.setBrand(productDetailDTO.getBrand());
        existingProduct.setOrigin(productDetailDTO.getOrigin());
        existingProduct.setMainImageUrl(productDetailDTO.getMainImageUrl());
        existingProduct.setActive(productDetailDTO.isActive());
        
        // Cập nhật các trường mới
        existingProduct.setStorageInstruction(productDetailDTO.getStorageInstruction());
        existingProduct.setHarvestDate(productDetailDTO.getHarvestDate());
        existingProduct.setFreshnessGuaranteePeriod(productDetailDTO.getFreshnessGuaranteePeriod());
        existingProduct.setDeliveryArea(productDetailDTO.getDeliveryArea());
        existingProduct.setReturnPolicy(productDetailDTO.getReturnPolicy());

        if (productDetailDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDetailDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", productDetailDTO.getCategoryId()));
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Sản phẩm được cập nhật với ID: {}", updatedProduct.getId());
        return mapProductToDetailDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Sản phẩm", "id", productId);
        }
        productRepository.deleteById(productId); // CascadeType.ALL in Product.variants will handle variants
        logger.info("Sản phẩm được xóa với ID: {}", productId);
    }


    // --- Product Variant Methods ---
    @Override
    @Transactional
    public ProductVariantDTO addVariantToProduct(Long productId, ProductVariantRequestDTO variantRequestDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        validateAttributeCombination(productId, null, variantRequestDTO.getSelectedAttributeValueIds());

        ProductVariant variant = modelMapper.map(variantRequestDTO, ProductVariant.class);
        variant.setProduct(product);

        Set<AttributeValue> selectedAttributeValues = fetchAndValidateAttributeValues(variantRequestDTO.getSelectedAttributeValueIds());
        variant.setSelectedAttributes(selectedAttributeValues);

        if (!StringUtils.hasText(variant.getVariantName())) {
            variant.setVariantName(generateVariantName(product, selectedAttributeValues));
        }

        // Generate SKU automatically if not provided or is empty
        if (!StringUtils.hasText(variantRequestDTO.getSku())) {
            String generatedSku = generateSku(product, selectedAttributeValues);
            variant.setSku(generatedSku);
            logger.info("Generated SKU for variant: {}", generatedSku);
        } else {
            // Check SKU uniqueness only if user provided a custom SKU
            if (productVariantRepository.findBySku(variantRequestDTO.getSku()).isPresent()) {
                throw new DuplicateResourceException("Biến thể sản phẩm", "SKU", variantRequestDTO.getSku());
            }
        }

        ProductVariant savedVariant = productVariantRepository.save(variant);
        logger.info("Biến thể sản phẩm được thêm với SKU: {} và ID sản phẩm: {}", savedVariant.getSku(), productId);
        return mapProductVariantToDTO(savedVariant);
    }


    @Override
    @Transactional
    public ProductVariantDTO updateProductVariant(Long variantId, ProductVariantRequestDTO variantRequestDTO) {
        ProductVariant existingVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể", "id", variantId));

        // Check SKU uniqueness if it's changed
        if (!existingVariant.getSku().equals(variantRequestDTO.getSku()) &&
            productVariantRepository.existsBySkuAndIdNot(variantRequestDTO.getSku(), variantId)) {
            throw new DuplicateResourceException("Biến thể sản phẩm", "SKU", variantRequestDTO.getSku());
        }

        validateAttributeCombination(existingVariant.getProduct().getId(), variantId, variantRequestDTO.getSelectedAttributeValueIds());

        // Map basic fields
        existingVariant.setSku(variantRequestDTO.getSku());
        existingVariant.setPrice(variantRequestDTO.getPrice());
        existingVariant.setOldPrice(variantRequestDTO.getOldPrice());
        existingVariant.setQuantityInStock(variantRequestDTO.getQuantityInStock());
        existingVariant.setThumbnailUrl(variantRequestDTO.getThumbnailUrl());
        existingVariant.setActive(variantRequestDTO.isActive());

        Set<AttributeValue> selectedAttributeValues = fetchAndValidateAttributeValues(variantRequestDTO.getSelectedAttributeValueIds());
        existingVariant.setSelectedAttributes(selectedAttributeValues);

        if (!StringUtils.hasText(variantRequestDTO.getVariantName())) {
            existingVariant.setVariantName(generateVariantName(existingVariant.getProduct(), selectedAttributeValues));
        } else {
            existingVariant.setVariantName(variantRequestDTO.getVariantName());
        }

        ProductVariant updatedVariant = productVariantRepository.save(existingVariant);
        logger.info("Biến thể sản phẩm được cập nhật với ID: {}", updatedVariant.getId());
        return mapProductVariantToDTO(updatedVariant);
    }

    private void validateAttributeCombination(Long productId, Long currentVariantIdToExclude, Set<Long> attributeValueIds) {
        if (CollectionUtils.isEmpty(attributeValueIds)) {
            throw new IllegalArgumentException("A variant must be defined by at least one attribute value.");
        }
        List<ProductVariant> variantsWithSameAttributes = productVariantRepository.findVariantsByProductAndAttributeValues(
                productId,
                attributeValueIds,
                attributeValueIds.size()
        );

        // Filter out the current variant being updated
        if (currentVariantIdToExclude != null) {
            variantsWithSameAttributes = variantsWithSameAttributes.stream()
                    .filter(v -> !v.getId().equals(currentVariantIdToExclude))
                    .collect(Collectors.toList());
        }

        if (!variantsWithSameAttributes.isEmpty()) {
            throw new DuplicateResourceException("Biến thể sản phẩm", "attributes", "Biến thể với thuộc tính đã chọn đã tồn tại.");
        }
    }


    private Set<AttributeValue> fetchAndValidateAttributeValues(Set<Long> attributeValueIds) {
        if (CollectionUtils.isEmpty(attributeValueIds)) {
            throw new IllegalArgumentException("Attribute value IDs cannot be empty for a variant.");
        }
        List<AttributeValue> foundValues = attributeValueRepository.findByIdIn(attributeValueIds);
        if (foundValues.size() != attributeValueIds.size()) {
            Set<Long> foundIds = foundValues.stream().map(AttributeValue::getId).collect(Collectors.toSet());
            attributeValueIds.removeAll(foundIds); // Get the missing IDs
            throw new ResourceNotFoundException("AttributeValue(s) not found with IDs: " + attributeValueIds);
        }
        return new HashSet<>(foundValues);
    }


    @Override
    @Transactional
    public void deleteProductVariant(Long variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("Biến thể sản phẩm", "id", variantId);
        }
        productVariantRepository.deleteById(variantId);
        logger.info("Deleted variant with ID: {}", variantId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantDTO getProductVariantById(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id", variantId));
        return mapProductVariantToDTO(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantDTO getProductVariantBySku(String sku) {
        ProductVariant variant = productVariantRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "SKU", sku));
        return mapProductVariantToDTO(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantDTO> getVariantsByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Sản phẩm", "id", productId);
        }
        return productVariantRepository.findByProductId(productId).stream()
                .map(this::mapProductVariantToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantDTO findActiveVariantByProductAndAttributes(Long productId, Set<Long> attributeValueIds) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Sản phẩm", "id", productId);
        }
        if (CollectionUtils.isEmpty(attributeValueIds)) {
            throw new IllegalArgumentException("Attribute value IDs must be provided to find a specific variant.");
        }

        List<ProductVariant> variants = productVariantRepository.findVariantsByProductAndAttributeValues(
                productId,
                attributeValueIds,
                attributeValueIds.size()
        );

        Optional<ProductVariant> activeVariant = variants.stream().filter(ProductVariant::isActive).findFirst();

        return activeVariant.map(this::mapProductVariantToDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No active variant found for product ID %d with the specified attributes.", productId)));
    }

    // --- Helper & Mapper Methods ---
    
    /**
     * Tự động tạo SKU theo format: PRODUCT_SLUG-ATTRIBUTE_VALUES-UNIQUE_ID
     * Ví dụ: ca-chua-do-500g-ABC123
     */
    private String generateSku(Product product, Set<AttributeValue> attributeValues) {
        StringBuilder skuBuilder = new StringBuilder();
        
        // 1. Thêm slug của product (rút gọn nếu quá dài)
        String productSlug = product.getSlug();
        if (productSlug.length() > 15) {
            productSlug = productSlug.substring(0, 15);
        }
        skuBuilder.append(productSlug.toUpperCase());
        
        // 2. Thêm các attribute values (sắp xếp theo tên attribute để đảm bảo tính nhất quán)
        if (attributeValues != null && !attributeValues.isEmpty()) {
            String attributeString = attributeValues.stream()
                    .sorted(Comparator.comparing(av -> av.getAttribute().getName()))
                    .map(av -> normalizeAttributeForSku(av.getValue()))
                    .collect(Collectors.joining("-"));
            
            if (StringUtils.hasText(attributeString)) {
                skuBuilder.append("-").append(attributeString);
            }
        }
        
        // 3. Thêm unique identifier để tránh trùng lặp
        String baseSkuCandidate = skuBuilder.toString();
        String finalSku = baseSkuCandidate;
        int counter = 1;
        
        // Kiểm tra và tăng counter nếu SKU đã tồn tại
        while (productVariantRepository.findBySku(finalSku).isPresent()) {
            finalSku = baseSkuCandidate + "-" + String.format("%03d", counter);
            counter++;
        }
        
        // Giới hạn độ dài SKU tối đa 50 ký tự
        if (finalSku.length() > 50) {
            String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            finalSku = baseSkuCandidate.substring(0, Math.min(baseSkuCandidate.length(), 38)) + "-" + uniquePart;
        }
        
        return finalSku;
    }
    
    /**
     * Chuẩn hóa attribute value để tạo SKU
     */
    private String normalizeAttributeForSku(String value) {
        if (!StringUtils.hasText(value)) return "";
        
        // Loại bỏ dấu tiếng Việt và ký tự đặc biệt, chuyển thành uppercase
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(normalized).replaceAll("");
        
        // Chỉ giữ lại chữ cái, số và một số ký tự đặc biệt
        result = result.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        
        // Giới hạn độ dài
        return result.length() > 10 ? result.substring(0, 10) : result;
    }

    private String generateSlug(String name) {
        if (!StringUtils.hasText(name)) return UUID.randomUUID().toString();
        String nowhitespace = name.trim().toLowerCase().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");
        slug = slug.replaceAll("[^a-z0-9\\-]", ""); // Remove invalid chars
        slug = slug.replaceAll("-+", "-"); // Replace multiple hyphens
        return slug.length() > 200 ? slug.substring(0,200) : slug; // Max length
    }

    private String generateVariantName(Product product, Set<AttributeValue> attributeValues) {
        String attributeString = attributeValues.stream()
                .sorted(Comparator.comparing(av -> av.getAttribute().getName())) // Sort for consistency
                .map(AttributeValue::getValue)
                .collect(Collectors.joining(" - "));
        return product.getName() + (StringUtils.hasText(attributeString) ? " - " + attributeString : "");
    }   
    
    private ProductBaseResponse mapProductToBaseDTO(Product product) {
        return ProductBaseResponse.fromEntity(product);
    }    private ProductDetailDTO mapProductToDetailDTO(Product product) {
        ProductDetailDTO dto = modelMapper.map(product, ProductDetailDTO.class);
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(this::mapProductVariantToDTO)
                    .collect(Collectors.toSet()));
        }        // Map danh sách ảnh
        if (product.getProductImages() != null) {
            dto.setImages(product.getProductImages().stream()
                    .map(image -> ProductImageDTO.builder()
                            .productId(image.getProduct().getId())
                            .imageUrl(image.getImageUrl())
                            .build())
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private ProductVariantDTO mapProductVariantToDTO(ProductVariant variant) {
        ProductVariantDTO dto = modelMapper.map(variant, ProductVariantDTO.class);
        dto.setProductId(variant.getProduct().getId());
        if (variant.getSelectedAttributes() != null) {
            dto.setSelectedAttributes(variant.getSelectedAttributes().stream()
                    .map(this::mapAttributeValueToDTO)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    private AttributeValueDTO mapAttributeValueToDTO(AttributeValue attributeValue) {
        AttributeValueDTO dto = modelMapper.map(attributeValue, AttributeValueDTO.class);
        if (attributeValue.getAttribute() != null) {
            dto.setAttributeId(attributeValue.getAttribute().getId()); // Set attributeId explicitly
            dto.setAttributeName(attributeValue.getAttribute().getName());
        }
        return dto;
    }
    @Override
    @Transactional
    public ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception {
        Product existingProduct = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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
        if (existingProduct.getMainImageUrl() == null ) {
            existingProduct.setMainImageUrl(newProductImage.getImageUrl());
        }
        productRepository.save(existingProduct);
        return productImageRepository.save(newProductImage);
    }

    @Override
    public Page<ProductBaseResponse> getAllProductsIsActive(Pageable pageable) {
        // TODO Auto-generated method stub
        Page<Product> productPage = productRepository.findAllByIsActiveTrue(pageable);
        return productPage.map(this::mapProductToBaseDTO);
    }
}


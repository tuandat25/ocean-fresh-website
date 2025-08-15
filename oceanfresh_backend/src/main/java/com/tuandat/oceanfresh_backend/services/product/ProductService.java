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

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tuandat.oceanfresh_backend.dtos.attribute.AttributeValueDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductCreateDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductDetailDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductImageDTO;
import com.tuandat.oceanfresh_backend.dtos.product.ProductUpdateDTO;
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
import com.tuandat.oceanfresh_backend.utils.FileUtils;

import lombok.RequiredArgsConstructor;

/**
 * Service để quản lý các sản phẩm và thuộc tính của sản phẩm.
 * Sử dụng Entity-Attribute-Value pattern để lưu trữ thuộc tính động.
 */
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ModelMapper modelMapper;

    // Tạo sản phẩm mới
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
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Danh mục", "id", productCreateDTO.getCategoryId()));
            product.setCategory(category);
        }

        product.setVariants(new HashSet<>());
        Product savedProduct = productRepository.save(product);

        // Tạo các biến thể cho sản phẩm
        if (productCreateDTO.getVariants() != null && !productCreateDTO.getVariants().isEmpty()) {
            for (ProductVariantRequestDTO variantDTO : productCreateDTO.getVariants()) {
                try {
                    addVariantToProduct(savedProduct.getId(), variantDTO);
                } catch (Exception e) {
                    productRepository.delete(savedProduct);
                    throw new RuntimeException("Lỗi khi tạo biến thể sản phẩm: " + e.getMessage(), e);
                }
            }
        }

        return getProductById(savedProduct.getId());
    }

    // Thêm method mới cho tạo sản phẩm với ảnh chính
    @Override
    @Transactional
    public ProductDetailDTO createProductWithMainImage(
            ProductCreateDTO productCreateDTO,
            MultipartFile mainImage) throws Exception {

        try {
            if (!StringUtils.hasText(productCreateDTO.getSlug())) {
                productCreateDTO.setSlug(generateSlug(productCreateDTO.getName()));
            }
            if (productRepository.existsBySlug(productCreateDTO.getSlug())) {
                throw new DuplicateResourceException("Sản phẩm", "slug", productCreateDTO.getSlug());
            }

            Product product = modelMapper.map(productCreateDTO, Product.class);

            product.setActive(productCreateDTO.getIsActive() != null ? productCreateDTO.getIsActive() : true);
            if (productCreateDTO.getCategoryId() != null) {
                Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id",
                                productCreateDTO.getCategoryId()));
                product.setCategory(category);
            }

            product.setVariants(new HashSet<>());
            Product savedProduct = productRepository.save(product);

            // Tạo variants
            if (productCreateDTO.getVariants() != null && !productCreateDTO.getVariants().isEmpty()) {
                for (ProductVariantRequestDTO variantDTO : productCreateDTO.getVariants()) {
                    try {
                        addVariantToProduct(savedProduct.getId(), variantDTO);
                    } catch (Exception e) {
                        throw new RuntimeException("Lỗi khi tạo biến thể sản phẩm: " + e.getMessage(), e);
                    }
                }
            }

            // Upload ảnh chính nếu có
            if (mainImage != null && !mainImage.isEmpty()) {
                try {
                    validateImageFile(mainImage);
                    String mainImageFileName = FileUtils.storeFile(mainImage);
                    savedProduct.setMainImageUrl(mainImageFileName);
                    productRepository.save(savedProduct);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi khi upload ảnh chính: " + e.getMessage(), e);
                }
            }

            return getProductById(savedProduct.getId());

        } catch (Exception e) {
            throw e;
        }
    }

    // Helper method để validate ảnh
    private void validateImageFile(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước ảnh không được vượt quá 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là hình ảnh");
        }
    }

    // Lấy sản phẩm theo id
    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));
        return mapProductToDetailDTO(product);
    }

    // Lấy sản phẩm theo slug
    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "slug", slug));
        return getProductById(product.getId());
    }

    // Admin: lây tất cả sản phẩm với phân trang
    @Override
    @Transactional(readOnly = true)
    public Page<ProductBaseResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapProductToBaseDTO);
    }

    // Admin: cập nhật sản phẩm
    @Override
    @Transactional
    public ProductDetailDTO updateProduct(Long productId, ProductUpdateDTO productUpdateDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        if (StringUtils.hasText(productUpdateDTO.getName())
                && !existingProduct.getName().equals(productUpdateDTO.getName()) &&
                (!StringUtils.hasText(productUpdateDTO.getSlug())
                        || productUpdateDTO.getSlug().equals(existingProduct.getSlug()))) {
            productUpdateDTO.setSlug(generateSlug(productUpdateDTO.getName()));
        } else if (existingProduct.getName().equals(productUpdateDTO.getName())) {
            productUpdateDTO.setSlug(existingProduct.getSlug());
        }

        if (StringUtils.hasText(productUpdateDTO.getSlug())
                && !existingProduct.getSlug().equals(productUpdateDTO.getSlug()) &&
                productRepository.existsBySlugAndIdNot(productUpdateDTO.getSlug(), productId)) {
            throw new DuplicateResourceException("Sản phẩm", "slug", productUpdateDTO.getSlug());
        }

        existingProduct.setName(productUpdateDTO.getName());
        existingProduct.setSlug(productUpdateDTO.getSlug());
        existingProduct.setDescription(productUpdateDTO.getDescription());
        existingProduct.setBrand(productUpdateDTO.getBrand());
        existingProduct.setOrigin(productUpdateDTO.getOrigin());
        // existingProduct.setMainImageUrl(productUpdateDTO.getMainImageUrl());
        existingProduct.setActive(productUpdateDTO.isActive());

        existingProduct.setStorageInstruction(productUpdateDTO.getStorageInstruction());
        existingProduct.setHarvestDate(productUpdateDTO.getHarvestDate());
        existingProduct.setFreshnessGuaranteePeriod(productUpdateDTO.getFreshnessGuaranteePeriod());
        existingProduct.setHarvestArea(productUpdateDTO.getHarvestArea());
        existingProduct.setReturnPolicy(productUpdateDTO.getReturnPolicy());

        if (productUpdateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productUpdateDTO.getCategoryId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Danh mục", "id", productUpdateDTO.getCategoryId()));
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return mapProductToDetailDTO(updatedProduct);
    }

    // Admin: xóa sản phẩm
    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Sản phẩm", "id", productId);
        }
        productRepository.deleteById(productId);
    }

    // Thêm biến thể sản phẩm Admin
    @Override
    @Transactional
    public ProductVariantDTO addVariantToProduct(Long productId, ProductVariantRequestDTO variantRequestDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        validateAttributeCombination(productId, null, variantRequestDTO.getSelectedAttributeValueIds());

        ProductVariant variant = modelMapper.map(variantRequestDTO, ProductVariant.class);
        variant.setActive(variantRequestDTO.getIsActive() == null ? true : variantRequestDTO.getIsActive());
        variant.setProduct(product);

        Set<AttributeValue> selectedAttributeValues = fetchAndValidateAttributeValues(
                variantRequestDTO.getSelectedAttributeValueIds());
        variant.setSelectedAttributes(selectedAttributeValues);

        if (!StringUtils.hasText(variant.getVariantName())) {
            variant.setVariantName(generateVariantName(product, selectedAttributeValues));
        }

        if (!StringUtils.hasText(variantRequestDTO.getSku())) {
            String generatedSku = generateSku(product, selectedAttributeValues);
            variant.setSku(generatedSku);
        } else {
            if (productVariantRepository.findBySku(variantRequestDTO.getSku()).isPresent()) {
                throw new DuplicateResourceException("Biến thể sản phẩm", "SKU", variantRequestDTO.getSku());
            }
        }

        ProductVariant savedVariant = productVariantRepository.save(variant);
        return mapProductVariantToDTO(savedVariant);
    }

    // Admin: cap nhat bien the san pham
    @Override
    @Transactional
    public ProductVariantDTO updateProductVariant(Long variantId, ProductVariantRequestDTO variantRequestDTO) {
        ProductVariant existingVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể", "id", variantId));

        if (!existingVariant.getSku().equals(variantRequestDTO.getSku()) &&
                productVariantRepository.existsBySkuAndIdNot(variantRequestDTO.getSku(), variantId)) {
            throw new DuplicateResourceException("Biến thể sản phẩm với", "SKU", variantRequestDTO.getSku());
        }

        validateAttributeCombination(existingVariant.getProduct().getId(), variantId,
                variantRequestDTO.getSelectedAttributeValueIds());

        existingVariant.setSku(variantRequestDTO.getSku());
        existingVariant.setPrice(variantRequestDTO.getPrice());
        existingVariant.setOldPrice(variantRequestDTO.getOldPrice());
        existingVariant.setQuantityInStock(variantRequestDTO.getQuantityInStock());
        existingVariant.setThumbnailUrl(variantRequestDTO.getThumbnailUrl());
        existingVariant.setActive(variantRequestDTO.getIsActive());

        Set<AttributeValue> selectedAttributeValues = fetchAndValidateAttributeValues(
                variantRequestDTO.getSelectedAttributeValueIds());
        existingVariant.setSelectedAttributes(selectedAttributeValues);

        if (!StringUtils.hasText(variantRequestDTO.getVariantName())) {
            existingVariant.setVariantName(generateVariantName(existingVariant.getProduct(), selectedAttributeValues));
        } else {
            existingVariant.setVariantName(variantRequestDTO.getVariantName());
        }

        ProductVariant updatedVariant = productVariantRepository.save(existingVariant);
        return mapProductVariantToDTO(updatedVariant);
    }

    private void validateAttributeCombination(Long productId, Long currentVariantIdToExclude,
            Set<Long> attributeValueIds) {
        if (CollectionUtils.isEmpty(attributeValueIds)) {
            throw new IllegalArgumentException("A variant must be defined by at least one attribute value.");
        }
        List<ProductVariant> variantsWithSameAttributes = productVariantRepository
                .findVariantsByProductAndAttributeValues(
                        productId,
                        attributeValueIds,
                        attributeValueIds.size());

        if (currentVariantIdToExclude != null) {
            variantsWithSameAttributes = variantsWithSameAttributes.stream()
                    .filter(v -> !v.getId().equals(currentVariantIdToExclude))
                    .collect(Collectors.toList());
        }

        if (!variantsWithSameAttributes.isEmpty()) {
            throw new DuplicateResourceException("Biến thể sản phẩm", "attributes",
                    "Biến thể với thuộc tính đã chọn đã tồn tại.");
        }
    }

    private Set<AttributeValue> fetchAndValidateAttributeValues(Set<Long> attributeValueIds) {
        if (CollectionUtils.isEmpty(attributeValueIds)) {
            throw new IllegalArgumentException("Attribute value IDs cannot be empty for a variant.");
        }
        List<AttributeValue> foundValues = attributeValueRepository.findByIdIn(attributeValueIds);
        if (foundValues.size() != attributeValueIds.size()) {
            Set<Long> foundIds = foundValues.stream().map(AttributeValue::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(attributeValueIds);
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException("Không tìm thấy AttributeValue(s) với ID: " + missingIds);
        }

        return new HashSet<>(foundValues);
    }

    // admin: xóa biến thể sản phẩm
    @Override
    @Transactional
    public void deleteProductVariant(Long variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("Biến thể sản phẩm", "id", variantId);
        }
        productVariantRepository.deleteById(variantId);
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
                attributeValueIds.size());

        Optional<ProductVariant> activeVariant = variants.stream().filter(ProductVariant::isActive).findFirst();

        return activeVariant.map(this::mapProductVariantToDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No active variant found for product ID %d with the specified attributes.",
                                productId)));
    }

    private String generateSku(Product product, Set<AttributeValue> attributeValues) {
        StringBuilder skuBuilder = new StringBuilder();

        String productSlug = product.getSlug();
        if (productSlug.length() > 15) {
            productSlug = productSlug.substring(0, 15);
        }
        skuBuilder.append(productSlug.toUpperCase());

        if (attributeValues != null && !attributeValues.isEmpty()) {
            String attributeString = attributeValues.stream()
                    .sorted(Comparator.comparing(av -> av.getAttribute().getName()))
                    .map(av -> normalizeAttributeForSku(av.getValue()))
                    .collect(Collectors.joining("-"));

            if (StringUtils.hasText(attributeString)) {
                skuBuilder.append("-").append(attributeString);
            }
        }

        String baseSkuCandidate = skuBuilder.toString();
        String finalSku = baseSkuCandidate;
        int counter = 1;

        while (productVariantRepository.findBySku(finalSku).isPresent()) {
            finalSku = baseSkuCandidate + "-" + String.format("%03d", counter);
            counter++;
        }

        if (finalSku.length() > 50) {
            String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            finalSku = baseSkuCandidate.substring(0, Math.min(baseSkuCandidate.length(), 38)) + "-" + uniquePart;
        }

        return finalSku;
    }

    private String normalizeAttributeForSku(String value) {
        if (!StringUtils.hasText(value))
            return "";

        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(normalized).replaceAll("");

        result = result.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        return result.length() > 10 ? result.substring(0, 10) : result;
    }

    private String generateSlug(String name) {
        if (!StringUtils.hasText(name))
            return UUID.randomUUID().toString();
        String nowhitespace = name.trim().toLowerCase().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");
        slug = slug.replaceAll("[^a-z0-9\\-]", "");
        slug = slug.replaceAll("-+", "-");
        return slug.length() > 200 ? slug.substring(0, 200) : slug;
    }

    private String generateVariantName(Product product, Set<AttributeValue> attributeValues) {
        String attributeString = attributeValues.stream()
                .sorted(Comparator.comparing(av -> av.getAttribute().getName()))
                .map(AttributeValue::getValue)
                .collect(Collectors.joining(" - "));
        return product.getName() + (StringUtils.hasText(attributeString) ? " - " + attributeString : "");
    }

    private ProductBaseResponse mapProductToBaseDTO(Product product) {
        return ProductBaseResponse.fromEntity(product);
    }

    private ProductDetailDTO mapProductToDetailDTO(Product product) {
        ProductDetailDTO dto = modelMapper.map(product, ProductDetailDTO.class);
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

        if (!variants.isEmpty()) {
            dto.setVariants(variants.stream()
                    .map(this::mapProductVariantToDTO)
                    .collect(Collectors.toSet()));
        }

        if (product.getProductImages() != null) {
            dto.setImages(product.getProductImages().stream()
                    .map(image -> ProductImageDTO.builder()
                            .id(image.getId())
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
            dto.setAttributeId(attributeValue.getAttribute().getId());
            dto.setAttributeName(attributeValue.getAttribute().getName());
        }
        return dto;
    }

    @Override
    @Transactional
    public ProductDetailDTO uploadMainImage(Long productId, String imageName) throws Exception {
        if (imageName == null || imageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên file ảnh không được để trống");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        if (product.getMainImageUrl() != null && !product.getMainImageUrl().trim().isEmpty()) {
            String oldImagePath = product.getMainImageUrl();
            try {
                FileUtils.deleteFile(oldImagePath);
            } catch (Exception e) {
                // Không throw exception vì ảnh mới đã upload thành công
            }
            product.setMainImageUrl(imageName);
        } else {
            product.setMainImageUrl(imageName);
        }

        Product savedProduct = productRepository.save(product);
        return mapProductToDetailDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception {
        Product existingProduct = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cannot find product with id: " + productImageDTO.getProductId()));
        ProductImage newProductImage = ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();

        int size = productImageRepository.findByProductId(productId).size();
        if (size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new InvalidParamException(
                    "Number of images must be <= "
                            + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        if (existingProduct.getMainImageUrl() == null) {
            existingProduct.setMainImageUrl(newProductImage.getImageUrl());
        }
        productRepository.save(existingProduct);
        return productImageRepository.save(newProductImage);
    }

    @Override
    public Page<ProductBaseResponse> getAllProductsIsActive(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByIsActiveTrue(pageable);
        return productPage.map(this::mapProductToBaseDTO);
    }

    @Override
    public Page<ProductBaseResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", categoryId));
        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        return productPage.map(this::mapProductToBaseDTO);
    }

    @Override
    public Page<ProductBaseResponse> getActiveProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", categoryId));
        Page<Product> productPage = productRepository.findByCategoryAndIsActiveTrue(category, pageable);
        return productPage.map(this::mapProductToBaseDTO);
    }

    // Search tất cả sản phẩm (admin)
    @Override
    @Transactional(readOnly = true)
    public Page<ProductBaseResponse> searchProducts(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return productRepository.findAll(pageable).map(this::mapProductToBaseDTO);
        }
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);
        return productPage.map(this::mapProductToBaseDTO);

    }

    // Search chỉ sản phẩm active (user)
    @Override
    @Transactional(readOnly = true)
    public Page<ProductBaseResponse> searchActiveProducts(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return productRepository.findAllByIsActiveTrue(pageable).map(this::mapProductToBaseDTO);
        }
        Page<Product> productPage = productRepository.searchActiveProducts(keyword, pageable);
        return productPage.map(this::mapProductToBaseDTO);
    }
}
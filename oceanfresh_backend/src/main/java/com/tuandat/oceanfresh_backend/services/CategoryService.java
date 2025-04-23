package com.tuandat.oceanfresh_backend.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import com.tuandat.oceanfresh_backend.dtos.CategoryDTO;
import com.tuandat.oceanfresh_backend.models.Category;
import com.tuandat.oceanfresh_backend.repositories.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;
    // private final ProductRepository productRepository;
    private static String UPLOADS_FOLDER = "uploads";
    @Override
    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
        Category newCategory = Category
                .builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .imageUrl(categoryDTO.getImageUrl())
                .build();
        return categoryRepository.save(newCategory);
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category updateCategory(long categoryId,
                                   CategoryDTO categoryDTO) {
        Category existingCategory = getCategoryById(categoryId);
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());
        
        // Chỉ cập nhật imageUrl nếu có giá trị mới
        if (categoryDTO.getImageUrl() != null && !categoryDTO.getImageUrl().isEmpty()) {
            existingCategory.setImageUrl(categoryDTO.getImageUrl());
        }
        
        categoryRepository.save(existingCategory);
        return existingCategory;
    }

    // @Override
    // @Transactional
    // public Category deleteCategory(long id) throws Exception {
    //     Category category = categoryRepository.findById(id)
    //             .orElseThrow(() -> new ChangeSetPersister.NotFoundException());

    //     List<Product> products = productRepository.findByCategory(category);
    //     if (!products.isEmpty()) {
    //         throw new IllegalStateException("Cannot delete category with associated products");
    //     } else {
    //         categoryRepository.deleteById(id);
    //         return  category;
    //     }
    // }


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
    }
    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        // Đường dẫn đến thư mục mà bạn muốn lưu file
        java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);
        // Kiểm tra và tạo thư mục nếu nó không tồn tại
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // Đường dẫn đầy đủ đến file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        // Sao chép file vào thư mục đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
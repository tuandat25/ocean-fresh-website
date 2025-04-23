package com.tuandat.oceanfresh_backend.services;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.tuandat.oceanfresh_backend.dtos.CategoryDTO;
import com.tuandat.oceanfresh_backend.models.Category;

public interface ICategoryService {
    Category createCategory(CategoryDTO category);
    Category getCategoryById(long id);
    Category getCategoryByName(String name);
    List<Category> getAllCategories();
    Category updateCategory(long categoryId, CategoryDTO category);
    // Category deleteCategory(long id) throws Exception;
    String storeFile(MultipartFile file) throws IOException;
    void deleteFile(String filename) throws IOException;

}

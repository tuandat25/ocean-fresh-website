package com.tuandat.oceanfresh_backend.services;

import java.util.List;

import com.tuandat.oceanfresh_backend.dtos.CategoryDTO;
import com.tuandat.oceanfresh_backend.models.Category;

public interface ICategoryService {
    Category createCategory(CategoryDTO category);
    Category getCategoryById(long id);
    List<Category> getAllCategories();
    Category updateCategory(long categoryId, CategoryDTO category);
    Category deleteCategory(long id) throws Exception;

}
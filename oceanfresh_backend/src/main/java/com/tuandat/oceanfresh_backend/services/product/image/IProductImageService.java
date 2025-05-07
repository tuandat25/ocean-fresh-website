package com.tuandat.oceanfresh_backend.services.product.image;

import com.tuandat.oceanfresh_backend.models.ProductImage;

public interface IProductImageService {
    ProductImage deleteProductImage(Long id) throws Exception;
}

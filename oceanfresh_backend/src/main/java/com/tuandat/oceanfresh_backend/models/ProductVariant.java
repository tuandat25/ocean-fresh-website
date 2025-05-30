package com.tuandat.oceanfresh_backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "variant_name")
    private String variantName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "old_price", precision = 15, scale = 2)
    private BigDecimal oldPrice;    @Column(name = "quantity_in_stock", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int quantityInStock = 0;

    @Column(name = "sold_quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int soldQuantity = 0;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean isActive = true;    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "product_variant_attribute_values",
            joinColumns = @JoinColumn(name = "product_variant_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_value_id")
    )
    @Builder.Default
    private Set<AttributeValue> selectedAttributes = new HashSet<>();
}

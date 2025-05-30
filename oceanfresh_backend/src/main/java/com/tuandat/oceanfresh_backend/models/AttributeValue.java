package com.tuandat.oceanfresh_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attribute_values", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attribute_id", "value"})
})
public class AttributeValue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(nullable = false)
    private String value;    @Column(name = "display_order", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int displayOrder = 0;

    @ManyToMany(mappedBy = "selectedAttributes", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ProductVariant> productVariants = new HashSet<>();
}
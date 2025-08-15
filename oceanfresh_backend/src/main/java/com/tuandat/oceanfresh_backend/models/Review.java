package com.tuandat.oceanfresh_backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Data
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "order_detail_id")
    private OrderDetail orderDetail;

    @Column(nullable = false)
    private Integer rating; // Từ 1 đến 5 sao

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = true; // Admin có thể cần duyệt

    @Column(name = "admin_responses", columnDefinition = "TEXT")
    private String adminResponses; // Để trả lời review
}

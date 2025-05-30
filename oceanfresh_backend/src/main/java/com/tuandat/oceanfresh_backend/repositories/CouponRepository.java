package com.tuandat.oceanfresh_backend.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tuandat.oceanfresh_backend.models.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByCode(String code);
    
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, Long id);
    
    List<Coupon> findByIsActiveTrue();
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now")
    List<Coupon> findActiveAndValidCoupons(@Param("now") LocalDateTime now);
    
    //  Sửa lại query để sử dụng field names đúng từ model
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now " +
           "AND (c.maxTotalUsage IS NULL OR c.currentUsageCount < c.maxTotalUsage)")
    List<Coupon> findAvailableCoupons(@Param("now") LocalDateTime now);
    
    //  Thêm query để validate coupon với điều kiện đầy đủ
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.isActive = true " +
           "AND c.startDate <= :now AND c.endDate >= :now " +
           "AND (c.maxTotalUsage IS NULL OR c.currentUsageCount < c.maxTotalUsage)")
    Optional<Coupon> findValidCoupon(@Param("code") String code, @Param("now") LocalDateTime now);
    
    //  Query để kiểm tra coupon có thể sử dụng với giá trị đơn hàng
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.isActive = true " +
           "AND c.startDate <= :now AND c.endDate >= :now " +
           "AND c.minimumOrderValue <= :orderValue " +
           "AND (c.maxTotalUsage IS NULL OR c.currentUsageCount < c.maxTotalUsage)")
    Optional<Coupon> findValidCouponForOrder(@Param("code") String code, 
                                           @Param("now") LocalDateTime now,
                                           @Param("orderValue") java.math.BigDecimal orderValue);
    
    // Thống kê
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.isActive = true")
    Long countActiveCoupons();
    
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.endDate < :now")
    Long countExpiredCoupons(@Param("now") LocalDateTime now);
    
    //  Thống kê coupon đã hết lượt sử dụng
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.maxTotalUsage IS NOT NULL AND c.currentUsageCount >= c.maxTotalUsage")
    Long countExhaustedCoupons();
    
    //  Lấy các coupon sắp hết hạn trong N ngày
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.endDate BETWEEN :now AND :endDate")
    List<Coupon> findCouponsExpiringBetween(@Param("now") LocalDateTime now, @Param("endDate") LocalDateTime endDate);
}
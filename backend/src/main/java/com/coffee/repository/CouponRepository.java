package com.coffee.repository;

import com.coffee.entity.Coupon;
import com.coffee.wrapper.CouponWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("SELECT NEW com.coffee.wrapper.CouponWrapper(c.id, c.name, c.code, " +
            "c.discount, c.expirationDate) FROM Coupon c")
    List<CouponWrapper> getAllCoupon();

    Coupon findByNameCoupon(@Param("name") String name);

    @Query("SELECT NEW com.coffee.wrapper.CouponWrapper(c.id, c.name, c.code, c.discount, c.expirationDate) " +
            "FROM Coupon c WHERE c.id=:id")
    CouponWrapper getCouponById(@Param("id") Long id);

    Coupon findByCode(@Param("couponCode") String couponCode);
}

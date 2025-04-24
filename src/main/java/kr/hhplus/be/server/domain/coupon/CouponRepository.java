package kr.hhplus.be.server.domain.coupon;

public interface CouponRepository {

    Coupon save(Coupon coupon);
    Coupon getById(Long couponId);
    Coupon getByIdLocking(Long couponId);
    void updateQuantity(long couponId, int remainingQuantity);
}

package kr.hhplus.be.server.infrastructure.coupon.persistence;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.shared.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity couponEntity = CouponEntity.builder()
                .name(coupon.getName())
                .type(coupon.getType())
                .discountValue(coupon.getDiscountValue())
                .initialQuantity(coupon.getInitialQuantity())
                .remainingQuantity(coupon.getRemainingQuantity())
                .startDate(coupon.getStartDateTime())
                .endDate(coupon.getEndDateTime())
                .build();

        return couponJpaRepository.save(couponEntity)
                .toDomain();
    }

    @Override
    public Coupon getById(Long couponId) {
        return couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 쿠폰 식별자입니다."))
                .toDomain();
    }

    @Override
    public Coupon getByIdLocking(Long couponId) {
        return couponJpaRepository.findByIdLocking(couponId)
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 쿠폰 식별자입니다."))
                .toDomain();
    }

    @Override
    public void updateQuantity(long couponId, int remainingQuantity) {
        CouponEntity couponEntity = couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 쿠폰 식별자입니다."));

        couponEntity.updateQuantity(remainingQuantity);
    }
}

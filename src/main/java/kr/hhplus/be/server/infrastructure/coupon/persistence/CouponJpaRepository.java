package kr.hhplus.be.server.infrastructure.coupon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
        SELECT
            C
        FROM
            CouponEntity C
        WHERE
            C.couponId = :couponId
    """)
    Optional<CouponEntity> findByIdLocking(Long couponId);
}

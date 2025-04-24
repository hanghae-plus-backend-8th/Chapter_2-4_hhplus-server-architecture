package kr.hhplus.be.server.infrastructure.coupon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface MemberCouponJpaRepository extends JpaRepository<MemberCouponEntity, Long>, MemberCouponJpaRepositoryCustom {

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
        SELECT
            MC
        FROM
            MemberCouponEntity MC
        WHERE
            MC.couponNumber = :couponNumber
    """)
    Optional<MemberCouponEntity> findByCouponNumber(String couponNumber);
}

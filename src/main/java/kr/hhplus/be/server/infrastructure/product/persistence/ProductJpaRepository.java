package kr.hhplus.be.server.infrastructure.product.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long>, ProductJpaRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT
            P
        FROM
            ProductEntity P
        WHERE
            P.productId in :productIds
        ORDER BY
            P.productId ASC
    """)
    List<ProductEntity> findAllByIdsLocking(List<Long> productIds);
}

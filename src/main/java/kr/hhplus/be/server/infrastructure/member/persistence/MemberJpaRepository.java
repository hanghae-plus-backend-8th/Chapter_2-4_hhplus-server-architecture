package kr.hhplus.be.server.infrastructure.member.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT
            M
        FROM
            MemberEntity M
        WHERE
            M.memberId = :memberId
    """)
    Optional<MemberEntity> findByIdLocking(Long memberId);
}

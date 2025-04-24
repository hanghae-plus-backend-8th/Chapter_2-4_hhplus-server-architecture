

## ❓ 동시성 이슈가 발생하는 기능 (문제 식별)

- (선착순) 쿠폰 발급 기능

> 동일 쿠폰을 다수의 사용자가 동시에 발급할 경우, 수량 차감이 정확히 반영되지 않습니다.

- 주문/결제 기능 

> 동일 상품을 다수의 사용자가 동시에 주문할 경우, 재고 차감이 정확히 반영되지 않습니다.

- 포인트 충전 기능

> 동일 사용자가 동시에 포인트 충전을 여러 번 하는 경우, 포인트 적립이 정확히 반영되지 않습니다.

---

## 🔍 동시성 이슈가 왜 발생하는가 (분석)

```java
// 상품 조회
@Override
public List<Product> findAllByIds(List<Long> productIds) {
   return productJpaRepository.findAllById(productIds)
           .stream()
           .map(ProductEntity::toDomain)
           .toList();
}

// 쿠폰 조회 
@Override
public Coupon getById(Long couponId) {
   return couponJpaRepository.findById(couponId)
           .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 쿠폰 식별자입니다."))
           .toDomain();
}
```
- Spring Data JPA에서 만들어주는 기본적인 조회 메소드는 별도의 락을 설정하지 않고 조회합니다.

- 따라서, 동시에 여러 명의 사용자가 조회하여 수정하게 된다면 "두 번의 갱신 분실 문제 (Second Lost Updates Problem)"가 발생하여 데이터 일관성이 맞지 않게 됩니다. 

---

## 기능별 적절한 Lock 적용 (해결)

- (선착순) 쿠폰 발급 기능

   - 여러 명의 사용자가 "동일한" 쿠폰을 발급받을 수 있어 충돌 빈도가 매우 높다고 판단했습니다.
   - 따라서, 낙관적 락을 적용하기보다 비관적 락을 적용하게 되었습니다. 

- 주문/결제 기능

   - 여러 명의 사용자가 "동일한" 상품을 주문할 수 있기 때문에 충돌 빈도가 매우 높다고 판단했습니다 (e.g. 인기 상품)
   - 따라서, 낙관적 락을 적용하기보다 비관적 락을 적용하게 되었습니다.

- 포인트 충전 기능

   - 사용자가 포인트 충전을 동시에 여러 번 요청하는 경우는 프론트에서 충전 버튼을 빠르게 "따닥" 연타하는 상황 이외에는 없다고 판단했습니다.
   - 충분히 프론트에서 처리할 수 있는 부분이지만 우회할 수 있기 때문에 (e.g. disable javascript) 데이터 일관성을 위해 비관적 락을 적용하게 되었습니다.
   - 물론, 충돌 빈도가 매우 낮을 확률이 높아 낙관적 락을 적용할 수도 있겠지만 별도의 퍼사드 클래스 + version 컬럼을 추가해야 하고 주문/결제 로직에서
     포인트 사용 시, 버저닝 업데이트를 해주어야 하기 때문에 개발 편의성을 위해 비관적 락을 선택하게 되었습니다.

### [참고] 한 메소드에 @Retryable + @Transactional 어노테이션 중복 사용 

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    @Retryable(retryFor = OptimisticLockException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public PointChargeResult charge(long memberId, long amount) {
        MemberPoint memberPoint = pointRepository.getByIdLocking(memberId);
   
        memberPoint.charge(amount);
   
        pointRepository.updatePoint(memberPoint.getMemberId(), memberPoint.getPoint());
        pointHistoryRepository.save(PointHistory.create(memberId, TransactionType.CHARGE, amount));
   
        return new PointChargeResult(memberPoint);
    }
}
```
- 위처럼 퍼사드 클래스를 만들지 않고 @Retryable + @Transactional 어노테이션을 서비스 메소드에 함께 사용할 경우, 적용 순서에 따라 사이드 이펙트가 발생할 수 있습니다.

- @Transactional이 먼저 적용되면 동일 트랜잭션 내에서 계속 재시도를 하게 되므로 영속성 컨텍스트가 초기화 되지 않아 격리 수준이 Read-Committed 이더라도 Repeatable-Read로
  작동하여 새롭게 커밋된 데이터를 읽어올 수 없어 Busy-Waiting 현상이 발생할 수 있습니다.

- (OSIV 설정을 끄더라도) 왜냐하면 트랜잭션의 라이프 사이클과 영속성 컨텍스트의 라이프 사이클이 동일하기 때문입니다. (in Spring) 

---

## 🚀 시간이 지나, 사용자 트래픽이 급증하게 된다면 어떠한 전략을 세워야 하는가 (대안)

- (선착순) 쿠폰 발급 기능

  - 데이터베이스의 비관적 락이 아닌 레디스의 분산락을 활용하거나 쿠폰 수량을 레디스에서 캐싱하여 사용하되 비동기적으로 데이터 동기화하기
  - 추가적으로 레디스 분산락 활용 시, 현재 쿠폰 테이블 (COUPON)의 PK키를 회원 쿠폰 테이블 (MEMBER_COUPON)에서 FK키로 설정되어 있기 때문에 
    FK키 데드락 이슈가 발생할 수 있어 FK키를 해제해야 합니다.

- 주문/결제 기능

  - 현재 상품에 대한 재고 수량은 상품 테이블에서 컬럼으로 관리되고 있기 때문에 별도의 재고 테이블로 분리한 다음 SKIP LOCKED을 활용하여
    데이터베이스 락 경합을 낮추기

  - 또는 데이터베이스의 비관적 락이 아닌 레디스의 분산락 활용하기 

---

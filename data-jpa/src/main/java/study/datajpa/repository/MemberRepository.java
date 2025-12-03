package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository  extends JpaRepository<Member, Long> {

    // 1. 메소드 쿼리
    // 자동으로 sql 작성해주는데 너무 길다~ ->
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // 2. @Query 사용
//    @Query(name ="Member.findByUsername") NamedQuery 사용안함 ㅅㄱ
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username")String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();


    //Dto로 반환 Dto 반환 시 new 로 가져올 필드명 명시
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    // 컬렉션
    List<Member> findListByUsername(String username);

    // 단건

    /**
     * 리턴 타입을 리스트로 선언한 경우
     * 절대 null을 반환하지 않고 [] 빈 리스트를 반환함
     *
     * 리턴 타입을 Entity로 선언한 경우
     * 결과 없음 -> null 반환
     * 결과 1개 -> 엔티티 반환
     * 결과 2개 이상 -> 예외 발생
     *
     * 리턴 타입을 Optional로 선언한 경우
     * 결과 없음 -> Optional.empty()
     *  결과 1개 -> Optional.of(member)
     *  결과 2개 -> 예외 발생
     */
    Member findMemberByUsername(String username);

    // 단건 Optional
    Optional<Member> findOptionalByUsername(String username);



    // 페이징+Slice 처리
    // 직접 쿼리를 날려서 카운트 쿼리를 안 날리게 해서 성능 최적화
    // 직접 카운트 쿼리를 만들어서 간단하게 토탈 카운트 조회
//    @Query(value= "select m from Member m left join m.team t",
//                    countQuery = "select count(m) from Member m")
    // 카운트 쿼리를 안날리고 싶으면
    //1. JPQL로 필요한 정보만 받아옴 -> List로 반환
    //2. SLice 사용 -> 다음 페이지의 유무만 확인함
    //3. countQuery = "selec~으로 직접 count를 조회하여 최적화 -> 잘 안씀
    Page<Member> findByAge(int age, Pageable pageable);
    // find -> select
    // ByAge -> where age = : age
    // Pageable 파라미터 있음 -> paging + sorting + count 쿼리 생성
    // -> select m from Member m where m.age = :age + 정렬, 페이징
//    Slice<Member> findByAge(int age, Pageable pageable);




    // 벌크 연산
    // 벌크 연산 자체가 영속성 컨텍스트를 무시하고 DB에 바로 실행됨
    // 그렇기 때문에 벌크 연산을 수행한 이후 1차 캐시를 싹다 날려야됨 -> 변경 감지 무시
    // 변경감지를 통해 원래 스냅샵 vs 1차 캐시를 비교하는데 이게 불가능 함
    // 그래야 다음에 조회할 때 DB의 정합성을 지킬 수 있음
    // execuateUpdate() 실행 + em.clear과정까지 해줌 -> DB 정합성 유지
    // @Modifying -> 해당 쿼리는 select가 아니라 update하는 쿼리다
    // JPQL은 update/delete와 select를 다른 쿼리로 판단함 -> 쿼리 실행 수 excuateUpdate()하는 역할
    // clearAutomatically= true를 통해 1차 캐시를 날려버림

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bilkAgePlus(@Param("age") int age);
    // 반환 값으로 해당 연산에 영향을 받은 row를 반환함



    // 엔티티 그래프가 나온 이유
    // 보통 JPA는 연관관계 전략으로 지연로딩을 사용함
    // 지연 로딩 중 다대다 관계에서 N+1문제가 발생함
    // 이를 막기 위해 fetch join를 사용하는데 이거 사용하려면 JPQL 작성해야됨
    // 매우 귀찮음 -> 엔티티 그래프
    // fetch Join(한방 쿼리)로 가져올 경우 프록시 객체가 아닌 실 객체로 조회
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();


    //엔티티 그래프 -> 동적으로 fetch 전략을 사용하게 해줌
    // 지연 로딩 설정을 무시하고 사용자가 지정한 특정 연관 엔티티와 즉시 로딩하도록 함
    // fetch 전략을 쓰고 싶음 -> 근데 쓰려면 @Query에다가 jpql써야됨
    // 아 귀찮은데 -> 앤티티 그래프, 엔티티 fetch 둘다 프록시 객체 x
    @Override
    @EntityGraph(attributePaths = {"team"} )
    List<Member> findAll();
    // member에 관한 모든 필드를 찾아내서 리스트로 반환할거야
    // select m.*, t.* from member m left join team t on m.team_id = t.team_id


    // jpql쓰면서 fetch전략까지 근데 굳이 이거는.. 흠
    @EntityGraph(attributePaths = {"team"} )
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    // 근데 이거까지는 잘 사용하지 않음
    @EntityGraph(attributePaths = {"team"} )
//    @EntityGraph("Member.all") // 엔티티에서 fetch전략 정의 후 재활용
    List<Member> findEntityGraphByUsername(@Param("username") String username);


    // JPA Hint -> hibernate에서 제공
    // 조회 할 때 스냅샷 객체를 만들지 않고
   @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true") )
    Member findReadonlyByUsername(String username);

   // Lock -> jpa에서 제공
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);
}

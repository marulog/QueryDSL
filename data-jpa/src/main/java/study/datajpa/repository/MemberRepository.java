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

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

//    @Query(name ="Member.findByUsername") NamedQuery 사용안함 ㅅㄱ
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username")String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();


    //Dto로 반환
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    // 컬렉션
    List<Member> findListByUsername(String username);

    // 단건
    Member findMemberByUsername(String username);

    // 단건 Optional
    Optional<Member> findOptionalByUsername(String username);

    // 직접 쿼리를 날려서 카운트 쿼리를 안 날리게 해서 성능 최적화
    // 직접 카운트 쿼리를 만들어서 간단하게 토탈 카운트 조회
//    @Query(value= "select m from Member m left join m.team t",
//                    countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

//    Slice<Member> findByAge(int age, Pageable pageable);

    // execuateUpdate() 실ㄸ행 + em.clear과정까지 해줌 -> DB 정합성 유지
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bilkAgePlus(@Param("age") int age);

    // 멤버와 관련된 팀을 한번에 가져옴
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();


    //엔티티 그래프
    // fetch 전략을 쓰고 싶음 -> 근데 쓰려면 @Query에다가 jpql써야됨
    // 아 귀찮은데 -> 앤티티 그래프
    @Override
    @EntityGraph(attributePaths = {"team"} )
    List<Member> findAll();


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

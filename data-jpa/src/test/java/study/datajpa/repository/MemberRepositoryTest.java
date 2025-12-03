package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext EntityManager em;

    @Test
    public void save(){
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member);

    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // count 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGraterThen() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan(m1.getUsername(), m1.getAge());
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    @Test
    public void nameQuery(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    void findUser() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA",10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    void findUsernameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void returnType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        Member findMember = memberRepository.findMemberByUsername("AAA");
        System.out.println("findMember = " + findMember);

        // List의 경우 못 찾아도 일단 반환됨(null)
        // 다만 단건조회의 경우 NoResultException를 터트리지 않고 Spring Data Jpa가 감싸서 null로 반환홤
        // 아~ 모르겠고 난 그냥 Optional 쓸래 ㅋㅋ 클라이언트가 알아서행 ㅎ
        // 단건 조회에서 2개가 조회되면 예외 터짐
        List<Member> nullList = memberRepository.findListByUsername("dlfjsdjds");
        System.out.println("nullList = " + nullList.size());
    }


    //페이징, 정렬
    @Test
    public void paging(){
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));

        // Spring Data JPA는 0페이지부터 시작
        // 0번재 페이지에서 데이터 3개를 넣을거임
        // 그러면 1번째 페이지에는 2개가 들어가겠지
        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

        //when -> count 쿼리 사용
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // API 반환할 때는 DTO로 반환해서 사용
        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));



        //then
        // total 카운트를 jpa가 자동으로 날리는데 DB전체를 조회하기 때문에
        // DB가 많은 경우 성능이 느려짐 -> 카운트 제외하는 쿼리 제공함
        // List로 반환하면 count 쿼리 안함
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void slice(){
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));

        // Spring Data JPA는 0페이지부터 시작
        // 0번재 페이지에서 데이터 3개를 넣을거임
        // 그러면 1번째 페이지에는 2개가 들어가겠지
        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

        //when slice는 limit에 +1를 붙어서 N+1개를 가져옴
        Slice<Member> page = memberRepository.findByAge(age, pageRequest);


        //then
        List<Member> content = page.getContent();
//        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(2);
//        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
//        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void bilkAgePlus() {
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));

        int resultCount = memberRepository.bilkAgePlus(20);

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);
        // 41이 아닌 40살로 저장되어있음
        // 벌크 연산 이후에는 영속성 컨텍스트를 싹다 날려야됨
        // 벌크 연산은 영속성 컨텍스트를 거치지 않고 DB에 바로 쿼리를 날림

        assertThat(resultCount).isEqualTo(3);

    }

    @Test
    public void findMemberLazy(){
        //given
        //member1 -> teamA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();
        List<Member> members = memberRepository.findAll();
        // 지금 앤티티 그래프라서 자동 fetch join으로 가져옴

        // team를 조회하지 않음 -> Lazy전략
        // memmber.team.name -> 직접 조회 할 시 그 때 join하여 팀을 조회함
        // 조회전까지는 프록시 객체이
        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
        }
    }

    @Test
    void findMemberFetchJoin() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();
        // fetch join 전략으로 변경
        // 이 경우 team은 프록시 객체가 아닌 진짜 객체를 조회해서 저장
        List<Member> members = memberRepository.findMemberFetchJoin();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
        }
    }

    @Test
    public void queryHint(){
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        //when
//        Member findMember = memberRepository.findById(member1.getId()).get();
//        findMember.setUsername("member2");
//
//        em.flush(); // 이 시점에서 1차 캐시와 앤티티값을 비교하여 변경감지함
//        // 1차 캐시에는 name = member1, 앤티니는 name = member2
//        // 변경을 감지하고 DB로 update문 쏨
//        // 다만, 비교를 하기 위해 스냅샷 => 2개의 객체를 따로 관리하는 단점
        // -> JPA Hint 사용함, 스냅샷을 만들지 않음 -> 변경감지를 안함 -> update 쿼리 x
        // 근데 요즘 기술이 좋아져서 성능차이는 그닥 없음, 진짜 트래픽이 많은 곳에만 적용 추천ㅇ
        Member findMember = memberRepository.findReadonlyByUsername(member1.getUsername());
        findMember.setUsername("member2");

    }

    @Test
    public void lock(){
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        List<Member> result = memberRepository.findLockByUsername(member1.getUsername());

    }
}
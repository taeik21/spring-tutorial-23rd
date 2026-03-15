# spring-tutorial-22rd
CEOS 백엔드 23기 스프링 튜토리얼

## IoC / DI

- **IoC (제어의 역전)**
    - **객체 생성, 관리**를 개발자가 아닌 **스프링이 담당**
- **DI** (의존성 주입)
    - **IoC는 디자인 패턴**
    - **DI는 IoC 구현 방식 중 하나**
        - 객체를 직접 생성하는 것이 아닌, **스프링 컨테이너가 생성한 객체(빈)를 주입받아** 사용
    
- **개발자가 객체의 생명주기 담당**하는 경우..
    - `new`로 객체 생성
        
        ```jsx
        public class OrderService {
            private final PaymentService paymentService;
        
            public OrderService() {
                this.paymentService = new KakaoPayService();
            }
        }
        ```
        
        - **변경에 닫혀**있음
            - 다른 구현체로 교체하려면 **클래스 코드 수정**해야함
        - 객체의 생성과 소멸을 개발자가 관리해 **메모리 낭비 가능**
        - **Mock 및 Test 하기 어려**움
            
            ```jsx
            @Test
            void 주문_테스트() {
                PaymentService mockPayment = mock(PaymentService.class);
                
                // new로 직접 생성 방식
                // OrderService 내부에서 new KakaoPayService()로 고정되어 있어서
                // mockPayment를 넣어줄 방법이 없음!
                // KakaoPayService가 강제됨
                OrderService orderService = new OrderService(); 
            }
            ```
            
- **IoC를 통해 스프링이 객체 생명주기 담당**!!
    
    ```jsx
    @Service
    @RequiredArgsConstructor
    public class OrderService {
    		private final PaymentService paymentService;
    }
    ```
    
    - 수정 없이 다른 구현체로 교체 가능
    - Mock 및 Test 편리함
        
        ```jsx
        @Test
        void 주문_테스트() {
            PaymentService mockPayment = mock(PaymentService.class);
            
            // 생성자 주입 방식
            OrderService orderService = new OrderService(mockPayment); 
        }
        ```
        
    - **DI 방식**
        - ***생성자를 통한 주입***
        - 필드 주입
        - setter로 주입
    - **생성자를 통한 의존성 주입** 권장
        - **필수 의존성** 보장
            - **참조하는 객체 없이 객체 초기화 불가능**
            - **setter 방식**은 의존성 없이 객체 생성 가능
                - 불완전한 객체 가능
        - **불변 객체** 생성
            - `final`를 통해 **객체 생성 후, 값 변경 불가능**
            - **setter, 필드 주입 방식**은 `final` 사용 불가
        - **순환 참조 감지**
            - **애플리케이션 시작 시점**에 에러 발생
            - **필드 주입**은 순환 참조가 **런타임에 에러 발생**시킴
        - **테스트의 편리함**
            - 스프링 없이도 테스트 가능
                
                ```jsx
                //생성자 주입 방식
                public class MemberServiceTest {
                		@Test
                		void 회원가입() {
                				MemberRepository repo = new MemberRepository();
                				MemberService service = new MemberService(repo);				
                				...
                		}
                }
                ```
                
            - **필드 주입 방식**은 직접 주입 불가능해, **스프링 컨테이너** 필요
                
                ```jsx
                @SpringBootTest    //실제 서버처럼 **스프링 컨테이너를 띄우고, 빈 등록**
                public class MemberServiceTest {
                		@Test
                		void 회원가입() {
                				@Autowired
                				MemberRepository memberRepository;
                				
                				@Autowired
                				MemberService memberService;
                				...
                		}
                }
                ```
---
## AOP (Aspect Oriented Programming)
- 여러 비지니스 로직에서 **반복되는 부가 기능**을 하나의 **공통 로직으로 모듈화**
    - `@RestControllerAdvice`, `@Transactional`, `@Valid` 등
- **관점**
    - **핵심 기능과 부가 기능으로 구분**해 **각각을 하나의 관점**으로 보는 것
        - 핵심 기능
            - 비지니스 로직
        - **부가 기능**
            - 트랜잭션 처리, 로깅 등
- 개념
    1. **Aspect**
        - 반복되는 기능을 **분리해 만든 모듈**
    2. **Join point**
        - Pointcut에 의해 대상이 될 수 있는 **비지니스 로직의 시작 지점**
    3. **Pointcut**
        - 부가 기능을 실행할 **대상을 선택하는 기준**
    4. **Advice**
        - **실행될 부가 기능** & 부가 기능이 **언제 실행**될지
        - **interceptor**로 모델링됨
            - JoinPoint 주변에서 **interceptor chain**을 유지
    5. **Target Object**
        - **부가 기능을 수행**하는 객체
            - 항상 프록시 객체
    6. **Weaving**
        - **핵심 로직 코드에 Aspect 적용되는 과정**
            - **런타임 위빙 (**스프링 AOP 방식**)**
                - **Proxy를 통한 방식**
                - 프로그램이 실행될 때, **Proxy 객체를 생성해 부가 기능 적용**
                    - 실제 객체: 비지니스 로직 수행
                    - **프록시**: 부가 기능 수행
    7. Introduction
        - 메소드 실행 흐름에 부가 로직 끼워넣는 것이 아닌,
        - **객체의 구조를 변경**하는 기술
            - **거의 사용 X!!!!!**
                - 클래스에 구현하지 않는 인터페이스 사용되므로, **어디서 구현했는지 흐름 파악 어려**움..

```java
@Slf4j
@Aspect 
@Component
public class TimeTraceAop {    

    @Around("execution(* com.example.spring-practice..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable { 
        long start = System.currentTimeMillis();
        log.info("START: {}", joinPoint.toString());
        
        try {
            return joinPoint.proceed();    // 핵심 기능 실행
        } finally {    
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            log.info("END: {} {}ms",joinPoint.toString(), timeMs);
        }
    }
}
```

- `@Around`: **Advice**에 해당
    - `Before`, `After`, `AfterReturning`, `AfterThrowing` 등 존재
- **`execution(...)`:** **Pointcut**에 해당
    - `*`(맨 앞): 모든 리턴 타입에 대해
    - `com.example.spring-practice..`: 해당 패키지의 하위 패키지 전부
    - `*`: 모든 클래스, 메소드 이름에 대해
    - `(..)`: 파라미터 상관 없이
- `ProceedingJoinPoint`
    - 실행되야하는 **핵심 비지니스 로직의 정보**를 담은 객체
        - `joinPoint.proceed`: **JoinPoint**에 해당
    - **`@Around` 어노테이션 사용 시,** 스프링이 `ProceedingJoinPoint` 객체를 파라미터로 넣어줌
- **작동 방식**
  
    
    1. **스프링**
        - 애플리케이션 실행 시, **MemberService**와 **MemberService의 Proxy를 빈**으로 생성
        - **MemberService에 AOP** 적용되어 있으므로
    2. **스프링**
        - **MemberController**에게 MemberService의 **Proxy 객체를 DI**
    3. **MemberController**
        - MemberService.save() 호출
        - **사실 Proxy를 호출**
    4. **Proxy**
        - 호출된 save() 메소드(Join point)가 Pointcut에 일치하는지 검사 (`@Before`, `@Around`)
            - 불일치: 부가 기능 없이 실제 MemberService 호출
            - 일치: Advice(인터셉터) 순서대로 실행 후 실제 MemberService 호출
    5. **MemberService**
        - 비지니스 로직 수행 후, Proxy에 반환
    6. **Proxy**
        - 호출된 save() 메소드(Join point)가 Pointcut에 일치하는지 검사 (`@After`, `@Around`)
            - 불일치: 부가 기능 없이 실제 MemberController 호출
            - 일치: Advice(인터셉터) 순서대로 실행 후 실제 MemberController 호출
    7. **MemberController**
        - 결과 받음
---

## OOP (객체 지향 프로그래밍)
- **절차 지향 프로그래밍**
    - 거대한 **프로그램을 한 덩어리로 작성**
        - 데이터와 함수 분리
        - **함수 단위**로 **위에서 아래로 순서**대로 실행
    - 특징
        - **매우 빠른 연산속도**
        - **유지보수 안 좋음**
            - **여러 함수가 동일한 데이터 처리**해, 데이터가 **어떻게 변했는지 추적 불가능**
            - 규모가 커질수록 **함수 간 호출 순서와 의존성 복잡**해, **새로운 기능 추가하기 위해 모든 호출 순서 확인**해야함
- **객체 지향 프로그래밍**
    - **독립적인 객체**들을 만들어 **객체들 간 서로 메시지 호출하며 실행**
        - 데이터와 그 데이터에 접근하는 함수를 **하나의 객체로 캡슐화**
    - **4가지 핵심 원칙**
        1. **추상화**
            - 여러 객체의 복잡한 세부사항은 버리고, **공통된 핵심 속성과 메소드만 추출**하는 과정
            - **인터페이스**나 추상클래스를 만들어, 어떻게 동작할지 정의
        2. **캡슐화**
            - 데이터와 데이터에 접근하는 메소드를 하나의 객체에 캡슐화
            - **정보 은닉**으로 내부 데이터의 직접적인 접근 막고, 필요한 인터페이스만 노출
        3. **상속**
            - 기존 클래스의 속성과 기능 재사용
            - 코드 중복 줄임
        4. **다형성**
            - 동일한 인터페이스나 부모 클래스를 통해 **다양한 구현체로 교체 가능**
            - **스프링 DI의 근간**
    - 특징
        - 객체를 통해 **핵심 관심사 분리**해 가독성, 재사용성 및 유지보수성 향상
    - **AOP는 OOP를 대체하는 기술이 아닌, 완벽한 OOP를 위한 기술**
        - **OOP로 핵심 비지니스 로직을 잘 설계**하려면, 반복되는 **부가 기능 처리해줄 AOP 반드시 필요**
            - OOP로 핵심 비지니스 로직을 분리해 설계해도, **여러 로직에 걸쳐 반복적으로 나타나는 부가 기능** 발생
                - 하나의 메소드에 **핵심 비지니스 로직과 공통 기능이 같이 나타남**
                    - 가독성 하락 및 유지보수하기 힘듦
            - OOP
                - 객체를 통해 **핵심 비지니스 로직 분리**
            - AOP
                - **핵심 비지니스 로직으로 부터 공통 관심사를 분리**

---
## PSA (Portable Service Abstraction)

- **하나의 추상화**로 **여러 서비스 묶어**둔 것
    - **비지니스 로직 수정하지 않고** 여러 서비스 교체 가능
        - **구현 기술이 달려져도 동일한 방식으로** 사용하도록 해줌
1. **트랜잭션 관리의 PSA**
    - **순수 JDBC**와 **JPA**로 DB에 연결할 때, 각각 **트랜잭션 처리하는 코드 다름**
        - JDBC에서 JPA로 변경하면 트랜잭션 코드 전부 수정해야함..
    - **PSA** 기술인 `@Transcational`을 통해 내부 기술이 **JDBC든 JPA든 상관 없이 동일 로직**으로 처리
        
        ```jsx
        @Service
        public class MemberService {
        		...
        		
        		@Transactional
        		public void join(Member member) {
        				memberRepository.save(member);
        		}
        }
        ```
        
2. **웹 서버의 PSA** 
    - 웹 서버마다 **웹 요청 받아** 처리하는 코드 다름
        - **서블릿 기술 사용하는 톰캣**에서 서블릿 지원하지 않는 네티**로 변경하면 코드 전부 수정**해야함..
    - **PSA기술인 DispathcerServlet을 통해 웹 기술에 대해 추상화**
        - 웹 서버가 **톰캣**이든, **네티**든 서버의 종류에 상관 없이 코드 수정 안해도 됨!
        - `@RestController`, `@GetMapping` 같은 일관된 어노테이션으로 개발!
        
        ```jsx
        @RestController
        public class MemberController {
        		...
        		
        		@GetMapping("/members/{id}")
        		public MemberDto getMember(@Pathvariable Long id) {
        				return memberService.findById(id);
        		}
        }
        ```
---
## Bean
- **스프링 IoC 컨테이너**에 의해 생성되고 관리되는 **자바 객체**
    - 직접 `new`로 생성하는 객체 X
- **빈 등록**
    - **컴포넌트 스캔 방식**
        - `@Component`
            - 스프링이 해당 클래스의 생성자를 호출해 객체 생성해 자동으로 빈 등록
        - 직접 작성하는 유틸리티 클래스를 빈으로 등록할 때
            - `@Service`, `@Repository`, `@Controller`, `@Component`
        
        ```jsx
        @RestController
        public class MemberController {
        		...
        }
        ```
        
        - **실행 과정**
            1. `@SpringBootApplication` 내부의 `@ComponentScan` 작동해 해당 패키지부터 모든 하위 패키지의 `@Component` 감지
                - `@Service`, `@Repository`, `@Controller` 모두 내부적으로 `@Component` 포함
            2. 스프링이 해당 클래스의 **BeanDefinition**이라는 빈 설계도 생성
                - 어떤 생성자를 사용하는지, 누굴 DI 받는지 등 기록
            3. 스프링이 해당 클래스의 생성자 호출해 객체 생성
            4. **BeanDefinition**에 따라 DI, 프록시와 같은 후처리 작업 후 빈 등록
            
    - **수동 등록 방식**
        - `@Configuration` + `@Bean`
            - `@Configuration` 설정 클래스 안에서 `@Bean` 붙인 **메서드의 반환 객체가 빈으로 등록**
        - **외부 라이브러리를 스프링 빈으로 등록**할 때
            - 외부 코드에 `@Component` 붙일 수 없음
                - 이미 컴파일 된 **.jar 파일 형태 (읽기 전용)이므로 수정 불가**
        
        ```jsx
        @Configuration
        public class QueryDslConfig {
        
        		@Bean
        		public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        				return new JPAQueryFactory(em);
        		}
        }
        ```
        
        - **실행 과정**
            1. 컴포넌트 스캔으로 `@Configuration` 감지해 **AppConfig** 클래스로 빈 등록
                - `@Configuration`은 내부적으로 `@Component` 포함
            2. **AppConfig** 내부에 `@Bean`이 붙은 메서드 전부 호출
            3. 직접 작성한 `new`를 통해 반환된 객체를 빈으로 모두 등록
        
- 하나의 interface를 구현한 Repository가 여러 개 있을 때 주입 방법
    - **접근하는 DB를 바꾸**는 사례 고려
        - **@Primary**
            - 대부분의 경우 A만 사용하고, B는 거의 사용 안하는 경우 사용
            
            ```jsx
            @Repository    
            public class DBMemberRepository implements MemberRepository { ... }
            ```
            
            ```jsx
            @Primary    //사용할 객체에 작성
            @Repository    
            public class MySqlMemberRepository implements MemberRepository { ... }
            ```
            
            ```jsx
            @Service @Transactional
            public class MemberService {
            		private final MemberRepository memberRepository;
            		
            		public MemberService(MemberRepository memberRepository) { ... }
            }
            ```
            
        - **@Qualifier(DI할 객체)**
            - 같은 환경 안에서 상황에 따라 A와 B 선택해서 사용하는 경우 (동시에 필요)
            
            ```jsx
            @Qualifier("dbRepository")
            @Repository
            public class DbMemberRepository implements MemberRepository { ... }
            ```
            
            ```jsx
            @Qualifier("mySqlRepository")
            @Repository
            public class MySqlMemberRepository implements MemberRepository { ... }
            ```
            
            ```jsx
            @Service @Transactional
            public class MemberService {
            		private final MemberRepository memberRepository;
            		
            		public MemberService(
            				@Qualifier("mySqlRepository")    // 동일한 이름의 객체 DI
            				MemberRepository memberRepository) { ... }
            }
            ```
            
        - **Profile**
            - 서로 다른 환경에 따라 A, B 구분해서 사용
            
            ```jsx
            @Profile("local")
            @Repository
            public class DbMemberRepository implements MemberRepository { ... }
            ```
            
            ```jsx
            @Profile("prod")
            @Repository
            public class MySqlMemberRepository implements MemberRepository { ... }
            ```
            
            ```jsx
            @Service @Transactional
            public class MemberService {
            		private final MemberRepository memberRepository;
            		
            		public MemberService(MemberRepository memberRepository) { ... }
            }
            ```
            
            ```jsx
            # application.yaml
            spring:
            		profiles:
            				active: local
            ---
            spring:
            		profiles:
            				active: prod
            ```
            

- **빈 스코프**
    - **객체의 생성 개수 및 생존 범위**
    1. **singleton**
        - **스프링 컨테이너에 딱 1개의 객체만 생성 후, 모든 요청에서 같은 객체 공유**
            - 요청 들어올 때마다 **동일한 객체를 계속 생성하면 메모리 낭비**되므로
            - 싱글톤 빈에 **상태(state) 저장하면 안** 됨
                - **여러 쓰레드가 동시 접근하므로 데이터 꼬임**
    2. prototype
        - 객체 **요청할 때마다 매번 새로운 객체 생성**
            - 스프링은 객체 생성, DI, 초기화 콜백까지만 처리
        - 객체 관리는 호출한 쪽에 위임
            - 스프링 컨테이너에 객체 등록 X
    3. request
        - HTTP 요청이 들어올 때마다 새로운 빈 생성 후, HTTP 응답 후 소멸
        - 동시에 여러 사용자의 요청을 받아도, 모두 독립적인 빈이 생성
            - 데이터 꼬일 위험 X
    4. session
        - HTTP 세션 하나당 하나의 빈 생성 후, 세션 만료 후 소멸
            - 사용자가 고유 세션 ID 받는 순간 생성
            - 사용자의 로그아웃, 타임아웃되는 순간 소멸
        - 로그인한 사용자의 정보 보관
    
- **빈 라이프사이클**
    - 스프링 컨테이너에서 **빈의 생성 및 소멸 과정**
        1. **스프링 컨테이너 생성**
            - 애플리케이션 실행 시, 컨테이너를 메모리에 생성
        2. **빈 인스턴스화**
            - 대상 클래스의 생성자를 호출해, 텅 빈 객체 생성
        3. **DI**
            - 생성된 객체에 다른 빈을 의존성 주입
        4. **초기화 콜백**
            - `@PostContruct` 실행
                - 실제 로직 수행 전, 콜백 작업 수행
        5. **빈 등록**
            - 초기화 완료 및 AOP 프록시 생성 마친 빈을 컨테이너에 등록
        6. **빈 사용**
            - 클라이언트 요청에 따라 비지니스 로직 수행
        7. **소멸 콜백**
            - `@PreDestroy` 실행
                - 스프링 컨테이너 내려가기 전, 콜백 작업 수행
        8. **애플리케이션 종료**
---
## Spring MVC
- **Servlet**
    - **Java에서 HTTP 요청을 처리하고, HTTP 응답 만들어 반환해주는 기술**
        - Java 웹 서버는 모두 **Servlet 기반** 동작
    - **웹 요청  처리 흐름**
        1. 애플리케이션 실행시, 톰캣(서블릿 컨테이너) 실행
        2. 클라이언트의 HTTP 요청 수신
        3. 객체 변환
            - 톰캣은 HTTP 메시지를 분석해 `HttpServletRequest`, `HttpServletResponse` 객체로 변환
                - `HttpServletRequest`
                    - 요청 정보 객체
                - `HttpServletResponse`
                    - 응답 준비 객체
        4. 스레드 할당
            - 여러 사용자의 요청을 동시에(병렬) 처리하기 위해 톰캣은 해당 요청을 처리할 스레드 배정
        5. 서블릿 매핑
            - 톰캣은 요청 URL과 매핑되는 서블릿에게 요청 전달해 로직 수행
                - `HttpServletRequest`, `HttpServletResponse` 객체를 매개변수로 전달
        6. 로직 수행 결과를 `HttpServletResponse` 객체에 저장 후, HTTP 응답 메시지로 변환
        7. 클라이언트에 전달
        
        ```jsx
        // 1. 로그인을 처리하기 위해 만든 전용 서블릿 클래스
        @WebServlet("/login") // /login 요청 오면 작동
        public class LoginServlet extends HttpServlet {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) {
                // 로그인 로직 처리
            }
        }
        ```
        
        ```jsx
        // 2. 로그아웃을 처리하기 위해 또 새로 만든 전용 서블릿 클래스
        @WebServlet("/logout") 
        public class OrderServlet extends HttpServlet {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) {
                // 로그아웃 로직 처리
            }
        }
        ```
        
        - **과거 순수 자바 서블릿**
            - **URL 하나당 하나의 서블릿 클래스**를 만들어야 했음..
                - 요청 받고 로직 처리하고 데이터 반환하는 **모든 역할 수행**
                    - 코드 길어지고 유지보수성 하락
                - **무수히 많은 클래스** 정의
                    - 연관된 로직 파편화
                    - 용량 과부화
                - 모든 요청에 사용되는 **공통 로직 한 번에 처리 불가**
                    - 코드 중복 및 유지보수성 하락
            - `/login` 처리용 서블릿, `/logout` 처리용 서블릿 등
            
        - **자바 서블릿 MVC**
            - **각 핵심 역할을 수행하는 여러 객체**들로 동작
            - **Controller**
                - 연관된 URL을 하나의 클래스에 정의해 로직 처리
            - 여전히 모든 요청에 사용되는 **공통 로직 한 번에 처리 불가**
            
        - **Spring MVC**
            - **DispatcherServlet**
                - 모든 공통 작업 처리 후, URL 매핑으로 **Controller**에게 전달
            
            
- **MVC 패턴** (Model + View + Controller)
    - **소프트웨어 설계 패턴**
        - **Model**
            - 비지니스 로직 처리로 가공된 데이터
        - **View**
            - 브라우저에 보여지는 화면
        - **Controller**
            - HTTP 요청 받고 로직 수행 결과인 **Model**을 **View**에 전달
    - 설계 흐름
        
        ```jsx
        브라우저 요청
              ↓
        Controller
              ↓
        Model 처리
              ↓
        View 반환
              ↓
        브라우저 응답
        ```
        

- **Spring MVC**
    - **MVC 패턴 구현**한 웹 프레임 워크
    - **Front Controller 패턴**을 사용
        - 모든 HTTP 요청을 **하나의 DispatcherServlet로 먼저 받음**
        - 기존 MVC 패턴과 달리 공통 작업 한 번에 처리 가능
    - 현대 실무 트렌드
        - **MVC 흐름은 전통적인 서버 렌더링**(HTML을 서버가 직접 만드는 방식) 방식
        - 요즘 실무에서는 **프론트엔드와 백엔드가 완전히 분리**
            - 백엔드는 View를 만들 필요 없이 **순수한 데이터(JSON)만 반환**
            - `@RestController`를 통해 데이터를 `ViewResolver`가 아닌, `HttpMessageConverter`에게 전달해 **JSON 문자열로 변환**해 **프론트엔드에 전달**
    - **Spring MVC 동작 흐름**
        1. **요청 수신**
            - 클라이언트의 HTTP 요청이 먼저 `DispatcherServlet`에 도착
        2. **목적지 검색**
            - `HandlerMapping`은 `DispatcherServlet`에게 URL과 매핑되는 `Controller` 정보를 반환
        3. **실행 어댑터 호출**
            - `HandlerAdapter`가 `Controller`가 받는 파라미터 타입에 맞춰 변환 후, `Controller` 실행
        4. **로직 실행**
            - `Controller`를 통해 비즈니스 로직 실행
        5. **결과 반환**
            - 로직 처리가 끝나면, 컨트롤러는 **순수한 자바 객체(DTO)** 자체를 `DispatcherServlet`에 반환
        6. **메시지 컨버터 개입** 
            - `HandlerAdapter`는 **`HttpMessageConverter`** 호출해 자바 객체를 **JSON 문자열로 변환**
                - 스프링은 `HttpMessageConverter`로 **Jackson 라이브러리** 사용
        7. **최종 응답** 
            - **JSON 문자열**이 HTTP 응답의 Body에 저장되고 프론트엔드에게 전송
        
        
        ```
        			    브라우저
        					   ↓
        			   HTTP 요청
        					   ↓
        		Tomcat (서블릿 컨테이너)
        				 	   ↓
        			 DispatcherServlet             //Spring MVC 패턴 시작
        			       ↓
        			  HandlerMapping
        			       ↓
        			  HandlerAdapter
        			       ↓
        			   Controller
        			       ↓
        			   객체 반환
        			       ↓
            HttpMessageConverter
        			       ↓
        		 	   JSON 생성       
        		 	       ↓
        		 	   HTTP 응답
        				     ↓
        			    브라우저
        ```
        

- `DispatcherServlet`
    - **모든 HTTP 요청**을 받아, 요청에 따라 **각기 다른 컨트롤러로 배분**(Dispatch)하는 **서블릿 객체**
        - 톰캣으로부터 `HttpServletRequest`와 `HttpServletResponse` 객체를 전달받고 로직 수행
    - `doDispatch()`
        - 해당 메서드를 통해 `DispatcherServlet`의 핵심 로직을 순서대로 실행 가능
        
        ```jsx
        protected void doDispatch(
        	HttpServletRequest request, HttpServletResponse response) throws Exception {
        		...
        }
        ```
        
        1. **핸들러 조회** (`getHandler`)
            - `HandlerMapping`를 호출해 요청 URL을 처리할 수 있는 **핸들러(컨트롤러 메서드)** 찾음
                - 메소드 앞뒤로 실행될 인터셉터 목록을 포함한 `HandlerExecutionChain`을 반환
        2. **핸들러 어댑터 조회** (`getHandlerAdapter`)
            - 찾아낸 핸들러를 실제로 실행할 `HandlerAdapter` 확인
        3. **인터셉터 전처리** (`applyPreHandle`)
            - 실제 로직을 실행하기 전, 등록된 **인터셉터들의 `preHandle()`** 메서드를 차례로 호출
                - 로그인 체크나 권한 검사 수행
                - `false`가 반환시 요청 즉시 중단
        4. **실제 핸들러 실행** (`ha.handle`)
            - `HandlerAdapter`를 통해 **진짜 컨트롤러 로직을 실행**
                - 파라미터 바인딩(ArgumentResolver), JSON 변환(HttpMessageConverter) 등 같이 수행
                - **JSON 데이터**를 `HttpServletResponse`에 작성
        5. **인터셉터 후처리** (`applyPostHandle`)
            - 로직 실행 후, **인터셉터의 `postHandle()`** 메서드를 호출
                - 로그 기록 등 로직 처리가 끝난 뒤 공통적으로 수행할 작업을 처리
        6. **결과 처리** (`processDispatchResult`)
            - JSON 데이터를 반환하므로 `ViewResolver` 호출 X
            - **예외 처리**
                - 실행 중 에러가 있었다면 `HandlerExceptionResolver`가 에러용 JSON 응답 구성
            - 인터셉터의 `afterCompletion()`을 호출
                - 사용한 리소스를 정리 등의 작업

### WAS와 톰캣

- 웹 서버 (Web Server)
    - 클라이언트의 HTTP 요청을 받아 **정적 리소스** 제공
        - 서버에 이미 완성된 상태로 저장되어 있는 파일들(HTML, CSS, JavaScript, 이미지 파일 등)
    - **동작 방식**
        - 요청받은 URL과 매칭되는 파일을 그대로 HTTP 응답으로 반환
        - 데이터베이스 조회나 별도의 로직 실행 X
    - Nginx, Apache HTTP Server 등
- **WAS (Web Application Server)**
    - 클라이언트의 HTTP 요청에 따라 데이터베이스를 조회나 서버 측 코드를 실행하여 **동적 리소스** 제공
    - 일반적으로 **`웹 서버 + 웹 컨테이너`** 로 구성
    - **동작 방식**
        - HTTP 요청이 들어오면 내장된 컨테이너가 비즈니스 로직 실행해 데이터 처리 후 HTTP 응답으로 반환
    - **Tomcat**, WebLogic, WebSphere 등
- **톰캣** (Tomcat)
    - **자바의 대표적인 WAS**이자 **서블릿 컨테이너**
    - 특징
        - **서블릿 생명주기 관리**
            - **Servlet 객체**(DispatcherServlet)의 생성, 실행, 소멸 관리
        - **HTTP 메시지 파싱**
            - HTTP 요청 텍스트를 `HttpServletRequest`와 `HttpServletResponse`객체로 변환
        - **스프링과의 관계**
            - 스프링 기반의 코드(`DispatcherServlet`, `@RestController` 등)는 독립적으로 실행 불가
                - 반드시 톰캣과 같은 **서블릿 컨테이너의 메모리 위**에서만 웹 서비스를 제공 가능
    
    
---

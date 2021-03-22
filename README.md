# kakaopay_coupon
coupon system
# Project
카카오페이 서버개발 사전과제 2번

## 1. 목적
1. 랜덤한 코드의 쿠폰을 N개 생성하여 데이터베이스에 보관하는 API를 구현하세요.
2. 생성된 쿠폰중 하나를 사용자에게 지급하는 API를 구현하세요.
3. 사용자에게 지급된 쿠폰을 조회하는 API를 구현하세요.
4. 지급된 쿠폰중 하나를 사용하는 API를 구현하세요. (쿠폰 재사용은 불가)
5. 지급된 쿠폰중 하나를 사용 취소하는 API를 구현하세요. (취소된 쿠폰 재사용 가능)
6. 발급된 쿠폰중 당일 만료된 전체 쿠폰 목록을 조회하는 API를 구현하세요.
7. API 인증을 위해 JWT(Json Web Token)를 이용해서 Token 기반 API 인증 기능을 개발하고 각 API 호출
   시에 HTTP Header에 발급받은 토큰을 가지고 호출하세요.
   signup 계정생성 API: ID, PW를 입력 받아 내부 DB에 계정을 저장하고 토큰을 생성하여 출력한
   다.
   단, 패스워드는 안전한 방법으로 저장한다.
   signin 로그인 API: 입력으로 생성된 계정 (ID, PW)으로 로그인 요청하면 토큰을 발급한다.
   
8. 10만개 이상 벌크 csv Import 기능

## 2. 개발 환경

- Intellij IDEA Ultimate 2020.3
- OS : Windows 10

## 3. 개발 프레임워크 구성

### 3.1 DB
- h2 (메모리 db)

###  3.2. Gradle 추가 dependencies 

implementation 'org.springframework.boot:spring-boot-starter-security' // password 해쉬, match

implementation group: 'org.apache.commons', name: 'commons-lang3', version: '3.0'

implementation('org.springframework.boot:spring-boot-starter-validation') //@RequestBody @Valid

implementation 'net.rakugakibox.util:yaml-resource-bundle:1.1'//message property .yml

compile group: 'io.jsonwebtoken', name: 'jjwt', version: '0.7.0'

compileOnly 'org.projectlombok:lombok'

- spring-boot-starter-security 
  - 회원가입시 토른 관리, password 해쉬 함수 사용 
- jjwt 
   - 토큰 생성(SHA-512)
   - 회원가입시 생성한 userId를 기반으로 토큰 생성

- spring-boot-starter-validation
   - @Valid 사용 : RequestBody의 validation 로직

## 빌드 및 실행 방법

#### Intellij에서 아래의 순서로 실행
```
1. Sync gradle
2. Run CouponApplication
```


### 문제해결 전략


### 서비스 실행 순서
1. 회원가입 
   - userId, password 입력
    > create data in User Table 

     <br/>
2. 로그인
   - 회원가입한 userId, password 입력 후 token을 받음
   - userId, password가 잘못된 경우 (status : 400 bad request)
   > User 확인후 token 발행
   >> 토큰생성 및 header check를 위해 (spring-boot-security, jwt 사용)

     <br/>
3. 쿠폰생성
   - 쿠폰생성 갯수, 만료일(year, month, day) 를 받는다.
   > create data in Coupon Table
   >> coupon Status 는 NOTYETUSED
   
     <br/>
4. 쿠폰 발급
   - 만료일이 오늘날짜를 포함한 이후의 쿠폰을 랜덤으로 한개 발급받는다.
   > select random data in Coupon Table
   >> coupon Status 는 USED
   > create data in UserCoupon Table

     <br/>
5. 쿠폰 List 조회
   - 사용자가 발급받은 쿠폰 List를 조회한다. (쿠폰코드, 만기일)
   > select data in UserCoupon & Coupon Table
   
    <br/>
6. 쿠폰 발급 취소
   - 발급받은 쿠폰을 취소한다.
    > delete data in UserCoupon Table
    > select data in Coupon Table
    >> update coupon Status  NOTYETUSED

    <br/>
7. 오늘 만료일자 쿠폰 List 조회
   - 발급받은 쿠폰중 만료일자가 금일인 쿠폰 List를 조회한다.
     > select data in UserCoupon & Coupon Table (expiredDate is Today)   

<br/><br/>

8. 10만개 이상 벌크 csv import  
    - 쿠폰 데이터(code, 만료일) (위치 : resource/test.csv) bulk create
   > create data in Coupon Table (1000개씩) *아래 설명 추가

<br/><br/>

### Controller

#### 1. UserController : 사용자 관리 Controller
- 회원가입 : /signin
- 로그인 : /auth

```java
@PostMapping(value ="/auth")
    public Object Authentication(@RequestBody @Valid UserRequest userRequest, Errors errors) {

        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }

        String jwt = userService.login(userRequest.getUserId(), userRequest.getPassword());

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }



    @PostMapping(value ="/signin")
    public Object signIn(@RequestBody @Valid UserRequest user, Errors errors) {
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        userService.registerUser(user.getUserId(), user.getPassword());
        return ResponseEntity.ok(new CreateResponse(ResponseStatus.SUCCESS));
    }
```

#### 2. CouponController
- 쿠폰 Generate : /createCoupon
- 랜덤으로 쿠폰 발급받기 : /issueCoupon
- 나의 쿠폰 List 조회 : /getCoupons
- 쿠폰 취소 : /cancelCoupon
- 금일 만료가 되는 나의 쿠폰 List조회 : /getTodayExpiredCoupon


```java
@PostMapping(value ="/createCoupon")
    public Object createCoupon(@RequestBody @Valid CreateCouponRequest createCouponRequest, Errors errors){

        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }

        long count = createCouponRequest.getCount();
        int year = createCouponRequest.getYear();
        int month = createCouponRequest.getMonth();
        int day = createCouponRequest.getDay();

        List<Coupon> couponList = couponService.createCoupons(count, year, month, day);

        return ResponseEntity.ok(new CreateResponse(ResponseStatus.SUCCESS));
    }

    @PostMapping(value ="/issueCoupon")
    public Object issueCoupon(Authentication authentication){

        Long userId = ((User)(authentication.getPrincipal())).getId();
        Coupon coupon = userCouponService.issueCoupone(userId);
        return ResponseEntity.ok(new GetCouponResponse(coupon));
    }

    @GetMapping(value ="/getCoupons")
    public Object getCoupons(Authentication authentication){

        Long userId = ((User)(authentication.getPrincipal())).getId();
        List<Coupon> userCouponList = userCouponService.searchCoupone(userId);
        return new ResponseEntity<List<GetCouponResponse>>(
                userCouponList.stream().map(s-> new GetCouponResponse(s)).collect(Collectors.toList())
                , HttpStatus.OK);

    }

    @GetMapping(value ="/gettodayExpiredCoupon")
    public Object gettodayExpiredCoupon(Authentication authentication){

        Long userId = ((User)(authentication.getPrincipal())).getId();
        List<Coupon> userCouponList = userCouponService.getExpiredCoupon(userId, LocalDate.now(), LocalDate.now());
        return new ResponseEntity<List<GetCouponResponse>>(
                userCouponList.stream().map(s-> new GetCouponResponse(s)).collect(Collectors.toList())
                , HttpStatus.OK);

    }

    @PostMapping(value ="/cancelCoupon")
    public Object cancelCoupon(Authentication authentication, @RequestBody @Valid Coupon cancelCouponRequest, Error error){

        Long userId = ((User)(authentication.getPrincipal())).getId();
        userCouponService.cancelCoupone(userId, cancelCouponRequest.getCode());
        return ResponseEntity.ok(new CancelResponse(ResponseStatus.SUCCESS));

    }
```


### 테이블 구성 (Custom 테이블)

#### 1. User
사용자 관리 테이블 (Spring-boot Security UserDetail 상속)

Column>
- Long id : PK
- String userId : 사용자 ID (ex : hongildong87)
- String password : 사용자 password(hash값 저장)


#### 2. Coupon
쿠폰 정보(쿠폰코드, 쿠폰 상태, 만료일) Table

Column>
- Long id : PK
- String code; : 쿠폰 코드 (형식 : xxxxx-xxxxxx-xxxxxxx)
- CouponStatus status : 쿠폰 상태
- LocalDate expiredDate : 쿠폰 만료 일자

CouponStatus(쿠폰 상태) :
- USED,   //발급된 쿠폰
- NOTYETUSED  //반급안된 쿠폰

쿠폰만료일자
- 쿠폰이 만료되는 날짜(ex : 2012.01.02이 만료일자이면, 만료일까지 발급받을수 있음)
- 만료일은 일까지 필요하기 때문에 LocalDate로 사용함.

### 3. UserCoupon
사용자에게 발급된 쿠폰을 관리하는 Table

Column>
- Long id : PK
- Long userId; : Foreign key(User PK)
- Long couponId : Foreign key(Coupon PK)

### Config

#### SecurityJavaConfig : token 을 발급받지 않은 경우 jwtRequest Filter를 거치도록 설정
 - 회원가입(/signin), 로그인(/auth) 예외처리
 - session 정책 사용 안함(SessionCreationPolicy.STATELESS)
 - 기본 유저의 경우(관리자가 아닌경우)를 제외하고 모두 filter를 타도록 설정
   (spring-boot securit 설정)
```java
@Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors().disable()		//cors방지
                .httpBasic().disable()
                .csrf().disable()		//csrf방지
                .formLogin().disable()	//기본 로그인 페이지 없애기
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                        .antMatchers("/**/signin", "/**/auth").permitAll()
                        .anyRequest().hasRole("USER")
                .and()
                    .addFilterBefore( jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
```

#### SecurityJavaConfig : toekn 체크 filter
-  사용자 ID와 jwt로 validation 체크
- token Header 정보
    - key : Authorization
    - value : Bearer {{token}}
```java
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String userId= null;
        String jwt = null;

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            jwt = authorizationHeader.substring(7);
            userId = jwtUtil.extractUserId(jwt);
        }

        if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null){
            User user = this.userService.getUser(userId);

            if(jwtUtil.validateToken(jwt, user)){
                UsernamePasswordAuthenticationToken userIdPasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                userIdPasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(userIdPasswordAuthenticationToken);
            }
        }
        chain.doFilter(request,response);
    }
```
#### JwtUtil : 토큰 관련 모듈
- 토큰 생성
- validation 체크 : token 24시간 유효
- 토큰은 userId로 생성
- token으로 userId 추출 

#### application.properties  
> token 키 ((토큰은 userid 와 secret key로 생성))
>> jwt.secret : slqkCjalfWjwt 

> UploadFileSize
>> spring.servlet.multipart.maxFileSize=10MB
>> spring.servlet.multipart.maxRequestSize=10MB
```java
@Value("${jwt.secret}")
    private String SECRET_KEY;

    public String extractUserId(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);

    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getUserId());
    }

    private String createToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }

    public Boolean validateToken(String token, User user) {
        final String userId = extractUserId(token);
        return (userId.equals(user.getUserId()) && !isTokenExpired(token));
    }
```
#### bulk csv file import
#### 1. application.properties 설정 : 대용량이기 때문에 size를 10MB로 늘림
> Size 설정
>> spring.servlet.multipart.maxFileSize=10MB
>> spring.servlet.multipart.maxRequestSize=10MB

#### 2. list가 10만개 이상시 out of memory exception 발생
##### thread관리 : ExecutorService
##### Task : CouponSaveTask(string parsing 및 저장)
##### memory 방지 : list를 1000개씩 나눠서 Task에서 저장, 이후 clear  

thread 관리는 ExecutorService의 Executors.newCachedThreadPool 사용
1000 건씩 최대 5개의 thread를 생성하고, readline으로 읽은 string은 task(CouponSaveTask)에서 parsing 및 저장
list는 clear해서 out of memory 방지

```java

    private static int maxThreadCnt = 5; //최대 쓰레드 수

    private static int inPutRowCnt = 1000; //쓰레드별 입력행수
    
    //쓰레드 그룹을 컨트롤 할 서비스 생성
    private static ExecutorService ex = Executors.newCachedThreadPool(new ThreadFactory(){
        @Override
        public Thread newThread(Runnable r){
            return new Thread(r);
        }
    });

    @Override
    public void createCoupons(MultipartFile file) {
        BufferedReader br = null;
        String line;
        int rowcount = 0; //max : 21억
        try {
            br = new BufferedReader(new InputStreamReader((FileInputStream)(file.getInputStream()), "euc-kr"));
            ArrayList<String> list = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                rowcount++;
                list.add(line);
                if (rowcount % inPutRowCnt == 0) {
                    Runnable r = new CouponSaveTask(couponRepository, (ArrayList<String>)(list.clone()));
                    saveThreadManage(r);
                    list.clear();
                }

            }

            //미처리 된 데이터에 대해 처리
            if(list.isEmpty()){
                System.out.println("파일 처리 완료 ");
            }else{
                Runnable r = new CouponSaveTask(couponRepository, (ArrayList<String>) list.clone());
                r.run();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //쓰레드 관리를 위한 쓰레드 관리함수
    private static void saveThreadManage(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);

        //활성중인 쓰레드의 개수가 최대 쓰레드 개수보다 작을 경우 EXECUTOR사용
        if(t.activeCount() < maxThreadCnt  && threadState){
            System.out.println("Active Thread count is : "+t.activeCount());
            ex.execute(t);
        }else{
            //최대 쓰레드 개수에 도달 시 executor에게 작업완료 후 종료명령
            if(ex.isTerminated()){
                //활성 쓰레드가 모두 종료되었을 경우 새 Executor 할당 후 실행
                threadState = true;
                ex = Executors.newCachedThreadPool(new ThreadFactory(){
                    @Override
                    public Thread newThread(Runnable r){
                        return new Thread(r);
                    }
                });
                ex.execute(t);
            }else{
                if(threadState){  //쓰레드 종료 명령이 호출되지 않은 경우만 수행
                    threadState = false;
                    System.out.println("Thread group Shutdown");
                    ex.shutdown();
                }
                t.run(); //쓰레드 종료 명령이 완료될 때까진 메인 클래스가 직접수행
            }
        }
    }
    

```
```java
@AllArgsConstructor
public class CouponSaveTask implements Runnable{

    CouponRepository couponRepository = null;

    ArrayList<String> couponCSVInfoList = null;

    @Override
    public void run() {
        List<Coupon> couponList = couponCSVInfoList.stream().map(s->{
            String[] strArr = s.split(",");
            return new Coupon(strArr[0], CouponStatus.NOTYETUSED, LocalDate.parse(strArr[1]) );
        }).collect(Collectors.toList());

        couponRepository.saveAll(couponList);
        System.out.println("쿠폰 저장 : " + couponList.size() + ", " + Thread.currentThread().getName());
    }
}
```

### Exception

#### APIExceptionHandler
- @RestController : Controller에서 try catch를 사용하지 않고, @RestController method에 injection시킴
- @Order(Ordered.HIGHEST_PRECEDENCE) : 다른 Advice보다 순위를 높임
- @ExceptionHandler(ApiRuntimeException.class) : ApiRuntimeException 발생시 message 처리

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class APIExceptionHandler {
    @ExceptionHandler(ApiRuntimeException.class)
    @ResponseBody
    protected ResponseEntity<APIErrorResponse> handleApiException(ApiRuntimeException e) {
        return ResponseEntity.badRequest().body(
                new APIErrorResponse(
                        e.getError(),
                        e.getMessage(),
                        null
                )
        );
    }
}
```
#### ApiError : APIError Message 및 code
```java
public enum ApiError {
    ERROR("E001", "오류"),
    ID_DUPLICATED("E002", "Id is duplicated."),
    ID_WRONG("E003", "There is no Id."),
    PASSWORD_WRONG("E004", "Password is Wrong."),
    No_COUPON("E005", "There is no coupon."),
    Wrong_COUPON_CODE("E006", "There is wrong coupon code."),
    Not_USER_COUPON_CODE("E007", "This is not user coupon code."),
    CAN_NOT_CACEL_BY_EXPIRED("E008", "This is expired coupon code, Can't cancel.");
}
```

#### APIException 
- Exception을 상속 받은 Coupon Project Exception

#### APIRuntimeException
- RuntimeException 상속 받은 Coupon Project Exception




##  API

### 1. API 공통

#### 1-1. 회원가입, 로그인을 제외한 모든 API의 request시 header에 token정보를 입력
- key : Authorization
- value : Bearer {{token}}

#### 1-2. API의 request시 header에 context-type 정보를 입력
- key : Content-Type
- value : application/json (csv file upload 시 : multipart/form-data)

#### 1-3. Controller에 @RequestBody annotaiton 사용
- API 실행시 RequestBody에 json 형태로 값을 입력.

### 2. 회원가입 API

 - URL : /signin
 - Post 방식
 - input : RequestBody
```json
{
    "userId" : "kakao5",
    "password" : "password1"
}
```
- Success response
    - status code: 200
```json
{
  "result": "SUCCESS"
}
```
- 중복 ID를 입력한경우
    - status code: 400
- Error response
```json
{
  "description": "Id is duplicated.",
  "code": "E002"
}
```


### 2. 로그인 API

- URL : /auth
- Post 방식
- input : RequestBody
```json
{
    "userId" : "kakao5",
    "password" : "password1"
}
```
- Success response
    - status code: 200
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYWthbzUiLCJleHAiOjE2MTY0MjIzOTYsImlhdCI6MTYxNjMzNTk5Nn0.1gQDMaPavRMiid6G3dgwSCLpdB0XFwkvJ2FAiTYstO5PCgSXK5eLz2MN3E5bDqlsCTD5e4y3vWPvW1RHo_e1dg"
}
```
- ID가 없는 경우
    - status code: 400
- Error response
```json
{
  "description": "There is no Id.",
  "code": "E003"
}
```

- Password를 잘못 입력한경우
    - status code: 400
- Error response
```json
{
  "description": "Password is Wrong.",
  "code": "E004"
}
```

### 3. 쿠폰생성

- URL : /createCoupon
- Post 방식
- input : RequestBody
```json
//만료일자 년/월/일
{
  "count" : "3",
  "year" : "2021",
  "month" : "03",
  "day" : "22"
}

```
- Success response
    - status code: 200
```json
{
  "result": "SUCCESS"
}
```
- 값을 안넣거나, 잘못넣은 경우
    - status code: 400


### 4. 쿠폰 발급받기

- URL : /issueCoupon
- Post 방식

- Success response
    - status code: 200
```json
{
  "code": "lLsHb-ksYSHf-hFPXfF0P",
  "expiredDate": "2021-03-22"
}
```
- 만료일이 오늘을 포함한 이후 쿠폰이 없는 경우
    - status code: 400
- Error response
```json
{
  "description": "There is no coupon.",
  "code": "E005"
}
```

### 5. 쿠폰 리스트 조회

- URL : /getCoupons
- Get 방식

- Success response
    - status code: 200
```json
[
  {
    "code": "mtccI-udTxmW-B5XwNI0l",
    "expiredDate": "2021-03-22"
  },
  {
    "code": "cKbkR-yLdV07-tBE8FfcY",
    "expiredDate": "2021-03-23"
  },
  {
    "code": "g6JXV-4vJyuE-7IsjAgdl",
    "expiredDate": "2021-03-23"
  }
]
```


### 6. 쿠폰 발급 취소

- URL : /cancelCoupon
- Post 방식
- input : RequestBody
```json
{
  "code" : "p7V6e-qjnkRJ-E8jdVOT2"
}
```
- Success response
    - status code: 200
```json
{
  "result": "SUCCESS"
}
```
- 없는 쿠폰 code를 취소 요청한 경우
    - status code: 400
- Error response
```json
{
  "description": "There is wrong coupon code.",
  "code": "E006"
}
```

- 사용자의 쿠폰 code가 아닌 경우
    - status code: 400
- Error response
```json
{
  "description": "This is not user coupon code.",
  "code": "E007"
}
```

- 쿠폰 만료일이 지난 경우
    - status code: 400
- Error response
```json
{
  "description": "This is expired coupon code, Can't cancel.",
  "code": "E008"
}
```
### 7. 오늘 만료 일자 쿠폰 조회

- URL : /getTodayExpiredCoupon
- Get 방식
- Success response
    - status code: 200
```json
[
  {
    "code": "mtccI-udTxmW-B5XwNI0l",
    "expiredDate": "2021-03-22"
  },
  {
    "code": "cKbkR-yLdV07-tBE8FfcY",
    "expiredDate": "2021-03-22"
  },
  {
    "code": "g6JXV-4vJyuE-7IsjAgdl",
    "expiredDate": "2021-03-22"
  }
]
```


### 8. csv 10만건 bulk import API

- URL : /uploadCoupon
- Post 방식
- input : RequestBody

form data
```json
{
    "file" : {{csv 파일}}
    
}
```
- Success response
    - status code: 200

## Test 코드  결과

#### Test 코드 
- CouponApplicationTests

1. 회원 가입

```text
MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /signin
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"43"]
             Body = {"userId":"kakaoId","password":"password1"}
    Session Attrs = {}

Handler:
             Type = com.kakaopay.coupon.biz.user.controller.UserController
           Method = com.kakaopay.coupon.biz.user.controller.UserController#signIn(UserRequest, Errors)


MockHttpServletResponse:
           Status = 200
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"1; mode=block", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"result":"SUCCESS"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
```

2. 로그인
```text

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /auth
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Content-Length:"43"]
             Body = {"userId":"kakaoId","password":"password1"}
    Session Attrs = {}

Handler:
             Type = com.kakaopay.coupon.biz.user.controller.UserController
           Method = com.kakaopay.coupon.biz.user.controller.UserController#Authentication(UserRequest, Errors)

MockHttpServletResponse:
           Status = 200
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"1; mode=block", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"token":"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYWthb0lkIiwiZXhwIjoxNjE2NDYxMTA2LCJpYXQiOjE2MTYzNzQ3MDZ9.P5fUjH37l1HUmM-1oTUxAkGWbtcg9oneC2xm7A0vqIb5A_FnoQVvwHV16NQAcQnPc1ZyfUKyzl7PZpG1KQrvPw"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
```

3. 쿠폰 생성 
```text

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /createCoupon
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Authorization:"Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYWthb0lkIiwiZXhwIjoxNjE2NDc0NjA5LCJpYXQiOjE2MTYzODgyMDl9.tPUpP6t4algSt0GSylDnp3rYzX5iwFioDhJkaAtv1JuphVG5d1TiXtkm_F-9rx5W2hhbfnGcTCA3Rb6cu2BlKg", Content-Length:"42"]
             Body = {"count":1,"year":2021,"month":6,"day":23}
    Session Attrs = {}

Handler:
             Type = com.kakaopay.coupon.biz.coupon.controller.CouponController
           Method = com.kakaopay.coupon.biz.coupon.controller.CouponController#createCoupon(CreateCouponRequest, Errors)

MockHttpServletResponse:
           Status = 200
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"1; mode=block", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"result":"SUCCESS"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
```

4. 쿠폰 발행
```text

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /issueCoupon
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Authorization:"Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYWthb0lkIiwiZXhwIjoxNjE2NDc0NjA5LCJpYXQiOjE2MTYzODgyMDl9.tPUpP6t4algSt0GSylDnp3rYzX5iwFioDhJkaAtv1JuphVG5d1TiXtkm_F-9rx5W2hhbfnGcTCA3Rb6cu2BlKg"]
             Body = null
    Session Attrs = {}

Handler:
             Type = com.kakaopay.coupon.biz.coupon.controller.CouponController
           Method = com.kakaopay.coupon.biz.coupon.controller.CouponController#issueCoupon(Authentication)

MockHttpServletResponse:
           Status = 200
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"1; mode=block", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"code":"mSOJq-0A9P6q-7Xt6OWEn","expiredDate":"2021-06-23"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
```

5. 나의 쿠폰 List 조회

```text

MockHttpServletRequest:
      HTTP Method = GET
      Request URI = /getCoupons
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Authorization:"Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYWthb0lkIiwiZXhwIjoxNjE2NDc0NjA5LCJpYXQiOjE2MTYzODgyMDl9.tPUpP6t4algSt0GSylDnp3rYzX5iwFioDhJkaAtv1JuphVG5d1TiXtkm_F-9rx5W2hhbfnGcTCA3Rb6cu2BlKg"]
             Body = null
    Session Attrs = {}

Handler:
             Type = com.kakaopay.coupon.biz.coupon.controller.CouponController
           Method = com.kakaopay.coupon.biz.coupon.controller.CouponController#getCoupons(Authentication)

MockHttpServletResponse:
           Status = 200
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"1; mode=block", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = [{"code":"mSOJq-0A9P6q-7Xt6OWEn","expiredDate":"2021-06-23"},{"code":"3PDzY-uJng1C-iEz0nvpE","expiredDate":"2021-03-22"},{"code":"5IzMj-ewhdTN-bPpZBqxm","expiredDate":"2021-03-22"}]
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
```   
6. 쿠폰 발급 취소
```text

MockHttpServletRequest:
      HTTP Method = POST
      Request URI = /cancelCoupon
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Authorization:"Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYWthb0lkIiwiZXhwIjoxNjE2NDc0NjA5LCJpYXQiOjE2MTYzODgyMDl9.tPUpP6t4algSt0GSylDnp3rYzX5iwFioDhJkaAtv1JuphVG5d1TiXtkm_F-9rx5W2hhbfnGcTCA3Rb6cu2BlKg", Content-Length:"75"]
             Body = {"id":null,"code":"mSOJq-0A9P6q-7Xt6OWEn","status":null,"expiredDate":null}
    Session Attrs = {}

Handler:
             Type = com.kakaopay.coupon.biz.coupon.controller.CouponController
           Method = com.kakaopay.coupon.biz.coupon.controller.CouponController#cancelCoupon(Authentication, Coupon, Error)

MockHttpServletResponse:
           Status = 200
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"1; mode=block", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = {"result":"SUCCESS"}
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
```   

7. 오늘 만료되는 쿠폰List 조회
```text

MockHttpServletRequest:
      HTTP Method = GET
      Request URI = /getTodayExpiredCoupon
       Parameters = {}
          Headers = [Content-Type:"application/json;charset=UTF-8", Authorization:"Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYWthb0lkIiwiZXhwIjoxNjE2NDc0NjA5LCJpYXQiOjE2MTYzODgyMDl9.tPUpP6t4algSt0GSylDnp3rYzX5iwFioDhJkaAtv1JuphVG5d1TiXtkm_F-9rx5W2hhbfnGcTCA3Rb6cu2BlKg"]
             Body = null
    Session Attrs = {}

Handler:
             Type = com.kakaopay.coupon.biz.coupon.controller.CouponController
           Method = com.kakaopay.coupon.biz.coupon.controller.CouponController#gettodayExpiredCoupon(Authentication)

MockHttpServletResponse:
           Status = 200
    Error message = null
          Headers = [Content-Type:"application/json", X-Content-Type-Options:"nosniff", X-XSS-Protection:"1; mode=block", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Frame-Options:"DENY"]
     Content type = application/json
             Body = [{"code":"3PDzY-uJng1C-iEz0nvpE","expiredDate":"2021-03-22"},{"code":"5IzMj-ewhdTN-bPpZBqxm","expiredDate":"2021-03-22"}]
    Forwarded URL = null
   Redirected URL = null
          Cookies = []
```
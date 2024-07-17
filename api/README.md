# The Survey API

The Survey의 Backend Repository 입니다. 
Spring Boot를 사용하여 구현되었으며, 다양한 REST API를 제공합니다.

### 인증 관련 API

- `POST /auth/register`: 회원가입
- `POST /auth/login`: 로그인

### 사용자 관련 API

- `GET /users/profile`: 사용자 정보 조회
- `GET /users/surveys`: 사용자 설문조사 목록 조회
- `GET /users/surveys/{surveyId}`: 특정 설문조사 결과 조회
- `PATCH /users/profile`: 사용자 정보 수정
- `GET /users/profile/certifications`: 사용자 인증 정보 조회
- `PATCH /users/profile/certifications`: 요청으로 사용자 인증 정보 수정
- `DELETE /users`: 사용자 삭제

### 설문조사 관련 API

- `GET /surveys`: 모든 설문조사 페이지별 조회
- `GET /surveys/{surveyId}`: 개별 설문조사 조회
- `POST /surveys`: 설문조사 생성
- `PATCH /surveys`: 설문조사 수정
- `DELETE /surveys/{surveyId}`: 특정 설문조사 삭제
- `POST /surveys/submit`: 설문조사 응답 제출

## 기술 스택

### 프레임워크 및 언어
- **Java**: 11
- **Spring Boot**: 2.7.9

### 빌드 및 의존성 관리
- **Gradle**
- **Spring Dependency Management**: 1.0.15.RELEASE

### 보안

*JWT를 사용하지 않았으며, Session 기반의 인증 방식을 사용합니다.
- **Spring Security**

### 데이터베이스
- **PostgreSQL**

### 캐싱 및 세션 관리
- **Redisson**
- **Spring Session Data Redis**

### Application 테스트
- **Spring Boot Starter Test**
- **Spring Security Test**
- **H2 Database**

### 부하테스트
- **Gatling**: 3.9.3
- **Gatling Charts Highcharts**: 3.9.3
- **Gatling Test Framework**: 3.9.3

### 모니터링
- **Spring Actuator**
- **Prometheus**
- **Grafana**

### API 문서화
- **Springdoc OpenAPI UI**: 1.7.0

### 기타
- **Lombok**

## 프로젝트 구조

- **config**: Spring Security, Redisson, Swagger 등의 설정 파일들을 포함합니다.
- **controller**: REST API 엔드포인트들을 정의합니다.
- **domain**: JPA 엔티티 클래스들을 포함합니다.
- **dto**: 데이터 전송 객체들을 포함합니다.
    - **request**: 클라이언트로부터의 요청 데이터를 담는 DTO를 포함합니다.
    - **response**: 서버에서 클라이언트로의 응답 데이터를 담는 DTO를 포함합니다.

- **exception**: 예외 처리 클래스들을 포함합니다.

- **repository**: JPA Repository 인터페이스들을 포함합니다.

- **service**: 서비스 레이어 클래스들을 포함합니다.

- **util**: 유틸리티 클래스들을 포함합니다.

## Swagger 문서

API 문서는 Swagger를 통해 확인할 수 있습니다. 아래 주소에서 접근 가능합니다:

```
http://localhost:8080/v1/docs
```

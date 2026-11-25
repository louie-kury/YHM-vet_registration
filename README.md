# YHM-vet_registration

`yhm-vet-registration`은 Eureka에 등록되는 독립 마이크로서비스입니다. Java 17, Gradle Wrapper, Spring Boot 4.1.3, Spring Cloud 2025.1.5, Spring Data JPA와 MySQL 8.4.13을 사용합니다.

## Hexagonal 구조

| 패키지 | 책임 |
| --- | --- |
| `domain` | 도메인 모델과 비즈니스 규칙 |
| `application` | 유스케이스 구현과 트랜잭션 |
| `application.port.in` | 웹 어댑터가 호출하는 유스케이스 계약 |
| `application.port.out` | 유스케이스가 필요로 하는 외부 연동 계약 |
| `adapter.in.web` | HTTP 요청과 응답을 유스케이스에 연결 |
| `adapter.out.persistence` | JPA 엔티티와 저장소를 통해 영속성 포트를 구현 |

웹 어댑터는 입력 포트를 호출하고, 유스케이스는 출력 포트를 통해 저장소에 접근합니다. JPA 의존성은 영속성 어댑터에 둡니다.

## 로컬 실행

저장소 루트의 `.env.local`에 로컬 DB 접속 정보를 저장합니다. `bootRun`은 기본적으로 `local` 프로필을 사용해 이 파일을 읽습니다. 값은 따옴표 없이 `키=값` 형식으로 입력합니다. 비밀번호가 없는 로컬 계정은 `MYSQL_PASSWORD=`로 둡니다.

```dotenv
MYSQL_PORT=3306
MYSQL_DATABASE=vet_registration
MYSQL_USER=vet_registration
MYSQL_PASSWORD=사용할 DB 비밀번호
```

```sh
./gradlew bootRun
```

Compose의 MySQL을 사용하려면 `.env.local`에 서비스 전용 DB 사용자와 비밀번호를 설정한 뒤 `docker compose --env-file .env.local up -d --wait mysql`을 실행합니다. 기존 MySQL 볼륨을 재사용할 때는 초기화된 계정 정보를 사용합니다. `3306`을 다른 MySQL이 사용 중이면 `MYSQL_PORT=13306`을 지정합니다.

기본 HTTP 포트는 `8081`이고, Eureka 주소는 `http://localhost:8761/eureka/`입니다. 필요에 따라 `SERVER_PORT`, `EUREKA_SERVER_URL`, `MYSQL_PORT`를 설정할 수 있습니다. Eureka 없이 로컬에서 실행할 때는 `EUREKA_CLIENT_ENABLED=false ./gradlew bootRun`을 사용합니다. JPA의 자동 스키마 변경은 비활성화되어 있습니다.

Gateway를 호스트에서 실행할 때는 `gateway` 디렉터리에서 `SPRING_PROFILES_ACTIVE=local bash ./gradlew bootRun`을 실행합니다. `local` 프로필은 기본값으로 `http://localhost:8761/eureka/`를 사용하며, `EUREKA_SERVER_URL`로 주소를 바꿀 수 있습니다. Gateway는 `/yhm-vet-registration/**` 요청에서 서비스 경로 접두사를 제거하고 Eureka가 알려 준 인스턴스로 전달합니다.

- Eureka 등록 상태는 `http://localhost:8761/eureka/apps/YHM-VET-REGISTRATION`에서 확인합니다.
- Gateway를 통해 OpenAPI 문서는 `http://localhost:8000/yhm-vet-registration/v3/api-docs`에서 확인합니다. 통합 Swagger UI는 `http://localhost:8000/swagger-ui/index.html`입니다.

실행 JAR은 `./gradlew build`로 빌드합니다. 배포 실행에는 환경 변수를 주입하거나 실행 디렉터리의 `.env`를 사용합니다. `.env`와 `.env.local`은 Git에서 제외됩니다.

## 수의사 등록 API

`POST /api/v1/veterinarian-registrations`는 병원 목록에서 선택한 병원 UUID로 수의사와 최초 소속을 함께 저장합니다. 서버는 UUID와 UTC 시각을 생성합니다. 초기 상태는 면허가 검증되지 않은 `RESTRICTED`이며 상태 사유는 `LICENSE_UNVERIFIED`입니다. 수의사의 진료 가능 여부는 `false`이고 최초 소속은 `current_flag=1`, `ended_at=NULL`로 저장됩니다.

### 면허번호 고유 인덱스

등록 API를 활성화하기 전에 대상 MySQL DB에서 기존 면허번호 중복을 확인합니다. 아래 조회는 기존 컬럼의 비교 규칙을 사용해 앞뒤 공백을 제거한 값도 검사합니다. 결과가 0이면 [고유 인덱스 SQL](db/migrations/001-veterinarian-license-unique.sql)을 적용합니다. 중복이 있으면 적용을 중단하고 데이터를 관리하는 담당자에게 확인합니다.

```sql
SELECT COUNT(*) AS duplicate_license_groups
FROM (
    SELECT REGEXP_REPLACE(license_no, '^[[:space:]]+|[[:space:]]+$', '') AS normalized_license
    FROM veterinarian
    GROUP BY normalized_license
    HAVING COUNT(*) > 1
) AS duplicates;
```

### 로컬에서 실행

`local` 프로필 단독 사용과 명시적인 활성화 설정이 모두 필요합니다.

```sh
SPRING_PROFILES_ACTIVE=local VET_REGISTRATION_LOCAL_ACCOUNT_ENABLED=true ./gradlew bootRun
```

대체 어댑터는 등록 레코드에 사용할 임시 계정 UUID를 서버에서 발급합니다. 등록 요청은 `accountUuid`를 받지 않습니다. 실제 auth 연동 전에는 운영 환경에 이 엔드포인트가 등록되지 않습니다. 실제 연동에서는 서버가 검증한 계정 UUID를 `RegistrationAccountPort`로 제공하고, 등록 허용 조건과 재시도 시 계정 정합성을 합의해야 합니다. 면허 검증과 진료 권한 부여는 별도의 절차입니다.

아래 예시의 `hospitalUuid`는 `GET /api/v1/hospitals`에서 조회한 병원 UUID로 교체합니다.

```json
{
  "name": "김수의",
  "major": "수의내과학",
  "specialty": "반려동물 내과",
  "licenseNo": "VET-TEST-001",
  "hospitalUuid": "12345678-1234-4234-a234-123456789010"
}
```

`201 Created` 응답 예시입니다.

```json
{
  "veterinarianUuid": "12345678-1234-4234-a234-123456789020",
  "hospitalUuid": "12345678-1234-4234-a234-123456789010"
}
```

입력 오류는 `400`, 병원 없음은 `404`, 면허번호 중복은 `409`를 반환합니다. 등록 API를 활성화하면 OpenAPI 문서는 `/v3/api-docs`에서 확인할 수 있습니다.

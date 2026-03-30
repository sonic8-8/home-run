# Homerun External Services

이 문서는 프로젝트가 실제로 사용하는 외부 서비스와, 가입 또는 키 발급 시 확인해야 할 정보를 정리한 문서다.

중요 원칙:

- 실제 secret 값은 제출본에 넣지 않는다.
- 어떤 서비스에서 어떤 값을 발급받아 어디에 넣는지만 기록한다.
- 현재 프로젝트는 소셜 로그인과 Photon Cloud를 사용하지 않는다.

## 1. 사용 중인 외부 서비스

| 서비스 | 용도 | 발급/관리 위치 | 필요한 값 | 주입 위치 |
| --- | --- | --- | --- | --- |
| SSAFY 금융 API | 회원 생성, 계좌/수시입출금, 신용등급 등 금융 연동 | SSAFY 금융망 포털 | `SSAFY_API_KEY`, `SSAFY_ACCOUNT_TYPE_UNIQUE_NO` | Jenkins credential, backend env |
| 금융감독원 FSS Open API | 대출 상품/추천 데이터 | `finlife.fss.or.kr` | `FSS_API_KEY` | `application.yml` 기반 backend 설정 |
| KIS Open API | 주식 시세 조회, 토큰 발급 | 한국투자증권 API Portal | `KIS_APP_KEY`, `KIS_APP_SECRET` | Jenkins credential, backend env |
| 공공데이터포털 | 법정동 코드, 아파트 실거래가 원천 데이터 | `data.go.kr` | `PUBLIC_DATA_SERVICE_KEY` | Jenkins credential, backend env |
| Naver Geocoding / Maps | 부동산 지도, 좌표 변환, 지도 JS SDK | NAVER Cloud Platform | `NAVER_GEOCODING_CLIENT_ID`, `NAVER_GEOCODING_CLIENT_SECRET` | Jenkins credential, backend env, frontend build arg |
| Oracle Cloud Object Storage | 카드/이미지 파일 저장 | Oracle Cloud Console | `OCI_REGION`, `OCI_NAMESPACE`, `OCI_BUCKET_NAME`, 로컬 `oci_config` | backend env 또는 로컬 config 파일 |
| GitLab | 소스 저장소, webhook 트리거 | SSAFY GitLab | Repository URL, PAT, webhook URL | Jenkins 연결, 서버 clone |
| Mattermost | 배포 성공/실패 알림 | 팀 Mattermost 워크스페이스 | webhook URL | Jenkins credential |

## 2. 서비스별 상세

### 2.1 SSAFY 금융 API

용도:

- 사용자 금융 연동
- 시드머니 계좌 생성/조회
- 입금/송금
- 신용등급 조회

필요 값:

```text
SSAFY_API_KEY
SSAFY_ACCOUNT_TYPE_UNIQUE_NO
SSAFY_BANK_CODE
SSAFY_BANK_NAME
```

정의 위치:

- `backend/src/main/resources/application.yml`
- `infrastructure/Jenkinsfile`
- `infrastructure/docker/docker-compose.runtime.yml`

운영 주입 방식:

- `ssafy-api-key` Jenkins credential
- `SSAFY_ACCOUNT_TYPE_UNIQUE_NO` Jenkins credential

참고 문서:

- `SSAFY_금융망_API/*.md`

### 2.2 금융감독원 FSS Open API

용도:

- 대출 추천 및 대출 상품 데이터 조회

기본 URL:

```text
https://finlife.fss.or.kr/finlifeapi
```

필요 값:

```text
FSS_API_KEY
```

정의 위치:

- `backend/src/main/resources/application.yml`
- `backend/src/main/java/.../config/FssApiProperties.java`
- `backend/src/main/java/.../client/fss/*`

비고:

- 현재 `application.yml` 에 기본값이 남아 있으나, 제출/운영 문서에서는 secret 값을 직접 기재하지 않는다.
- 운영 전환 시 별도 secret 관리로 옮기는 것이 안전하다.

### 2.3 KIS Open API

용도:

- 주식 현재가 조회
- KIS OAuth 토큰 발급

기본 URL:

```text
https://openapi.koreainvestment.com:9443
```

필요 값:

```text
KIS_APP_KEY
KIS_APP_SECRET
```

정의 위치:

- `backend/src/main/resources/application.yml`
- `infrastructure/Jenkinsfile`
- `infrastructure/docker/docker-compose.runtime.yml`

운영 주입 방식:

- `KIS_APP_KEY` Jenkins credential
- `KIS_APP_SECRET` Jenkins credential

### 2.4 공공데이터포털

용도:

- 법정동 코드 조회
- 아파트 실거래가 수집

기본 URL:

```text
https://apis.data.go.kr/1741000/StanReginCd
https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade
```

필요 값:

```text
PUBLIC_DATA_SERVICE_KEY
```

정의 위치:

- `backend/src/main/resources/application.yml`
- `infrastructure/Jenkinsfile`
- `infrastructure/docker/docker-compose.runtime.yml`

운영 주입 방식:

- `public-data-service-key` Jenkins credential

### 2.5 Naver Geocoding / Maps

용도:

- 부동산 주소 geocoding
- 프론트 지도 렌더링용 Naver JS SDK 로딩

기본 URL:

```text
https://maps.apigw.ntruss.com/map-geocode/v2/geocode
https://oapi.map.naver.com/openapi/v3/maps.js
```

필요 값:

```text
NAVER_GEOCODING_CLIENT_ID
NAVER_GEOCODING_CLIENT_SECRET
```

정의 위치:

- `backend/src/main/resources/application.yml`
- `frontend/src/features/realEstate/utils/naverMapScript.ts`
- `infrastructure/Jenkinsfile`

운영 주입 방식:

- backend: Jenkins credential -> runtime env
- frontend: `docker build --build-arg VITE_NAVER_MAP_CLIENT_ID=...`

### 2.6 Oracle Cloud Object Storage

용도:

- 이미지 오브젝트 저장
- 카드 이미지 URL 생성

필요 값:

```text
OCI_ENABLED
OCI_REGION
OCI_NAMESPACE
OCI_BUCKET_NAME
OCI_CONFIG_PATH   (로컬)
```

정의 위치:

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-local.yml`
- `infrastructure/docker/docker-compose.runtime.yml`

운영 기준:

```text
OCI_ENABLED=true
OCI_REGION=ap-singapore-1
OCI_NAMESPACE=axyqj6o2a3yw
OCI_BUCKET_NAME=bucket-20260316-1523
```

로컬 기준:

```text
OCI_CONFIG_PATH=C:/key/oci_config
```

참고 문서:

- `infra/oracle_bucket.md`

### 2.7 GitLab

용도:

- 소스 저장소 clone
- Jenkins webhook 트리거
- CI/CD 대상 branch 관리

필요 정보:

```text
Repository URL
GitLab PAT
Webhook URL
```

운영 기준:

- branch: `develop`
- Jenkins Job: `homerun-develop`
- webhook: `https://j14c103.p.ssafy.io/jenkins/project/homerun-develop`

### 2.8 Mattermost

용도:

- Jenkins 배포 성공/실패 알림

필요 값:

```text
mattermost-webhook-url
```

정의 위치:

- `infrastructure/Jenkinsfile`

알림 내용:

- 마지막 커밋 메시지
- 실패 시 stage 이름
- Jenkins build URL

## 3. 미사용 외부 서비스

현재 저장소 기준으로 아래 항목은 사용하지 않는다.

- 소셜 로그인
- Photon Cloud
- Firebase
- AWS S3
- 별도 외부 CI SaaS

## 4. 서비스 점검 순서

운영 배포 후 외부 서비스 이상 여부는 보통 아래 순서로 확인한다.

1. Jenkins credential 이 누락되지 않았는지 확인
2. backend 컨테이너 env 에 키 이름이 주입됐는지 확인
3. backend 로그에서 구성 바인딩 오류가 없는지 확인
4. 지도, 주식, 대출 추천, 금융 연동 기능을 화면 또는 API 스모크로 점검


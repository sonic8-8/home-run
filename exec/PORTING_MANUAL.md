# Homerun Porting Manual

## 1. 문서 목적

이 문서는 `Homerun` 프로젝트를 새 서버에 다시 배포하거나, 동일 구조의 EC2 환경을 재구성할 때 사용하는 제출용 포팅 매뉴얼이다.

문서 목표는 아래 세 가지다.

- GitLab 소스 clone 이후 빌드와 배포가 가능해야 한다.
- 운영에 필요한 버전, 설정값, 환경변수, 외부 서비스 주입 위치를 한 문서에서 볼 수 있어야 한다.
- DB, 웹서버, Jenkins, 모니터링, 시연 준비까지 재현 가능한 수준의 정보를 남겨야 한다.

기준 도메인:

```text
https://j14c103.p.ssafy.io
```

## 2. 최종 서비스 구조

### 2.1 공개 라우팅

```text
/           -> frontend
/api/       -> backend
/jenkins/   -> jenkins
```

추가 내부 경로:

```text
/docs/      -> frontend 컨테이너에 포함된 백엔드 REST Docs
/grafana    -> frontend 컨테이너 nginx를 통해 grafana 프록시
```

### 2.2 실행 흐름

```text
Browser
  -> nginx(host, 80/443)
      -> 127.0.0.1:3000  homerun-frontend
      -> 127.0.0.1:8081  homerun-backend
      -> 127.0.0.1:8080  homerun-jenkins

homerun-frontend
  -> grafana:3000 (/grafana 프록시)
  -> /usr/share/nginx/html/docs (/docs 정적 문서)

homerun-backend
  -> postgres:5432
  -> redis:6379

prometheus
  -> backend:8080/actuator/prometheus
```

### 2.3 포트 정책

외부 공개 포트:

```text
22   SSH
80   HTTP
443  HTTPS
```

외부 비공개 포트:

```text
127.0.0.1:3000  frontend
127.0.0.1:8081  backend
127.0.0.1:8080  jenkins
127.0.0.1:3002  grafana
127.0.0.1:9090  prometheus
127.0.0.1:5432  postgres
127.0.0.1:6379  redis
```

## 3. 사용 제품 / 버전 / 설정값

### 3.1 운영 기준 버전

| 구분 | 제품 | 버전 | 주요 설정 |
| --- | --- | --- | --- |
| OS | Ubuntu | `24.04.4 LTS` | EC2 host, timezone `Asia/Seoul` |
| JVM | Eclipse Temurin OpenJDK | `17.0.18` | backend runtime, `JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Seoul` |
| Backend Framework | Spring Boot | `3.5.11` | `server.port=8080`, `SPRING_PROFILES_ACTIVE=prod` |
| Build Tool | Gradle Wrapper | `8.14.4` | repo 내 wrapper 사용 |
| Backend build image | gradle | `8.14.3-jdk17` | `backend/Dockerfile.buildkit-cache` |
| Frontend runtime/build | Node.js / npm | `22.21.1` / `10.9.4` | frontend local build 기준 |
| Frontend framework | React / Vite | `19.2.0` / `7.3.1` | SPA build |
| Host reverse proxy | nginx | `1.24.0 (Ubuntu)` | TLS 종료, `/`, `/api/`, `/jenkins/` 라우팅 |
| Frontend web server | nginx | `1.29.7` | 정적 파일 서빙, `/docs/`, `/grafana` 처리 |
| Container engine | Docker Engine | `29.3.0` | 운영 컨테이너 관리 |
| Compose | Docker Compose | `v5.1.0` | runtime / observability / jenkins compose 실행 |
| Database | PostgreSQL | `16.13` | DB명 `homerun`, 계정 `homerun_user` |
| Cache | Redis | `7.2.13` | `appendonly yes` |
| Monitoring | Prometheus | `2.54.1` | backend actuator scrape |
| Monitoring UI | Grafana | `11.2.0` | `/grafana` subpath 서빙 |
| IDE | IntelliJ IDEA Ultimate | `2023.3.8` | 로컬 개발에 확인된 IDE |

### 3.2 런타임 컨테이너명

```text
homerun-frontend
homerun-backend
homerun-postgres
homerun-redis
homerun-prometheus
homerun-grafana
homerun-jenkins
```

## 4. 서버 준비

### 4.1 필수 패키지

- Ubuntu EC2
- Docker Engine / Docker Compose
- nginx
- certbot
- Git

예시 확인 명령:

```bash
docker --version
docker compose version
nginx -v
git --version
```

### 4.2 UFW 정책

```bash
sudo ufw allow 22
sudo ufw allow 80
sudo ufw allow 443
sudo ufw status numbered
```

`8080`, `8081`, `3000`, `3002`, `9090`, `5432`, `6379` 는 외부 공개하지 않는다.

## 5. GitLab clone 이후 수동 빌드 / 배포 절차

아래 예시는 서버 기준 경로를 다음처럼 가정한다.

```text
/home/ubuntu/app/S14P21C103   -> GitLab clone repo
/home/ubuntu/infra            -> Jenkins 전용 compose 보관 폴더
```

### 5.1 저장소 clone

```bash
mkdir -p /home/ubuntu/app
cd /home/ubuntu/app
git clone <GitLab repository URL> S14P21C103
cd /home/ubuntu/app/S14P21C103
```

### 5.2 환경변수 파일 준비

운영 런타임용 예시:

```bash
cp infrastructure/docker/.env.runtime.example .env.runtime
```

`Jenkinsfile` 기준으로 실제 운영에서 사용하는 주요 값은 다음과 같다.

```text
FRONTEND_HOST_PORT=3000
BACKEND_HOST_PORT=8081
POSTGRES_HOST_PORT=5432
REDIS_HOST_PORT=6379
PUBLIC_BASE_URL=https://j14c103.p.ssafy.io
POSTGRES_DB=homerun
POSTGRES_USER=homerun_user
POSTGRES_PASSWORD=<secret>
SSAFY_API_KEY=<secret>
SSAFY_ACCOUNT_TYPE_UNIQUE_NO=<secret>
KIS_APP_KEY=<secret>
KIS_APP_SECRET=<secret>
PUBLIC_DATA_SERVICE_KEY=<secret>
NAVER_GEOCODING_CLIENT_ID=<secret>
NAVER_GEOCODING_CLIENT_SECRET=<secret>
SPRING_JPA_HIBERNATE_DDL_AUTO=update
REAL_ESTATE_IMPORT_ENABLED=false
REAL_ESTATE_IMPORT_REGIONS=SEOUL,GWANGJU
REAL_ESTATE_IMPORT_FROM_YEAR_MONTH=2025-04
REAL_ESTATE_IMPORT_TO_YEAR_MONTH=2026-03
REAL_ESTATE_IMPORT_DATASET_TYPES=APT_SALE
FRONTEND_IMAGE=homerun-frontend:develop
BACKEND_IMAGE=homerun-backend:develop
PROMETHEUS_IMAGE=homerun-prometheus:develop
GRAFANA_IMAGE=homerun-grafana:develop
```

### 5.3 권장 사전 검증

프론트 lint:

```bash
docker build --target validator -t homerun-frontend-validator -f frontend/Dockerfile frontend
```

백엔드 테스트:

```bash
docker buildx build \
  --progress=plain \
  --load \
  --target tester \
  -t homerun-backend-tester \
  -f backend/Dockerfile.buildkit-cache \
  backend
```

### 5.4 백엔드 REST Docs 생성

`frontend/Dockerfile` 은 `frontend/generated-docs` 디렉터리를 기대하므로, 아래 순서로 문서를 먼저 생성해야 한다.

```bash
rm -rf frontend/generated-docs
mkdir -p frontend/generated-docs
```

```bash
docker buildx build \
  --progress=plain \
  --load \
  --target docs \
  -t homerun-backend-docs:develop \
  -f backend/Dockerfile.buildkit-cache \
  backend
```

```bash
DOCS_CONTAINER=$(docker create homerun-backend-docs:develop)
docker cp "$DOCS_CONTAINER":/workspace/build/docs/asciidoc/. frontend/generated-docs/
docker rm -f "$DOCS_CONTAINER"
```

### 5.5 런타임 이미지 빌드

```bash
docker build \
  --build-arg VITE_NAVER_MAP_CLIENT_ID=${NAVER_GEOCODING_CLIENT_ID} \
  -t homerun-frontend:develop \
  -f frontend/Dockerfile \
  frontend
```

```bash
docker buildx build \
  --progress=plain \
  --load \
  -t homerun-backend:develop \
  -f backend/Dockerfile.buildkit-cache \
  backend
```

```bash
docker build \
  -t homerun-prometheus:develop \
  -f infrastructure/monitoring/prometheus/Dockerfile \
  infrastructure/monitoring/prometheus
```

```bash
docker build \
  -t homerun-grafana:develop \
  -f infrastructure/monitoring/grafana/Dockerfile \
  infrastructure/monitoring/grafana
```

### 5.6 런타임 기동

```bash
docker compose \
  --env-file .env.runtime \
  -f infrastructure/docker/docker-compose.runtime.yml \
  -f infrastructure/docker/docker-compose.observability.yml \
  up -d postgres redis backend frontend prometheus grafana
```

### 5.7 기동 확인

```bash
docker ps --format '{{.Names}}\t{{.Status}}\t{{.Ports}}' | grep homerun
```

```bash
curl -I http://127.0.0.1:3000/
curl -I http://127.0.0.1:3000/docs/
curl -I http://127.0.0.1:3000/grafana
curl -I http://127.0.0.1:8081/actuator/health
```

정상 기준:

- frontend root 응답 `200`
- `/docs/` 응답 `200`
- `/grafana` 응답 `302` 또는 Grafana 로그인 응답
- backend health 응답 `200`

## 6. Jenkins 자동배포 기준

현재 운영 배포는 `Jenkins + GitLab webhook` 구조다.

### 6.1 Jenkins 컨테이너 구성

운영 기준 Jenkins compose 는 repo 밖 `/home/ubuntu/infra/docker-compose.jenkins.yml` 에 저장한다.

핵심 조건:

- 이미지: `homerun-jenkins:lts`
- prefix: `/jenkins`
- timezone: `TZ=Asia/Seoul`
- 바인드 마운트:
  - `/var/run/docker.sock:/var/run/docker.sock`
  - `/home/ubuntu/infra:/home/ubuntu/infra`

기동 예시:

```bash
mkdir -p /home/ubuntu/infra/jenkins
cd /home/ubuntu/infra
docker compose -f docker-compose.jenkins.yml up -d --build
```

초기 비밀번호 확인:

```bash
docker exec homerun-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 6.2 Jenkins Job 설정

| 항목 | 값 |
| --- | --- |
| Job name | `homerun-develop` |
| Branch | `*/develop` |
| Script Path | `infrastructure/Jenkinsfile` |
| Trigger | GitLab webhook |

Webhook URL:

```text
https://j14c103.p.ssafy.io/jenkins/project/homerun-develop
```

### 6.3 현재 파이프라인 단계

```text
Checkout Info
Validate And Test
  - Frontend Validate
  - Backend Test
Build Runtime Images
Deploy Runtime
Verify Runtime Startup
Verify Frontend HTTP
Collect Monitoring Evidence
```

### 6.4 Jenkins Credentials

| ID | 타입 | 용도 |
| --- | --- | --- |
| `homerun-postgres-password` | Secret text | PostgreSQL 비밀번호 |
| `ssafy-api-key` | Secret text | SSAFY API 키 |
| `SSAFY_ACCOUNT_TYPE_UNIQUE_NO` | Secret text | SSAFY 계좌 상품 고유번호 |
| `KIS_APP_KEY` | Secret text | KIS app key |
| `KIS_APP_SECRET` | Secret text | KIS app secret |
| `public-data-service-key` | Secret text | 공공데이터 API 키 |
| `naver-geocoding-client-id` | Secret text | Naver Geocoding client id |
| `naver-geocoding-client-secret` | Secret text | Naver Geocoding client secret |
| `mattermost-webhook-url` | Secret text | 배포 성공/실패 알림 |

주의:

- 운영 서버에는 `.env.runtime` 파일을 상시 보관하지 않는다.
- Jenkins 가 배포 시점에 `.env.runtime` 을 생성하고, 배포 후 삭제한다.
- 따라서 운영 설정의 소스 오브 트루스는 `Jenkins Credentials + Jenkinsfile + compose + 실행 중 컨테이너 env` 다.

## 7. nginx 설정 기준

호스트 nginx는 아래 역할만 담당한다.

- `/` -> `127.0.0.1:3000`
- `/api/` -> `127.0.0.1:8081/api/`
- `/jenkins/` -> `127.0.0.1:8080/jenkins/`

주의:

- `/api/` 프록시는 prefix 를 유지해야 한다.
- 안전한 형태는 `proxy_pass http://127.0.0.1:8081/api/;`
- `proxy_pass http://127.0.0.1:8081/;` 로 쓰면 `/api/auth/login` 이 `/auth/login` 으로 변형될 수 있다.

## 8. 환경변수 상세

### 8.1 빌드/배포에 직접 쓰는 핵심 환경변수

| 변수 | 설명 | 현재 운영 기준 |
| --- | --- | --- |
| `FRONTEND_HOST_PORT` | frontend host bind port | `3000` |
| `BACKEND_HOST_PORT` | backend host bind port | `8081` |
| `POSTGRES_HOST_PORT` | postgres host bind port | `5432` |
| `REDIS_HOST_PORT` | redis host bind port | `6379` |
| `PUBLIC_BASE_URL` | grafana subpath root URL 계산 | `https://j14c103.p.ssafy.io` |
| `POSTGRES_DB` | 운영 DB명 | `homerun` |
| `POSTGRES_USER` | 운영 DB 사용자 | `homerun_user` |
| `POSTGRES_PASSWORD` | 운영 DB 비밀번호 | Jenkins secret |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | prod JPA ddl-auto | `update` |
| `REAL_ESTATE_IMPORT_ENABLED` | 배포 후 부동산 적재 자동 실행 여부 | `false` |
| `REAL_ESTATE_IMPORT_REGIONS` | 적재 지역 | `SEOUL,GWANGJU` |
| `REAL_ESTATE_IMPORT_FROM_YEAR_MONTH` | 적재 시작 월 | `2025-04` |
| `REAL_ESTATE_IMPORT_TO_YEAR_MONTH` | 적재 종료 월 | `2026-03` |
| `REAL_ESTATE_IMPORT_DATASET_TYPES` | 적재 데이터셋 | `APT_SALE` |

### 8.2 backend 앱 설정 환경변수

| 변수 | 설명 | 정의 위치 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | 활성 프로필 | runtime compose |
| `SERVER_PORT` | backend 내부 포트 | runtime compose |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `application-prod.yml` + runtime compose |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL 사용자 | `application-prod.yml` + runtime compose |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL 비밀번호 | `application-prod.yml` + runtime compose |
| `SPRING_DATA_REDIS_HOST` | Redis host | runtime compose |
| `SPRING_DATA_REDIS_PORT` | Redis port | runtime compose |
| `APP_DOCS_ENABLED` | 로컬/운영 docs 활성화 | `application.yml`, `application-prod.yml`, `application-local.yml` |
| `JWT_SECRET` | JWT 서명키 | `application.yml` |

### 8.3 외부 연동 환경변수

| 변수 | 설명 | 비고 |
| --- | --- | --- |
| `SSAFY_API_KEY` | SSAFY 금융 API 인증키 | Jenkins secret 사용 |
| `SSAFY_ACCOUNT_TYPE_UNIQUE_NO` | SSAFY 계좌 상품 고유번호 | Jenkins secret 사용 |
| `FSS_API_KEY` | 금융감독원 대출 상품 API 인증키 | `application.yml` 기본값 존재, 운영 주입 여부는 별도 확인 필요 |
| `KIS_APP_KEY` | KIS 시세 API 키 | Jenkins secret 사용 |
| `KIS_APP_SECRET` | KIS 시세 API 시크릿 | Jenkins secret 사용 |
| `PUBLIC_DATA_SERVICE_KEY` | 공공데이터 API 키 | Jenkins secret 사용 |
| `NAVER_GEOCODING_CLIENT_ID` | Naver Geocoding client id | Jenkins secret 사용 |
| `NAVER_GEOCODING_CLIENT_SECRET` | Naver Geocoding client secret | Jenkins secret 사용 |
| `OCI_ENABLED` | OCI Object Storage 사용 여부 | 운영 기본값 `true` |
| `OCI_REGION` | OCI 리전 | 운영 기본값 `ap-singapore-1` |
| `OCI_NAMESPACE` | OCI namespace | 운영 기본값 `axyqj6o2a3yw` |
| `OCI_BUCKET_NAME` | OCI bucket 이름 | 운영 기본값 `bucket-20260316-1523` |
| `OCI_CONFIG_PATH` | 로컬 OCI config 경로 | `application-local.yml`, 기본값 `C:/key/oci_config` |

## 9. 주요 계정 / 프로퍼티 / ERD 관련 파일 목록

| 파일 | 역할 |
| --- | --- |
| `backend/src/main/resources/application.yml` | 공통 기본 설정, JWT, SSAFY, FSS, KIS, OCI, 공공데이터, Naver Geocoding 설정 |
| `backend/src/main/resources/application-prod.yml` | 운영 datasource, JPA, actuator/prometheus 설정 |
| `backend/src/main/resources/application-local.yml` | 로컬 H2, OCI local path, docs 설정 |
| `infrastructure/Jenkinsfile` | 운영 `.env.runtime` 생성, credential 주입, 이미지 빌드/배포 절차 |
| `infrastructure/docker/docker-compose.runtime.yml` | frontend/backend/postgres/redis 런타임 구성 |
| `infrastructure/docker/docker-compose.observability.yml` | prometheus/grafana 런타임 구성 |
| `backend/src/main/resources/schema.sql` | DB schema 기준 |
| `backend/src/main/resources/homerun_dummy.sql` | 더미 데이터 기준 |
| `erd/erd.md` | ERD 문서 |
| `infra/postman/homerun-auth-smoke.postman_collection.json` | 인증 API 스모크 테스트 컬렉션 |
| `SSAFY_금융망_API/*.md` | SSAFY 금융망 API 기능별 명세 참고 문서 |

## 10. DB 접속 정보 및 덤프

### 10.1 운영 DB 접속 기준

| 항목 | 값 |
| --- | --- |
| DB 엔진 | PostgreSQL 16.13 |
| 컨테이너명 | `homerun-postgres` |
| DB명 | `homerun` |
| 사용자 | `homerun_user` |
| 접속 방식 | EC2 내부 Docker exec 또는 localhost bind |
| 비밀번호 | Jenkins secret 또는 `.env.runtime` 값 |

EC2 내부 점검:

```bash
docker exec -it homerun-postgres psql -U homerun_user -d homerun
```

### 10.2 제출 포함 dump

제출본에는 아래 파일을 포함했다.

```text
exec/db/homerun-ec2-20260330-102837.dump
```

형식:

```text
pg_dump custom format (-Fc)
```

복원 예시:

```bash
createdb -U homerun_user homerun_restore
pg_restore --no-owner --no-privileges -U homerun_user -d homerun_restore exec/db/homerun-ec2-20260330-102837.dump
```

## 11. 배포 시 특이사항

### 11.1 frontend 이미지는 generated-docs 디렉터리가 필요함

- backend REST Docs 생성 후 `frontend/generated-docs` 를 채워야 frontend 이미지 빌드가 된다.
- 이 단계가 빠지면 frontend Docker build 가 실패한다.

### 11.2 postgres 비밀번호는 volume 초기값에 고정됨

- `POSTGRES_PASSWORD` 만 바꿔도 기존 volume 이 남아 있으면 인증 오류가 날 수 있다.
- 증상: `password authentication failed for user "homerun_user"`

### 11.3 운영 `.env.runtime` 은 서버에 상시 남지 않음

- 운영 값 확인은 파일 탐색보다 `Jenkins Credentials + Jenkinsfile + 실행 중 컨테이너 env` 를 봐야 한다.

### 11.4 `/grafana` 는 host nginx가 아니라 frontend nginx에서 처리함

- host nginx 설정에 `/grafana` location 이 없어도 정상일 수 있다.
- frontend 컨테이너 nginx가 `/grafana` 를 grafana 컨테이너로 프록시한다.

### 11.5 부동산 자동 적재는 현재 기본 비활성화

- `REAL_ESTATE_IMPORT_ENABLED=false`
- 필요 시 월 범위를 바꿔 재배포하거나 별도 수동 적재 절차를 사용한다.

## 12. 운영 확인 체크리스트

### 12.1 컨테이너 상태

```bash
docker ps --format '{{.Names}}\t{{.Status}}\t{{.Ports}}' | grep homerun
```

### 12.2 공개 URL 확인

```bash
curl -I https://j14c103.p.ssafy.io/
curl -I https://j14c103.p.ssafy.io/api/
curl -I https://j14c103.p.ssafy.io/jenkins/
```

### 12.3 내부 URL 확인

```bash
curl -I http://127.0.0.1:3000/
curl -I http://127.0.0.1:3000/docs/
curl -I http://127.0.0.1:3000/grafana
curl -I http://127.0.0.1:8081/actuator/health
```

### 12.4 로그 확인

```bash
docker logs --tail=200 homerun-backend
docker logs --tail=200 homerun-frontend
docker logs --tail=200 homerun-jenkins
docker logs --tail=200 homerun-grafana
docker logs --tail=200 homerun-prometheus
```

## 13. 요약

운영 배포의 핵심은 다음 네 가지다.

- repo clone 후 backend docs 생성
- frontend/backend/monitoring 이미지 빌드
- `.env.runtime` 기반 compose 기동
- 필요 시 Jenkins 와 GitLab webhook 으로 자동배포 구조 연결

이 네 단계를 지키면 현재 운영 구조와 동일한 형태로 재현할 수 있다.


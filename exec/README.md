# Homerun Exec Package

검증 기준일: `2026-03-30`

이 폴더는 제출용 산출물 `exec` 패키지다.  
기준 정보는 아래 두 축으로 맞췄다.

- 현재 저장소: `C:\LEE\finance\S14P21C103`
- 운영 서버 실측: `j14c103.p.ssafy.io` EC2

## 포함 파일

| 파일 | 설명 |
| --- | --- |
| `PORTING_MANUAL.md` | GitLab clone 이후 빌드, 배포, 운영 복원 절차를 정리한 본문 매뉴얼 |
| `EXTERNAL_SERVICES.md` | 프로젝트에서 사용하는 외부 서비스, 발급 위치, 주입 위치, 사용 목적 정리 |
| `DEMO_SCENARIO.md` | 평가/시연용 핵심 해피패스 시나리오 |
| `db/homerun-ec2-20260330-102837.dump` | 운영 EC2 PostgreSQL 기준 최신 custom dump |

## 작성 원칙

- 실제 비밀번호, API 키, 토큰 값은 넣지 않았다.
- secret 값 대신 `credential ID`, `환경변수명`, `주입 경로`만 적었다.
- 제출 시 바로 참조할 수 있도록 운영 버전, 포트, 컨테이너명, 경로를 절대값으로 고정했다.

## 빠른 확인 포인트

- 서비스 진입 URL: `https://j14c103.p.ssafy.io`
- Jenkins URL: `https://j14c103.p.ssafy.io/jenkins/`
- 운영 DB 컨테이너: `homerun-postgres`
- 운영 DB 이름: `homerun`
- 운영 DB 사용자: `homerun_user`

## 참고 문서 원본

아래 문서들을 대조해서 제출용 문서로 재구성했다.

- `infra/PORTING_MANUAL.md`
- `infra/TEAM_INFRA_GUIDE.md`
- `infra/SERVER_RUNTIME_ENV_HANDOFF.md`
- `infra/POSTGRES_DUMP_RESTORE_MANUAL.md`
- `S14P21C103/infrastructure/Jenkinsfile`
- `S14P21C103/infrastructure/docker/docker-compose.runtime.yml`
- `S14P21C103/infrastructure/docker/docker-compose.observability.yml`


# WebP Converter Java

WebP 이미지를 PNG 또는 JPG로 일괄 변환하는 Spring Boot 웹 애플리케이션입니다. 기존 [Python 데스크톱 버전](https://github.com/edgarshlee/webp-converter)의 기능을 웹 서비스로 재설계했습니다.

## 핵심 기능

- 최대 20개 WebP 이미지 일괄 업로드
- PNG 알파 채널 보존
- JPG 품질(1~100) 및 투명 영역 배경색 지정
- 성공 결과와 파일별 처리 보고서를 ZIP으로 다운로드
- 손상 파일이 섞여 있어도 정상 파일은 계속 변환
- 파일명 정규화와 중복 이름 자동 해결
- 업로드 파일 비영구 처리
- Actuator 상태 확인, Docker 이미지, GitHub Actions CI

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Runtime | Java 21 |
| Framework | Spring Boot 3.5 |
| Web | Spring MVC, Thymeleaf |
| Image decoding | webp-imageio (libwebp) |
| Test | JUnit 5, AssertJ, MockMvc, Mockito |
| Build | Maven Wrapper, JaCoCo |

## 구조

```text
web                  HTTP 요청, 입력 제한, ZIP 응답, 오류 응답
  ↓
application          일괄 변환 흐름과 PNG/JPG 인코딩
  ↓
application.port     WebP 디코더 추상화
  ↑
infrastructure       libwebp ImageIO 어댑터
domain               변환 옵션, 입력/결과, 도메인 예외
```

프레임워크 코드와 이미지 변환 규칙을 분리해 핵심 로직을 웹 서버 없이 단위 테스트할 수 있습니다.

## 실행

Java 21 이상이 필요합니다. Maven은 별도로 설치하지 않아도 됩니다.

```powershell
.\mvnw.cmd spring-boot:run
```

브라우저에서 <http://localhost:8080>을 여세요.

```powershell
.\mvnw.cmd verify
.\mvnw.cmd package
java -jar .\target\webp-converter-java-0.1.0-SNAPSHOT.jar
```

## API

`POST /api/conversions` (`multipart/form-data`)

| 필드 | 설명 | 기본값 |
| --- | --- | --- |
| `files` | WebP 파일, 최대 20개 | 필수 |
| `format` | `PNG`, `JPG`, `JPEG` | `PNG` |
| `quality` | JPG 품질 1~100 | `90` |
| `background` | JPG 투명 영역 배경 `#RRGGBB` | `#ffffff` |

성공 시 변환 이미지와 `conversion-report.json`이 포함된 ZIP을 반환합니다. 입력 오류는 Problem Details JSON으로 응답합니다.

## Docker

```powershell
docker build -t webp-converter-java .
docker run --rm -p 8080:8080 webp-converter-java
```

## 처리 정책

- 파일당 20MB, 요청당 100MB, 요청당 최대 20개
- 애니메이션 WebP는 첫 프레임만 변환하고 보고서에 표시
- 원본 메타데이터와 ICC 프로파일은 결과에 복사하지 않음
- 애플리케이션이 업로드 파일이나 결과를 영구 저장하지 않음

상태 확인: `GET /actuator/health`

## 라이선스

[MIT](LICENSE)

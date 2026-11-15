# 새록 (Saerok)

현대차그룹 차량용 앱 개발 공모전 출품작 — Pleos Connect용 주행 기억 앱

> 내가 다닌 길이 분재의 가지로 자랍니다. 자주 다닌 길은 굵은 가지, 처음 가 본 길은 얇은 가지 하나. 주행이 끝나면 "오늘 얼마나 자랐나" 카드로 하루의 길을 돌아봅니다.

## 바로 실행

```
adb install -r apk/saerok-0.6.3-debug.apk
```

- 버전 0.6.3 (versionCode 9), 패키지 `com.saerok.pleos.bonsai`, debug 빌드
- SHA-256: `4799dc6ec7b6506b26c4e5f8e89fff76db5338fdc4b1570d7944efb9afad32f3`
- Pleos 에뮬레이터 실행 예시와 빌드 방법은 [`android/README.md`](android/README.md)에 있습니다.

## 저장소 구성

| 경로 | 내용 |
|---|---|
| `android/` | Android Studio 프로젝트 전체 (Kotlin WebView 앱 + HTML/CSS/JS assets, 글꼴 포함) |
| `apk/saerok-0.6.3-debug.apk` | 위 소스로 빌드한 APK |
| `web/saerok.html` | 같은 화면 코드를 한 파일로 합친 브라우저용 사본 |
| `prototype/` | 초기 HTML 프로토타입과 디자인 변형안 |
| `docs/Saerok_Low_Fidelity_Flow.pdf` | 저충실도 화면 흐름도 |

## 구조

- Kotlin `MainActivity`가 WebView로 APK에 포함된 HTML·CSS·JavaScript를 표시합니다. 웹 서버나 인터넷 연결 없이 실행됩니다.
- 대략적인 위치를 받아 일출·일몰에 맞춰 테마를 자동 전환합니다.
- 계획: 위치·차량 신호(FusedLocation, Vehicle SDK 읽기)를 JS 브릿지로 전달해 실제 주행 기록을 분재로 키웁니다. 원시 좌표 7일 후 자동 삭제, 집 주변 흐림 처리, 암호화 저장, 프로필별 분리.

## 현재 구현 범위

분재 성장, 타임랩스, 회전·확대, 추억 지도, 다크모드는 동작합니다. 주행·P단·차량 신호·추억 지도 데이터는 예시 시뮬레이션이며, 실제 차량 SDK와 주행 경로 수집은 아직 연결하지 않았습니다.

## 라이선스 고지

- 글꼴 Gowun Batang, Song Myung: SIL Open Font License (`android/app/src/main/assets/saerok/fonts/`)
- SunCalc: BSD-2-Clause (`android/app/src/main/assets/saerok/SunCalc-LICENSE.txt`)

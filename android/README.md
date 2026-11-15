# 새록 분재 데모 · Android Studio 제출용

최종 버전: **0.6.3 (versionCode 9)**  
앱 이름: 새록 분재 데모  
설치 패키지: `com.saerok.pleos.bonsai`  
Kotlin 클래스: `com.saerok.pleos.MainActivity`

Kotlin Activity 안의 WebView에서 APK에 포함된 HTML·CSS·JavaScript를 표시합니다. 한글 글꼴도 APK에 포함되어 있어 별도 웹 서버나 인터넷 연결 없이 앱이 실행됩니다. Kotlin 클래스의 패키지와 설치 패키지가 서로 다른 것은 의도된 설정입니다.

## Android Studio에서 열기

1. 이 README가 있는 `android` 폴더를 Android Studio에서 엽니다.
2. Gradle JDK는 Android Studio 내장 JDK 21을 사용하고, Android SDK Location은 자신의 PC에 설치된 SDK 경로로 지정합니다. PC별 경로가 기록되는 `local.properties`는 제출물에서 제외했습니다.
3. Android SDK Platform 36.1을 설치하고 Gradle 동기화를 완료합니다. 첫 동기화·빌드에는 Gradle과 의존성을 받기 위한 인터넷 연결이 필요합니다.
4. Pleos Connect 에뮬레이터를 실행하고 `app` 구성을 선택해 실행합니다.

확인한 구성: Android Gradle Plugin 9.2.1, Gradle Wrapper 9.4.1, AndroidX WebKit 1.16.0, compileSdk 36.1, targetSdk 34, minSdk 26. Kotlin은 Android Gradle Plugin의 내장 기능을 사용합니다.

Windows 명령줄 빌드:

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

생성 APK: `app/build/outputs/apk/debug/app-debug.apk`

## APK 설치와 Pleos 실행

동봉 APK는 최종 소스로 빌드한 debug 버전입니다.

```powershell
adb install -r ../apk/saerok-0.6.3-debug.apk
```

이 PC에서 확인한 Pleos 화면은 왼쪽 차량 대시보드와 오른쪽 앱 영역입니다. 아래 명령은 해당 에뮬레이터의 사용자 10에서 여러 창 모드로 실행합니다. 다른 장치에서는 사용자 번호와 화면 배치가 다를 수 있습니다.

```powershell
adb shell am start --user 10 --windowingMode 6 -n com.saerok.pleos.bonsai/com.saerok.pleos.MainActivity
```

## 전체 소스 구성

| 경로 | 내용 |
| --- | --- |
| `app/src/main/java/com/saerok/pleos/MainActivity.kt` | WebView, 앱 내부 파일 로딩, 테마 연결, 대략적인 위치 권한·갱신, 화면 수명주기 |
| `app/src/main/assets/saerok/index.html` | 분재 Canvas, 터치 조작, 타임랩스, 지도, 메뉴, 예시 기록 |
| `app/src/main/assets/saerok/refinement.css` | 배치·줄바꿈·버튼·전환 모션 |
| `app/src/main/assets/saerok/theme.css` | 일반·다크 테마와 자막 |
| `app/src/main/assets/saerok/theme.js` | 테마 선택·저장, 일출·일몰 자동 전환 |
| `app/src/main/assets/saerok/suncalc.js` | 일출·일몰 계산 라이브러리 |
| `app/src/main/assets/saerok/fonts/` | Gowun Batang / Song Myung 글꼴과 SIL OFL 라이선스 |
| `app/src/main/assets/saerok/SunCalc-LICENSE.txt` | SunCalc BSD-2-Clause 라이선스 |
| `app/src/main/AndroidManifest.xml` | 자동차 앱·Activity·위치 권한 선언 |
| `app/src/main/res/drawable/ic_saerok.xml` | 앱 아이콘 |
| `app/build.gradle.kts`, 루트 Gradle 파일, `gradle/wrapper/` | 빌드 설정과 Gradle Wrapper |

## 최종 기능과 수정

- 내 분재 / 길의 기억 / 추억 지도 / 설정을 통합했습니다. 시작 화면은 내 분재이며 성장 다시 보기로 타임랩스를 재생합니다.
- 일반·다크 모드와 일출·일몰 자동 전환을 제공합니다. 자동은 기기 시각과 대략적인 위치를 사용하며 처음 켤 때 위치 권한을 요청합니다. 위치 미확인 상태에서는 현재 테마를 유지합니다.
- 한 손가락 회전·지도 이동, 두 손가락 확대·축소, 지도 이동 한계, 낮은 시점에서 화분을 통과하는 현상 수정을 포함합니다.
- 어제에서는 오늘 성장 표식을 숨기며 오늘의 표시 버튼으로 자란 위치를 확인합니다. 어제·오늘 안내는 짧게 나타납니다.
- 가지 굵기는 미리보기 후 경고창에서 확정하고 확정하지 않고 나가면 취소합니다. 메뉴와 표시 버튼·하단 박스에 전환 모션을 적용했습니다.
- 누적 회전 후 되감기, 카메라보다 늦게 시작하던 성장, 새 열매의 잘못된 표식과 이전 표식 겹침을 수정했습니다. 전체 표식은 카메라가 전체 구도로 복귀한 뒤 표시합니다.
- 다크모드 자막은 상아색 글씨·금빛 밑줄·먹색 바탕으로 표시합니다.

## 구현 범위와 확인

분재·주행·P단·추억 지도·보호 구역 화면은 예시 데이터 시뮬레이션입니다. 실제 차량 SDK, 실제 주행 경로 기록, 실제 보호 구역 처리는 연결하지 않았습니다. 일출·일몰 자동 테마용 대략적인 위치 연결은 Kotlin에 구현되어 있습니다. 테마 선택·자동 여부와 확정 가지 굵기는 저장되며, 예시 주행 기록과 삭제 상태는 새로고침 시 초기화됩니다.

버전 0.6.3의 Android 빌드와 Pleos 다크 자막 화면 확인을 완료했습니다. 성장 타임랩스·회전·지도·가지치기·테마의 이전 검증 내용과 수정이 포함되어 있습니다. 실제 위치를 허용한 장기 주행이나 실차 시험은 수행하지 않았습니다.

저장소의 `web/saerok.html`은 같은 화면 코드를 한 파일로 합친 브라우저용 사본입니다. Android 앱은 위 assets와 로컬 글꼴을 사용하며 브라우저용 사본의 Google Fonts 로딩에는 인터넷이 필요합니다.

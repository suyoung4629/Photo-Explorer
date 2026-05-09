# Unsplash Photo Explorer

Unsplash API를 사용한 사진 탐색 안드로이드 앱입니다. Jetpack Compose, Coroutines/Flow, 멀티모듈 구조, MVVM + UDF로 구성되어 있습니다.

> [!IMPORTANT]
> 실행하려면 루트의 `local.properties`에 Unsplash API 키가 필요합니다.
> ```properties
> UNSPLASH_ACCESS_KEY=your_unsplash_access_key_here
> ```
> 키 발급: [Unsplash Developers](https://unsplash.com/developers) → New Application

> **시연 영상**
> - https://github.com/user-attachments/assets/9dca3eeb-2ff3-4dd9-9fcd-4dcde99bb248
> - https://github.com/user-attachments/assets/ed40e9f2-4a02-4eb6-abea-e851f668b30c
<img width="1070" height="882" alt="image" src="https://github.com/user-attachments/assets/dc68a681-b0a8-4af8-9e62-83d60b466377" />

---

## 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [모듈 구조](#모듈-구조)
- [빌드 & 실행](#빌드--실행)

---

## 주요 기능

- 사진 목록 (무한 스크롤, Staggered Grid)
- 사진 상세 보기
- 즐겨찾기: 원본 이미지를 앱 내부 저장소에 다운로드, 해제 시 파일/DB 함께 삭제
- 화면 간 즐겨찾기 상태 동기화
- Pull-to-refresh, 타이틀 클릭 시 상단으로 이동

---

## 기술 스택

| 영역 | 사용 기술 |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose · Material3 · Compose BOM 2026.02.01 |
| Async | Coroutines 1.9.0 · Flow / StateFlow / Channel |
| DI | Hilt 2.59.2 (KSP) |
| Network | Retrofit 2.11 · OkHttp 4.12 · kotlinx.serialization |
| Local | Room 2.7 |
| Pagination | Paging 3 + paging-compose |
| Image | Coil 3 |
| Navigation | Navigation Compose 2.8 (type-safe routes) |
| Build | AGP 9.2.0 · Gradle Version Catalog · Convention Plugins |
| Test | JUnit4 · MockK · kotlinx-coroutines-test · Turbine |

---

## 아키텍처

### 레이어

```
   Presentation                              Data
   (feature/*)                            (core:data)
   Compose Screen                  Repository 구현 · Paging
   ViewModel : StateFlow              Retrofit · Room · Mapper
        │                                       │
        │ UseCase 호출                           │ Repository 인터페이스 구현
        ▼                                       ▼
       ┌───────────────────────────────────────────┐
       │           Domain (core:domain)            │
       │   Model · UseCase · Repository(interface) │
       └───────────────────────────────────────────┘
```

- Domain은 다른 레이어에 의존하지 않으며, Repository 인터페이스만 정의하고 구현은 Data 레이어에 위임합니다.
- 네트워크 응답(DTO)과 DB 모델(Entity)은 `core:data` 안에서만 사용하고, 다른 모듈에는 도메인 모델로 변환해서 전달합니다.

### 화면 간 상태 동기화

Room의 `Flow<List<String>>`을 SSOT(Single Source of Truth)으로 사용해, 목록 / 상세 / 즐겨찾기 화면이 동일한 즐겨찾기 상태를 공유합니다.

---

## 모듈 구조

```
:app                       애플리케이션 + DI 모듈 + Navigation 그래프
:core:domain               모델 · UseCase · Repository 인터페이스
:core:data                 Repository 구현 · Retrofit · Room · Paging · Mapper
:core:ui                   디자인 시스템 · 공용 Compose 컴포넌트
:feature:photolist         사진 목록 화면
:feature:photodetail       상세 화면
:feature:favorites         즐겨찾기 목록 화면
:build-logic:convention    Gradle Convention Plugin
```

---

## 빌드 & 실행

### 요구사항

- Android Studio Ladybug+ (AGP 9.2 호환)
- JDK 17
- compileSdk 36 / minSdk 24

### 실행

```bash
./gradlew :app:installDebug
# 또는 Android Studio에서 Run 'app'
```

### 테스트

```bash
./gradlew test
```

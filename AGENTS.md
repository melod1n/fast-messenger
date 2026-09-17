# Fast Messenger: Инструкции и критические правила для AI-агентов (AGENTS.md)

В этом документе собраны ключевые архитектурные особенности, неочевидные подводные камни и частые ошибки кодовой базы Fast Messenger, чтобы избежать их повторения в будущих сессиях разработки.

---

## 1. 🌐 Сеть, VK API и интерцепторы OkHttp

### ⚠️ Ошибка `ERR_UPLOAD_BAD_SIGNATURE` при загрузке медиа (аудио, фото, файлы)
* **Причина:** Серверы загрузки ВКонтакте (`pu.vk.ru`, `pu.vk.com`, `upload.php`) генерируют URL со строгой MD5-подписью `_sig` по точному набору query-параметров. Если глобальные интерцепторы OkHttp (`AccessTokenInterceptor`, `VersionInterceptor`, `LanguageInterceptor`) добавляют к URL загрузки лишние параметры (`access_token=...`, `v=...`, `lang=...`), сервер VK отвергает запрос с ошибкой `ERR_UPLOAD_BAD_SIGNATURE`.
* **Правило:** В любых интерцепторах OkHttp, модифицирующих query-параметры или заголовки, **всегда** проверяйте хост и путь запроса:
  ```kotlin
  val request = chain.request()
  val host = request.url.host
  val path = request.url.encodedPath

  // Пропускаем upload-серверы без модификации параметров
  if (!host.startsWith("api.vk.") || path.contains("upload.php")) {
      return chain.proceed(request)
  }
  ```

---

## 2. 📦 Сериализация Moshi и `ResponseConverterFactory`

### ⚠️ Ошибка `Required value 'errorCode' (JSON name 'error_code') missing at $` / `No JsonAdapter`
* **Причина:** В проекте используется Moshi с кодогенерацией (ksp / kapt) без fallback на reflection. `ResponseConverterFactory` сначала пытается распарсить тело ответа в `SuccessType`. Если у класса модели отсутствует аннотация `@JsonClass(generateAdapter = true)`, Moshi падает с `IllegalArgumentException: No JsonAdapter for ...`. Затем `ResponseConverterFactory` пытается распарсить успешный ответ как `RestApiError`, что приводит к ошибке отсутствия поля `error_code` и выбрасыванию `UnknownFailure`.
* **Правило:** **Все** DTO и Response-модели (`FilesResponses.kt`, `PhotosResponses.kt`, `AudiosResponses.kt`, `VideosResponses.kt`, `VkUserData.kt` и др.) **обязаны** быть помечены аннотацией:
  ```kotlin
  @JsonClass(generateAdapter = true)
  data class MyApiResponse(...)
  ```

---

## 3. 👆 Jetpack Compose и жесты удержания (Hold-to-Record)

### ⚠️ Ошибка `MediaRecorder: stop failed: -1007` (Преждевременная остановка записи)
* **Причина:** Нельзя связывать модификатор жестов `Modifier.pointerInput(...)` или ветвление `.then(if (...) ...)` с промежуточным состоянием UI (например, `isRecordingVoice`), которое меняется прямо во время удержания пальца. При смене `isRecordingVoice = true` Compose делает рекомпозицию, сбрасывает/отменяет корутину жеста через ~70-100 мс, вызывая экстренную остановку `MediaRecorder` до того, как пользователь отпустил палец.
* **Правило:**
  1. Ключом для `pointerInput` должен быть только статический режим экрана (`actionMode`), но **не** флаг текущей активности записи `isRecordingVoice`.
  2. Колбэки внутри `pointerInput` оборачивайте в `rememberUpdatedState`, чтобы они не перезапускали корутину жеста при рекомпозиции:
     ```kotlin
     val currentOnRecordStart by rememberUpdatedState(onRecordStart)
     val currentOnRecordFinish by rememberUpdatedState(onRecordFinish)
     val currentOnRecordCancel by rememberUpdatedState(onRecordCancel)
     ```

---

## 4. 🎙️ Запись голоса и аудиокодеки Android (`VoiceRecorder.kt`)

* **Аппаратные кодеки:** На некоторых устройствах Android (Samsung, Xiaomi и др.) аппаратный кодировщик `OutputFormat.OGG` + `AudioEncoder.OPUS` может быть недоступен или выдавать сбой. В `VoiceRecorder.kt` всегда должен присутствовать fallback на `OutputFormat.MPEG_4` + `AudioEncoder.AAC` (44.1 kHz, 64 kbps).
* **Минимальная длительность:** Запись короче 600 мс нельзя передавать в `MediaRecorder.stop()` — это вызывает аппаратный краш `-1007`. Такие короткие нажатия нужно отменять через `cancel()`.
* **MIME-тип загрузки:** При загрузке на VK API MIME-тип выставляется динамически: `"audio/ogg"` для `.ogg` и `"audio/mp4"` для `.m4a`.

---

## 5. 🧱 Межмодульные Smart Casts в Kotlin (`core:model` ⇄ `feature:*`)

* **Причина:** Компилятор Kotlin не выполняет smart cast для `val` свойств моделей, объявленных в другом Gradle-модуле (`public API property declared in different module`), так как они теоретически могут иметь кастомный геттер в runtime.
* **Правило:** В Compose экранах всегда сохраняйте поля в локальные переменные перед проверкой на `null`:
  ```kotlin
  val status = user.status
  val birthday = user.birthday
  val about = user.about
  val site = user.site
  if (!status.isNullOrBlank()) {
      Text(text = status)
  }
  ```

---

## 6. 🗄️ Безопасность схемы базы данных Room

* При добавлении новых полей в доменные модели (например, `VkUser`) всегда задавайте значения по умолчанию (`= null`, `= false`, `= true`).
* Это гарантирует, что мапперы сущностей (`VkUserEntity.asExternalModel()`) и существующие тесты не сломаются и не потребуют миграций Room, если новые поля пока не сохраняются в локальную БД.

# Jyoti — Android voice assistant

A modular, Kotlin + Jetpack Compose Android app with a neon-styled home screen, on-device
voice input/output, Hindi/Hinglish support, and two selectable voice personalities
(Breezy / Firm). Built so new capabilities (phone controls, WhatsApp workflows, an
incoming-call assistant) can be added as new modules later, without restructuring
what already exists.

## What's in this first drop

- A complete, working **home screen**: neon gradient background, pulsing mic orb that
  reflects state (idle / listening / thinking / speaking), live conversation bubbles,
  personality quick-switch, and a settings entry point.
- A clean **settings screen**: personality choice, language choice, and a "Coming soon"
  section previewing future modules.
- Voice **input** via Android's built-in `SpeechRecognizer` and voice **output** via
  Android's built-in `TextToSpeech` — both on-device APIs, no audio sent to any third
  party by the app itself.
- An **AI conversation layer** that talks only to your own backend, never to an AI
  provider directly (see "Security model" below).
- A **module layout** designed so `feature:home`, `feature:settings`, and a future
  `feature:phonecontrol` / `feature:whatsapp` / `feature:callassistant` can each be
  built, tested, and shipped independently.

## Project structure

```
Jyoti/
├── app/                     # Thin shell: DI wiring, nav host, manifest, launcher activity
├── core/
│   ├── common/               # Shared result types, enums, constants — no Android UI deps
│   ├── designsystem/         # Neon theme, colors, typography, reusable Compose components
│   ├── network/               # Retrofit client to YOUR backend proxy (no provider keys)
│   └── voice/                 # SpeechRecognizer + TextToSpeech wrappers, personality mapping
├── feature/
│   ├── assistant/             # Conversation repository — the only class that calls ChatApi
│   ├── home/                  # Home screen (UI + ViewModel)
│   └── settings/              # Settings screen (UI + ViewModel + DataStore preferences)
└── server-example/           # Minimal reference backend proxy (Node/Express)
```

Each `core:*` module has no dependency on any `feature:*` module, and each `feature:*`
module only depends on the `core:*` modules it actually needs. `app` just wires
everything together via Hilt and Navigation Compose. Adding a new feature later means:

1. Create `feature/<name>` with its own `build.gradle.kts` depending on the relevant
   `core:*` modules.
2. Add a `<name>Screen(navController)` extension function + a Hilt `@HiltViewModel`,
   following the pattern in `feature/home` or `feature/settings`.
3. Register the route in `app/.../navigation/JyotiNavHost.kt` — one line.

Nothing in `core:*` or in other feature modules needs to change.

## Security model — keeping AI keys out of the client

**The Android app never holds an AI provider secret key.** Instead:

```
Android app  --HTTPS-->  your backend proxy  --HTTPS-->  AI provider (holds the real key)
```

- `core:network` only knows about `ChatApi`, which calls **your** backend at
  `BuildConfig.API_BASE_URL` (a plain URL, not a secret, set via `local.properties`).
- A minimal, clearly-labeled reference proxy lives in `/server-example`. It shows where
  the real provider key belongs (`AI_PROVIDER_API_KEY`, server-side environment variable
  only) and how requests are authenticated before being forwarded.
- If/when the app needs per-user auth, that should be a short-lived token from your own
  auth system (e.g. a Firebase Auth ID token), attached at request time — never a static
  value baked into the APK, since anything embedded in a built app can be extracted.

This is also why voice recognition and speech synthesis use Android's **built-in**
`SpeechRecognizer` / `TextToSpeech` engines: no separate cloud STT/TTS key is needed for
this first drop, and no raw audio leaves the device through the app itself.

## Hindi / Hinglish support

- `HomeUiState.languageTag` drives both the recognizer's locale (`hi-IN` for Hindi/Hinglish
  speech, switchable to `en-IN`) and the reply language requested from the backend
  (`hi`, `hi-en` for Hinglish, or `en` — see `core/common/Constants.kt`).
- The backend's system prompt (see `server-example/index.js`) is what actually instructs
  the model to reply in Devanagari Hindi vs. code-mixed Hinglish vs. English, so this is
  easy to refine without touching the app.

## Voice personalities

`core/voice/VoicePersonality.kt` defines `Personality.BREEZY` and `Personality.FIRM`,
each mapping to TTS pitch/rate values and a personality id sent to the backend so the
model's *tone*, not just the *voice's pitch*, matches the chosen personality. Adding a
third personality is one new enum entry plus (optionally) a new backend system prompt.

## Permissions and future modules — what's intentionally NOT here yet

Only `RECORD_AUDIO` and `INTERNET` are requested in this drop. The roadmap features
listed in Settings ("Coming soon") are deliberately unimplemented placeholders, and when
they're built they will only ever use documented, publicly-supported Android APIs:

- **Phone controls** — via Android's Telecom / Intent APIs, with each permission
  requested at the point of use and explained to the user.
- **WhatsApp workflows** — via the official WhatsApp Business Platform API from the
  backend, or Android's standard Share/Intent sheet from the client. No unofficial
  automation, scraping, or Accessibility Service abuse to puppet WhatsApp's UI.
- **Incoming-call assistant** — via Android's official `CallScreeningService` /
  `ANSWER_PHONE_CALLS` APIs, not by working around platform restrictions.

## Getting started

1. Install Android Studio (Koala or newer) with SDK 34.
2. `cp local.properties.example local.properties` and fill in your `sdk.dir` and
   `JYOTI_API_BASE_URL` (point it at a running instance of `/server-example`, or your
   own backend once you build one).
3. Optionally run the reference proxy: see `server-example/README.md`.
4. Open the project root in Android Studio and run the `app` configuration on a
   device/emulator running API 26+.

## Responsiveness

The home and settings screens use `Modifier.fillMaxSize()` / `weight()` layouts, system
bar insets, and `widthIn(max = ...)` constraints on conversation bubbles rather than fixed
pixel dimensions, so they adapt across phone screen sizes without extra work. A dedicated
tablet/large-screen layout can be added later as its own composable variant without
affecting the phone layout.

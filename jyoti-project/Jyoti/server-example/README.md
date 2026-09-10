# Jyoti backend proxy (reference implementation)

This is a **minimal, illustrative** server, not a production-ready deployment. Its only job
is to demonstrate the correct security boundary for the app:

```
Android app  -->  YOUR backend (this folder)  -->  AI provider (OpenAI / Anthropic / etc.)
   (no secret key here)     (holds the real secret key)
```

## Why the app can't call the AI provider directly

Any key embedded in an Android APK — even obfuscated, even in native code, even behind
"encryption" that must decrypt on-device — can be extracted by someone with the APK.
There is no safe way to ship a real secret key in a client app. The only reliable fix is:
put the key on a server you control, and have the app call that server instead.

## Running locally

```bash
cd server-example
cp .env.example .env   # fill in AI_PROVIDER_API_KEY and APP_SHARED_SECRET
npm install
npm start
```

Then point `local.properties` in the Android project at wherever you deploy this
(`JYOTI_API_BASE_URL`), matching `APP_SHARED_SECRET` on both sides for now.

## Before shipping to real users

- Replace the shared-secret header with real per-user auth (e.g. Firebase Auth ID tokens)
  so access can be scoped and revoked per install, not just one static string.
- Add proper logging/monitoring, request size limits, and provider-side rate limits.
- Terminate TLS in front of this service (e.g. via your cloud provider's load balancer).
- Add retry/backoff and timeout handling around the AI provider call.
- Consider streaming responses for lower perceived latency.

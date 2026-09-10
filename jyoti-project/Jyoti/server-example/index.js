// Minimal reference proxy for Jyoti.
//
// Purpose: the Android app must NEVER embed an AI provider secret key. Instead it calls
// this server, which holds the real key in an environment variable and forwards the
// request to the AI provider. Swap `callAiProvider` for your chosen provider's SDK/API.
//
// Run: cp .env.example .env  (fill in real values)  ->  npm install  ->  npm start

require('dotenv').config();
const express = require('express');
const rateLimit = require('express-rate-limit');

const app = express();
app.use(express.json({ limit: '256kb' }));

// Basic abuse protection. Tune for your real traffic.
app.use(rateLimit({ windowMs: 60 * 1000, max: 30 }));

function requireAppAuth(req, res, next) {
  const provided = req.header('X-App-Auth');
  if (!provided || provided !== process.env.APP_SHARED_SECRET) {
    return res.status(401).json({ error: 'Unauthorized' });
  }
  next();
}

const PERSONALITY_SYSTEM_PROMPTS = {
  breezy: 'You are Jyoti, a warm, upbeat, lightly playful voice assistant. Keep replies short and natural for speech.',
  firm: 'You are Jyoti, a direct, efficient, no-nonsense voice assistant. Keep replies short and natural for speech.',
};

const LANGUAGE_INSTRUCTIONS = {
  hi: 'Respond in Hindi (Devanagari script).',
  'hi-en': 'Respond in natural Hinglish (Hindi-English code-mixed, Latin script), the way young urban Indians speak.',
  en: 'Respond in English.',
};

app.post('/v1/chat', requireAppAuth, async (req, res) => {
  try {
    const { messages, language, personality } = req.body;

    if (!Array.isArray(messages) || messages.length === 0) {
      return res.status(400).json({ error: 'messages[] is required' });
    }

    const systemPrompt = [
      PERSONALITY_SYSTEM_PROMPTS[personality] || PERSONALITY_SYSTEM_PROMPTS.breezy,
      LANGUAGE_INSTRUCTIONS[language] || LANGUAGE_INSTRUCTIONS['hi-en'],
    ].join(' ');

    const reply = await callAiProvider(systemPrompt, messages);

    res.json({ reply, language: language || 'hi-en' });
  } catch (err) {
    console.error(err);
    res.status(502).json({ error: 'Upstream AI provider error' });
  }
});

// Replace this with a real call to your chosen LLM provider's API, using
// process.env.AI_PROVIDER_API_KEY. This stub only exists so the file runs standalone.
async function callAiProvider(systemPrompt, messages) {
  const lastUserMessage = [...messages].reverse().find((m) => m.role === 'user');
  return `(demo) I heard: "${lastUserMessage ? lastUserMessage.content : ''}"`;
}

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`Jyoti proxy listening on :${port}`));

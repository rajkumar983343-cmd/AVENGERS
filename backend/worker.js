const SYSTEM_PROMPT =
  "You are JARVIS, a refined, highly capable personal AI assistant. " +
  "You are calm, precise and subtly witty, and you address the user respectfully. " +
  "Your replies are spoken aloud, so keep them concise and conversational. " +
  "Do not use markdown, bullet symbols, or emoji. " +
  "Use earlier messages in the conversation to understand follow-up questions. " +
  "If you are unsure, say so honestly.";

export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return json({ error: "Use POST." }, 405);
    }

    // Optional shared password between the app and this worker.
    if (env.APP_TOKEN && request.headers.get("X-App-Token") !== env.APP_TOKEN) {
      return json({ error: "Unauthorized." }, 401);
    }

    // The Gemini key comes from a Cloudflare secret, never from code.
    if (!env.GEMINI_API_KEY) {
      return json({ error: "Server is missing GEMINI_API_KEY." }, 500);
    }

    let body;
    try {
      body = await request.json();
    } catch {
      return json({ error: "Invalid JSON." }, 400);
    }

    const incoming = Array.isArray(body.messages) ? body.messages.slice(-24) : [];

    // Gemini needs alternating roles that start with "user". Merge repeats.
    const contents = [];
    for (const m of incoming) {
      const role = m.role === "model" ? "model" : "user";
      const text = String(m.text ?? "").slice(0, 8000);
      if (!text.trim()) continue;
      const last = contents[contents.length - 1];
      if (last && last.role === role) {
        last.parts[0].text += "\n" + text;
      } else {
        contents.push({ role, parts: [{ text }] });
      }
    }
    while (contents.length && contents[0].role !== "user") contents.shift();
    if (!contents.length || contents[contents.length - 1].role !== "user") {
      return json({ error: "No user message found." }, 400);
    }

    const model = env.GEMINI_MODEL || "gemini-3.6-flash";
    const url =
      "https://generativelanguage.googleapis.com/v1beta/models/" +
      encodeURIComponent(model) +
      ":generateContent";

    let resp;
    try {
      resp = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": env.GEMINI_API_KEY,
        },
        body: JSON.stringify({
          systemInstruction: { parts: [{ text: SYSTEM_PROMPT }] },
          contents,
        }),
      });
    } catch {
      return json({ error: "Could not reach Gemini." }, 502);
    }

    const data = await resp.json().catch(() => ({}));
    if (!resp.ok) {
      const message =
        (data && data.error && data.error.message) || "Gemini error " + resp.status;
      return json({ error: message }, 502);
    }

    const parts =
      (data.candidates &&
        data.candidates[0] &&
        data.candidates[0].content &&
        data.candidates[0].content.parts) ||
      [];
    const reply = parts
      .filter((p) => !p.thought)
      .map((p) => p.text || "")
      .join("")
      .trim();

    if (!reply) {
      return json(
        { error: "Gemini returned no text (the reply may have been blocked)." },
        502
      );
    }
    return json({ reply });
  },
};

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

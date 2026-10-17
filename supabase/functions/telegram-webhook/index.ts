import { db, json, safeEqual } from "../_shared/util.ts";

const TOKEN = Deno.env.get("TELEGRAM_BOT_TOKEN")!;
const SECRET = Deno.env.get("TELEGRAM_WEBHOOK_SECRET")!;

async function tg(method: string, payload: Record<string, unknown>) {
  await fetch(`https://api.telegram.org/bot${TOKEN}/${method}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  }).catch(() => {});
}

const say = (chat: number, text: string, extra: Record<string, unknown> = {}) =>
  tg("sendMessage", { chat_id: chat, text, ...extra });

function normalizePhone(raw: string): string | null {
  let d = raw.replace(/\D/g, "");
  if (d.startsWith("0098")) d = d.slice(4);
  else if (d.startsWith("98")) d = d.slice(2);
  else if (d.startsWith("0")) d = d.slice(1);
  if (!/^9\d{9}$/.test(d)) return null;
  return "0" + d;
}

Deno.serve(async (req) => {
  // فقط تلگرام (با secret_token) مجاز است
  const got = req.headers.get("x-telegram-bot-api-secret-token") ?? "";
  if (!SECRET || !safeEqual(got, SECRET)) return new Response("forbidden", { status: 403 });

  let update: any;
  try { update = await req.json(); } catch { return json({ ok: true }); }
  const msg = update?.message;
  if (!msg || msg.chat?.type !== "private") return json({ ok: true });

  const chat: number = msg.chat.id;
  const fromId: number = msg.from?.id;
  const nowIso = new Date().toISOString();

  // ۱) /start <code>
  if (typeof msg.text === "string" && msg.text.startsWith("/start")) {
    const code = msg.text.split(/\s+/)[1] ?? "";
    if (!/^[A-Z0-9]{16}$/.test(code)) {
      await say(chat, "برای ورود، از داخل برنامهٔ رایدو دکمهٔ «ورود با تلگرام» را بزنید.");
      return json({ ok: true });
    }
    const { data: ls } = await db.from("login_sessions").select("code,status,expires_at").eq("code", code).maybeSingle();
    if (!ls || ls.expires_at < nowIso || (ls.status !== "pending" && ls.status !== "awaiting_contact")) {
      await say(chat, "این درخواست ورود منقضی شده. دوباره از داخل برنامه اقدام کنید.");
      return json({ ok: true });
    }
    await db.from("login_sessions")
      .update({ telegram_chat_id: chat, telegram_user_id: fromId, status: "awaiting_contact" })
      .eq("code", code);
    await say(chat, "برای تأیید شمارهٔ موبایل، دکمهٔ زیر را بزنید 👇", {
      reply_markup: {
        keyboard: [[{ text: "📱 تأیید شمارهٔ من", request_contact: true }]],
        resize_keyboard: true,
        one_time_keyboard: true,
      },
    });
    return json({ ok: true });
  }

  // ۲) شمارهٔ تماس
  if (msg.contact) {
    // باید شمارهٔ خود فرستنده باشد (نه شمارهٔ دیگران)
    if (msg.contact.user_id !== fromId) {
      await say(chat, "فقط می‌توانید شمارهٔ خودتان را تأیید کنید.");
      return json({ ok: true });
    }
    const phone = normalizePhone(String(msg.contact.phone_number ?? ""));
    if (!phone) {
      await say(chat, "فعلاً فقط شمارهٔ موبایل ایران پشتیبانی می‌شود.", { reply_markup: { remove_keyboard: true } });
      return json({ ok: true });
    }
    const { data: ls } = await db.from("login_sessions").select("code")
      .eq("telegram_user_id", fromId).eq("status", "awaiting_contact").gt("expires_at", nowIso)
      .order("created_at", { ascending: false }).limit(1).maybeSingle();
    if (!ls) {
      await say(chat, "درخواست ورود فعالی پیدا نشد. از داخل برنامه دوباره شروع کنید.", { reply_markup: { remove_keyboard: true } });
      return json({ ok: true });
    }
    // شماره یونیک است؛ کاربر موجود دوباره استفاده می‌شود
    const { data: existing } = await db.from("users").select("id").eq("phone", phone).maybeSingle();
    let userId = existing?.id as string | undefined;
    if (userId) {
      await db.from("users").update({ telegram_id: fromId }).eq("id", userId);
    } else {
      const { data: created, error } = await db.from("users").insert({ phone, telegram_id: fromId }).select("id").single();
      if (error || !created) {
        await say(chat, "خطای سرور. چند لحظه بعد دوباره امتحان کنید.");
        return json({ ok: true });
      }
      userId = created.id;
    }
    await db.from("login_sessions").update({ user_id: userId, status: "verified" }).eq("code", ls.code);
    await say(chat, "✅ شمارهٔ شما تأیید شد. به برنامهٔ رایدو برگردید.", { reply_markup: { remove_keyboard: true } });
    return json({ ok: true });
  }

  await say(chat, "برای ورود از داخل برنامهٔ رایدو اقدام کنید.");
  return json({ ok: true });
});

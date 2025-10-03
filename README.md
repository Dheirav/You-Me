# ❤️ You&Me – A Private App for Two

**You&Me** is a private emotional space for two people in a relationship. Built to strengthen connection, understanding, and love through meaningful features that go beyond chat—supporting real-time emotions, reflection, scheduling, and healing.

---

## ✨ MVP Highlights

- 🔐 Secure 1:1 pairing using Couple Codes
- 💬 Real-time messaging with invisible message option
- 😊 Mood sharing system
- 💭 Thought Bubbles with delayed reveals
- ⏳ Reassurance message scheduler
- 🔄 Misunderstanding logger and emotion analytics
- 🔒 Full privacy control with app lock
- 📅 Shared calendar & 🧠 AI Companion (future scope)

---

## 🚀 Tech Stack

- **Frontend**: Android Studio, Kotlin (Jetpack Compose)
- **Backend**: Firebase Firestore (NoSQL), Firebase Auth, Firebase Functions (optional)
- **Storage**: Firebase Cloud Storage
- **Notifications**: Firebase Cloud Messaging (FCM)
- **Analytics**: Firebase Analytics / custom Firestore logic

---

## 🌟 Features

### 🔐 Couple Code System
- Generate or enter a **6-digit code** to securely pair with your partner.
- Ensures that **only one other person** can connect with you.
- All app data is scoped to a shared `coupleId`.

### 💬 Real-Time Messaging
- Fast, low-latency chatting system using Firebase Firestore.
- Timestamped messages with sender identity.
- Optionally send **"invisible" messages** that get revealed on interaction.

### 😊 Mood Sharing
- Choose from preset moods (Happy, Sad, Angry, Anxious, Loved, etc.).
- Share instantly with partner—each mood update is real-time.
- Useful for days when words are hard.

### 💭 Thought Bubble (Delayed Reveal Notes)
- Add emotional or sensitive thoughts.
- Partner gets notified but must **tap to reveal** when ready.
- Great for healing from misunderstandings or building trust.

### ⏳ Reassurance Scheduler
- Pre-schedule loving or encouraging messages:
    - “You’re doing amazing.”
    - “You are loved.”
    - “I believe in you.”
- Supports:
    - One-time or **recurring messages**
    - Scheduled **reveal time**
    - Optional animation or “gift-wrap” effects

### 📅 Shared Calendar
- Create and share events (trips, anniversaries, exams, etc.).
- View partner's additions with countdown timers.
- Receive push notifications for upcoming events.

### 🔄 Misunderstanding Logger
- Log a conflict with:
    - Timestamp
    - Your thoughts and feelings
    - Optional partner feedback
- Track which ones have been resolved.
- Analytics to show how your communication is improving.

### 🧠 Emotion Analytics Dashboard
- Mood trends over weeks/months.
- Average resolution time for misunderstandings.
- Most common reassurance messages.
- **Private to the couple only.**

### 🔒 App Lock & Privacy Mode
- PIN, password, or biometric app locking.
- Privacy Mode: Hides text previews, emoji reactions, and sensitive thoughts when locked.

### 🕰️ Relationship Timeline *(Auto-generated)*
- Automatically creates a timeline of milestones:
    - First linked date
    - Messages with special tags (e.g., first "I love you")
    - Shared calendar events
    - Custom entries

### ✅ Bucket List for Two
- Add dreams and goals together.
- Check them off with:
    - Date
    - Proof (photo, audio, etc.)
- Keeps the relationship fun and forward-looking.

### 💤 Sleep Together Mode
- Soft background music with animated heartbeats.
- Shows each other’s **online/offline** sleep status.
- Tied to personal schedules (optional).

### 🤖 Relationship Bot (Fun + AI)
- AI bot that speaks like your partner.
- Pre-trained with favorite phrases or memories.
- Say things like:
    - “I miss you. Eat something.”
    - “I love you, always.”

---

## 🛠️ Setup & Development

1. **Clone this repo**
   ~~~bash
   git clone https://github.com/yourusername/youandme.git
   cd youandme
   ~~~
2. **Open in Android Studio**
3. Connect to your Firebase Project:
    - Firebase Authentication (Email/Phone)
    - Firebase Firestore (couple data, chat, mood, etc.)
    - Firebase Storage (optional images)
4. Setup environment variables (`google-services.json`)
5. Start developing!

---

## 🧪 Testing & Deployment

- Use **emulators** or **two physical devices** to test the couple connection flow.
- Manual QA checklist for:
    - Pairing flow
    - Real-time message sync
    - Mood state sync
    - Privacy/Lock behavior
- Deploy via internal testing → Google Play Console (Closed Alpha)

---

## 🤝 Built With Love

This app is a personal project to heal, connect, and build trust between two people. Every feature was designed with emotional intention, not just functionality.

If you want to contribute or need help building something like this, feel free to reach out.

---

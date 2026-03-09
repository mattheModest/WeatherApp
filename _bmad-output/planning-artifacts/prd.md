---
stepsCompleted: ['step-01-init', 'step-02-discovery', 'step-02b-vision', 'step-02c-executive-summary', 'step-03-success', 'step-04-journeys', 'step-05-domain', 'step-e-03-edit']
inputDocuments: ['_bmad-output/brainstorming/brainstorming-session-2026-03-08-1200.md']
workflowType: 'prd'
lastEdited: '2026-03-08'
editHistory:
  - date: '2026-03-08'
    changes: 'Added Functional Requirements (18 FRs), Non-Functional Requirements (5 NFRs), and Platform Requirements sections — all critical and warning validation findings addressed'
briefCount: 0
researchCount: 0
brainstormingCount: 1
projectDocsCount: 0
classification:
  projectType: mobile_app
  domain: general
  complexity: low-medium
  projectContext: greenfield
---

# Product Requirements Document - WeatherApp

**Author:** Lafayette
**Date:** 2026-03-08

## Executive Summary

WeatherApp is an Android weather application built on a single core insight: people don't want weather data, they want anxiety resolved. The primary job hired for is "will today surprise me badly?" — answered in under 2 seconds, without the user doing any mental work. The app achieves this through calendar-native intelligence: it reads the user's calendar silently, overlays weather relevance only where it matters, and shifts the widget state proactively as events approach. The user's only job is to glance.

**Target users:** Android users who check weather compulsively for reassurance rather than information — commuters protecting a departure window, athletes protecting a morning run, anyone protecting a specific plan. The product serves the general consumer population with no regulated domain requirements.

**Business model:** Free tier delivering genuine value (clothing-language translation, contextual bring list, mood line, confirmation-first alerts). Premium tier at $7.99/year unlocking calendar integration and proactive event intelligence. Break-even at ~40 paying subscribers given ~$23/month infrastructure costs (Open-Meteo $10 + Cloudflare Workers $5 + Google Play amortized $8/month). Built and operated by a solo developer; success is defined as genuine user enjoyment and self-sustaining infrastructure costs, not growth metrics.

### What Makes This Special

Every weather app starts from the same assumption: show data, let users map it to their lives. WeatherApp inverts this. Weather is the input; the user's life — their calendar events, their commute, their Saturday BBQ — is the interface.

The "how did it know?!" moment: the widget shifts to "Your BBQ starts in 2h. You're clear." before the user thinks to check. No input required. This is delivered through Android's CalendarContract API (READ_CALENDAR permission), granted once during 60-second onboarding, never asked for again.

Three stolen patterns that define the experience: Calm's positioning (premium = peace of mind, not more features), Superhuman's UX standard (speed + the feeling it already did the thinking), Fantastical's mechanic (one magic interaction that makes everything click). Combined with Dark Sky's alert philosophy — earn every interrupt, or don't interrupt at all.

Key design laws non-negotiable in v1:
- Zero ongoing effort — the app reads, changes, and speaks silently
- Adaptive ink — the UI shrinks when there's nothing to say; silence is the message
- Verdict before data — the conclusion is always the primary surface
- Confirmation-first alerts — proactively confirm good news; escalate only on change

### Project Classification

| Field | Value |
|---|---|
| **Project Type** | Mobile App (Android-first, Kotlin/Jetpack Compose) |
| **Domain** | General / Consumer App |
| **Complexity** | Low–Medium |
| **Project Context** | Greenfield |
| **Distribution** | Google Play ($25 one-time registration) |
| **Infrastructure** | Open-Meteo + Cloudflare Workers, ~$23/month |

## Success Criteria

### User Success

- **The 2-second verdict:** User opens widget, reads the verdict, closes. No mental mapping required. No data to interpret.
- **The trust loop:** After 7 days, user stops compulsively checking weather elsewhere. The widget is their only source. Trust is demonstrated by silence — a near-empty widget on a clear day is accepted as accurate, not doubted.
- **The "how did it know?" moment:** On first calendar-aware shift ("Your BBQ starts in 2h. You're clear."), user experiences genuine surprise and delight. This is the moment that converts a free user to a premium advocate.
- **Zero effort sustained:** User has not been required to input anything after the 60-second onboarding. All intelligence is derived silently from granted calendar access.
- **Confirmation-first resonates:** User reports feeling "looked after" rather than "notified at." No alert is dismissed as irrelevant within the first 30 days.

### Business Success

- **Break-even:** 40 paying premium subscribers ($7.99/year) covering ~$23/month infrastructure. Target: achieved within 6 months of launch.
- **Self-sustaining:** Monthly infrastructure costs covered by subscription revenue without requiring new user acquisition every month. Recurring subscribers, not one-time buyers.
- **Genuine enjoyment signal:** Google Play rating ≥ 4.5 stars with reviews citing specific moments of delight (not just "works as expected").
- **Solo dev sustainability:** Lafayette can maintain and improve the app in evenings/weekends without burnout — feature scope and operational overhead match a one-person operation.

### Technical Success

- **Onboarding:** Calendar permission granted → working widget in ≤ 60 seconds, zero additional prompts.
- **Widget reliability:** Widget state reflects current forecast within 30 minutes at all times. No stale data displayed without a staleness signal.
- **Alert precision:** Change-triggered alerts fire on genuine material forecast changes only. Zero false positives in first 30 days of production.
- **Battery/performance:** WorkManager background refresh does not appear in Android battery usage complaints. Calendar sync adds no perceptible drain.
- **API cost ceiling:** Open-Meteo call volume stays within budget at 1,000 MAU through location-cluster caching.

### Measurable Outcomes — 6-Month Targets

| Metric | Target | Notes |
|---|---|---|
| Premium subscribers | 40 | Break-even floor |
| Google Play rating | ≥ 4.5 stars | |
| Day-7 retention | ≥ 40% | Conservative floor; well-executed utility apps can reach 60%+ |
| Onboarding completion rate | ≥ 80% | Calendar permission granted |
| Alert dismissal rate (within 3 sec) | < 20% | 3-second window filters reflex swipes; meaningful irrelevance signal |
| Monthly infrastructure cost | ≤ $23 | |

## Product Scope

### MVP — Minimum Viable Product

Two surfaces: widget + config screen.

**Free tier:**
- Widget-as-Signal (state changes, no push interruptions)
- Clothing-language translation (never raw temperature)
- Contextual bring list (umbrella/sunscreen only when warranted)
- Best window brief ("Best time outside: 11am–2pm")
- Mood line + shareable card
- "You're good. Go live your day." all-clear state
- Verdict-first, hourly data one tap behind
- Confirmation-first alerts
- 3-day max forecast, no maps, no hourly default
- One-permission onboarding (READ_CALENDAR → location inferred), ≤ 60 seconds

**Premium ($7.99/year):**
- Calendar integration via CalendarContract
- Proactive event shift (widget shifts before you check)
- Change-triggered alerts (fires on forecast change, not schedule)
- Stakes-scaled alert windows (inferred from event duration + title keywords)
- Silent travel pre-load (non-home calendar event locations)

### Growth Features (Post-MVP)

- Health signals: UV index, AQI, pollen (opt-in, per signal type)
- Forecast staleness indicator ("Updated 12 min ago")
- Silent alert sensitivity calibration (learns over time what you act on)
- Natural language alerts ("Warn me if rain threatens any outdoor plan this week")

### Vision (Future)

- iOS port — once Android proves the model
- Android Auto / Wear OS widget surfaces
- Historical personal pattern grounding ("same conditions as last Tuesday when you wore your light jacket")

## User Journeys

### Journey 1: Amara — The First Week (Free Tier, Happy Path)

**Who she is:** Amara is a 29-year-old teacher who checks weather every morning out of habit, usually while making coffee. She's had Dark Sky, Apple Weather, and three other weather apps over the past two years. None stuck. She downloads WeatherApp because a colleague shared the mood card ("Nicer than it looks. Get outside if you can.") on a WhatsApp group.

**Opening scene:** She opens the app for the first time at 7:12am. One screen. A single permission request — calendar access — with a plain-language explanation: "We use your calendar to tell you when weather matters to your plans." She grants it. The widget appears on her home screen 40 seconds later.

**Rising action:** Day one: "You're good. Go live your day." She's skeptical. Day two, same. Day three: "Bring an umbrella · Rain 4–6pm." She almost ignores it out of habit, then pauses — she has a walk home from school at 4:30. She brings the umbrella. It rains at 4:45.

**Climax:** Day five, she realizes she hasn't opened any other weather app all week. The widget just handled it. She opens the app deliberately — not because she needs to, but out of curiosity. She taps through to the hourly view. She didn't need it. She closes it.

**Resolution:** By day 14, the widget is her only weather source. She has never typed anything, adjusted any setting, or thought about the app.

*Requirements revealed: widget reliability · clothing-language translation · bring list accuracy · silent operation · mood line · all-clear state trust.*

---

### Journey 2: James — The BBQ That Changed His Tier (Free → Premium Conversion)

**Who he is:** James is a 35-year-old software engineer and weekend griller. Three weeks on the free tier. He trusts the widget. Saturday morning: his ritual check before deciding whether to fire up the grill.

**Opening scene:** Saturday, 9am. Widget says "You're good. Go live your day." He fires up the grill at noon. Invites six friends. Drives to buy charcoal.

**Rising action:** At 1:30pm, clouds roll in faster than expected. By 2pm, a brief 40-minute shower — low probability at 9am, not flagged. The free tier doesn't know he had "Backyard BBQ" at 12pm. It had no reason to monitor that specific window.

**Climax:** Standing under the awning while rain kills the charcoal, James opens the app and sees the premium upgrade screen for the first time. "Calendar integration: we'd have caught this." He pays $7.99.

**Resolution:** The following Saturday, at 8:47am, before he's even thought about the grill: "Your BBQ (12pm) is clear. Brief clouds pass by 10:30." He texts his friends before getting up. The conversion required no marketing, no push notification, no discount. The gap sold itself.

*Requirements revealed: calendar integration · proactive event shift · premium paywall trigger · change-triggered alerts · stakes-scaled monitoring.*

---

### Journey 3: Elena — The Complex Calendar (Premium, Edge Case)

**Who she is:** Elena is a 41-year-old project manager with a packed calendar: commute events, recurring team lunches, school pickups, a Saturday morning 10km run she's been training for. Upgraded to premium on day one.

**Opening scene:** Tuesday evening. Six calendar events tomorrow. Three are outdoors.

**Rising action:** 6:15am — widget shifts: "Your Run (6:30am) is clear." She runs. 11am — "Your Team Lunch (12:30pm) · light rain possible 12–1pm. Consider indoor seating." She books the restaurant's covered terrace.

**Edge case:** Two events overlap in her calendar: "School Pickup" at 3pm and "Site Visit" also at 3pm, different location. The app detects both, doesn't know which to prioritize. Rather than guessing: "2 outdoor events at 3pm · Light rain · Check both."

**Climax:** She notices the widget being unusually non-specific. She taps through to the app — first time in a week — and sees two conflicting events flagged. She cancels the site visit reschedule from her calendar. The widget updates within 20 minutes: "School Pickup (3pm) · You're clear."

**Resolution:** The edge case was handled transparently. The app didn't guess, didn't fail silently, didn't pick the wrong event. It surfaced the ambiguity and let her resolve it. Her trust increased because the app was honest about its limits.

*Requirements revealed: calendar conflict detection · ambiguity surfacing (don't guess) · widget update latency after calendar change · tap-through to detail.*

---

### Journey 4: Lafayette — The Solo Operator (Admin/Operations)

**Who he is:** Lafayette. He built this. He checks the infrastructure once a week, not daily.

**Opening scene:** Sunday afternoon. Cloudflare dashboard, Open-Meteo usage console. Monthly API calls: 68,000. Under the 72,000 threshold. 47 premium subscribers. Infrastructure covered.

**Rising action:** A 1-star review appears: "Widget stopped updating, shows yesterday's weather." The user has a Chinese-language calendar with non-ASCII characters in event titles. WorkManager refresh ran fine, but the CalendarContract query returned malformed strings and threw an uncaught exception, silently stalling the widget.

**Climax:** Lafayette reproduces the bug with a test calendar, fixes the string sanitization in CalendarContract event title parsing, ships a patch. He replies to the review: "Fixed in v1.0.3 — thank you for the detail. Update and let me know if it's resolved." The reviewer updates to 4 stars.

**Resolution:** One dashboard check, one bug report, one patch. The architecture is simple enough that a single developer can reason about the full system on a Sunday afternoon. Infrastructure cost: $21 this month.

*Requirements revealed: error handling for malformed CalendarContract data · widget failure state visibility · update delivery · solo-maintainable architecture.*

---

### Journey 5: Priya — The Permanent Free User (Word-of-Mouth Driver)

**Who she is:** Priya is a 26-year-old graphic designer. Downloaded WeatherApp eight months ago. Never upgraded. Never will — no recurring outdoor events, doesn't see the need. Also the reason four of her friends downloaded the app.

**Her journey:** The free tier is her complete experience. Clothing language, bring list, mood line, all-clear state. She has never seen a paywall prompt. The premium features exist behind a settings screen she glanced at once. It didn't feel like a locked door — it felt like something for a different kind of user.

**The moment that spread it:** The widget said "Honestly lovely today. Eat lunch outside." She screenshotted it and sent it to three people. Two of those three downloaded it that week.

**What she never experiences:** A nag on the widget. A degraded all-clear state. A push notification telling her what she's missing. The free tier is the product, not a preview of it.

**Resolution:** Priya represents 80% of the install base. She costs ~$0.02/month in API calls. She has never complained, never left a bad review, and has generated more installs than any paid acquisition channel could. She is the business model working as designed.

*Design law reinforced: Free tier must be genuinely complete for non-calendar users. Premium is additive, not restorative. The upgrade path is environmental — never coercive.*

---

### Journey Requirements Summary

| Journey | Key Capabilities Revealed |
|---|---|
| Amara (Free, happy path) | Widget reliability · Clothing language · Bring list · Silent operation · Trust through silence |
| James (Free→Premium conversion) | Calendar integration · Event-specific monitoring · Proactive shift · Paywall trigger |
| Elena (Premium, edge case) | Calendar conflict detection · Ambiguity surfacing · Widget update latency · Tap-through detail |
| Lafayette (Operations) | Error handling · WorkManager reliability · Solo-maintainable stack · Update delivery |
| Priya (Free, non-converter) | Free tier completeness · No nagging · Word-of-mouth mechanics · Low API cost floor |

## Platform Requirements

### Minimum Platform

| Field | Value |
|---|---|
| **Minimum Android API** | 34 (Android 14) |
| **Distribution** | Google Play Store |
| **Language / UI** | Kotlin, Jetpack Compose |

### Device Permissions

| Permission | Purpose | If Denied |
|---|---|---|
| `READ_CALENDAR` | Premium calendar integration — read event titles, times, and locations to provide weather context | App operates in weather-only mode; all free tier features remain available |
| `ACCESS_COARSE_LOCATION` | Weather data retrieval for current position | User prompted to enter home location manually; fine location (`ACCESS_FINE_LOCATION`) is never requested |
| `POST_NOTIFICATIONS` | Change-triggered and confirmation-first alerts | Alert functionality silently disabled; widget-only mode remains fully functional; no in-app re-prompt |

`READ_CALENDAR` is requested once during onboarding with a plain-language explanation: "We use your calendar to tell you when weather matters to your plans." If revoked after grant, the app detects this on next launch and reverts to weather-only mode without crashing.

`POST_NOTIFICATIONS` is requested after first widget render — not during onboarding — to avoid front-loading permission requests.

### Offline Mode

- **Brief outage (< 4 hours):** Widget displays last cached forecast with a staleness signal ("Last updated 2h ago"). No error state shown.
- **Extended outage (≥ 4 hours without refresh):** Widget displays "Weather unavailable — check connection."
- **Cache retention:** 24 hours. WorkManager retries on network restoration without user intervention.
- Widget never displays stale data without a staleness signal.

### Push Notification Strategy

- **Channel:** "Weather Alerts" — single channel, user-dismissable in Android system settings.
- **Android category:** `CATEGORY_RECOMMENDATION` (non-intrusive priority).
- **Free tier (confirmation-first):** Fires at most once per day per location; only when the all-clear is confirmed for a user-relevant time window.
- **Premium (change-triggered):** Fires only on material forecast change for a specific calendar event window; minimum 2-hour lead time before event start.

### Google Play Store Compliance

- **Content rating:** Everyone (no restricted categories).
- **Data safety form declarations:** Calendar data (`READ_CALENDAR`) — accessed, not shared with third parties; approximate location — used for weather retrieval only; no data sold.
- **Prominent disclosure required:** "Calendar data is used locally on-device to provide weather context for your events. It is never uploaded, stored on servers, or shared." This disclosure must appear in the onboarding permission screen and in the Play Store listing.
- App must comply with Google Play's sensitive permissions policy for `READ_CALENDAR` prior to submission.

## Functional Requirements

### Free Tier

**FR-001** — Users can read the day's weather verdict in clothing language (e.g., "light jacket weather") without seeing raw temperature values.

**FR-002** — Users receive a contextual bring list item (umbrella, sunscreen) only when precipitation probability or UV index crosses a threshold warranting that item for that day.

**FR-003** — Users can view the best outdoor window for the day (e.g., "Best time outside: 11am–2pm") derived from hourly forecast data.

**FR-004** — Users see an explicit all-clear state ("You're good. Go live your day.") when no weather action is warranted.

**FR-005** — Users can view a mood line and generate a shareable weather card from the app.

**FR-006** — Users can access hourly forecast detail with one tap from the widget verdict; the hourly view is not the default surface.

**FR-007** — Users receive confirmation-first alerts proactively on good news; the alert system escalates only when a previously confirmed forecast materially changes.

**FR-008** — Users complete onboarding — granting `READ_CALENDAR` permission and seeing a live widget — in a single session requiring no additional prompts after the permission grant.

### Premium Tier

**FR-009** — Premium users: the widget shifts state to reflect a specific upcoming calendar event (e.g., "Your BBQ (12pm) is clear.") before the user opens the app.

**FR-010** — Premium users receive a change-triggered alert when the forecast for a specific calendar event window changes materially after a prior all-clear confirmation.

**FR-011** — Premium users receive alerts with monitoring windows scaled to inferred event importance, derived from event duration and title keyword signals (e.g., "BBQ", "run", "match").

**FR-012** — Premium users: the widget silently pre-loads and displays weather for the location of upcoming non-home calendar events without any user action.

**FR-013** — Premium users: when two or more calendar events with outdoor potential overlap in time, the widget surfaces the conflict explicitly (e.g., "2 outdoor events at 3pm · Light rain · Check both.") rather than selecting one event silently.

### Reliability & Operations

**FR-014** — When the `CalendarContract` query returns malformed or non-ASCII event data, the app sanitizes the input and continues widget operation without stalling or crashing.

**FR-015** — When forecast data cannot be refreshed, the widget displays a staleness signal indicating the age of the last successful update; it never silently displays stale data as current.

**FR-016** — After a user modifies or removes a calendar event, the widget reflects the updated state within 30 minutes.

**FR-017** — The free tier widget surface displays no upgrade prompts, degraded states, or feature previews; the premium upgrade path is accessible only through the in-app settings screen.

**FR-018** — When `READ_CALENDAR` permission is denied or revoked, the app operates in weather-only mode without crashing, preserving all free tier features.

## Non-Functional Requirements

**NFR-001 — Widget Freshness**
The widget shall reflect the current forecast within 30 minutes at all times, as measured by the delta between the last WorkManager refresh timestamp and the time displayed in the widget staleness signal.

**NFR-002 — Onboarding Speed**
The system shall complete the path from first app launch to a live widget in ≤ 60 seconds, as measured from app open to first widget render, requiring no more than one permission prompt during that flow.

**NFR-003 — Alert Precision**
The system shall produce zero false-positive change-triggered alerts in the first 30 days of production, as measured by alerts firing without a verifiable material forecast change (defined as ≥ 20% shift in precipitation probability, or wind speed crossing 25 mph, within a calendar event window).

**NFR-004 — Background Battery Impact**
The WorkManager refresh cycle shall not appear in the Android battery usage breakdown for any user device under normal refresh frequency (every 30 minutes), as measured by absence of WeatherApp in the system battery usage attribution list during standard usage.

**NFR-005 — API Call Budget**
The system shall not exceed 72,000 Open-Meteo API calls per month at 1,000 MAU, enforced through location-cluster caching that groups requests by geographic proximity rather than per-device coordinates, as measured by the Open-Meteo usage dashboard.

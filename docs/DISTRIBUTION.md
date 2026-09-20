# 📦 Hyperreal Journal — Distribution Channels

A research overview of every viable Android distribution channel for **Hyperreal Journal**, an offline-first, privacy-first harm-reduction journaling app.

---

## 1. Google Play Store

| Attribute | Details |
|-----------|---------|
| **Reach** | 3+ billion devices worldwide |
| **Approval** | Manual review (3–7 days typically) |
| **Cost** | $25 one-time registration fee |
| **Minimum SDK** | Any (app targets SDK 26+) |
| **Key concern** | Harm-reduction / drug-related apps face strict content policies. Listing as "Health & Fitness" with clear disclaimers required. |

**Strategy:**  
- Frame as a **personal wellness journal** for tracking substance interactions and harm reduction.  
- Avoid explicit drug-use framing in screenshots and description.  
- Include a clear Terms of Service and Privacy Policy linked from the listing.  
- Consider starting with **Closed Testing** track to gather user feedback before public launch.

**Links:**  
- [Google Play Console](https://play.google.com/console)  
- [Developer Policy Center](https://support.google.com/googleplay/android-developer/answer/9898964)

---

## 2. F-Droid

| Attribute | Details |
|-----------|---------|
| **Reach** | ~1M privacy-conscious users |
| **Approval** | Community review (weeks–months) |
| **Cost** | Free |
| **Requirement** | 100% open-source; no proprietary SDKs or trackers |
| **Key concern** | Any closed-source dependency (e.g., Firebase, Crashlytics) must be removed |

**Strategy:**  
- The app is already offline-first and open-source on GitHub — ideal fit.  
- Submit via [F-Droid Submission](https://gitlab.com/fdroid/fdroiddata/-/merge_requests).  
- Build must be reproducible from source.  

**Links:**  
- [F-Droid Inclusion Policy](https://f-droid.org/docs/Inclusion_Policy/)  
- [fdroiddata repository](https://gitlab.com/fdroid/fdroiddata)

---

## 3. GitHub Releases ✅ (Already Active)

| Attribute | Details |
|-----------|---------|
| **Reach** | Developer community, power users |
| **Approval** | Instant (self-published) |
| **Cost** | Free |
| **Current status** | Active — APKs published at `v0.4.0-beta` |

**Strategy:**  
- Continue publishing signed APKs + SHA-256 checksums with each release.  
- Maintain changelog in `CHANGELOG.md`.  
- Add a download badge to README pointing to latest release.  
- Consider GitHub Actions for automated release builds.

---

## 4. Amazon Appstore

| Attribute | Details |
|-----------|---------|
| **Reach** | Amazon Fire tablets, some Android users (~50M devices) |
| **Approval** | Manual review (1–3 days) |
| **Cost** | Free |
| **Key concern** | Same content sensitivity as Google Play |

**Strategy:**  
- APK compatibility is high — Jetpack Compose apps work well on Fire OS 7+.  
- May need to handle lack of Google Play Services on Fire tablets gracefully.  

**Links:**  
- [Amazon Developer Console](https://developer.amazon.com/apps-and-games)

---

## 5. APKPure / APKMirror

| Attribute | Details |
|-----------|---------|
| **Reach** | ~200M users in markets with limited Play access |
| **Approval** | APKMirror: curated/manual; APKPure: self-submit |
| **Cost** | Free |
| **Key concern** | APKs must be signed; APKMirror only accepts apps already on Play |

**Strategy:**  
- APKPure: direct submission via developer dashboard.  
- APKMirror: submit after Google Play listing goes live.  

---

## 6. IzzyOnDroid (F-Droid compatible repo)

| Attribute | Details |
|-----------|---------|
| **Reach** | ~500K F-Droid users |
| **Approval** | Fast (days) — maintained by a single trusted curator |
| **Cost** | Free |
| **Requirement** | Open-source; privacy-friendly |

**Strategy:**  
- Submit to IzzyOnDroid as a bridge before official F-Droid inclusion.  
- Much faster approval than F-Droid main repo.  

**Links:**  
- [IzzyOnDroid Repository](https://apt.izzysoft.de/fdroid/)

---

## 7. Obtainium

| Attribute | Details |
|-----------|---------|
| **Reach** | Power users tracking GitHub/GitLab releases directly |
| **Approval** | Zero — users point to GitHub Releases directly |
| **Cost** | Free |
| **Requirement** | Just keep GitHub Releases up to date |

**Strategy:**  
- Works automatically from existing GitHub Releases.  
- Document in README: `Add to Obtainium: github.com/merx666/hyperreal-journal`.

---

## Summary & Recommended Priority

| Priority | Channel | Effort | Reach | Timeline |
|----------|---------|--------|-------|----------|
| 1 | **GitHub Releases** | ✅ Done | Dev/power users | Now |
| 2 | **F-Droid / IzzyOnDroid** | Medium | Privacy community | 2–4 weeks |
| 3 | **Google Play (Closed Testing)** | High | Mainstream | 2–6 weeks |
| 4 | **Amazon Appstore** | Low | Amazon ecosystem | 1–2 weeks |
| 5 | **APKPure** | Low | Global markets | 1 week |
| 6 | **Obtainium** | ✅ Auto | Power users | Now |

---

## Legal / Privacy Checklist

- [ ] Privacy Policy hosted at a public URL (required for Play & Amazon)
- [ ] Terms of Service document
- [ ] GDPR-compliant: confirm no personal data leaves device
- [ ] App Store description reviewed for content policy compliance
- [ ] Harm reduction disclaimer visible in app

---

*Last updated: September 2026*

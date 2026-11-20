# AI CAM PRO

Android app for Dream Home Interior Solution.

## Project identity
- Application ID: `com.dreamhome.aicampro`
- Language: English
- Platform: Android 8.0+ (minimum SDK 26)
- Stack: Kotlin, Jetpack Compose, CameraX, Room, DataStore
- Local-first by default. Photos and project data are not uploaded automatically.
- This project is separate from SIGNALX PRO.

## Planned modules

1. **Dashboard** — entry point for all app modules.
2. **Camera Studio** — in-app photo/video capture, photo enhancement and editing.
3. **AI Clothes Changer** — camera/gallery photo input; adult, consent-based virtual outfit replacement; select or upload clothing references; preserve the subject's identity as much as possible. Includes outfit categories such as shirts, T-shirts, trousers, dresses, suits, swimwear and bikinis. This is outfit try-on, not undressing or generating nudity; underwear remains covered. The app must not infer hidden garments.
4. **Background Studio** — keep the original background, choose a built-in background (studio, beach, room, garden, city, etc.), or upload a custom background image. Subject/background compositing should preserve realistic lighting and edges where possible.
5. **Interior AI Studio** — room redesign concepts for flooring, wall finishes, ceilings, kitchens and bedrooms, aiming to preserve room geometry.
6. **Measurement Calculator** — room-wise measurements and estimates for tiles, wastage, putty, paint, cement, sand and other materials.
7. **Material Rate Manager** — editable material rates and historical rate snapshots for quotations.
8. **Quotation Maker** — multi-item estimates, cost/markup calculations and customer-facing PDF exports. Internal costs and profit must not appear in customer PDFs. Default markup target: 20%, with a warning below 10%.
9. **Project Manager** — customer/site records, checklists, progress status and project photos.
10. **Payments & Accounts** — advances, receipts, balances, expenses and project profitability.
11. **Private Gallery / LOCK MY ROOM** — PIN-protected local gallery and user-controlled deletion.

## Privacy and safety requirements
- Camera and gallery access only after user permission.
- Photo editing/virtual try-on only for consenting adults and authorized images.
- No nudity generation, underwear removal, or hidden-underwear inference. Swimwear try-on is an outfit replacement with intimate areas remaining covered.
- No automatic photo uploads. Any cloud AI processing must be explicit, disclosed and user-initiated.
- Keep private gallery content local and protected; do not claim encryption unless implemented and tested.

## Development status
This repository is under active development. A listed module is a plan, not a claim that the feature is implemented. Only mark a feature complete after code is committed and the relevant behavior is built and tested. APK availability depends on a successful build workflow.

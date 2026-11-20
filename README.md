# AI CAM PRO

Android app for Dream Home Interior Solution.

## Project identity
- Application ID: `com.dreamhome.aicampro`
- Language: English
- Platform: Android 8.0+ (minimum SDK 26)
- Stack: Kotlin, Jetpack Compose, CameraX, Room, DataStore
- Default appearance: premium dark black-and-gold; include a light white-and-gold theme and an in-app theme switch.
- Local-first by default. Photos and project data are not uploaded automatically.
- This project is separate from SIGNALX PRO.

## Planned modules

1. **Dashboard** — polished home screen linking to all modules, recent projects, quick actions and summary cards.
2. **Camera Studio** — in-app photo/video capture, photo enhancement and editing.
3. **AI Clothes Changer** — camera/gallery photo input; adult, consent-based virtual outfit replacement; select from a broad ready-made catalogue for men, women and kids (shirts, T-shirts, trousers, jeans, dresses, gowns, suits, traditional wear, sportswear, jackets, swimwear and more) or upload a clothing reference. This is outfit try-on, not undressing or generating nudity; underwear remains covered. The app must not infer hidden garments.
4. **Background Studio** — keep the original background, choose a built-in background (studio, beach, room, garden, city, etc.), or upload a custom background image. Subject/background compositing should preserve realistic lighting and edges where possible.
5. **Interior AI Studio + Interior Design Catalogue** — automatically browsable catalogue of interior design references, filterable by room, style, colour and design element. Cover living rooms, bedrooms, kitchens, bathrooms, dining rooms, balconies, wardrobes and storage, TV units, doors/windows, furniture, lighting, false ceilings, wall panels, paint/wallpaper, tiles/flooring and bathroom finishes. User can take/upload a room photo, select a catalogue design, preview a redesign, combine design elements or upload a reference. Aim to preserve room geometry, doors, windows and fixed layout; label generated visuals as design previews, not guaranteed construction drawings.
6. **AI Photo Studio** — photo enhancement, lighting/colour correction, background tools, product-photo presentation, crop/rotate and collage tools, subject to supported AI services.
7. **Measurement Calculator** — room-wise measurements and estimates for tiles, wastage, putty, paint, cement, sand and other materials.
8. **Material Rate Manager** — editable material rates and historical rate snapshots for quotations.
9. **Quotation Maker** — multi-item estimates, cost/markup calculations and customer-facing PDF exports. Internal costs and profit must not appear in customer PDFs. Default markup target: 20%, with a warning below 10%.
10. **Project Manager** — customer/site records, checklists, progress status, project photos and milestones.
11. **Customer Management / CRM** — customer contacts, site-visit records, follow-up reminders and customer/project history.
12. **Payments & Accounts** — advances, receipts, balances, expenses and project profitability.
13. **Business Reports** — monthly income, expenses, profit, pending payments and project summaries.
14. **Design Collection / My Designs** — saved clothes, room designs, colour combinations, favourites and personal references.
15. **Private Gallery / LOCK MY ROOM** — PIN-protected local gallery and user-controlled deletion.
16. **Backup & Restore** — local export/restore plus optional user-initiated cloud backup. Clearly explain cloud provider, permissions, storage and privacy before enabling it; never silently upload private photos.

## Editing workflow requirements
- Before/after comparison for supported edits.
- Undo/redo and reset controls.
- Save/export the final image only after user action.
- Keep original images unchanged unless the user explicitly chooses to overwrite.
- Show progress and clear errors when AI processing is unavailable or fails.

## Catalogue behavior
- Clothes and interior catalogues should provide ready-made choices without requiring the user to upload every design.
- Catalogue browsing may use an online source/service and must disclose when internet is required.
- Users may upload their own reference images.
- Save selected favourites locally where licensing and storage allow.
- Do not present catalogue reference images as owned designs or guaranteed build specifications. Respect image/content licensing and service terms.

## Privacy and safety requirements
- Camera and gallery access only after user permission.
- Photo editing/virtual try-on only for consenting adults and authorized images.
- No nudity generation, underwear removal, or hidden-underwear inference. Swimwear try-on is an outfit replacement with intimate areas remaining covered.
- No automatic photo uploads. Any cloud AI processing or cloud backup must be explicit, disclosed and user-initiated.
- Keep private gallery content local and protected; do not claim encryption unless implemented and tested.
- Provide app lock and separate private-gallery PIN controls; securely handle PINs and do not store them in plain text.
- Include clear data export and restore controls.

## Development status
This repository is under active development. A listed module is a plan, not a claim that the feature is implemented. Only mark a feature complete after code is committed and the relevant behavior is built and tested. AI catalogues, virtual try-on, background replacement and interior redesign require suitable external or on-device AI services; do not claim these are operational until integrated and tested. APK availability depends on a successful build workflow.

# Essential Key Remapper

Remap the **Nothing Phone (3a) Essential Key** to any action — or open a customisable quick-action panel — without Android Studio.

---

## Features

- **Single action mode** – one press → one action (torch, camera, DND, timer, …)
- **Quick Panel mode** – one press → floating overlay with multiple action tiles
- **Full toggle** – disable remapping to restore the original Essential Key behaviour
- **Background service** – works when the app is closed
- **NothingOS aesthetic** – pure black, monospace, minimal

### Available actions
Flashlight · Glyph Torch · Camera · Do Not Disturb · Sleep/Display Off · Timer · Hotspot · Aeroplane Mode · Battery Saver · Bluetooth · Nothing Modes · QR Scanner

---

## Build & Install (no Android Studio needed)

### Step 1 – Create a GitHub repository
1. Go to [github.com](https://github.com) and sign in (or create a free account)
2. Click **New repository** → name it `EssentialRemap` → **Create repository**

### Step 2 – Upload the files
Upload this entire folder to your repository.  
*Easiest method:* Drag all files into the GitHub web interface.  
Preserve the folder structure exactly as-is.

### Step 3 – Run the build
1. Click the **Actions** tab in your repository
2. You should see **"Build APK"** – click it
3. Click **Run workflow** → **Run workflow**
4. Wait ~5–10 minutes for the build to complete ✅

### Step 4 – Download the APK
1. Open the completed workflow run
2. Under **Artifacts** → click **EssentialRemap-Debug-APK**
3. A `.zip` downloads – extract it to get `app-debug.apk`

### Step 5 – Install on your phone
1. Transfer the APK to your Nothing Phone (AirDrop equivalent: Nearby Share, Google Drive, WhatsApp, USB, etc.)
2. On the phone: **Settings → Apps → Special app access → Install unknown apps**
3. Allow your file manager / browser
4. Tap the APK and install

---

## First-time Setup (in the app)

After installing, open **Essential Key** and grant these three permissions:

| Permission | Why |
|---|---|
| **Accessibility Service** | Intercepts button presses |
| **Display over other apps** | Shows the quick panel overlay |
| **Do Not Disturb access** | Required only for the DND action |

Then:
1. Tap **DETECT** and press your Essential Key → the app auto-identifies its keycode
2. Choose **Single Action** or **Quick Panel** mode
3. Flip the **REMAP BUTTON** toggle → status turns green

---

## Troubleshooting

**Button not detected?**  
→ Make sure the Accessibility Service is enabled, then use the DETECT function.  
→ Nothing Phone 3a uses `KEYCODE_STEM_PRIMARY` (265) — the app tries this automatically.

**Quick Panel not appearing?**  
→ Grant the **Display over other apps** permission.

**DND not toggling?**  
→ Grant **Do Not Disturb access** via the SETUP section.

**App killed in background?**  
→ Go to **Settings → Battery → Battery optimisation** and set Essential Key to **Not optimised**.  
→ Or enable **Allow background activity** for this app.

---

## Technical Notes

The app uses an **AccessibilityService** to intercept hardware key events at the system level. This is the only method available to third-party apps without root. The service runs persistently in the background and is automatically restarted after reboot by Android.

The overlay panel uses `TYPE_APPLICATION_OVERLAY` which requires the `SYSTEM_ALERT_WINDOW` permission granted in Setup.

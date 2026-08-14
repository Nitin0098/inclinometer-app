# Digital Level

A simple Android app that turns your phone into a bubble level / inclinometer,
using only the accelerometer.

## How it works

At rest, the accelerometer measures the reaction force to gravity, giving a
vector `(gx, gy, gz)` in the phone's own coordinate frame. That vector alone
is enough to compute how tilted the phone's flat face is relative to level
(horizontal), without needing the compass:

- **Overall angle** (shown large, in the middle): the angle between the
  phone's flat face and level — `acos(gz / |g|)`. 0° = perfectly flat,
  90° = phone standing on its edge.
- **Left/right** and **front/back**: the two tilt components, so it behaves
  like a real two-axis bubble level, not just one number.

A low-pass filter smooths raw sensor noise so the display doesn't jitter.

## Using it

Lay the phone flat against whatever surface you want to measure (a shelf,
a table, a ramp, the back of a picture frame, etc.), screen or back facing
the surface. The bubble centers and turns green when you're within 0.5° of
level. Tap **Calibrate zero** to zero out the current position (useful if
your case/phone has a slight built-in tilt); **Reset** clears that.

## Getting an installable APK — no local install needed

This repo includes `.github/workflows/build-apk.yml`, which builds a real,
signed debug APK on GitHub's servers and hands it back to you as a
download. This is the easiest path if you don't want to install Android
Studio:

1. Create a free account at [github.com](https://github.com) if you don't
   have one.
2. Create a new repository (public or private, either works) and upload
   the contents of this folder to it — either drag-and-drop everything
   through the "Add file → Upload files" button on the repo page, or, if
   you're comfortable with git:
   ```
   cd DigitalLevel
   git init
   git add .
   git commit -m "Digital level app"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git push -u origin main
   ```
3. Open the **Actions** tab on the repo. The "Build APK" workflow should
   start automatically (if not, click it and press **Run workflow**).
4. Wait for the green checkmark (a few minutes).
5. Click into the finished run, scroll to **Artifacts**, and download
   `DigitalLevel-debug-apk`. It's a zip containing `app-debug.apk`.
6. Get that file onto your phone (email it to yourself, Google Drive,
   USB cable — whatever's easiest), tap it, and allow "install unknown
   apps" when prompted. It installs and runs like any other app.

This produces a **debug-signed APK**, which Android happily installs on
any device without needing the Play Store — it's just not meant for
Play Store *distribution* (that needs a release signature), which doesn't
matter for personal use.

## Building it locally instead

You'll need [Android Studio](https://developer.android.com/studio)
(Koala or later recommended).

1. Unzip the project and open the `DigitalLevel` folder in Android Studio
   as an existing project.
2. Let Gradle sync (Android Studio will offer to generate the Gradle
   wrapper automatically if it's missing — accept it).
3. Connect an Android phone via USB with USB debugging enabled (or use an
   emulator, though emulators don't have a real accelerometer, so a
   physical device is best for this app).
4. Click **Run ▶**, or use **Build → Build Bundle(s)/APK(s) → Build
   APK(s)** to get an installable file without a device attached.

Minimum SDK: Android 7.0 (API 24). No special permissions are required —
motion sensors don't need a runtime permission grant.

## Possible improvements

- Switch to `Sensor.TYPE_GRAVITY` (a fused sensor) on devices that support
  it, for a steadier reading than a raw low-passed accelerometer.
- Add landscape/vertical mode for measuring plumb (wall) angles instead of
  flat surfaces.
- Add a short vibration pulse when it crosses into "level."
- Persist the calibration offset across app restarts.

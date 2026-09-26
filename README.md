# Wallpaper HD 4K

Android wallpaper app (Kotlin) that pulls **live** photos from the [Pixabay API](https://pixabay.com/api/docs/)
across 12 categories (nature, space, animals, abstract, etc.), each paginated — so total browsable
wallpapers comfortably exceed **2,000+**, all HD/4K resolution, with an optional "4K Only" filter.
Pixabay content is free for commercial use under Pixabay's content license.

Features: category browsing, search, infinite scroll, full-screen pinch-zoom preview,
Set Wallpaper (Home / Lock / Both), Download to gallery.

## 1. Get a free Pixabay API key
Sign up at https://pixabay.com/accounts/register/ then grab your key at
https://pixabay.com/api/docs/ (top of the page, after login). Free tier is fine for this app.

## 2. Push this project into your existing repo
```bash
git clone https://github.com/OxClub/wallpaper.git
# copy every file from this project into the cloned "wallpaper" folder, overwriting as needed
cd wallpaper
git add .
git commit -m "Add Wallpaper HD 4K Android app"
git push origin main
```

## 3. Add GitHub Actions secrets
In your repo: **Settings → Secrets and variables → Actions → New repository secret**

| Secret name | Value |
|---|---|
| `PIXABAY_API_KEY` | your Pixabay API key from step 1 |
| `KEYSTORE_BASE64` | base64 of your release keystore (see step 4) |
| `KEYSTORE_PASSWORD` | your keystore password |
| `KEY_ALIAS` | your key alias |
| `KEY_PASSWORD` | your key password |

If you skip the keystore secrets, the debug APK still builds fine; the release AAB/APK will build
unsigned (you'd need to sign it yourself before uploading to Play Console).

## 4. Generate a release keystore (skip if you already have one)
```bash
keytool -genkey -v -keystore release.keystore -alias wallpaper -keyalg RSA -keysize 2048 -validity 10000
base64 -w 0 release.keystore > keystore_base64.txt
```
Paste the contents of `keystore_base64.txt` into the `KEYSTORE_BASE64` secret.
**Keep `release.keystore` somewhere safe outside git** — losing it means you can never update
your Play Store listing again.

## 5. Build
Push to `main` (or run the workflow manually from the **Actions** tab → "Build Wallpaper App" → **Run workflow**).
GitHub Actions will produce:
- `app-debug-apk` — installable debug APK
- `app-release-apk` — signed release APK
- `app-release-aab` — signed **.aab bundle for Play Store upload**

Download them from the finished workflow run's **Artifacts** section.

## 6. Upload to Play Store
Go to Play Console → your app → Production (or Internal testing) → Create new release →
upload the `.aab` file from `app-release-aab`.

## Local development (optional)
Copy `local.properties.example` to `local.properties`, set `sdk.dir` and `pixabay.api.key`,
then open the project in Android Studio (Hedgehog or newer) and let it sync/build normally
(Android Studio will generate the Gradle wrapper for you automatically).

## Project structure
```
app/src/main/java/com/oxclub/wallpaper/
├── model/            Pixabay data models
├── network/          Retrofit API + repository (multi-category pagination)
└── ui/               MainActivity, WallpaperDetailActivity, adapters
```

## Notes
- App icon uses your provided "0X" logo (adaptive icon + legacy fallback icons already generated).
- minSdk 26 (Android 8.0+), targetSdk 34.
- No Gradle wrapper jar is committed (binary files can't be generated in this environment);
  CI provisions Gradle 8.7 directly via `gradle/actions/setup-gradle`. Opening the project in
  Android Studio will auto-generate the wrapper for local builds.

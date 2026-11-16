# Space & AMOLED Wallpapers (Wyrix)

Android wallpaper app (Kotlin). Wallpapers are loaded **live** from free sources and mixed together:

| Source | Key | Used for | Licence / credit shown |
|---|---|---|---|
| [Pixabay](https://pixabay.com/api/docs/) | yes (free) | all search categories | Pixabay Content License, "Photo by X on Pixabay" |
| [Pexels](https://www.pexels.com/api/) | optional (free) | portrait photos, AMOLED/dark/3D, **original full-size files** | Pexels License, "Photo by X on Pexels" |
| [NASA Image and Video Library](https://images.nasa.gov) | no | Space, Galaxy, Nebula, Planets, Moon, Sun, Earth, AMOLED; **original full-size files** | NASA media guidelines, "Image: NASA / center" |
| [NASA SDO](https://sdo.gsfc.nasa.gov/data/) | no | **Live Sun** images (10 wavelengths, refreshed every ~30 min) | "NASA/SDO and the AIA, EVE and HMI science teams" |
| [ESA/Hubble Picture of the Week](https://esahubble.org/images/potw/) | no | Space, Hubble & Webb | CC BY 4.0, "ESA/Hubble & NASA" + link to full credit |
| [ESA/Webb Picture of the Month](https://esawebb.org/images/potm/) | no | Space, Hubble & Webb | CC BY 4.0, "ESA/Webb, NASA & CSA" + link to full credit |
| [ESO Picture of the Week](https://www.eso.org/public/images/potw/) | no | Night Sky | CC BY 4.0, "ESO" + link to full credit |

Categories: Space, Hubble & Webb, Galaxy, Nebula, Planets, Moon, Sun, Earth, Night Sky, AMOLED, Dark, Neon, 3D,
Abstract, Minimal, Favorites.

Features: category browsing, search (Pixabay, Pexels, NASA), infinite scroll, full-screen preview that matches the
final crop, **AMOLED black switch**, **Favorites** (saved on device), Set Wallpaper (Home / Lock / Both),
Download, per-image credit (tap to open the original page) and a Credits dialog (tap the footer).

### How the ESA / ESO / SDO sources work
- ESA/Hubble, ESA/Webb and ESO are read from their official RSS picture feeds (about the latest pictures,
  paged locally). Set/Download tries the bigger `large` file for ESA and falls back to the 1280px `screen` file.
  ESO uses the 1280px file. These feeds cannot be searched by keyword, so they only appear in categories.
- SDO "latest" images are square with a black background, so they are fitted on black instead of cropped.
- CC BY 4.0 needs a visible credit and a note when changed: the credit line is always shown on the wallpaper
  screen and the Credits dialog says images may be cropped or darkened.

### Quality note (please read before writing the Play listing)
Pixabay's API only serves up to ~1280px images, so Pixabay wallpapers are **not 4K**. Pexels and NASA
wallpapers use the original full-resolution file when you tap Set / Download. The old "4K Only" filter was
removed because it did not exist in the UI and could not be honest for Pixabay. ESA and ESO files are 1280px
(ESA may load a larger file). In the listing say "HD" and "full-resolution originals from NASA and Pexels",
not blanket "4K".

### AMOLED mode
Colors at or below 28/255 become pure black and the rest is stretched back to full range, live in the preview
and when setting the wallpaper. The switch starts ON inside the AMOLED category. Change `AMOLED_THRESHOLD`
in `WallpaperDetailActivity.kt` to make it stronger or weaker.

### Adding categories or sources
- New category: add a `Category(...)` to `WallpaperRepository.categories` (one line).
- New source: add a value to `ImageSource`, a fetch function + mapper in `WallpaperRepository`, and a credit
  line in `strings.xml`. Always check the source's license and attribution rules first.

## 1. Get free API keys
- **Pixabay (required):** https://pixabay.com/api/docs/ (key is at the top of the page after login).
- **Pexels (optional):** https://www.pexels.com/api/ . Without it the app still works with Pixabay + NASA.
- **NASA:** no key.

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
| `PEXELS_API_KEY` | your Pexels key (optional, can be left unset) |
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
app/src/main/java/com/wyrix/wallpaper/
├── model/            WallpaperItem + Pixabay/Pexels/NASA response models
├── network/          Retrofit APIs + WallpaperRepository (categories, mixing, credits)
├── data/             FavoritesStore (on-device favorites)
└── ui/               MainActivity, WallpaperDetailActivity, adapters
```

## Notes
- App icon uses your provided "0X" logo (adaptive icon + legacy fallback icons already generated).
- minSdk 26 (Android 8.0+), targetSdk 34.
- No Gradle wrapper jar is committed (binary files can't be generated in this environment);
  CI provisions Gradle 8.7 directly via `gradle/actions/setup-gradle`. Opening the project in
  Android Studio will auto-generate the wrapper for local builds.

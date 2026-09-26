# Wallify — Android Wallpaper App

Kotlin + Jetpack Compose wallpaper app. Images currently come from Pixabay's public API — a near-unlimited catalog, free to use, no storage cost.

(There used to be a second "admin-uploaded library" source backed by Firebase — that's been removed to keep the project simple. See the note at the bottom of `WallpaperRepository.kt` if you want to add your own curated image source back later.)

## Project structure
```
app/src/main/java/com/example/wallpaperapp/
  data/model/        Wallpaper, Category, Pixabay DTOs
  data/remote/        Retrofit (Pixabay)
  data/local/          Room DB — local favorites, works offline
  data/repository/  WallpaperRepository — merges both sources for the UI
  ui/screens/           Explore, Categories, Favorites, Detail
  ui/components/     Reusable grid
  viewmodel/            WallpaperViewModel (StateFlow-driven UI state)
  util/WallpaperSetter.kt   Applies wallpaper via WallpaperManager (home/lock/both)
```

## 1. Open the project
Open the `WallpaperApp` folder in Android Studio (Koala or newer). Let Gradle sync.

## 2. Get a Pixabay API key (free)
1. Create a free account at https://pixabay.com and sign in
2. Go to https://pixabay.com/api/docs/ — your API key is shown at the top of that page once logged in
3. Create `local.properties` in the project root (it's git-ignored) and add:
   ```
   PIXABAY_API_KEY=your_key_here
   ```
   Pixabay's default rate limit is 100 requests per 60 seconds, at no cost — no separate "production" approval step required.

   Note on licensing: Pixabay's Terms of Service don't prohibit wallpaper apps the way Unsplash's and Pexels' guidelines do, but terms can change — worth a quick re-read at https://pixabay.com/service/terms/ before you publish. Most content is CC0 (free for commercial use), and showing the photographer's name/link (already wired via `authorUrl`) is good practice even where not strictly required.

## 3. Run it
Standard Run ▶ in Android Studio, or `./gradlew installDebug`.

## Rename the package before publishing
`com.example.wallpaperapp` is a placeholder. Use Android Studio's Refactor → Rename to your real applicationId (e.g. `com.yourcompany.wallify`) before you build a release — Google Play requires a unique, permanent applicationId per app.

---

## Publishing to Google Play — checklist

1. **App icon** — replace the placeholder in `res/mipmap-anydpi-v26/` using Android Studio's Image Asset Studio (right-click `res` → New → Image Asset).
2. **Signing key** — generate a release keystore (Build → Generate Signed Bundle/APK), then fill `local.properties`:
   ```
   RELEASE_STORE_FILE=/absolute/path/to/release.keystore
   RELEASE_STORE_PASSWORD=...
   RELEASE_KEY_ALIAS=...
   RELEASE_KEY_PASSWORD=...
   ```
   Keep this file and the keystore out of git and back them up — Google Play requires the same key for every future update.
3. **Build an Android App Bundle**: `./gradlew bundleRelease` → produces `app/build/outputs/bundle/release/app-release.aab`, which is what you upload to Play Console (not an APK).
4. **Play Console setup** (https://play.google.com/console, one-time $25 registration):
   - Create the app, fill in the Store listing (title, description, screenshots, feature graphic)
   - Complete the **Data safety** form — since this app calls Pixabay's API, declare what data leaves the device
   - Add a **Privacy Policy URL** — required for any app using internet access / analytics
   - Set a **Content rating** via the questionnaire
   - If you keep the AdMob dependency, replace the sample `APPLICATION_ID` in `AndroidManifest.xml` with your real AdMob app ID, and disclose ads in the Play listing
5. **Target API level** — `targetSdk = 34` already satisfies current Play requirements; keep this updated yearly.
6. Upload the `.aab` to an **Internal testing** track first, test on a real device, then promote to Production.
7. Pixabay images can be freely hotlinked or cached; the app already stores each photographer's name/profile link via `authorUrl` if you want to surface it in the UI.

## Notes / good next additions
- Pagination is wired (`loadFeed`) but the grid doesn't yet trigger it on scroll — hook `onLoadMore` to a `LazyVerticalGrid` scroll listener.
- Consider Pixabay's second image size (`largeImageURL`) vs `fullHDURL`/`imageURL` if you want higher-res options for large-screen devices.
- Want your own curated image library back? Add a service (Firebase, a custom backend, etc.) and merge it into `WallpaperRepository.getExploreFeed()`/`getByCategory()`.
- Consider Play Billing (already added as a dependency) to offer an ad-free/premium tier gated on `isPremium` wallpapers.

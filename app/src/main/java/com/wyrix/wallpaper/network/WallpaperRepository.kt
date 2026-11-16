package com.wyrix.wallpaper.network

import android.net.Uri
import com.wyrix.wallpaper.BuildConfig
import com.wyrix.wallpaper.model.NasaItem
import com.wyrix.wallpaper.model.PexelsPhoto
import com.wyrix.wallpaper.model.PixabayImage
import com.wyrix.wallpaper.model.WallpaperItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

enum class ImageSource { PIXABAY, PEXELS, NASA, SDO, ESA_HUBBLE, ESA_WEBB, ESO }

/** One search on one source. A category is a list of feeds that get mixed together. */
data class Feed(
    val source: ImageSource,
    val query: String,
    val imageType: String = "all" // Pixabay only: all | photo | illustration | vector
)

data class Category(
    val id: String,
    val label: String,
    val feeds: List<Feed>,
    /** AMOLED black switch starts ON for this category. */
    val amoledByDefault: Boolean = false
)

/**
 * Loads wallpapers live from several free sources, mixes them and returns one list.
 * Nothing is bundled in the app.
 *
 *  - Pixabay (key), Pexels (optional key): searchable photo sites
 *  - NASA Image and Video Library: searchable, no key
 *  - NASA SDO: live Sun images at fixed URLs documented by NASA
 *  - ESA/Hubble Picture of the Week, ESA/Webb Picture of the Month, ESO Picture of the Week:
 *    official RSS feeds, CC BY 4.0 (credit must stay visible)
 *
 * To add a category, add a Category(...) below. To add a source, add a value to [ImageSource],
 * a fetch function and a mapper here, and credit it in strings.xml.
 */
object WallpaperRepository {

    const val FAVORITES_ID = "favorites"

    /** "Live" images (SDO) keep the same URL while the picture changes; cache them for 30 minutes only. */
    const val LIVE_BUCKET_MS = 30L * 60L * 1000L

    val hasPexels: Boolean get() = BuildConfig.PEXELS_API_KEY.isNotBlank()

    private const val HUBBLE_FEED = "https://esahubble.org/images/potw/feed/"
    private const val WEBB_FEED = "https://esawebb.org/images/potm/feed/"
    private const val ESO_FEED = "https://www.eso.org/public/images/potw/feed/"

    private const val SDO_BASE = "https://sdo.gsfc.nasa.gov/assets/img/latest/latest_"

    /** SDO channel code -> label. Codes are the ones listed on NASA's SDO "best practice" page. */
    private val SDO_CHANNELS = listOf(
        "0304" to "AIA 304 \u00C5 (red)",
        "0171" to "AIA 171 \u00C5 (gold)",
        "0193" to "AIA 193 \u00C5 (bronze)",
        "0211" to "AIA 211 \u00C5 (purple)",
        "0335" to "AIA 335 \u00C5 (blue)",
        "0131" to "AIA 131 \u00C5 (teal)",
        "0094" to "AIA 94 \u00C5 (green)",
        "1600" to "AIA 1600 \u00C5",
        "1700" to "AIA 1700 \u00C5 (pink)",
        "hmiic" to "HMI visible light (orange)"
    )

    private fun px(q: String, type: String = "all") = Feed(ImageSource.PIXABAY, q, type)
    private fun pe(q: String) = Feed(ImageSource.PEXELS, q)
    private fun na(q: String) = Feed(ImageSource.NASA, q)
    private fun sdo() = Feed(ImageSource.SDO, "sun")
    private fun hubble() = Feed(ImageSource.ESA_HUBBLE, "potw")
    private fun webb() = Feed(ImageSource.ESA_WEBB, "potm")
    private fun eso() = Feed(ImageSource.ESO, "potw")

    val categories = listOf(
        Category("space", "Space", listOf(px("outer space"), pe("space"), na("space telescope"), hubble(), webb())),
        Category("hubble_webb", "Hubble & Webb", listOf(hubble(), webb(), na("hubble"), na("james webb"))),
        Category("galaxy", "Galaxy", listOf(px("galaxy"), pe("galaxy"), na("galaxy"))),
        Category("nebula", "Nebula", listOf(px("nebula"), pe("nebula"), na("nebula"))),
        Category("planets", "Planets", listOf(px("planet"), pe("planets"), na("planet"))),
        Category("moon", "Moon", listOf(px("moon"), pe("moon"), na("moon"))),
        Category("sun", "Sun", listOf(sdo(), px("sun"), pe("sun"), na("sun solar"))),
        Category("earth", "Earth", listOf(px("earth planet"), pe("earth from space"), na("earth from space"))),
        Category("night_sky", "Night Sky", listOf(eso(), px("milky way"), pe("night sky"), px("starry night"))),
        // AMOLED: Pixabay black backgrounds + real space/Sun images (they are mostly pure black).
        Category(
            "amoled", "AMOLED",
            listOf(
                px("black background"), px("dark abstract"), px("neon dark"), px("night sky stars"),
                pe("black background"), pe("dark minimal"),
                na("stars"), sdo(), webb(), hubble()
            ),
            amoledByDefault = true
        ),
        Category(
            "dark", "Dark",
            listOf(px("dark"), px("dark aesthetic"), px("dark forest"), px("dark city night"), pe("dark aesthetic"))
        ),
        Category(
            "neon", "Neon",
            listOf(px("neon"), px("neon lights"), px("neon city"), px("cyberpunk"), pe("neon lights"))
        ),
        Category(
            "3d", "3D",
            listOf(
                px("3d render", "illustration"), px("3d abstract", "illustration"),
                px("3d shapes", "illustration"), px("glass 3d", "illustration"),
                pe("3d render")
            )
        ),
        Category(
            "abstract", "Abstract",
            listOf(px("abstract"), px("abstract gradient"), px("fluid art"), px("geometric abstract"), pe("abstract"))
        ),
        Category(
            "minimal", "Minimal",
            listOf(px("minimal"), px("minimalist"), px("simple gradient"), px("pastel gradient"), pe("minimalist"))
        ),
        Category(FAVORITES_ID, "\u2665 Favorites", emptyList())
    )

    /** Free-text search across the searchable sources. */
    fun searchFeeds(query: String): List<Feed> = listOf(
        Feed(ImageSource.PIXABAY, query),
        Feed(ImageSource.PEXELS, query),
        Feed(ImageSource.NASA, query)
    )

    /**
     * Loads one page for all feeds in parallel and interleaves the results.
     * A feed that fails (no key, no internet, page out of range) just contributes nothing.
     * An empty result means there is nothing more to load.
     */
    suspend fun load(feeds: List<Feed>, page: Int): List<WallpaperItem> = coroutineScope {
        val active = feeds.filter { it.source != ImageSource.PEXELS || hasPexels }
        if (active.isEmpty()) return@coroutineScope emptyList()
        val perFeed = (36 / active.size).coerceIn(12, 40)
        val lists = active
            .map { feed -> async { safe(emptyList()) { fetch(feed, page, perFeed) } } }
            .awaitAll()
        interleave(lists)
    }

    private suspend fun fetch(feed: Feed, page: Int, perFeed: Int): List<WallpaperItem> =
        when (feed.source) {
            ImageSource.PIXABAY -> fetchPixabay(feed, page, perFeed)
            ImageSource.PEXELS -> fetchPexels(feed, page, perFeed)
            ImageSource.NASA -> fetchNasa(feed, page, perFeed)
            ImageSource.SDO -> paginate(sdoItems(), page, perFeed)
            ImageSource.ESA_HUBBLE -> fetchRss(HUBBLE_FEED, page, perFeed) { it.toEsaItem("hubble", "ESA/Hubble & NASA", true, "/screen640/") }
            ImageSource.ESA_WEBB -> fetchRss(WEBB_FEED, page, perFeed) { it.toEsaItem("webb", "ESA/Webb, NASA & CSA", true, "/screen640/") }
            ImageSource.ESO -> fetchRss(ESO_FEED, page, perFeed) { it.toEsaItem("eso", "ESO", false, "/thumb700x/") }
        }

    private fun <T> paginate(all: List<T>, page: Int, perPage: Int): List<T> =
        all.drop((page - 1) * perPage).take(perPage)

    private suspend fun fetchPixabay(feed: Feed, page: Int, perPage: Int): List<WallpaperItem> {
        val response = RetrofitClient.pixabay.searchImages(
            key = BuildConfig.PIXABAY_API_KEY,
            query = feed.query,
            imageType = feed.imageType,
            perPage = perPage,
            page = page
        )
        return response.hits.orEmpty().mapNotNull { it.toWallpaper() }
    }

    private suspend fun fetchPexels(feed: Feed, page: Int, perPage: Int): List<WallpaperItem> {
        val response = RetrofitClient.pexels.search(
            apiKey = BuildConfig.PEXELS_API_KEY,
            query = feed.query,
            perPage = perPage,
            page = page
        )
        return response.photos.orEmpty().mapNotNull { it.toWallpaper() }
    }

    private suspend fun fetchNasa(feed: Feed, page: Int, perPage: Int): List<WallpaperItem> {
        val response = RetrofitClient.nasa.search(query = feed.query, page = page, pageSize = perPage)
        return response.collection?.items.orEmpty().mapNotNull { it.toWallpaper() }
    }

    /** RSS feeds have no paging, so the whole (small, cached) feed is read and sliced per page. */
    private suspend fun fetchRss(
        feedUrl: String,
        page: Int,
        perPage: Int,
        map: (RssItem) -> WallpaperItem?
    ): List<WallpaperItem> {
        val items = withContext(Dispatchers.IO) {
            RetrofitClient.feeds.get(feedUrl).use { body -> RssParser.parse(body.byteStream()) }
        }
        return paginate(items.mapNotNull(map), page, perPage)
    }

    // ---------------- Mappers (with credits) ----------------

    private fun PixabayImage.toWallpaper(): WallpaperItem? {
        val thumb = webformatURL ?: return null
        val large = largeImageURL ?: thumb
        return WallpaperItem(
            id = "pixabay:$id",
            source = "pixabay",
            thumbUrl = thumb,
            previewUrl = large,
            fullUrl = large,
            credit = "Photo by ${user ?: "a contributor"} on Pixabay",
            pageUrl = pageURL ?: "https://pixabay.com"
        )
    }

    private fun PexelsPhoto.toWallpaper(): WallpaperItem? {
        val s = src ?: return null
        val portrait = s.portrait ?: s.large2x ?: return null
        return WallpaperItem(
            id = "pexels:$id",
            source = "pexels",
            thumbUrl = portrait,
            previewUrl = portrait,
            fullUrl = s.original ?: s.large2x ?: portrait,
            credit = "Photo by ${photographer ?: "a photographer"} on Pexels",
            pageUrl = url ?: "https://www.pexels.com"
        )
    }

    private fun NasaItem.toWallpaper(): WallpaperItem? {
        val d = data?.firstOrNull() ?: return null
        val nasaId = d.nasaId ?: return null
        val thumb = links?.firstOrNull { it.rel == "preview" }?.href
            ?.replace("http://", "https://") ?: return null
        val lower = thumb.lowercase()
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png"))) return null

        val who = listOf(d.photographer, d.secondaryCreator, d.center)
            .firstOrNull { !it.isNullOrBlank() && !it.equals("NASA", ignoreCase = true) }
        return WallpaperItem(
            id = "nasa:$nasaId",
            source = "nasa",
            thumbUrl = thumb.replace("~thumb.", "~medium."),
            previewUrl = thumb.replace("~thumb.", "~large."),
            fullUrl = "", // resolved when needed, see pickBestNasaUrl()
            credit = if (who != null) "Image: NASA / $who" else "Image: NASA",
            pageUrl = "https://images.nasa.gov/details/" + Uri.encode(nasaId)
        )
    }

    /**
     * ESA/Hubble, ESA/Webb and ESO feed items. The feed gives the 1280px "screen" file; the other
     * sizes follow the same folder naming on the same servers (verified for Hubble and ESO thumbs;
     * if a size does not exist the app falls back to the screen file automatically).
     * Licence: CC BY 4.0, so the credit line is always shown and links to the page with the full credit.
     */
    private fun RssItem.toEsaItem(
        key: String,
        creditName: String,
        hasLarge: Boolean,
        thumbFolder: String
    ): WallpaperItem? {
        val screen = enclosureUrl?.replace("http://", "https://") ?: return null
        if (!screen.contains("/screen/")) return null
        val page = link?.trim().orEmpty()
        val slug = page.trimEnd('/').substringAfterLast('/')
        if (slug.isBlank()) return null
        return WallpaperItem(
            id = "$key:$slug",
            source = key,
            thumbUrl = screen.replace("/screen/", thumbFolder),
            previewUrl = screen,
            fullUrl = if (hasLarge) screen.replace("/screen/", "/large/") else screen,
            credit = "Image: $creditName \u00B7 CC BY 4.0 \u00B7 tap for full credit",
            pageUrl = page.ifBlank { "https://esahubble.org" }
        )
    }

    /** Live Sun images from NASA SDO. Square with a black background, so they are fitted, not cropped. */
    private fun sdoItems(): List<WallpaperItem> = SDO_CHANNELS.map { (code, label) ->
        WallpaperItem(
            id = "sdo:$code",
            source = "sdo",
            thumbUrl = "${SDO_BASE}512_$code.jpg",
            previewUrl = "${SDO_BASE}2048_$code.jpg",
            fullUrl = "${SDO_BASE}4096_$code.jpg",
            credit = "Live Sun \u00B7 NASA/SDO and the AIA, EVE and HMI science teams \u00B7 $label",
            pageUrl = "https://sdo.gsfc.nasa.gov/data/",
            fitOnBlack = true
        )
    }

    // ---------------- URL helpers ----------------

    /** If a smaller derived file does not exist, fall back to a size that always exists. */
    fun thumbFallback(url: String) = url
        .replace("~medium.", "~thumb.")
        .replace("/screen640/", "/screen/")
        .replace("/thumb700x/", "/screen/")

    fun previewFallback(url: String) = url.replace("~large.", "~medium.")

    /** From the NASA asset list, pick the original (full resolution), else large, else medium. */
    fun pickBestNasaUrl(urls: List<String>): String? {
        val images = urls
            .map { it.replace("http://", "https://") }
            .filter {
                val l = it.lowercase()
                l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png")
            }
        return images.firstOrNull { it.contains("~orig") }
            ?: images.firstOrNull { it.contains("~large") }
            ?: images.firstOrNull { it.contains("~medium") }
    }

    // ---------------- internals ----------------

    private fun interleave(lists: List<List<WallpaperItem>>): List<WallpaperItem> {
        val out = ArrayList<WallpaperItem>()
        val longest = lists.maxOfOrNull { it.size } ?: 0
        for (i in 0 until longest) {
            for (list in lists) {
                if (i < list.size) out.add(list[i])
            }
        }
        return out.distinctBy { it.id }
    }

    private suspend fun <T> safe(default: T, block: suspend () -> T): T =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            default
        }
}

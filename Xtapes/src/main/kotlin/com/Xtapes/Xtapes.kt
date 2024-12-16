package com.Xtapes

import com.lagradost.api.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class Xtapes : MainAPI() {
    override var mainUrl              = "https://v.xtapes.to"
    override var name                 = "Xtapes"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasDownloadSupport   = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
        "porn-movies-hd" to "Latest",
        "86026" to "BangBros",
        "57964" to "Brazzers",
        "61802" to "Naughty America",
        "56050" to "Reality Kings",
        "05407" to "Play Ground",
        "11075" to "Evil Angle",
        "11007" to "Harmony Films",
        "40100" to "New Sensations",
        "37001" to "Sweet Sinner",
        "32718" to "Blacked",
        "12337" to "Tonight's Girlfriend",
        "70587" to "Porn Fidelity",
        "63704" to "Porn World",
        "41606" to "Mofos",
        "63416" to "Jules Jordan"     
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl/${request.data}/page/$page/").document
        val home     = document.select("ul.listing-videos li").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(
            list    = HomePageList(
                name               = request.name,
                list               = home,
                isHorizontalImages = true
            ),
            hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title     = this.select("img").attr("title")
        val href      = fixUrl(this.select("a").attr("href"))
        val posterUrl = fixUrlNull(this.select("img").attr("src"))
        println(posterUrl)
        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..5) {
            val document = app.get("${mainUrl}/page/$i/?s=$query").document

            val results = document.select("ul.listing-videos li").mapNotNull { it.toSearchResult() }

            if (!searchResponse.containsAll(results)) {
                searchResponse.addAll(results)
            } else {
                break
            }

            if (results.isEmpty()) break
        }

        return searchResponse
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title       = document.selectFirst("meta[property=og:title]")?.attr("content")?.trim().toString()
        val poster      = "https://previews.agefotostock.com/previewimage/medibigoff/6a9bd0bbe77dc222cc98115970b36678/zon-7579670.jpg"
        val description = document.selectFirst("meta[property=og:description]")?.attr("content")?.trim()


        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot      = description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        document.select("#video-code iframe").forEach { links ->
            val url=links.attr("src")
            Log.d("Owen Test",url)
            loadExtractor(url,subtitleCallback, callback)
        }
        return true
    }
}

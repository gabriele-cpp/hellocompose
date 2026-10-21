package com.example.ch09.data

import com.example.ch09.data.local.ArticleDao
import com.example.ch09.data.local.ArticleEntity
import com.example.ch09.data.mapper.toDomain
import com.example.ch09.data.mapper.toEntity
import com.example.ch09.data.model.Article
import com.example.ch09.data.model.NewsCategory
import com.example.ch09.data.model.NewsFeed
import com.example.ch09.data.model.OfflineInfo
import com.example.ch09.data.remote.NewsApiService
import com.example.ch09.data.remote.dto.NewsResponseDto
import kotlin.coroutines.cancellation.CancellationException

interface NewsRepository {
    suspend fun getTopHeadlines(category: NewsCategory): Result<NewsFeed>
    suspend fun searchNews(query: String): Result<NewsFeed>
    // Untuk layar detail (Tugas 2): artikel apa pun yang pernah dimuat
    suspend fun getArticle(url: String): Article?
}

private const val SEARCH_CACHE_KEY = "__search__"

class NewsRepositoryImpl(
    private val api: NewsApiService,
    private val dao: ArticleDao,
    private val clock: () -> Long = System::currentTimeMillis
) : NewsRepository {

    override suspend fun getTopHeadlines(category: NewsCategory): Result<NewsFeed> =
        fetch(
            cacheKey = category.apiValue,
            cacheFetcher = { dao.getArticles(category.apiValue) },
            remote = { api.getTopHeadlines(category = category.apiValue) }
        )

    override suspend fun searchNews(query: String): Result<NewsFeed> =
        fetch(
            cacheKey = SEARCH_CACHE_KEY,
            cacheFetcher = { dao.searchCached(query) },
            remote = { api.searchNews(query = query) }
        )

    override suspend fun getArticle(url: String): Article? =
        dao.getArticleByUrl(url)?.toDomain()

    private suspend fun fetch(
        cacheKey: String,
        cacheFetcher: suspend () -> List<ArticleEntity>,
        remote: suspend () -> NewsResponseDto
    ): Result<NewsFeed> {
        val result = runCatching {
            remote().articles
                .mapNotNull { it.toDomain() }
                // NewsAPI bisa mengirim url ganda; LazyColumn(key = url) akan crash
                .distinctBy { it.url }
        }

        // runCatching juga menangkap CancellationException (mis. pengguna sudah
        // pindah kategori). Itu BUKAN error jaringan: lempar ulang.
        val exception = result.exceptionOrNull()
        if (exception is CancellationException) throw exception

        return if (result.isSuccess) {
            val articles = result.getOrThrow()
            val now = clock()
            val entities = articles.mapIndexed { index, article ->
                article.toEntity(category = cacheKey, position = index, cachedAt = now)
            }
            dao.replaceCategory(cacheKey, entities)
            Result.success(NewsFeed(articles))
        } else {
            val cachedEntities = cacheFetcher()
            if (cachedEntities.isNotEmpty()) {
                val cachedAt = cachedEntities.first().cachedAt
                val articles = cachedEntities.map { it.toDomain() }.distinctBy { it.url }
                Result.success(
                    NewsFeed(
                        articles = articles,
                        offline = OfflineInfo(
                            cachedAt = cachedAt,
                            reason = exception?.toUserMessage() ?: "Terjadi kesalahan."
                        )
                    )
                )
            } else {
                Result.failure(exception ?: Exception("Unknown error"))
            }
        }
    }
}

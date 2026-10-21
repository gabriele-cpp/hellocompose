package com.example.ch09.data

import com.example.ch09.data.model.NewsCategory
import com.example.ch09.testutil.FakeArticleDao
import com.example.ch09.testutil.FakeNewsApiService
import com.example.ch09.testutil.articleDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NewsRepositoryImplTest {

    private val api = FakeNewsApiService()
    private val dao = FakeArticleDao()
    private var currentTime = 100_000L
    private val repo = NewsRepositoryImpl(api = api, dao = dao, clock = { currentTime })

    @Test
    fun `berhasil memuat berita dan menyimpannya ke cache Room`() = runTest {
        api.articles = listOf(articleDto(1), articleDto(2))
        val result = repo.getTopHeadlines(NewsCategory.Technology)

        assertTrue(result.isSuccess)
        val feed = result.getOrThrow()
        assertEquals(2, feed.articles.size)
        assertNull(feed.offline)

        val cached = dao.getArticles("technology")
        assertEquals(2, cached.size)
        assertEquals(0, cached[0].position)
        assertEquals(1, cached[1].position)
        assertEquals(currentTime, cached[0].cachedAt)
    }

    @Test
    fun `gagal jaringan mengembalikan data dari cache Room dengan OfflineInfo`() = runTest {
        api.articles = listOf(articleDto(1))
        repo.getTopHeadlines(NewsCategory.General)

        api.error = IOException("Network error")
        val result = repo.getTopHeadlines(NewsCategory.General)

        assertTrue(result.isSuccess)
        val feed = result.getOrThrow()
        assertEquals(1, feed.articles.size)
        assertNotNull(feed.offline)
        assertEquals(currentTime, feed.offline?.cachedAt)
        assertEquals("Tidak ada koneksi internet.", feed.offline?.reason)
    }

    @Test
    fun `gagal jaringan tanpa cache mengembalikan Result failure`() = runTest {
        api.error = IOException()
        val result = repo.getTopHeadlines(NewsCategory.Business)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getArticle mengembalikan artikel berdasarkan url dari cache Room`() = runTest {
        api.articles = listOf(articleDto(3))
        repo.getTopHeadlines(NewsCategory.Science)

        val article = repo.getArticle("https://contoh.com/3")
        assertNotNull(article)
        assertEquals("Berita 3", article?.title)

        val notFound = repo.getArticle("https://contoh.com/unknown")
        assertNull(notFound)
    }

    @Test
    fun `pencarian offline menemukan artikel dari cache`() = runTest {
        api.articles = listOf(articleDto(1, title = "Android Kotlin"), articleDto(2, title = "Flutter"))
        repo.getTopHeadlines(NewsCategory.Technology)

        api.error = IOException()
        val searchResult = repo.searchNews("Kotlin")

        assertTrue(searchResult.isSuccess)
        val feed = searchResult.getOrThrow()
        assertEquals(1, feed.articles.size)
        assertEquals("Android Kotlin", feed.articles[0].title)
        assertNotNull(feed.offline)
    }
}

package com.example.ch09.data.mapper

import com.example.ch09.data.remote.dto.ArticleDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleMapperTest {

    @Test fun `field null diganti nilai yang layak tampil`() {
        val article = ArticleDto(title = " Judul ", url = "https://a.com", author = "  ").toDomain()!!

        assertEquals("Judul", article.title)
        assertEquals("Anonim", article.author)
        assertEquals("", article.description)
        assertEquals("Tanpa sumber", article.sourceName)
        assertNull(article.imageUrl)
    }

    @Test fun `artikel tanpa judul, tanpa url, atau Removed dibuang`() {
        assertNull(ArticleDto(title = null, url = "https://a.com").toDomain())
        assertNull(ArticleDto(title = "A", url = " ").toDomain())
        assertNull(ArticleDto(title = "[Removed]", url = "https://removed.com").toDomain())
    }

    @Test fun `penanda potongan content dihapus`() {
        val article = ArticleDto(
            title = "A", url = "https://a.com",
            content = "Kalimat pembuka berita… [+2345 chars]"
        ).toDomain()!!
        assertEquals("Kalimat pembuka berita…", article.content)
    }

    @Test fun `konversi Article ke ArticleEntity dan kembali ke Article`() {
        val article = com.example.ch09.data.model.Article(
            title = "Judul",
            author = "Penulis",
            description = "Deskripsi",
            content = "Konten",
            url = "https://a.com",
            imageUrl = "https://a.com/img.jpg",
            publishedAt = "2025-05-01T08:00:00Z",
            sourceName = "Sumber"
        )
        val entity = article.toEntity(category = "technology", position = 0, cachedAt = 12345L)
        assertEquals("technology", entity.category)
        assertEquals(0, entity.position)
        assertEquals(12345L, entity.cachedAt)
        assertEquals(article, entity.toDomain())
    }

}

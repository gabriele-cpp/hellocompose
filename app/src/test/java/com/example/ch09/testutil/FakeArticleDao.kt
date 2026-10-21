package com.example.ch09.testutil

import com.example.ch09.data.local.ArticleDao
import com.example.ch09.data.local.ArticleEntity

class FakeArticleDao : ArticleDao() {
    val items = mutableListOf<ArticleEntity>()

    override suspend fun getArticles(category: String): List<ArticleEntity> =
        items.filter { it.category == category }.sortedBy { it.position }

    override suspend fun getArticleByUrl(url: String): ArticleEntity? =
        items.firstOrNull { it.url == url }

    override suspend fun searchCached(query: String): List<ArticleEntity> =
        items.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }.sortedWith(compareByDescending<ArticleEntity> { it.cachedAt }.thenBy { it.position })

    override suspend fun insertAll(articles: List<ArticleEntity>) {
        for (article in articles) {
            items.removeAll { it.category == article.category && it.url == article.url }
            items.add(article)
        }
    }

    override suspend fun deleteCategory(category: String) {
        items.removeAll { it.category == category }
    }

    override suspend fun replaceCategory(category: String, articles: List<ArticleEntity>) {
        deleteCategory(category)
        insertAll(articles)
    }
}

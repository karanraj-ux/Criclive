package com.example.model

data class NewsArticle(
    val title: String,
    val link: String,
    val pubDate: Long = 0L,
    val pubDateStr: String = ""
)

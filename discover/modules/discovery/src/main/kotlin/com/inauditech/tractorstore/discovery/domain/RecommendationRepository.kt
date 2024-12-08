package com.inauditech.tractorstore.discovery.domain

interface RecommendationRepository {
    fun saveAll(products: List<Recommendation>)

    fun findAll(): List<Recommendation>
}

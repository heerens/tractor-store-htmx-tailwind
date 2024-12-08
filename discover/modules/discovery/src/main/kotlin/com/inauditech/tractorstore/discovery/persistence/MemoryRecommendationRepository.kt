package com.inauditech.tractorstore.discovery.persistence

import com.inauditech.tractorstore.discovery.domain.Recommendation
import com.inauditech.tractorstore.discovery.domain.RecommendationRepository
import org.springframework.stereotype.Repository

@Repository
class MemoryRecommendationRepository : RecommendationRepository {
    var data: MutableList<Recommendation> = mutableListOf()

    override fun saveAll(products: List<Recommendation>) {
        data.clear()
        data.addAll(products)
    }

    override fun findAll(): List<Recommendation> = data.toList()
}

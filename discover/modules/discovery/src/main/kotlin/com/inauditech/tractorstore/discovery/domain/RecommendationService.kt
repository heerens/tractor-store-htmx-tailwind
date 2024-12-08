package com.inauditech.tractorstore.discovery.domain

import mu.KLogging
import org.springframework.stereotype.Component
import kotlin.math.pow
import kotlin.math.sqrt

@Component
class RecommendationService(
    val recommendationRepository: RecommendationRepository,
) {
    fun findTop(): List<Recommendation> {
        val top =
            recommendationRepository
                .findAll()
                .distinctBy { it.productId }
        return top
    }

    fun findSimilar(sku: Sku): List<Recommendation> {
        val all = recommendationRepository.findAll()
        val reference = all.first { it.sku == sku }
        val other = all.filter { it.productId != reference.productId }
        val similar =
            other
                .map { Pair(euclideanDistance(reference.rgbColor, it.rgbColor), it) }
                .sortedByDescending { it.first }
                .map { it.second }
        return similar
    }

    private fun euclideanDistance(
        rgb1: Triple<Int, Int, Int>,
        rgb2: Triple<Int, Int, Int>,
    ): Double {
        val (r1, g1, b1) = rgb1
        val (r2, g2, b2) = rgb2

        val distance =
            sqrt(
                (r1 - r2).toDouble().pow(2.0) +
                    (g1 - g2).toDouble().pow(2.0) +
                    (b1 - b2).toDouble().pow(2.0),
            )

        val maxDistance = sqrt(3 * 255.0.pow(2.0))

        return 1 - (distance / maxDistance)
    }

    companion object : KLogging()
}

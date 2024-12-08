package com.inauditech.tractorstore.checkout.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.inauditech.tractorstore.checkout.domain.ProductApi
import com.inauditech.tractorstore.checkout.domain.ProductId
import com.inauditech.tractorstore.checkout.domain.SellableVariant
import com.inauditech.tractorstore.checkout.domain.Sku
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class ProductRestApi(
    val mapper: ObjectMapper,
    @Value("\${checkout.product-feed-url}") val productFeedUrl: String,
) : ProductApi {
    private fun ApiProduct.toRecommendation(): List<SellableVariant> =
        this.variants.map { v ->
            SellableVariant(
                productId = ProductId(this.id),
                sku = Sku(v.sku),
                name = this.name + " " + v.name,
                image = v.image,
                color = v.color,
                price = v.price,
                inventory = 0,
            )
        }

    override fun findAll(): List<SellableVariant> = load().products.flatMap { it.toRecommendation() }

    private val retryTemplate: RetryTemplate =
        RetryTemplate
            .builder()
            .exponentialBackoff(2000, 2.0, 1000 * 60 * 1)
            .maxAttempts(5)
            .build()

    fun load(): ApiResponse =
        retryTemplate.execute<ApiResponse, RuntimeException>(
            { run ->
                if (run.retryCount > 0) {
                    run.lastThrowable?.let { throwable ->
                        logger.warn {
                            "Starting retry #${run.retryCount}, last " +
                                "API call failed with: ${throwable.message}"
                        }
                    }
                }
                call()
            },
            { noMoreRetry ->
                noMoreRetry.lastThrowable?.let { throwable ->
                    logger.error {
                        "Giving up after ${noMoreRetry.retryCount} attempts " +
                            "of retries with: ${throwable.message}"
                    }
                    throw throwable
                }
            },
        )

    private fun call(): ApiResponse {
        val client = HttpClient.newBuilder().build()

        logger.info { "Calling $productFeedUrl" }
        val request = HttpRequest.newBuilder().uri(URI.create(productFeedUrl)).build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return mapper.readValue(response.body(), ApiResponse::class.java)
    }

    companion object : KLogging() {
    }
}

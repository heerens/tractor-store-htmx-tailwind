package com.inauditech.tractorstore.navigation.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.inauditech.tractorstore.navigation.domain.Store
import com.inauditech.tractorstore.navigation.domain.StoreId
import com.inauditech.tractorstore.navigation.domain.StoreRepository
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository

@Repository
class FileStoreRepository(
    val mapper: ObjectMapper,
) : StoreRepository {
    private val stores: MutableList<Store> = mutableListOf()

    @PostConstruct
    fun init() {
        val resource = ClassPathResource("database_stores.json")
        val fileData: FileData = resource.inputStream.use { mapper.readValue(it, FileData::class.java) }
        stores.addAll(fileData.stores)
    }

    override fun findAll(): List<Store> = stores

    override fun findById(storeId: StoreId): Store? = stores.find { it.id == storeId }

    private data class FileData(
        val stores: List<Store>,
    )
}

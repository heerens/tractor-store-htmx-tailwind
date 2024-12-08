package com.inauditech.tractorstore.discovery.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.inauditech.tractorstore.discovery.domain.Category
import com.inauditech.tractorstore.discovery.domain.CategoryKey
import com.inauditech.tractorstore.discovery.domain.CategoryRepository
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository

@Repository
class FileCategoryRepository(
    val mapper: ObjectMapper,
) : CategoryRepository {
    private val categories: MutableList<Category> = mutableListOf()

    @PostConstruct
    fun init() {
        val resource = ClassPathResource("database_categories.json")
        val fileData: FileData = resource.inputStream.use { mapper.readValue(it, FileData::class.java) }
        categories.addAll(fileData.categories)
    }

    override fun findAll(): List<Category> = categories

    override fun findById(key: CategoryKey): Category? = categories.firstOrNull { it.key == key }
}

data class FileData(
    val categories: List<Category>,
)

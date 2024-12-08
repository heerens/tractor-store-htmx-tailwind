package com.inauditech.tractorstore.discovery.presentation.discovery.search

import com.inauditech.tractorstore.discovery.domain.CategoryKey
import com.inauditech.tractorstore.discovery.domain.CategoryRepository
import com.inauditech.tractorstore.discovery.domain.SearchService
import com.inauditech.tractorstore.discovery.domain.SearchableProduct
import com.inauditech.tractorstore.discovery.presentation.discovery.image.ImageView
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class SearchResult(
    val searchService: SearchService,
    val categoryRepository: CategoryRepository,
) {
    @PostMapping("/discovery/fragments/v1/search")
    fun render(
        @ModelAttribute("searchForm") searchForm: SearchForm,
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        val view = createView(searchForm)

        model.addAttribute("searchResultView", view)
        return "search/SearchResult"
    }

    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        // convert empty form input strings to null
        binder.registerCustomEditor(String::class.java, StringTrimmerEditor(true))
    }

    fun createView(searchForm: SearchForm): SearchResultView {
        val categoryKey = searchForm.key?.let { CategoryKey(it) }
        val result = searchService.findAllByCategory(categoryKey)

        val filter =
            listOf(Filter(name = "All", key = null)) +
                categoryRepository.findAll().map {
                    Filter(
                        name = it.name,
                        key = it.key.value,
                    )
                }

        val view =
            SearchResultView(
                results = result.map { ResultItmeView(it) },
                filter = filter,
            )
        return view
    }

    companion object : KLogging()
}

data class SearchForm(
    val key: String? = null,
)

data class SearchResultView(
    val results: List<ResultItmeView>,
    val filter: List<Filter>,
)

data class Filter(
    val name: String,
    val key: String? = null,
)

data class ResultItmeView(
    val product: SearchableProduct,
) {
    val imageView: ImageView
        get() =
            ImageView(
                url = product.image,
                srcsetSizes = listOf(200, 400),
                sizes = "200px",
                alt = product.name,
                width = 200,
                height = 200,
            )
}

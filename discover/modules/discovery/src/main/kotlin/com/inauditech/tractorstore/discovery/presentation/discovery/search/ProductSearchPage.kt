package com.inauditech.tractorstore.discovery.presentation.discovery.search

import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProductSearchPage(
    val searchResult: SearchResult,
) {
    @GetMapping("/discovery/products/{key}")
    fun render(
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
        @PathVariable(required = false) key: String? = null,
    ): String {
        val searchForm = SearchForm()
        model.addAttribute("searchForm", searchForm)
        model.addAttribute("searchResultView", searchResult.createView(searchForm))
        return "search/ProductSearchPage"
    }

    @GetMapping("/discovery/products")
    fun all(
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String = render(httpServletResponse, model, null)

    companion object : KLogging()
}

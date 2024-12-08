package com.inauditech.tractorstore.discover.config

import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.TemplateNotFoundException
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.springframework.boot.autoconfigure.JteConfigurationException
import gg.jte.springframework.boot.autoconfigure.JteProperties
import gg.jte.springframework.boot.autoconfigure.JteViewResolver
import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.View
import org.springframework.web.servlet.ViewResolver
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.Locale

@Configuration
class JteModuleConfiguration(
    var jteProperties: JteProperties,
) {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun jteTemplateEngine(): TemplateEngine {
        if (jteProperties.isDevelopmentMode && jteProperties.usePreCompiledTemplates()) {
            throw JteConfigurationException("You can't use development mode and precompiledTemplates together")
        }
        if (jteProperties.usePreCompiledTemplates()) {
            // Templates will need to be compiled by the maven/gradle build task
            return TemplateEngine.createPrecompiled(ContentType.Html)
        }
        if (jteProperties.isDevelopmentMode) {
            val codeResolver: CodeResolver = ModuleDirectoryCodeResolver()
            return TemplateEngine.create(
                codeResolver,
                Paths.get("jte-classes"),
                ContentType.Html,
                javaClass.classLoader,
            )
        }
        throw JteConfigurationException("You need to either set gg.jte.usePrecompiledTemplates or gg.jte.developmentMode to true ")
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    fun viewResolver(viewResolver: JteViewResolver): ViewResolver = ModuleViewResolver(viewResolver)
}

class ModuleDirectoryCodeResolver : CodeResolver {
    private val folders = findFoldersWithRootFile(Paths.get("."))
    private val resolvers = folders.map { DirectoryCodeResolver(it) }

    private fun findFoldersWithRootFile(rootDirectory: Path): List<Path> {
        val foldersWithRootFile = mutableListOf<Path>()

        Files.walkFileTree(
            rootDirectory,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    if (file.fileName.toString() == ".jteroot") {
                        foldersWithRootFile.add(file.parent)
                    }
                    return FileVisitResult.CONTINUE
                }
            },
        )

        return foldersWithRootFile
    }

    override fun resolve(name: String?): String? = resolvers.firstNotNullOfOrNull { it.resolve(name) }

    override fun resolveRequired(name: String?): String =
        resolvers.firstNotNullOfOrNull {
            try {
                it.resolveRequired(name)
            } catch (e: TemplateNotFoundException) {
                null
            }
        } ?: throw TemplateNotFoundException("$name not found (tried to load file at $folders)")

    override fun getLastModified(name: String?): Long {
        val list = resolvers.map { it.getLastModified(name) }.filter { it != 0L }
        return list.firstOrNull() ?: 0
    }

    override fun resolveAllTemplateNames(): List<String> {
        val list = resolvers.flatMap { it.resolveAllTemplateNames() }
        return list
    }
}

class ModuleViewResolver(
    val viewResolver: JteViewResolver,
) : ViewResolver {
    override fun resolveViewName(
        viewName: String,
        locale: Locale,
    ): View? {
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val module =
            request.requestURI
                .split("/")
                .first { it.isNotEmpty() }
        logger.info { "URI " + request.requestURI }
        logger.info { "Resolving view '$viewName' for module '$module'" }
        return viewResolver.resolveViewName("$module/$viewName", locale)
    }

    companion object : KLogging()
}

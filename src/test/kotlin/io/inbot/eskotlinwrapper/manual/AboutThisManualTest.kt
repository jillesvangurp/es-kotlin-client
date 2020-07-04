package io.inbot.eskotlinwrapper.manual

import io.inbot.eskotlinwrapper.AbstractElasticSearchTest
import org.junit.jupiter.api.Test

class AboutThisManualTest : AbstractElasticSearchTest(indexPrefix = "manual") {
    @Test
    fun `about this manual`() {

        val markdown = sourceRepository.md {
            +"""
                ## About this manual
                
                This manual is an attempt at 
                ${com.jillesvangurp.kotlin4example.mdLink("literate programming","https://en.wikipedia.org/wiki/Literate_programming")}.

                ## Epub support.
                
                I'm also thinking about eventually publishing an ebook version of the manual at some point. The build 
                currently produces scripts in the epub directory that allow you to create an epub using pandoc. This 
                is very much a work in progress but was a big motivation for me to sink time in creating this 
                little literate programming framework.
                
                You can find the latest epub
                [here](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/book.epub).
            """
        }
        markdownPageWithNavigation(aboutThisManualPage, markdown.value)
    }
}

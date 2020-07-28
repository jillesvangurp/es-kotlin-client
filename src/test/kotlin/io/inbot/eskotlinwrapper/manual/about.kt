package io.inbot.eskotlinwrapper.manual

import com.jillesvangurp.kotlin4example.mdLink
import io.inbot.eskotlinwrapper.withTestIndex

val about by withTestIndex<Thing, Lazy<String>> {
    sourceGitRepository.md {

        +"""
        ## About this manual
        
        This manual is an attempt at 
        ${mdLink("literate programming", "https://en.wikipedia.org/wiki/Literate_programming")}. When I 
        started writing it, I quickly discovered that copying bits of source code to quickly leads to broken
        or inaccurate documentation samples. I fixed it by hacking together a solution to grab code samples
        from Kotlin through reflection and by making some assumptions about where source files are in a typical
        gradle project.
        
        Eventually, I turned that into a separate project called [kotlin4example](https://github.com/jillesvangurp/kotlin4example)
        This manual is the first of (hopefully) many projects that use it.

        ## Epub support.
        
        I'm also thinking about eventually publishing an ebook version of the manual at some point. The build 
        currently produces scripts in the epub directory that allow you to create an epub using pandoc. This 
        is very much a work in progress but was a big motivation for me to sink time in creating this 
        little literate programming framework.
        
        You can find the latest epub
        [here](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/book.epub).
    """
    }
}


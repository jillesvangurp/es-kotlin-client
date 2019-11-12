package io.inbot.eskotlinwrapper.manual

import org.junit.jupiter.api.Test

val indexMd = "index.md"
val createClientMd = "creating-client.md"
val crudSupportMd = "crud-support.md"
val bulkIndexingMd = "bulk-indexing.md"

val indexPage = Page("Es Kotlin Wrapper Client Manual", indexMd)
val createClientPage = Page("How to create the client", createClientMd, parent = indexMd, next = crudSupportMd)
val crudPage = Page("How to create the client", crudSupportMd,parent = indexMd, next = bulkIndexingMd, previous = createClientMd)
val bulkPage = Page("Bulk Indexing", bulkIndexingMd,parent = indexMd, previous = crudSupportMd)

class MainPageTest {

    @Test
    fun `generate index md`() {
        KotlinForExample.markdownPage(indexPage) {
            +"""
                The Elasticsearch Kotlin Wrapper Client adds a lot of functionality to the 
                Elasticsearch Rest High Level Client. This is mostly done through extension functions.
                
                In this manual, we introduce the features through examples.
                
                - ${mdLink(createClientPage)}
                - ${mdLink(crudPage)}
                - ${mdLink(bulkPage)}
            """
        }
    }
}

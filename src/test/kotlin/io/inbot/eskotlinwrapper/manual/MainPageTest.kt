package io.inbot.eskotlinwrapper.manual

val indexPage = Page("Es Kotlin Wrapper Client Manual", "index.md")
val createClientPage = Page("How to create the client", "creating-client.md", parent = indexPage)
val crudPage = Page("How to create the client", "crud-support.md")

class MainPageTest {

    fun `generate index md`() {
    }
}

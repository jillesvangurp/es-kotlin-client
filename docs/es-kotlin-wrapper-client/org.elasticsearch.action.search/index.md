[es-kotlin-wrapper-client](../index.md) / [org.elasticsearch.action.search](./index.md)

## Package org.elasticsearch.action.search

Extension functions to add a few alternate source methods on queries. E.g. you can use multiline Kotlin templated strings.

	

### Functions

| Name | Summary |
|---|---|
| [source](source.md) | `fun SearchRequest.source(json: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Adds the missing piece to the SearchRequest API that allows you to paste raw json. This makes sense in Kotlin because it has multiline strings and support for template variables.`fun SearchRequest.source(reader: `[`Reader`](https://docs.oracle.com/javase/8/docs/api/java/io/Reader.html)`, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Adds the missing piece to the SearchRequest API that allows you to paste raw using a Reader. Useful if you store your queries in files.`fun SearchRequest.source(inputStream: `[`InputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html)`, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Supports taking the query straight from an InputStream. You probably should use the reader version. |

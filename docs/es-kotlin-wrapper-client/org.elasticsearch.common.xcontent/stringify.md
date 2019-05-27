[es-kotlin-wrapper-client](../index.md) / [org.elasticsearch.common.xcontent](index.md) / [stringify](./stringify.md)

# stringify

`fun ToXContent.stringify(pretty: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)

Helper to stringify to json.

### Parameters

`pretty` - pretty print, defaults to false`fun ToXContent.stringify(out: `[`OutputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html)`, pretty: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Helper to write json to an output stream.

### Parameters

`pretty` - pretty print, defaults to false
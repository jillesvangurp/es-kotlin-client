package io.inbot.eskotlinwrapper.manual

import org.junit.jupiter.api.Test
import kotlin.random.Random

class KotlinForExampleTest {
    @Test
    fun `Example markdown`() {
        val markdown = KotlinForExample.markdown {
            // we can inject arbitrary markdown, up to you

            +"""
                ## We can put some markdown in a string and include that

                Some text with .
                with multiple
                   lines
                   and some
                        indentation
                that we will try to trim responsibly.
            """

            +"""
                ## Block with a return value

                Here comes the magic. We find the source file, extract the block, and include that as a
                markdown code block.

                We also capture the return value of the block and print it.
            """
            block {
                val aNumber =Random.nextInt(10)
                if(aNumber > 10) {
                    "That would be unexpected"
                } else {
                    // block's return value is captured and printed
                    "Wow random works!"
                }
            }

            +"""
                ## Here's a block that returns unit

                Not all examples have to return a value.
            """

            block {
                // Unit is the return value of this block, don't print that
            }

            +"""
                ## We can also do blocks that take a buffered writer

                Buffered writers have a print and println method.
                So you can pretend to print to stdout and we capture the output

            """
            blockWithOutput {
                // Obviously ...
                println("Hello world")
            }
        }
        // what you do with the markdown is your problem. I'd suggest writing it to a file.


        println(markdown)
    }
}
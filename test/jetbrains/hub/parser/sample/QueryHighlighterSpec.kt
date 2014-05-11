package jetbrains.hub.parser.sample

import org.spek.Spek
import jetbrains.hub.parser.sample.context.queryContext
import jetbrains.jetpass.dao.query.parser.QueryParser
import kotlin.test.assertEquals
import jetbrains.mps.parser.runtime.base.StyleRange
import jetbrains.jetpass.dao.query.parser.Style
import kotlin.test.assertTrue
import org.spek.givenData

public class QueryHighlighterSpec : Spek() {{
    val groups = streamOf("jetbrains-team", "youtrack-developers")
    val queryContext = queryContext {
        field("is") {
            value("guest")
            value("admin")
            value("Maxim Mazin")
        }
        field("group") {
            value("All Users")
            values(groups)
        }

        tuple("access") {
            field("space") {
                value("Global")
            }
            field("with") {
                value("System Admin")
                value("}{a(ker ;)")
            }
        }
    }
    val queryParser = QueryParser(queryContext)

    givenData(listOf(
            TestData("empty", "", streamOf<StyleRange>()),
            TestData("or", "is: guest or  is: admin", streamOf(fn(0, 2), fv(2, 5), op(1, 2), fn(2, 2), fv(2, 5))),
            TestData("and", "is: guest and is: admin", streamOf(fn(0, 2), fv(2, 5), op(1, 3), fn(1, 2), fv(2, 5))),
            TestData("not", "not is: guest", streamOf(op(0, 3), fn(1, 2), fv(2, 5))),
            TestData("simple field", "is: guest", streamOf(fn(0, 2), fv(2, 5))),
            TestData("complex", "is: {Maxim Mazin}", streamOf(fn(0, 2), fv(2, 13))),
            TestData("tuple", "access(space: Global, with: {System Admin})", streamOf(fn(0, 6), fn(1, 5), fv(2, 6), fn(2, 4), fv(2, 14))),
            TestData("bad field name", "badField: value", streamOf(er(0, 8), fv(2, 5))),
            TestData("bad value", "is: badValue", streamOf(fn(0, 2), er(2, 8))),
            TestData("phrase", "\"Hello world!\"", streamOf(tx(0, 14))),
            TestData("parens", "((is:  guest) or ( is: admin))", streamOf(fn(2, 2), fv(3, 5), op(2, 2), fn(3, 2), fv(2, 5))),
            TestData("text", "foo and bar", streamOf(tx(0, 3), op(1, 3), tx(1, 3))),
            TestData("value with escaped chars", "access(with: {\\}{a(ker ;)})", streamOf(fn(0, 6), fn(1, 4), fv(2, 13))),
            TestData("bad tuple name", "access2(with: me)", streamOf(er(0, 7), fn(1, 4), fv(2, 2))))) {
        val (_, query, expected) = it

        on("highlight [$query]") {
            val astNode = queryParser.parseQuery(query)!!

            if (query.isNotEmpty()) {
                it("should accept query") {
                    assertTrue(astNode.accepted)
                }
            }

            var cnt = 0;
            val expectedRanges = expected.map {
                val s = cnt + it.getStartOffset()
                cnt += it.getStartOffset() + it.getLength()
                StyleRange(s, it.getLength(), it.getStyleClass(), it.getTitle())
            }
            val actualRanges = astNode.getStyleRanges()!!.stream()

            val expectedHighlight = query.highlight(expectedRanges)
            val actualHighlight = query.highlight(actualRanges)
            it("should be highlighted as $expectedHighlight") {
                assertEquals(expectedHighlight, actualHighlight)
            }
        }
    }
}


    private fun fn(start: Int, length: Int): StyleRange {
        return StyleRange(start, length, Style.FIELD_NAME.getStyleClass(), null)
    }


    private fun fv(start: Int, length: Int): StyleRange {
        return StyleRange(start, length, Style.FIELD_VALUE.getStyleClass(), null)
    }


    private fun op(start: Int, length: Int): StyleRange {
        return StyleRange(start, length, Style.OPERATOR.getStyleClass(), null)
    }


    private fun tx(start: Int, length: Int): StyleRange {
        return StyleRange(start, length, Style.TEXT.getStyleClass(), null)
    }


    private fun er(start: Int, length: Int): StyleRange {
        return StyleRange(start, length, Style.ERROR.getStyleClass(), null)
    }


    private fun String.highlight(ranges: Stream<StyleRange>): String {
        val builder = StringBuilder()
        var last = 0

        for (range in ranges) {
            if (last < range.getStartOffset()) {
                builder.append(this.substring(last, range.getStartOffset()))
            }
            builder.append(range.getStyleClass())
            builder.append('<')
            builder.append(this.substring(range.getStartOffset(), range.getStartOffset() + range.getLength()))
            builder.append('>')
            last = range.getStartOffset() + range.getLength()
        }
        if (last < this.length()) {
            builder.append(this.substring(last))
        }
        return builder.toString()
    }

    private data class TestData(val name: String, val query: String, val expected: Stream<StyleRange>) {
        override fun toString(): String {
            return name
        }
    }
}
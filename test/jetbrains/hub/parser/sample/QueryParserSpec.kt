package jetbrains.hub.parser.sample

import org.spek.Spek
import jetbrains.hub.parser.sample.context.queryContext
import jetbrains.jetpass.dao.query.parser.QueryParser
import kotlin.test.assertEquals
import org.spek.givenData

public class QueryParserSpec : Spek() {{

    val groups = streamOf("jetbrains-team", "youtrack-developers")
    val queryContext = queryContext {
        primaryField("user") {
            value("Maxim Mazin")
            value("Evgeniy Schepotiev")
        }
        field("group") {
            value("All Users")
            values(groups)
        }

        tuple("create") {
            field("name") {
                value("Muzzy")
            }
            field("with") {
                value("Clocks")
            }
        }
    }
    val queryParser = QueryParser(queryContext)


    givenData(listOf(
            TestData("field", "test:test", "test:test"),
            TestData("complex field name", "{test test2}:test", "test test2:test"),
            TestData("complex field value", "test:{test test2}", "test:test test2"),
            TestData("unclosed field value", "test:{test test2", "test:test test2"),
            TestData("text", "test", "text(test)"),
            TestData("phrase", "\"test\"", "phrase(test)"),
            TestData("parens", "(test)", "(text(test))"),
            TestData("more parens", "(((test)))", "(((text(test))))"),
            TestData("unclosed parens", "(((test", "(((text(test))))"),
            TestData("text with spaces", "Maxim Mazin", "text(Maxim Mazin)"),
            TestData("text with more spaces", "Maxim A Mazin", "text(Maxim A Mazin)"),
            TestData("or", "Maxim or Mazin", "or(text(Maxim), text(Mazin))"),
            TestData("field value with @", "name:zheka@humburg", "name:zheka@humburg"),
            TestData("tuple", "create(name:Muzzy)", "tuple/create(name:Muzzy)"),
            TestData("tuple with two fields", "create(name:Muzzy, with: Clocks)", "tuple/create(name:Muzzy, with:Clocks)"),
            TestData("tuple with four fields", "create(name:Muzzy, with:Clocks, and:Hat, in:Boots)", "tuple/create(name:Muzzy, with:Clocks, and:Hat, in:Boots)"),
            TestData("creepy field value", "create(name: {\\}{a(ker ;)})", "tuple/create(name:}{a(ker ;))"),
            TestData("or fields", "is:Guest or is:Admin", "or(is:Guest, is:Admin)")
    )) {
        val (name, query, expected) = it
        on("parse [$query]") {
            it("should be parsed to [$expected]") {
                assertEquals(expected, queryParser.parseQuery(query)?.toDebugString())
            }
        }
    }
}

    private data class TestData(val name: String, val query: String, val expected: String) {
        override fun toString(): String {
            return name
        }
    }
}
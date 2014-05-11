package jetbrains.hub.parser.sample

import org.spek.Spek
import jetbrains.hub.parser.sample.context.queryContext
import jetbrains.jetpass.dao.query.parser.QueryParser
import kotlin.test.assertEquals

public class QueryParserSpec : Spek() {{
    given("query context and query parser") {
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

        val expectations = linkedMapOf(
                "test:test" to "test:test",
                "test:test" to "test:test",
                "test test2:test" to "{test test2}:test",
                "test:test test2" to "test:{test test2}",
                "test:test test2" to "test:{test test2",
                "text(test)" to "test",
                "phrase(test)" to "\"test\"",
                "(text(test))" to "(test)",
                "(((text(test))))" to "(((test)))",
                "(((text(test))))" to "(((test",
                "text(Maxim Mazin)" to "Maxim Mazin",
                "text(Maxim A Mazin)" to "Maxim A Mazin",
                "or(text(Maxim), text(Mazin))" to "Maxim or Mazin",
                "name:zheka@humburg" to "name:zheka@humburg",
                "tuple/create(name:Muzzy)" to "create(name:Muzzy)",
                "tuple/create(name:Muzzy, with:Clocks)" to "create(name:Muzzy, with: Clocks)",
                "tuple/create(name:Muzzy, with:Clocks, and:Hat, in:Boots)" to "create(name:Muzzy, with:Clocks, and:Hat, in:Boots)",
                "tuple/create(name:}{a(ker ;))" to "create(name: {\\}{a(ker ;)})",
                "or(is:Guest, is:Admin)" to "is:Guest or is:Admin")

        for ((expected, query) in expectations) {
            on("parse [$query]") {
                it("should be parsed to [$expected]") {
                    assertEquals(expected, queryParser.parseQuery(query)?.toDebugString())
                }
            }
        }

    }
}
}
package jetbrains.hub.parser.sample

import org.spek.Spek
import jetbrains.hub.parser.sample.context.queryContext
import jetbrains.jetpass.dao.query.parser.QueryParser
import jetbrains.mps.parser.runtime.base.SuggestItem

public class QueryCompletionSpec : Spek() {{
    given("query context and query parser") {
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

        val expectations = mapOf(
                "|" to streamOf("not |", "(|)", "\"|\"", "is: |", "access(|)", "is: admin |", "is: guest |", "is: {maxim mazin} |"),
                " |" to streamOf("not |", "(|)", "\"|\"", "is: |", "access(|)", "is: admin |", "is: guest |", "is: {maxim mazin} |"),
                "n|" to streamOf("not |", "n or |", "n and |"),
                "i|" to streamOf("is: |", "i or |", "i and |"),
                "not |" to streamOf("not (|)", "not \"|\"", "not is: |", "not access(|)", "not is: admin |", "not is: guest |", "not is: {maxim mazin} |"),
                "not|" to streamOf("not |", "not (|)", "not \"|\"", "not is: |", "not access(|)", "not is: admin |", "not is: guest |", "not is: {maxim mazin} |"),
                "(|)" to streamOf("(not |)", "((|))", "(\"|\")", "(is: |)", "(access(|))", "(is: admin |)", "(is: guest |)", "(is: {maxim mazin} |)"),
                "(|" to streamOf("(not |", "((|)", "(\"|\"", "(is: |", "(access(|)", "(is: admin |", "(is: guest |", "(is: {maxim mazin} |"),
                "(text|" to streamOf("(text or |", "(text and |"),
                "is: |" to streamOf("is: admin|", "is: guest|", "is: {maxim mazin}|"),
                "is: a|" to streamOf("is: admin|", "is: a or |", "is: a and |"),
                "is: m|" to streamOf("is: {maxim mazin}|", "is: m or |", "is: m and |"),
                "is:|" to streamOf("is: admin|", "is: guest|", "is: {maxim mazin}|"),
                "access(|)" to streamOf("access(with: |)", "access(space: |)"),
                "access(sp, |)" to streamOf("access(sp, with: |)", "access(sp, space: |)"),
                "access(space: |)" to streamOf("access(space: global|)"),
                "access(space: global|)" to streamOf("access(space: global|)", "access(space: global, |)"),
                "is: admin|" to streamOf("is: admin|", "is: admin or |", "is: admin and |"),
                "is: admin |" to streamOf("is: admin or |", "is: admin and |"),
                "is: admin and |" to streamOf("is: admin and not |", "is: admin and (|)", "is: admin and \"|\"", "is: admin and is: |", "is: admin and access(|)", "is: admin and is: admin |", "is: admin and is: guest |", "is: admin and is: {maxim mazin} |"),
                "is: admin or |" to streamOf("is: admin or not |", "is: admin or (|)", "is: admin or \"|\"", "is: admin or is: |", "is: admin or access(|)", "is: admin or is: admin |", "is: admin or is: guest |", "is: admin or is: {maxim mazin} |"),
                "access(with: {sy|})" to streamOf("access(with: {system admin}|)"),
                "access(with: system ad|)" to streamOf("access(with: {system admin}|)"),
                "access(with: {system ad|})" to streamOf("access(with: {system admin}|)")
        )

        for ((query, options) in expectations) {
            on("completion of [${query}]") {

            }
        }
    }
}

    private fun String.complete(suggestItem: SuggestItem): String {
        val beforeReplace = this.substring(0, suggestItem.getCompletionStart())
        val pfx = suggestItem.getPrefix() ?: ""
        val sfx = suggestItem.getSuffix() ?: ""
        val afterReplace = this.substring(suggestItem.getCompletionEnd())
        val completed = beforeReplace + pfx + suggestItem.getOption() + sfx + afterReplace
        return completed.substring(0, suggestItem.getCaretPosition()) + "|" + completed.substring(suggestItem.getCaretPosition());
    }

}
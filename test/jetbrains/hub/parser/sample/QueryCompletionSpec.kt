package jetbrains.hub.parser.sample

import org.spek.Spek
import jetbrains.hub.parser.sample.context.queryContext
import jetbrains.jetpass.dao.query.parser.QueryParser
import jetbrains.mps.parser.runtime.base.SuggestItem
import org.spek.givenData
import kotlin.test.assertTrue
import jetbrains.jetpass.dao.query.suggest.OnePositionCompletionContextTracker
import kotlin.test.assertEquals

public class QueryCompletionSpec : Spek() {{
    val queryContext = queryContext {
        primaryField("is") {
            value("guest")
            value("admin")
            value("Maxim Mazin")
        }

        tuple("access") {
            field("with") {
                value("System Admin")
                value("}{a(ker ;)")
            }
            field("space") {
                value("Global")
            }
        }
    }
    val queryParser = QueryParser(queryContext)

    givenData(listOf(
            TestData("empty", "|", streamOf("not |", "(|)", "\"|\"", "is: |", "access(|)", "is: admin |", "is: guest |", "is: {maxim mazin} |")),
            TestData("blank", " |", streamOf("not |", "(|)", "\"|\"", "is: |", "access(|)", "is: admin |", "is: guest |", "is: {maxim mazin} |")),
            TestData("no", "no|", streamOf("not |", "no or |", "no and |")),
            TestData("i", "i|", streamOf("is: |", "is: admin |", "is: {maxim mazin} |", "i or |", "i and |")),
            TestData("not with a space", "not |", streamOf("not (|)", "not \"|\"", "not is: |", "not access(|)", "not is: admin |", "not is: guest |", "not is: {maxim mazin} |")),
            TestData("not without a space", "not|", streamOf("not |", "not (|)", "not \"|\"", "not is: |", "not access(|)", "not is: admin |", "not is: guest |", "not is: {maxim mazin} |")),
            TestData("in parens", "(|)", streamOf("(not |)", "((|))", "(\"|\")", "(is: |)", "(access(|))", "(is: admin |)", "(is: guest |)", "(is: {maxim mazin} |)")),
            TestData("after paren", "(|", streamOf("(not |", "((|)", "(\"|\"", "(is: |", "(access(|)", "(is: admin |", "(is: guest |", "(is: {maxim mazin} |")),
            TestData("after text in paren", "(text|", streamOf("(text or |", "(text and |")),
            TestData("blank field value context", "is: |", streamOf("is: admin|", "is: guest|", "is: {maxim mazin}|")),
            TestData("after a letter in field value context", "is: ad|", streamOf("is: admin|", "is: ad or |", "is: ad and |")),
            TestData("after a letter in field value context", "is: ma|", streamOf("is: {maxim mazin}|", "is: ma or |", "is: ma and |")),
            TestData("empty field value context", "is:|", streamOf("is: admin|", "is: guest|", "is: {maxim mazin}|")),
            TestData("empty tuple", "access(|)", streamOf("access(with: |)", "access(space: |)")),
            TestData("bad tuple field", "access(sp, |)", streamOf("access(sp, with: |)", "access(sp, space: |)")),
            TestData("blank tuple field value", "access(space: |)", streamOf("access(space: global|)")),
            TestData("after tuple field value", "access(space: global|)", streamOf("access(space: global|)", "access(space: global, |)")),
            TestData("after field value", "is: admin|", streamOf("is: admin|", "is: admin or |", "is: admin and |")),
            TestData("after a space after field value", "is: admin |", streamOf("is: admin or |", "is: admin and |")),
            TestData("after and", "is: admin and |", streamOf("is: admin and not |", "is: admin and (|)", "is: admin and \"|\"", "is: admin and is: |", "is: admin and access(|)", "is: admin and is: admin |", "is: admin and is: guest |", "is: admin and is: {maxim mazin} |")),
            TestData("after or", "is: admin or |", streamOf("is: admin or not |", "is: admin or (|)", "is: admin or \"|\"", "is: admin or is: |", "is: admin or access(|)", "is: admin or is: admin |", "is: admin or is: guest |", "is: admin or is: {maxim mazin} |")),
            TestData("braced field value", "access(with: {sy|})", streamOf("access(with: {system admin}|)")),
            TestData("unbraced complex field value", "access(with: system ad|)", streamOf("access(with: {system admin}|)")),
            TestData("braced complex field value", "access(with: {system ad|})", streamOf("access(with: {system admin}|)")))) {

        val (_, queryWithCaret, expectedCompletions) = it

        on("completion of [${queryWithCaret}]") {
            val (query, caret) = queryWithCaret.findCaret()

            val tracker = OnePositionCompletionContextTracker(caret)
            queryParser.setCompletionContextTracker(tracker)
            queryParser.parse(query)

            val actualCompletions = tracker.getSuggestItems(query, caret)!!.map { query.complete(it) }
            it("should suggest ${expectedCompletions.count()} options") {
                assertEquals(expectedCompletions.count(), actualCompletions.size, actualCompletions.makeString(", "))
            }

            val expI = expectedCompletions.iterator()
            val actI = actualCompletions.iterator()
            while (expI.hasNext() && actI.hasNext()) {
                val exp = expI.next()
                val act = actI.next()
                it("should suggest option $exp") {
                    assertEquals(exp, act)
                }
            }
        }
    }
}

    private fun String.findCaret(): Pair<String, Int> {
        val caret = this.indexOf('|')
        assertTrue(caret >= 0, "No caret")
        val query = this.substring(0, caret) + this.substring(caret + 1)
        return Pair(query, caret)
    }

    private fun String.complete(suggestItem: SuggestItem): String {
        val beforeReplace = this.substring(0, suggestItem.getCompletionStart())
        val pfx = suggestItem.getPrefix() ?: ""
        val sfx = suggestItem.getSuffix() ?: ""
        val afterReplace = this.substring(suggestItem.getCompletionEnd())
        val completed = beforeReplace + pfx + suggestItem.getOption() + sfx + afterReplace
        return completed.substring(0, suggestItem.getCaretPosition()) + "|" + completed.substring(suggestItem.getCaretPosition());
    }


    private data class TestData(val name: String, val query: String, val options: Stream<String>) {
        override fun toString(): String {
            return name
        }
    }
}
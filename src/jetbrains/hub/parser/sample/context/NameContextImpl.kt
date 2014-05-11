package jetbrains.hub.parser.sample.context

import jetbrains.jetpass.dao.query.context.FieldNameContext
import jetbrains.jetpass.dao.query.context.NameContext
import jetbrains.jetpass.dao.query.context.NameOption
import java.util.ArrayList
import jetbrains.hub.parser.sample.context
import jetbrains.hub.parser.sample
import java.util.HashSet

class NameContextImpl(val fields: Set<String>): BaseNameContext {
    override fun getValues(): Stream<String> {
        return fields.stream()
    }
}

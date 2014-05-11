package jetbrains.hub.parser.sample.context

import jetbrains.jetpass.dao.query.context.FieldNameContext
import jetbrains.jetpass.dao.query.context.NameContext
import jetbrains.jetpass.dao.query.context.NameOption
import java.util.ArrayList
import jetbrains.hub.parser.sample.context
import jetbrains.hub.parser.sample
import java.util.LinkedHashMap

class FieldNameContextImpl(val fields: Map<String, NameContext>) : FieldNameContext, BaseNameContext {
    override fun getValues(): Stream<String> {
        return fields.keySet().stream()
    }

    override fun getFieldValueContext(fieldName: String?): NameContext? {
        return fields[fieldName?.toLowerCase()]
    }
}
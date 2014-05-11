package jetbrains.hub.parser.sample.context

import jetbrains.jetpass.dao.query.context.TupleContext
import jetbrains.jetpass.dao.query.context.NameContext
import jetbrains.jetpass.dao.query.context.NameOption
import java.util.LinkedHashMap

class TupleContextImpl(val fields: Map<String, NameContext>): TupleContext, BaseNameContext {
    val values = LinkedHashMap<String, String>()

    override fun getValues(): Stream<String> {
        return fields.keySet().stream().filter { ! values.containsKey(it.toLowerCase()) }
    }

    override fun getFieldValueContext(fieldName: String?): NameContext? {
        return fields[fieldName?.toLowerCase()]
    }

    override fun addField(fieldName: String?, fieldValue: String?) {
        if (fieldName != null && fieldValue != null) {
            values[fieldName] = fieldValue
        }
    }
}
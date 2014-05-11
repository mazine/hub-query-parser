package jetbrains.hub.parser.sample.context

import jetbrains.jetpass.dao.query.context.QueryContext
import jetbrains.jetpass.dao.query.context.TupleNameContext
import jetbrains.jetpass.dao.query.context.FieldNameContext
import java.util.ArrayList
import jetbrains.hub.parser.sample

class QueryContextImpl(
        val _primaryFields: MutableIterable<String>,
        val _fieldNameContext: FieldNameContext,
        val _tupleNameContext: TupleNameContext): QueryContext {
    override fun getPrimaryFields(): MutableIterable<String> {
        return _primaryFields
    }
    override fun getFieldNameContext(): FieldNameContext {
        return _fieldNameContext
    }
    override fun getTupleNameContext(): TupleNameContext {
        return _tupleNameContext
    }
}
package jetbrains.hub.parser.sample.context

import jetbrains.jetpass.dao.query.context.TupleNameContext
import jetbrains.jetpass.dao.query.context.TupleContext
import jetbrains.jetpass.dao.query.context.NameOption
import jetbrains.jetpass.dao.query.context.FieldNameContext


class TupleNameContextImpl(val tuples: Map<String, () -> TupleContext>) : TupleNameContext, BaseNameContext {
    override fun getValues(): Stream<String> {
        return tuples.keySet().stream()
    }

    override fun getFieldNameContext(tupleName: String?): TupleContext? {
        return tuples[tupleName?.toLowerCase()]?.invoke()
    }
}
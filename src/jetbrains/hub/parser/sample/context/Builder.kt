package jetbrains.hub.parser.sample.context

import java.util.HashSet
import java.util.LinkedHashMap
import jetbrains.jetpass.dao.query.context.NameContext
import jetbrains.jetpass.dao.query.context.TupleContext


public fun queryContext(collect: QueryContextBuilder.() -> Unit): QueryContextImpl {
    val queryContextBuilder = QueryContextBuilder()
    queryContextBuilder.collect()
    return queryContextBuilder.build()
}

class QueryContextBuilder : FieldContextBuilder() {
    private val primaryFields = HashSet<String>()
    private val tuples = LinkedHashMap<String, () -> TupleContext>()

    fun primaryField(fieldName: String, collectValues: ValueContextBuilder.() -> Unit) {
        field(fieldName, collectValues)
        primaryFields.add(fieldName)
    }

    fun tuple(tupleName: String, collectFields: TupleContextBuilder.() -> Unit) {
        val tupleContextBuilder = TupleContextBuilder()
        tupleContextBuilder.collectFields()
        tuples[tupleName.toLowerCase()] = tupleContextBuilder.build()
    }

    fun build(): QueryContextImpl {
        return QueryContextImpl(primaryFields, FieldNameContextImpl(fields), TupleNameContextImpl(tuples))
    }
}

open class FieldContextBuilder {
    protected val fields: LinkedHashMap<String, NameContext> = LinkedHashMap<String, NameContext>()

    fun field(fieldName: String, collectValues: ValueContextBuilder.() -> Unit) {
        val valueContextBuilder = ValueContextBuilder()
        valueContextBuilder.collectValues()
        fields[fieldName.toLowerCase()] = valueContextBuilder.build()
    }
}

class TupleContextBuilder : FieldContextBuilder() {
    fun build(): () -> TupleContextImpl {
        return {
            TupleContextImpl(fields)
        }
    }
}

class ValueContextBuilder {
    private val values = HashSet<String>()

    public fun value(value: String) {
        values.add(value.toLowerCase())
    }

    public fun values(values: Stream<String>) {
        this.values.addAll(values.map { it.toLowerCase() })
    }

    fun build(): NameContextImpl {
        return NameContextImpl(values)
    }
}


package jetbrains.hub.parser.sample.context

import jetbrains.jetpass.dao.query.context.NameContext
import jetbrains.jetpass.dao.query.context.NameOption

trait BaseNameContext : NameContext {
    final override fun isValid(value: String?): Boolean {
        return getValues().contains(value?.toLowerCase())
    }

    final override fun getNameOptions(valuePrefix: String?): MutableIterable<NameOption>? {
        return getValues().toNameOptions(valuePrefix)
    }

    open fun getValues(): Stream<String>

    private fun Stream<String>.toNameOptions(prefix: String?): MutableIterable<NameOption> {
        return if (prefix == null || prefix.isEmpty()) {
            this.map {
                NameOption(it, "Some description", 0)
            }
        } else {
            this.map {
                val indexOf = it.toLowerCase().indexOf(prefix.toLowerCase())
                if (indexOf >= 0) {
                    NameOption(it, "Some description", indexOf)
                } else {
                    null
                }
            }.filterNotNull()
        }.toArrayList<NameOption>()
    }
}
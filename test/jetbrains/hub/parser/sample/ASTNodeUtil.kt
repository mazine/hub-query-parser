package jetbrains.hub.parser.sample

import jetbrains.mps.parser.runtime.context.ASTNode
import jetbrains.mps.parser.runtime.context.ASTType
import jetbrains.jetpass.dao.query.parser.QueryParser

fun <T : ASTNode>ASTType<T>.of(node: ASTNode?): Boolean {
    return node?.hasType(this) ?: false
}

fun <T : ASTNode>ASTNode.getChild(astType: ASTType<T>): T? {
    return this.children<T>(astType)?.firstOrNull()
}

fun <T : ASTNode>ASTNode.getChildren(astType: ASTType<T>): Stream<T> {
    return this.children<T>(astType)?.stream() ?: streamOf<T>()
}

fun StringBuilder.appendNode(node: ASTNode?): StringBuilder {
    if (node == null) return this

    return when {
        QueryParser.START.of(node) -> appendNode(node.getChild(QueryParser.OR_EXPRESSION))
        QueryParser.OR_EXPRESSION.of(node) -> appendNodes("or", node.getChildren(QueryParser.AND_EXPRESSION))
        QueryParser.AND_EXPRESSION.of(node) -> appendNodes("and", node.getChildren(QueryParser.ITEM))
        QueryParser.ITEM.of(node) -> appendNode(node.children<ASTNode>(QueryParser.NOT_EXPRESSION, QueryParser.FIELD, QueryParser.TUPLE, QueryParser.PHRASE, QueryParser.TEXT, QueryParser.PAREN)?.firstOrNull())
        QueryParser.NOT_EXPRESSION.of(node) -> append("not ").appendNode(node.getChild(QueryParser.ITEM))
        QueryParser.FIELD.of(node) -> appendNode(node.getChild(QueryParser.FIELD_NAME)).append(':').appendNode(node.getChild(QueryParser.FIELD_VALUE))
        node is QueryParser.FieldNameASTNode -> append(node.value)
        node is QueryParser.FieldValueASTNode -> append(node.value)
        QueryParser.TUPLE.of(node) -> append("tuple/").appendNode(node.getChild(QueryParser.FIELD_NAME)).appendNodes("", node.getChildren(QueryParser.FIELD), wrapAlone = true)
        node is QueryParser.PhraseASTNode -> append("phrase(").append(node.value).append(")")
        node is QueryParser.TextASTNode -> append("text(").append(node.value).append(")")
        QueryParser.PAREN.of(node) -> append("(").appendNode(node.getChild(QueryParser.OR_EXPRESSION)).append(")")
        else -> this
    }
}


fun StringBuilder.appendNodes(symbol: String, nodes: Stream<ASTNode>, wrapAlone: Boolean = false): StringBuilder {
    return when {
        !nodes.iterator().hasNext() -> this
        !wrapAlone && !nodes.drop(1).iterator().hasNext() -> appendNode(nodes.first())
        else -> {
            append(symbol).append("(")
            var first = true
            nodes.forEach {
                if (first) {
                    first = false
                } else {
                    append(", ")
                }
                appendNode(it)
            }
            append(")")
        }
    }
}

fun ASTNode.toDebugString(): String {
    val builder = StringBuilder()
    builder.appendNode(this)
    return builder.toString()
}
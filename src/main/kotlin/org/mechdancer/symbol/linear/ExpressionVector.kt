package org.mechdancer.symbol.linear

import org.mechdancer.algebra.core.Vector
import org.mechdancer.algebra.implement.vector.toListVector
import org.mechdancer.symbol.core.Expression
import org.mechdancer.symbol.core.ExpressionStruct
import org.mechdancer.symbol.core.VariableSpace
import org.mechdancer.symbol.core.parallelism
import org.mechdancer.symbol.sqrt
import org.mechdancer.symbol.sumBy
import org.mechdancer.symbol.times
import kotlin.streams.toList

/** unnamed vector of simplified expressions */
@JvmInline value class ExpressionVector(
    val expressions: List<Expression>
) : ExpressionStruct<Vector> {
    fun length() = sqrt(expressions.sumBy { it * it })

    override fun toFunction(space: VariableSpace): (Vector) -> Vector {
        val list = expressions.map { it.toFunction(space) }
        return if (list.size > parallelism)
            { v -> list.parallelStream().mapToDouble { it(v) }.toList().toListVector() }
        else
            { v -> list.map { it(v) }.toListVector() }
    }
}

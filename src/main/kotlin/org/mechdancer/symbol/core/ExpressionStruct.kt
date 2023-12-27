package org.mechdancer.symbol.core

import org.mechdancer.algebra.core.Vector

/** An arithmetic structure composed of expressions. When a numerical value is added, it will be converted into [T] */
interface ExpressionStruct<T> {
    /** The specified structure is located in space [space], and the construction brings in the operation closure */
    fun toFunction(space: VariableSpace): (Vector) -> T
}

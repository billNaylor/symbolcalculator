package org.mechdancer.symbol.core

import org.mechdancer.algebra.core.Vector

/**
 * An arithmetic structure composed of expressions. When a numerical value is added, it will be converted into [T]
 *
 * 表达式组成的算数结构，带入数值将转化为 [T]
 */
interface ExpressionStruct<T> {
    /** The specified structure is located in space [space], and the construction brings in the operation closure */
    /** 指定结构位于空间 [space] 中，构造带入运算闭包 */
    fun toFunction(space: VariableSpace): (Vector) -> T
}

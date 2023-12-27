package org.mechdancer.symbol.core

import org.mechdancer.symbol.core.Constant.Companion.`1`

/** Differentiable expression */
interface Expression : ExpressionStruct<Double> {
    /** Find the total differential of the expression */
    fun d(): Expression

    /** Will use [from] [to] substitute */
    fun substitute(from: Expression, to: Expression): Expression

    /** Will try to replace keys with values ​​from [map] */
    fun substitute(map: Map<out FunctionExpression, Expression>): Expression

    /** Specify the structure with respect to the variable [v], and the construction brings in the operation closure */
    fun toFunction(v: Variable): (Double) -> Double

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    override fun toString(): String
    fun toTex(): TeX = toString()

    // region Provide optimization opportunities for operations with constants

    operator fun plus(c: Constant): Expression = Sum[c, this]
    operator fun minus(c: Constant): Expression = Sum[-c, this]
    operator fun times(c: Constant): Expression = Product[c, this]
    operator fun div(c: Constant): Expression = Product[`1` / c, this]

    // endregion
}

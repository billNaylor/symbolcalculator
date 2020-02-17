package org.mechdancer.symbol

import org.mechdancer.symbol.Constant.Companion.`1`

/** 可微表达式 */
interface Expression {
    /** 对 [v] 求偏导 */
    infix fun d(v: Variable): Expression

    /** 将 [from] 用 [to] 代换 */
    fun substitute(from: Expression, to: Expression): Expression

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    // region 为与常数之间的运算提供优化机会

    operator fun plus(c: Constant): Expression = Sum[c, this]
    operator fun minus(c: Constant): Expression = Sum[-c, this]
    operator fun times(c: Constant): Expression = Product[c, this]
    operator fun div(c: Constant): Expression = Product[`1` / c, this]

    // endregion
}

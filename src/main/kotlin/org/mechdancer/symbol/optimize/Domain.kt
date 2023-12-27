package org.mechdancer.symbol.optimize

import org.mechdancer.symbol.`^`
import org.mechdancer.symbol.core.Constant
import org.mechdancer.symbol.core.Constant.Companion.e
import org.mechdancer.symbol.core.Variable
import org.mechdancer.symbol.linear.NamedExpressionVector
import org.mechdancer.symbol.minus
import org.mechdancer.symbol.plus

/** Constraints on variable [v] in the interval [[min], [max]] */
data class Domain(val v: Variable, val min: Constant, val max: Constant) {
    private val function by lazy {
        when {
            min.re.isInfinite() -> e `^` v - max
            max.re.isInfinite() -> e `^` min - v
            else                -> (e `^` min - v) + (e `^` v - max)
        }
    }

    /** Substituting into the expression vector,
     * if the variable is not in the target area, a linear loss term is generated
     */
    fun mapLinear(p: NamedExpressionVector) =
        (p[v] as? Constant)?.let {
            when {
                it < min -> Triple(v, v * (it - min), it - min)
                it > max -> Triple(v, v * (it - max), it - max)
                else     -> null
            }
        }

    /** Substituting into the expression vector,
     *if the variable is not in the target area, an exponential loss term is generated.
     */
    fun mapExp(p: NamedExpressionVector) =
        (p[v] as? Constant)?.let {
            when {
                it < min -> Triple(v, function, it - min)
                it > max -> Triple(v, function, it - max)
                else     -> null
            }
        }
}

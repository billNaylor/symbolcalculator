package org.mechdancer.symbol.optimize

import org.mechdancer.symbol.core.*
import org.mechdancer.symbol.div
import org.mechdancer.symbol.linear.NamedExpressionVector
import org.mechdancer.symbol.substitute
import org.mechdancer.symbol.toDouble
import org.mechdancer.symbol.variable
import kotlin.math.abs
import kotlin.math.sign

/** Construct a value range structure */
operator fun Variable.get(min: Constant, max: Constant) =
    Domain(this, min, max)

/** Optimization step function:= current position -> (new position, actual step size) */
typealias OptimizeStep<T> = (T) -> Pair<T, Double>

/** Use Newton iteration to find the minimum value of the one-variable function [f]
 * with respect to the variable [v]
 */
fun newton(
    f: Expression,
    v: Variable
): OptimizeStep<Double> {
    operator fun Expression.get(x: Double) =
        substitute(v, Constant(x)).toDouble()

    val dv = Differential(v)
    val df = f.d() / dv

    val ndf = df.toFunction(v)
    val d2f = (df.d() / dv).toFunction(v)

    return { p ->
        val g = ndf(p)
        val l = abs(g / d2f(p))
        p - g.sign * l to l
    }
}

/** Determining the optimal rate of descent using Newton's method */
internal fun fastestWithNewton(
    e: Expression,
    p: NamedExpressionVector,
    dp: NamedExpressionVector
): Pair<NamedExpressionVector, Double> {
    val l by variable
    val next = p - dp * l
    val a = optimize(1.0, 20, 1e-9, newton(e.substitute(next), l))
    return next.substitute(l, Constant(a)) to dp.length().toDouble() * abs(a)
}

/** Mapping inequality constraints and steepest descent using Newton's method */
internal inline fun Array<out Domain>.fastestOf(
    e: Expression,
    p: NamedExpressionVector,
    dp: NamedExpressionVector,
    which: Domain.(NamedExpressionVector) -> Triple<Variable, Expression, Constant>?
): Pair<NamedExpressionVector, Double> {
    val limit = mapNotNull { it.which(p) }
    return if (limit.isNotEmpty()) {
        val en = Sum[limit.map { (_, e, _) -> e } + e]
        val dn = limit.associate { (v, _, d) -> v to d }.let(::NamedExpressionVector)
        fastestWithNewton(en, p, dp + dn)
    } else
        fastestWithNewton(e, p, dp)
}

/** Optimization calculation */
inline fun <T> optimize(
    init: T,
    maxTimes: Int,
    minStep: Double,
    block: OptimizeStep<T>
): T {
    var t = init
    repeat(maxTimes) {
        val (p, s) = block(t)
        if (s < minStep) return p
        t = p
    }
    return t
}

/** Recursive calculation */
fun <T> recurrence(init: T, block: (T) -> T) =
    sequence {
        var t = init
        while (true) {
            t = block(t)
            yield(t)
        }
    }

/** Convergence or exit */
inline fun <T : Any> Sequence<T>.firstOrLast(block: (T) -> Boolean): T {
    var last: T? = null
    for (t in this) {
        if (block(t)) return t
        last = t
    }
    return last ?: throw NoSuchElementException("Sequence is empty.")
}

/** Collection conditions */
inline fun conditions(block: ConditionCollector.() -> Unit) =
    ConditionCollector().also(block).build()

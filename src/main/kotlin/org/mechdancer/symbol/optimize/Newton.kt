package org.mechdancer.symbol.optimize

import org.mechdancer.algebra.function.matrix.inverse
import org.mechdancer.algebra.function.matrix.times
import org.mechdancer.algebra.function.vector.dot
import org.mechdancer.algebra.function.vector.plus
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.symbol.core.Expression
import org.mechdancer.symbol.core.VariableSpace
import org.mechdancer.symbol.linear.Hamiltonian.Companion.gradient
import org.mechdancer.symbol.linear.Hessian.Companion.hessian
import org.mechdancer.symbol.linear.NamedExpressionVector

/**
 * Basic multivariate Newton iteration method
 *
 * @param error loss function
 * @param space variable space
 * @return optimization step function
 */
fun newton(
    error: Expression,
    space: VariableSpace
): OptimizeStep<NamedExpressionVector> {
    val df = error.d()
    val gradient = gradient(df, space).toFunction(space)
    val hessian = hessian(df.d(), space).toFunction(space)
    return { p ->
        val v = p.toVector(space)
        val g = gradient(v)
        val h = hessian(v).inverse() * g
        val s = h dot g
        val step = if (s < 0) g else {
            val k = s / h.length / g.length
            h * k + g * (1 - k)
        }
        p - space.order(step) to step.length
    }
}

/**
 * Damped Newton iterative optimization
 *
 * @param error loss function
 * @param space variable space
 * @return optimization step function
 */
fun dampingNewton(
    error: Expression,
    space: VariableSpace,
    vararg domains: Domain
): OptimizeStep<NamedExpressionVector> {
    // differential
    val df = error.d()
    val gradient = gradient(df, space).toFunction(space)
    val hessian = hessian(df.d(), space).toFunction(space)
    return { p ->
        val v = p.toVector(space)
        val g = gradient(v)
        val h = hessian(v).inverse() * g
        // Determine the optimal descent direction
        val dp = if (g dot h < 0) g else h
        domains.fastestOf(error, p, space.order(dp), Domain::mapLinear)
    }
}

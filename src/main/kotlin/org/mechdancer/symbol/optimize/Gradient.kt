package org.mechdancer.symbol.optimize

import org.mechdancer.algebra.function.vector.plus
import org.mechdancer.algebra.implement.vector.toListVector
import org.mechdancer.symbol.core.Expression
import org.mechdancer.symbol.core.VariableSpace
import org.mechdancer.symbol.linear.Hamiltonian.Companion.gradient
import org.mechdancer.symbol.linear.NamedExpressionVector
import org.mechdancer.symbol.toDouble

/**
 * Standard full gradient descent optimization
 *
 * @param error loss function
 * @param space variable space
 * @param controller gradient controller
 * @return optimization step function
 */
fun batchGD(
    error: Expression,
    space: VariableSpace,
    vararg domains: Domain,
    controller: LinearController
): OptimizeStep<NamedExpressionVector> {
    val gradient = gradient(error.d(), space).toFunction(space)
    return { p ->
        val v = p.toVector(space)
        val limit = domains.mapNotNull { it.mapLinear(p) }
        val g = if (limit.isNotEmpty()) {
            val values = limit.associate { (v, _, d) -> v to d.toDouble() }
            gradient(v) + space.variables.map { values[it] ?: .0 }.toListVector()
        } else {
            gradient(v)
        }
        val h = controller(g)
        p - space.order(h) to h.length
    }
}

/**
 * Steepest descent method using Newton iteration to determine step size
 *
 * @param error loss function
 * @param space variable space
 * @return optimization step function
 */
fun fastestBatchGD(
    error: Expression,
    space: VariableSpace,
    vararg domains: Domain
): OptimizeStep<NamedExpressionVector> {
    val gradient = gradient(error.d(), space).toFunction(space)
    return { p ->
        val dp = space.order(gradient(p.toVector(space)))
        domains.fastestOf(error, p, dp, Domain::mapLinear)
    }
}

/**
 * Stochastic gradient descent method using mean square loss function and large step size priority
 *
 * @param errors sample loss function
 * @param block dependent batch gradient descent function
 * @return optimization step function
 */
inline fun stochasticGD(
    errors: List<Expression>,
    block: (error: Expression) -> OptimizeStep<NamedExpressionVector>
): OptimizeStep<NamedExpressionVector> {
    val dim = errors.size
    // Sample step function
    val steps = errors.map(block)
    // iterative function
    var i = 0
    return { steps[i++ % dim](it) }
}

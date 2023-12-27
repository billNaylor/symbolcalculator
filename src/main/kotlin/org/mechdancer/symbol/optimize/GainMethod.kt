package org.mechdancer.symbol.optimize

import org.mechdancer.algebra.core.Vector
import org.mechdancer.algebra.function.vector.times

/** Gain the vector */
class GainMethod(
    override val dim: Int,
    private val k: Double
) : LinearController {
    override fun invoke(signal: Vector) =
        signal * k
}

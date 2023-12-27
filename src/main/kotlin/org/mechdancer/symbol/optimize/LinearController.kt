package org.mechdancer.symbol.optimize

import org.mechdancer.algebra.core.Vector

/** Linear controller := (vector) -> vector */
interface LinearController {
    /** The input and output vectors must have certain dimensions in order to be initialized. */
    val dim: Int

    /** Signal passes through the controller */
    operator fun invoke(signal: Vector): Vector
}

package org.mechdancer.symbol.linear

import org.mechdancer.symbol.core.Differential
import org.mechdancer.symbol.core.Expression
import org.mechdancer.symbol.core.VariableSpace
import org.mechdancer.symbol.div

/** Hamiltonian operator (gradient operator) */
inline class Hamiltonian(private val space: VariableSpace) {
    /** Find the gradient of the [f] quantity field on this variable space */
    operator fun times(f: Expression) =
        gradient(f.d(), space)

    companion object {
        fun gradient(df: Expression, space: VariableSpace) =
            ExpressionVector(space.variables.map { df / Differential(it) })
    }
}

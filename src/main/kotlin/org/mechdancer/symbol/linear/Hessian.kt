package org.mechdancer.symbol.linear

import org.mechdancer.symbol.`^-1`
import org.mechdancer.symbol.core.Differential
import org.mechdancer.symbol.core.Expression
import org.mechdancer.symbol.core.VariableSpace
import org.mechdancer.symbol.mapParallel
import org.mechdancer.symbol.times

/** Hessian operator */
@JvmInline value class Hessian(val space: VariableSpace) {
    /** Find the Hessian matrix of [f] quantity field on this variable space */
    operator fun times(f: Expression) =
        hessian(f.d().d(), space)

    companion object {
        fun hessian(ddf: Expression, space: VariableSpace): HessianMatrix {
            val d = space.variables.map(::Differential)
            // The Hessian matrix is ​​a symmetric matrix and only saves and calculates the lower triangle
            return sequence { for (r in space.variables.indices) for (c in 0..r) yield((d[r] * d[c]).`^-1`) }
                .toList()
                .mapParallel { ddf * it }
                .let(::HessianMatrix)
        }
    }
}

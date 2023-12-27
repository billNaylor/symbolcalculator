package org.mechdancer.symbol.core

import org.mechdancer.algebra.core.Vector
import org.mechdancer.symbol.linear.Hamiltonian
import org.mechdancer.symbol.linear.Hessian
import org.mechdancer.symbol.linear.NamedExpressionVector

/** Variable space */
inline class VariableSpace(val variables: List<Variable>) {
    /** Dimensions of space */
    val dim get() = variables.size

    /** Ordinary field on this space */
    val ordinaryField get() = NamedExpressionVector(variables.associateWith { it })

    /** Hamiltonian operator on this space */
    val hamiltonian get() = Hamiltonian(this)

    /** Hessian operator on this space */
    val hessian get() = Hessian(this)

    /** The vector becomes an expression vector */
    fun order(vector: Vector) =
        variables
            .mapIndexed { i, v -> v to Constant(vector[i]) }
            .toMap()
            .let(::NamedExpressionVector)

    /** Find the intersection space of variable spaces */
    operator fun times(others: VariableSpace) =
        variables(variables.toSet() intersect others.variables.toSet())

    /** Find the union space of variable spaces */
    operator fun plus(others: VariableSpace) =
        variables(variables + others.variables)

    /** Find the difference space of variable space */
    operator fun minus(others: VariableSpace) =
        variables(variables - others.variables)

    companion object {
        fun variables(variables: Iterable<Variable>) =
            VariableSpace(variables.toSortedSet().toList())

        fun variables(vararg names: String) =
            VariableSpace(names.toSortedSet().map(::Variable))

        fun variables(range: CharRange) =
            VariableSpace(range.map { Variable(it.toString()) })

        val xyz = variables('x'..'z')
        val characters = variables('a'..'z')
    }
}
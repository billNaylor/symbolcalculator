package org.mechdancer.symbol.core

import org.mechdancer.algebra.core.Vector
import org.mechdancer.symbol.linear.Hamiltonian
import org.mechdancer.symbol.linear.Hessian
import org.mechdancer.symbol.linear.NamedExpressionVector

/**
 * Variable space
 *
 * 变量空间
 */
@JvmInline value class VariableSpace(val variables: List<Variable>) {
    /** Dimensions of space */
    /** 空间的维度 */
    val dim get() = variables.size

    /** Ordinary field on this space */
    /** 此空间上的平凡场 */
    val ordinaryField get() = NamedExpressionVector(variables.associateWith { it })

    /** Hamiltonian operator on this space */
    /** 此空间上的哈密顿算子 */
    val hamiltonian get() = Hamiltonian(this)

    /** Hessian operator on this space */
    /** 此空间上的海森算子 */
    val hessian get() = Hessian(this)

    /** The vector becomes an expression vector */
    /** 向量变为表达式向量 */
    fun order(vector: Vector) =
        variables
            .mapIndexed { i, v -> v to Constant(vector[i]) }
            .toMap()
            .let(::NamedExpressionVector)

    /** Find the intersection space of variable spaces */
    /** 求变量空间的交空间 */
    operator fun times(others: VariableSpace) =
        variables(variables.toSet() intersect others.variables.toSet())

    /** Find the union space of variable spaces */
    /** 求变量空间的并空间 */
    operator fun plus(others: VariableSpace) =
        variables(variables + others.variables)

    /** Find the difference space of variable space */
    /** 求变量空间的差空间 */
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
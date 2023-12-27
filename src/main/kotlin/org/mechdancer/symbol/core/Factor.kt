@file:Suppress("MemberVisibilityCanBePrivate")

package org.mechdancer.symbol.core

import org.mechdancer.algebra.core.Vector
import org.mechdancer.symbol.core.Constant.Companion.`0`
import org.mechdancer.symbol.core.Constant.Companion.`1`
import org.mechdancer.symbol.core.Constant.Companion.`-1`
import org.mechdancer.symbol.core.Constant.Companion.ln
import kotlin.math.pow
import kotlin.math.sign

/**
 * Factor, an object that cannot be combined as a product component
 *
 * Factors are basic elementary functions
 * Basic elementary functions are functions with one and only one parameter (expression)
 * The addition, subtraction, multiplication, division and composition
 * of basic elementary functions are called elementary functions
 */
sealed class Factor : FactorExpression {
    /** Elementary functions are all unary functions, with one and only one elementary function parameter */
    internal abstract val member: FunctionExpression

    /** Determine whether it is a basic elementary function */
    val isBasic
        get() = member is Variable

    /** Chain rule for derivation of composite functions */
    final override fun d() = Product[df, member.d()]

    /**
     * Substitution rules for composite functions
     *
     * Check the form of the function, and substitute the basic elementary functions directly,
     * otherwise expand and substitute
     */
    final override fun substitute(from: Expression, to: Expression) =
        when (from) {
            this -> to
            member -> substituteMember(to)
            else -> substituteMember(member.substitute(from, to))
        }
    /**
     * The multiple bring-in rule for composite functions
     *
     * Check the form of the function, and substitute the basic elementary functions directly, otherwise expand and substitute
     */
    final override fun substitute(map: Map<out FunctionExpression, Expression>) =
        map[this]
        ?: map[member]?.let(this::substituteMember)
        ?: substituteMember(member.substitute(map))

    /** Expand one layer of the chain rule */
    protected abstract val df: Expression

    /** Substitute into constructor */
    protected abstract fun substituteMember(e: Expression): Expression

    /** Add brackets to the components of the composite function */
    protected val parameterString get() = if (isBasic) "$member" else "($member)"

    /** Add brackets to the components of the composite function */
    protected val parameterTex get() = if (isBasic) member.toTex() else "(${member.toTex()})"
}

/** Power factor */
class Power private constructor(
    override val member: BaseExpression,
    val exponent: Constant
) : Factor(),
    ExponentialExpression {
    init {
        // As a derivative operator, the order can only be an integer
        if (member is Differential) require(exponent.re == exponent.re.toInt().toDouble())
    }

    override val df by lazy { get(member, exponent - `1`) * exponent }
    override fun substituteMember(e: Expression) = get(e, exponent)
    override fun toFunction(v: Variable): (Double) -> Double =
        member.toFunction(v).let { { n -> it(n).pow(exponent.re) } }

    override fun toFunction(space: VariableSpace): (Vector) -> Double =
        member.toFunction(space).let { { v -> it(v).pow(exponent.re) } }

    override fun equals(other: Any?) =
        this === other || other is Power && exponent == other.exponent && member == other.member

    override fun hashCode() = member.hashCode() xor exponent.hashCode()
    override fun toString() = "$parameterString^${exponent.toStringAsComponent()}"
    override fun toTex(): TeX =
        when (exponent) {
            Constant(.5)  -> "\\sqrt{${member.toTex()}}"
            Constant(-.5) -> "\\frac{1}{\\sqrt{${member.toTex()}}}"
            else          -> "{$parameterTex}^{${exponent.toTex()}}"
        }

    companion object Builder {
        operator fun get(b: Expression, e: Constant): Expression {
            fun simplify(f: FunctionExpression): Expression =
                when (f) {
                    is BaseExpression -> Power(f, e)
                    is Power          -> get(f.member, f.exponent * e)
                    is Exponential    -> Exponential[f.base, get(f.member, e)]
                    else              -> throw UnsupportedOperationException()
                }

            return when (e) {
                `0`  -> `1`
                `1`  -> b
                else -> when (b) {
                    `0`                 -> `0`
                    is Constant         -> b pow e
                    is FactorExpression -> simplify(b)
                    is Product          -> Product[b.factors.map(::simplify)] * (b.times pow e)
                    is Sum              -> Power(b, e)
                    else                -> throw UnsupportedOperationException()
                }
            }
        }
    }
}

/** exponential factor */
class Exponential private constructor(
    val base: Constant,
    override val member: ExponentialExpression
) : Factor(),
    BaseExpression,
    ExponentialExpression {
    override val df by lazy { this * ln(base) }
    override fun substituteMember(e: Expression) = get(base, e)

    override fun toFunction(v: Variable): (Double) -> Double =
        member.toFunction(v).let { { n -> base.re.pow(it(n)) } }

    override fun toFunction(space: VariableSpace): (Vector) -> Double =
        member.toFunction(space).let { { v -> base.re.pow(it(v)) } }

    override fun equals(other: Any?) =
        this === other || other is Exponential && base == other.base && member == other.member

    override fun hashCode() = base.hashCode() xor member.hashCode()
    override fun toString() = "${base.toStringAsComponent()}^$parameterString"
    override fun toTex(): TeX = "{${base.toTex()}}^{${member.toTex()}}"

    companion object Builder {
        operator fun get(e: Expression) =
            get(Constant.e, e)

        operator fun get(b: Constant, e: Expression): Expression =
            when (b) {
                `0`  -> `0`
                `1`  -> `1`
                else -> when (e) {
                    is Constant              -> b pow e
                    is Product               ->
                        when (val core = e.resetTimes(`1`)) {
                            is Product -> Exponential(b pow e.times, core)
                            else       -> get(b pow e.times, core)
                        }
                    is ExponentialExpression -> Exponential(b, e)
                    is Ln                    -> Power[e.member, ln(b)]
                    is Sum                   -> Product[e.products.map { get(b, it) }] * (b pow e.tail)
                    else                     -> throw UnsupportedOperationException()
                }
            }
    }
}

/** natural logarithm factor */
class Ln private constructor(
    override val member: LnExpression
) : Factor(),
    BaseExpression,
    LnExpression {
    override val df by lazy { Power[member, `-1`] }
    override fun substituteMember(e: Expression) = get(e)

    override fun toFunction(v: Variable): (Double) -> Double =
        member.toFunction(v).let { { n -> kotlin.math.ln(it(n)) } }

    override fun toFunction(space: VariableSpace): (Vector) -> Double =
        member.toFunction(space).let { { v -> kotlin.math.ln(it(v)) } }

    override fun equals(other: Any?) = this === other || other is Ln && member == other.member
    override fun hashCode() = member.hashCode()
    override fun toString() = "ln$parameterString"
    override fun toTex(): TeX = "\\ln $parameterTex"

    companion object Builder {
        private fun simplify(f: FactorExpression) =
            when (f) {
                is LnExpression -> Ln(f)
                is Power        -> get(f.member) * f.exponent
                is Exponential  -> f.member * ln(f.base)
                else            -> throw UnsupportedOperationException()
            }

        operator fun get(e: Expression): Expression =
            when (e) {
                is Constant         -> ln(e)
                is FactorExpression -> simplify(e)
                is Product          -> {
                    val groups = e.factors.groupBy { it is Exponential }
                    val exps = groups[true]?.map { it as Exponential; get(it.member) * ln(it.base) } ?: emptyList()
                    val others = groups[false] ?: emptyList()
                    val sign = Constant(e.times.re.sign)
                    Sum[Ln((Product[others] * sign) as LnExpression), Sum[exps], ln(e.times / sign)]
                }
                is Sum              -> Ln(e)
                else                -> throw UnsupportedOperationException()
            }

        operator fun get(base: Constant, e: Expression): Expression =
            when {
                base <= `0` || base == `1` -> throw IllegalArgumentException()
                else                       -> get(e) / ln(base)
            }
    }
}

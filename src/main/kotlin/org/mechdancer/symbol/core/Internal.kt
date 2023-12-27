package org.mechdancer.symbol.core

import java.util.concurrent.ForkJoinPool

// {Variables, sums, exponential factors, logarithmic factors} = {base subfunction} ⊆ {function}
// {variable, product, power factor, exponential factor} = {exponential subfunction} ⊆ {function}
// {variable, sum, logarithmic factor} = {logarithmic subfunction} ⊆ {function}

internal val parallelism = ForkJoinPool.getCommonPoolParallelism() * 2

/** product expression */
internal interface ProductExpression : FunctionExpression

/** Factor expression */
internal interface FactorExpression : ProductExpression

/** Base expression */
internal interface BaseExpression : FunctionExpression

/** Exponential expression */
internal interface ExponentialExpression : FunctionExpression

/** Logarithmic expression */
internal interface LnExpression : FunctionExpression

/** TeX syntax representation */
internal typealias TeX = String

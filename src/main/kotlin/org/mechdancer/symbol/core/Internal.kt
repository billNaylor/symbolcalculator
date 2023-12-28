package org.mechdancer.symbol.core

import java.util.concurrent.ForkJoinPool

// {Variables, sums, exponential factors, logarithmic factors} = {base subfunction} ⊆ {function}
// {variable, product, power factor, exponential factor} = {exponential subfunction} ⊆ {function}
// {variable, sum, logarithmic factor} = {logarithmic subfunction} ⊆ {function}
// {变量，和式，指数因子，对数因子} = {底数子函数} ⊆ {函数}
// {变量，积式，幂因子，指数因子} = {指数子函数} ⊆ {函数}
// {变量，和式，对数因子} = {对数子函数} ⊆ {函数}

internal val parallelism = ForkJoinPool.getCommonPoolParallelism() * 2

/** product expression */
/** 积表达式 */
internal interface ProductExpression : FunctionExpression

/** Factor expression */
/** 因子表达式 */
internal interface FactorExpression : ProductExpression

/** Base expression */
/** 底数表达式 */
internal interface BaseExpression : FunctionExpression

/** Exponential expression */
/** 指数表达式 */
internal interface ExponentialExpression : FunctionExpression

/** Logarithmic expression */
/** 对数表达式 */
internal interface LnExpression : FunctionExpression

/** TeX syntax representation */
/** TeX 语法表示 */
internal typealias TeX = String

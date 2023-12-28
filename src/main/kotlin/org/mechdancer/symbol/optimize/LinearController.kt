package org.mechdancer.symbol.optimize

import org.mechdancer.algebra.core.Vector

/**
 * Linear controller := (vector) -> vector
 *
 * 线性控制器 := (向量) -> 向量
 */
interface LinearController {
    /** The input and output vectors must have certain dimensions in order to be initialized. */
    /** 输入输出向量必须有确定的维数，以便进行初始化 */
    val dim: Int

    /** Signal passes through the controller */
    /** 信号通过控制器 */
    operator fun invoke(signal: Vector): Vector
}

package org.mechdancer.symbol.system

/**
 * Each measurement establishes a relationship between two anchor points
 *
 * 每次测量在两个定位点之间建立一个关系
 */
data class Measure(
    val a: Position,
    val b: Position,
    val time: Long,
    val distance: Double
) {
    init {
        // This relationship is unordered for both labels
        // 这种关系对两个标签来说是无序的
        require(a.beacon.id < b.beacon.id)
    }
}

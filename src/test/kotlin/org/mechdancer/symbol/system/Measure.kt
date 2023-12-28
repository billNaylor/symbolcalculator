package org.mechdancer.symbol.system

/**
 * Each measurement establishes a relationship between two anchor points
 */
data class Measure(
    val a: Position,
    val b: Position,
    val time: Long,
    val distance: Double
) {
    init { // This relationship is unordered for both labels
        require(a.beacon.id < b.beacon.id)
    }
}

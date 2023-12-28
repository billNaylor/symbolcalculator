package org.mechdancer.symbol.system

/**
 * Each tag corresponds to a specific physical entity
 * Each meaningful label movement corresponds to a specific anchor point
 */
@JvmInline value class Beacon(val id: Int) : Comparable<Beacon> {
    fun static() = Position(this, -1)
    fun move(t: Long) = Position(this, t)
    override fun compareTo(other: Beacon) = id.compareTo(other.id)
    override fun toString() = "[$id]"
}

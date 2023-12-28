package org.mechdancer.symbol.system

import org.mechdancer.algebra.function.vector.euclid
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.Vector3D
import org.mechdancer.algebra.implement.vector.to3D
import org.mechdancer.algebra.implement.vector.vector3DOfZero
import org.mechdancer.symbol.core.VariableSpace.Companion.variables
import org.mechdancer.symbol.minus
import org.mechdancer.symbol.optimize.*
import org.mechdancer.symbol.sum
import org.mechdancer.symbol.system.LocatingSystem.Companion.get
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.random.Random.Default.nextDouble

/** A positioning system consisting of tags with a maximum measurable [maxMeasure] length */
class LocatingSystem(private val maxMeasure: Double) {
    // Drawing subroutine
    var painter: (Map<Beacon, Vector3D>) -> Unit = {}

    // Length storage
    private val measures =
        hashMapOf<Pair<Position, Position>, MutableList<Double>>()

    //Coordinate storage
    private val positions =
        hashMapOf<Beacon, SortedMap<Long, Vector3D>>()

    //Relationship cache, used to speed up queries
    private val relationMemory =
        hashMapOf<Position, SortedSet<Position>>()

    /** Store the ranging [l] from [a] to [b] at time [time] */
    operator fun set(a: Position, b: Position, time: Long, l: Double) {
        // For a new location point, copy the latest known coordinates of the same label, or randomly generate a coordinate
        fun Position.copyLastOrRandom() {
            positions.update(
                beacon,
                { map -> map.computeIfAbsent(time) { map[map.lastKey()]!! } },
                { sortedMapOf(time to Vector3D(nextDouble(), nextDouble(), nextDouble()) * 1e-4) })
        }

        require(a != b)
        a.copyLastOrRandom()
        b.copyLastOrRandom()
        measures.update(sortedPairOf(a, b), { it += l }, { mutableListOf(l) })
        relationMemory.update(a, { it += b }, { sortedSetOf(a, b) })
        relationMemory.update(b, { it += a }, { sortedSetOf(b, a) })
    }

    /** Stores a set of ranging measurements at time [t] of [measures] */
    operator fun set(t: Long, measures: Map<Pair<Position, Position>, Double>) {
        for ((pair, l) in measures) {
            val (a, b) = pair
            this[a, b, t] = l
        }
    }

    /** Optimize all coordinates using all known measurement data */
    fun optimize() {
        calculate(positions.flatMap { (beacon, set) -> set.keys.map(beacon::move) }.toSortedSet())
    }

    /** Optimize a label and all labels directly ranging from this label */
    operator fun get(beacon: Beacon): Map<Beacon, Vector3D> {
        val p = positions[beacon]?.lastKey()?.let(beacon::move)
                ?: return mapOf(beacon to vector3DOfZero())
        return relationMemory[p]!!.let {
            positions[p] = calculate(p, it - p)
            calculate(it).mapKeys { (key, _) -> key.beacon }
        }
    }

    /** Get a deep copy of the complete location list */
    fun copy() = positions.mapValues { (_, map) -> map.toSortedMap() }

    /** Get the latest location list for each tag */
    fun newest() = positions.mapValues { (_, map) -> map[map.lastKey()]!! }

    // Update coordinates using the partial relationship you care about
    private fun calculate(targets: SortedSet<Position>)
        : Map<Position, Vector3D> {
        // Collect optimization conditions
        val (errors, domain, init) = conditions {
            // Triangular traversal in the narrow sense
            val list = targets.toList()
            for (i in list.indices) for (j in i + 1 until list.size) {
                val a = list[i]
                val b = list[j]
                // If there is distance measurement data between two points, average the distance measurement and add it to the system of equations
                measures[a to b]?.average()?.let { l -> this += (a euclid b) - l }
                // Otherwise, if one of them is a fixed label,
                // it is considered that the distance between the two labels is greater than the measurable limit length,
                // and added to the inequality constraint
                //?: if (a.isStatic() || b.isStatic())
                //    this[domain(maxMeasure - (a euclid b))] = maxMeasure - (positions[a]!! euclid positions[b]!!)
            }
            //Add initial value
            for (target in targets)
                this[target.space] = positions[target]!!
        }
        // Determine the unknown number space
        val space = variables(init.expressions.keys)
        //Construct optimization step function
        val f = when {
            domain.isEmpty() -> batchGD(errors.sum(), space, *domain, controller = NagMethod(space.dim, 1.0, .99))
            space.dim <= 24 -> dampingNewton(errors.sum(), space, *domain)
            else -> fastestBatchGD(errors.sum(), space, *domain)
        }
        // Optimize iteration
        val result = recurrence(init to .0) { (p, _) -> f(p) }
            .onEach { (p, _) ->
                targets.associate { b -> b.beacon to p.toVector(b.space).to3D() }.let(painter)
            }
            .take(1000)
            .firstOrLast { (_, step) -> step < 4e-4 }
            .first
        // The optimization solution is organized into coordinates
        return targets.associateWith { p ->
            result.toVector(p.space).to3D().also { positions[p] = it }
        }
    }

    // Lock other points and locate a position point
    private fun calculate(p: Position, beacons: Set<Position>): Vector3D {
        // Collect optimization conditions
        val (errors, domains, init) = conditions {
            for (b in beacons) this += (p euclid positions[b]!!) - measures[sortedPairOf(p, b)]!!.average()
            this[p.space] = positions[p]!!
        }
        //Construct optimization step function
        val f = dampingNewton(errors.sum(), variables(init.expressions.keys), *domains)
        val result = optimize(init, 1000, 2e-4, f)
        return result.toVector(p.space).to3D()
    }

    private companion object {
        //Modify hash map
        fun <TK, TV> HashMap<TK, TV>.update(key: TK, block: (TV) -> Unit, default: () -> TV) =
            compute(key) { _, last -> last?.also(block) ?: default() }

        // Find two-dimensional table
        operator fun <T> Map<Beacon, Map<Long, T>>.get(p: Position) =
            get(p.beacon)?.get(p.time)

        //Modify the two-dimensional table
        operator fun <T> HashMap<Beacon, SortedMap<Long, T>>.set(p: Position, t: T) =
            update(p.beacon, { it[p.time] = t }, { sortedMapOf(p.time to t) })
    }
}

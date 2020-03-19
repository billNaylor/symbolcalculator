package org.mechdancer.symbol.system

import org.mechdancer.algebra.function.vector.*
import org.mechdancer.algebra.implement.vector.Vector3D
import org.mechdancer.algebra.implement.vector.to3D
import org.mechdancer.geometry.transformation.toTransformationWithSVD
import org.mechdancer.symbol.*
import org.mechdancer.symbol.optimize.dampingNewton
import org.mechdancer.symbol.optimize.optimize
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.sqrt

/** 测距仿真 */
class SimulationWorld internal constructor(
    private val beacons: Map<Beacon, Vector3D>,
    var temperature: Double,
    actualTemperature: Double,
    private val maxMeasureTime: Long,
    private val sigmaMeasure: Double
) {
    private val positions =
        beacons.entries.map { (b, p) -> b.static() to p }

    var actualTemperature = actualTemperature
        set(value) {
            if (value == field) return
            field = value
            edges = positions.buildEdges(value, maxMeasureTime)
        }

    private var edges =
        positions.buildEdges(actualTemperature, maxMeasureTime)

    fun edges() = edges.keys

    fun preMeasures(): Map<Pair<Position, Position>, Double> {
        val c0 = soundVelocity(temperature)
        return edges.mapValues { (_, t) -> t * c0 + gaussian(sigmaMeasure) }
    }

    fun measure(mobile: Position, p: Vector3D) =
        sequence {
            val c0 = soundVelocity(temperature)
            val ca = soundVelocity(actualTemperature)
            for ((beacon, p0) in positions) {
                val t = (p euclid p0) / ca
                if (t < maxMeasureTime / 1000.0)
                    yield(beacon to mobile to t * c0 + gaussian(sigmaMeasure))
            }
        }

    fun transform(map: Map<Beacon, Vector3D>): List<Vector3D> {
        val pairs = map.mapNotNull { (key, p) -> beacons[key]?.to(p) }.toMutableList()
        val (a, b, c) = pairs
        val (a0, a1) = a
        val (b0, b1) = b
        val (c0, c1) = c
        pairs += (b0 - a0 cross c0 - a0) + a0 to (b1 - a1 cross c1 - a1) + a1
        val tf = pairs.toTransformationWithSVD(1e-8)
        return map.map { (_, p) -> (tf * p).to3D() }
    }

    companion object {
        private val random = Random()

        private fun gaussian(sigma: Double = 1.0) = random.nextGaussian() * sigma

        /** [t]℃ 时的声速 */
        fun soundVelocity(t: Double) = 20.048 * sqrt(t + 273.15)

        private val collector = collector()
        private val t by variable(collector) // 飞行时间
        private val x by variable(collector) // 有效声音方向
        private val y by variable(collector) //
        private val z by variable(collector) //
        private val space = collector.toSpace()

        /** 传声模型 := 均匀风场 [wind] 中，声音按声速 [c] 通过向量 [s] 的时间 */
        fun flightDuration(s: Vector3D, wind: Vector3D, c: Double): Double {
            val t0 = (s.length / c).takeIf { it > 0 } ?: return .0
            if (wind.isZero()) return t0

            val error = listOf(
                (x + wind.x) * t - s.x,
                (y + wind.y) * t - s.y,
                (z + wind.z) * t - s.z,
                sqrt(x * x + y * y + z * z) - c
            ).meanSquare()

            val (x0, y0, z0) = s / t0
            val init = point(t to t0, x to x0, y to y0, z to z0)
            val f = dampingNewton(error, space)
            return optimize(init, 200, 1e-9, f)[t]!!.toDouble()
        }

        private fun List<Pair<Position, Vector3D>>.buildEdges(
            t: Double,
            maxTime: Long
        ) =
            sequence {
                val ca = soundVelocity(t)
                for (i in indices) for (j in i + 1 until size) {
                    val (a, pa) = get(i)
                    val (b, pb) = get(j)
                    val time = (pa euclid pb) / ca
                    if (time < maxTime / 1000.0) yield(a to b to time)
                }
            }.toMap()

//        @JvmStatic
//        private fun main(args: Array<String>) {
//            val remote = remoteHub("定位优化").apply {
//                openAllNetworks()
//                println(networksInfo())
//            }
//
//            while (true) {
//                fun random() = Random.nextDouble(-1.0, 1.0)
//                val s = vector3D(25, 0, 0)
//                val wind = vector3D(random(), random(), random()).normalize() * (10 + 10 * random())
//                val c = 340.0
//                val t0 = flightDuration(s, vector3DOfZero(), c)
//                val t = flightDuration(s, wind, c)
//
//                remote.paint("关系", wind.length, (t - t0) * c)
//            }
//        }
    }
}

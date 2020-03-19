package org.mechdancer.symbol.experiments.system

import org.mechdancer.algebra.function.vector.*
import org.mechdancer.algebra.implement.vector.Vector3D
import org.mechdancer.algebra.implement.vector.vector3D
import org.mechdancer.remote.presets.remoteHub
import org.mechdancer.symbol.networksInfo
import org.mechdancer.symbol.paint
import org.mechdancer.symbol.paintFrame3
import org.mechdancer.symbol.system.Beacon
import org.mechdancer.symbol.system.LocatingSystem
import org.mechdancer.symbol.system.WorldBuilderDsl.Companion.world
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

// 6 个固定标签组成矩形，1 个移动标签沿矩形对角线运动

private const val maxMeasure = 30.0

private val lx = .5
private val ly = sqrt(1 - lx * lx)

private val shape = vector3D(lx, ly, 0) * maxMeasure * .95 + vector3D(0, 0, 1)
private val beacons = (0 until 6).map { deploy(vector3D(it / 2, it % 2, 0) * shape) }

private fun deploy(p: Vector3D) = p + java.util.Random().run {
    vector3D(nextGaussian(), nextGaussian(), nextGaussian()) * .1
}

fun main() {
    val world = world(beacons.mapIndexed { i, p -> Beacon(i) to p }.toMap())
    val grid = world.edges().map { it.toList().map { (b, _) -> b.id } }
    val system = LocatingSystem(maxMeasure).apply { this[-1L] = world.preMeasures() }

    val remote = remoteHub("实验").apply {
        openAllNetworks()
        println(networksInfo())
        paintFrame3("实际地图", grid.map { it.map(beacons::get) })
        system.painter = { paintFrame3("步骤", listOf(world.transform(it))) }
    }

    println("optimize in ${measureTimeMillis { system.optimize() }}ms")

    val mobile = Beacon(beacons.size)
    val steps = 200
    val dl = vector3D(2, 1, 0) * shape / steps
    var m = vector3D(0, 0, -1.5) - dl
    for (i in 0 until steps) {
        m += dl
        val result = System.currentTimeMillis().let {
            system[it] = world.measure(mobile.move(it), m).toMap()
            world.transform(system[mobile]).last().run { copy(z = -abs(z)) }
        }
        with(remote) {
            paintFrame3("实际地图", grid.map { it.map(beacons::get) })
            paint("目标", m)
            paint("历史", result)
        }
        println("step $i: ${m euclid result}\t${m.select(0..1) euclid result.select(0..1)}")
    }
}

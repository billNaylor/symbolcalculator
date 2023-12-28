package org.mechdancer.symbol.system

import org.mechdancer.algebra.function.vector.plus
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.Vector3D
import org.mechdancer.algebra.implement.vector.vector3DOfZero

class WorldBuilderDsl private constructor() {
    /** initial ambient air temperature */
    var temperature = 15.0

    /** Ranging standard deviation */
    var sigmaMeasure = .0

    /** Label deployment position standard deviation */
    var sigmaDeploy = vector3DOfZero()

    private var thermometer = { t: Double -> t }

    /** temperature calculation method */
    fun thermometer(block: (Double) -> Double) {
        thermometer = block
    }

    companion object {
        private val random = java.util.Random()

        fun world(beacons: Map<Beacon, Vector3D>,
                  maxMeasure: Double,
                  block: WorldBuilderDsl.() -> Unit = {}
        ) =
            WorldBuilderDsl()
                .apply(block)
                .run {
                    fun deploy(p: Vector3D) =
                        Vector3D(random.nextGaussian(),
                                 random.nextGaussian(),
                                 random.nextGaussian()
                        ) * sigmaDeploy + p

                    SimulationWorld(
                        layout = beacons.mapValues { (_, p) -> deploy(p) },
                        temperature = temperature,
                        thermometer = thermometer,
                        maxMeasureTime = (maxMeasure * 1000 / SimulationWorld.soundVelocity(temperature)).toLong(),
                        sigmaMeasure = sigmaMeasure)
                }
    }
}

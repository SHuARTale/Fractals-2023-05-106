package math.fractals

import math.Complex

class Julia(var cX: Double = -0.74543, var cY: Double = 0.11301) : AlgebraicFractal {
    override var maxIterations: Int = 300
        set(value) { field = value.coerceIn(20..10000)}
    var r = 2.0
    override fun isInSet(_z: Complex): Float {
        val z = Complex(_z.re,_z.im)
        val c = Complex(cX, cY)
        val r2 = r*r
        for (i in 1..maxIterations){
            z*=z
            z+=c
            if (z.abs2() >= r2)
                return i.toFloat()/ maxIterations
        }
        return 1f
    }

}
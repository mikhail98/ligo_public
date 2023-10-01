package com.ligo.common.map

import com.google.android.gms.maps.model.LatLng

object MapUtil {
    fun decodeRoutePath(points: String): List<LatLng> {
        val path = ArrayList<LatLng>(points.length / 2)
        var index = 0
        var lat = 0
        var lng = 0
        while (index < points.length) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = points[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            result = 1
            shift = 0
            do {
                b = points[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            path.add(LatLng(lat * 1e-5, lng * 1e-5))
        }
        return path
    }
}
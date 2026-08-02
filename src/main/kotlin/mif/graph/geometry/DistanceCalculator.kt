package mif.graph.geometry

import de.topobyte.osm4j.core.model.iface.OsmNode
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import kotlin.math.pow
import kotlin.math.sqrt

class PreciseDistanceCalculator(src: String? = null, dst: String? = null): DistanceCalculator {
    companion object {
        const val DEFAULT_SRC = "EPSG:4326" // WGS 84
        const val DEFAULT_DST = "EPSG:3059" // LKS 92
    }
    private val factory = CoordinateTransformFactory()
    private val transform = factory.createTransform(
        CRSFactory().createFromName(src ?: DEFAULT_SRC),
        CRSFactory().createFromName(dst ?: DEFAULT_DST),
    )

    fun distance(from: OsmNode, to: OsmNode) = distance(from.toNode(), to.toNode())

    override fun distance(from: Node, to: Node): Double {
        val fromCoord = ProjCoordinate()
        transform.transform(from.toProjCoordinate(), fromCoord)

        val toCoord = ProjCoordinate()
        transform.transform(to.toProjCoordinate(), toCoord)

        return sqrt((fromCoord.x - toCoord.x).pow(2) + (fromCoord.y - toCoord.y).pow(2))
    }
}

private fun OsmNode.toNode() = Node(longitude, latitude)

data class Node(
    val lon: Double,
    val lat: Double,
)

private fun Node.toProjCoordinate() = ProjCoordinate(lon, lat)

fun interface DistanceCalculator {
    fun distance(from: Node, to: Node): Double
}
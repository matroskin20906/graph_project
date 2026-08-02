package mif.graph.osmdata

import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.pbf.seq.PbfWriter
import org.locationtech.proj4j.CoordinateTransformFactory

class OsmData(nodes: List<OsmNode>, ways: List<OsmWay>, relations: List<OsmRelation>) {
    private val nodesMap = nodes.associateBy { it.id }
    private val waysMap = ways.associateBy { it.id }
    private val relationsMap = relations.associateBy { it.id }

    fun node(id: Long) = nodesMap[id] ?: throw OsmNodeNotFound(id)
    fun way(id: Long) = waysMap[id] ?: throw OsmWayNotFound(id)
    fun relation(id: Long) = relationsMap[id]

    fun nodes() = nodesMap.values
    fun ways() = waysMap.values
    fun relations() = relationsMap.values

    fun write(writer: PbfWriter) {
        TODO("implement output file writer")
    }

    override fun toString() = "Nodes: ${nodesMap.size}, Ways: ${waysMap.size}, Relations: ${relationsMap.size}"
}

fun OsmWay.nodeIds(): List<Long> {
    val nodes = mutableListOf<Long>()

    for (i in 0 until numberOfNodes) {
        nodes.add(getNodeId(i))
    }

    return nodes
}

fun OsmWay.oneway(): Boolean {
    for (i in 0 until numberOfTags) {
        val tag = getTag(i)
        if (tag.key == "oneway") {
            return tag.value == "true"
        }
    }

    return false
}

class OsmNodeNotFound(id: Long) : RuntimeException("Osm node $id not found.")
class OsmWayNotFound(id: Long) : RuntimeException("Osm way $id not found.")

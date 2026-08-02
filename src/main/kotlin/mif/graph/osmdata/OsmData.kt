package mif.graph.osmdata

import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.pbf.seq.PbfWriter

class OsmData(nodes: List<OsmNode>, ways: List<OsmWay>, relations: List<OsmRelation>) {
    private val nodesMap = nodes.associateBy { it.id }
    private val waysMap = ways.associateBy { it.id }
    private val relationsMap = relations.associateBy { it.id }

    fun node(id: Long) = nodesMap[id]

    fun way(id: Long) = waysMap[id]

    fun relation(id: Long) = relationsMap[id]

    fun write(writer: PbfWriter) {
        TODO("implement output file writer")
    }

    override fun toString() = "Nodes: ${nodesMap.size}, Ways: ${waysMap.size}, Relations: ${relationsMap.size}"
}
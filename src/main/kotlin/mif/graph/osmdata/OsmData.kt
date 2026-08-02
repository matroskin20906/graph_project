package mif.graph.osmdata

import com.slimjars.dist.gnu.trove.list.array.TLongArrayList
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmTag
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.core.model.impl.Tag
import de.topobyte.osm4j.core.model.impl.Way
import de.topobyte.osm4j.pbf.seq.PbfWriter
import mif.graph.Edge
import mif.graph.RoadGraph
import mif.graph.SimpleEdge
import mif.graph.bridges

class OsmData(nodes: List<OsmNode>, ways: List<OsmWay>, relations: List<OsmRelation>) {
    private val nodesMap = nodes.associateBy { it.id }
    private val waysMap = ways.associateBy { it.id }.toMutableMap()
    private val relationsMap = relations.associateBy { it.id }

    private var maxWayId = waysMap.keys.max()

    fun node(id: Long) = nodesMap[id] ?: throw OsmNodeNotFound(id)
    fun way(id: Long) = waysMap[id] ?: throw OsmWayNotFound(id)
    fun relation(id: Long) = relationsMap[id]

    fun nodes() = nodesMap.values
    fun ways() = waysMap.values
    fun relations() = relationsMap.values

    fun calcBridges() =
        markEdges(
            Tag("is_bridge", "true"),
            RoadGraph.from(this)
                .bridges()
                .toHashSet(),
        )

    private fun markEdges(tag: Tag, toMark: Set<Edge>) {
        val affectedWays = mutableListOf<Pair<Long, List<Edge>>>()

        for (way in ways()) {
            val edgesToMark = mutableListOf<Edge>()

            var prev = way.getNodeId(0)
            for (i in 1 until way.numberOfNodes) {
                val curr = way.getNodeId(i)
                val edge = SimpleEdge(prev, curr)

                if (edge in toMark) {
                    edgesToMark += edge
                }

                prev = curr
            }

            if (edgesToMark.isNotEmpty()) {
                affectedWays += way.id to edgesToMark
            }
        }

        for ((wayId, edgesToMark) in affectedWays) {
            val splitWays = way(wayId).splitWithTag(edgesToMark, tag)

            waysMap.remove(wayId)
            splitWays.forEach { waysMap[it.id] = it }
        }
    }

    private fun OsmWay.splitWithTag(edges: Collection<Edge>, tag: OsmTag): List<OsmWay> {
        if (edges.isEmpty()) return listOf(this)

        val edgesSet = edges.toHashSet()
        val result = mutableListOf<OsmWay>()

        val current = TLongArrayList(numberOfNodes)
        current.add(getNodeId(0))

        fun emit(nodes: TLongArrayList, mark: Boolean) {
            if (nodes.size() < 2) return

            val tags = if (mark) {
                tags() + tag
            } else {
                tags()
            }

            val id = if (result.isEmpty()) this.id else ++maxWayId

            result += Way(id, TLongArrayList(nodes), tags, metadata)
        }

        for (i in 1 until numberOfNodes) {
            val from = getNodeId(i - 1)
            val to = getNodeId(i)
            val edge = SimpleEdge(from, to)

            if (edge in edgesSet) {
                // Finish accumulated non-spec section.
                emit(current, mark = false)

                // Emit the spec itself.
                val nodes = TLongArrayList(2)
                nodes.add(from)
                nodes.add(to)
                emit(nodes, mark = true)

                // Start next section from the spec end.
                current.resetQuick()
                current.add(to)
            } else {
                current.add(to)
            }
        }

        emit(current, mark = false)

        return result
    }

    fun write(writer: PbfWriter) {
        nodes().forEach { writer.write(it) }
        ways().forEach { writer.write(it) }
        relations().forEach { writer.write(it) }
        writer.complete()
    }

    override fun toString() = "Nodes: ${nodesMap.size}, Ways: ${waysMap.size}, Relations: ${relationsMap.size}"
}

private fun TLongArrayList.add(edge: Edge) {
    add(edge.from)
    add(edge.to)
}

private fun OsmWay.tags(): List<OsmTag> {
    val tags = mutableListOf<OsmTag>()

    for (i in 0 until numberOfTags) {
        tags.add(getTag(i))
    }

    return tags
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

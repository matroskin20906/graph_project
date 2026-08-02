package mif.graph

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.topobyte.osm4j.core.model.iface.EntityType
import de.topobyte.osm4j.core.model.iface.OsmNode
import de.topobyte.osm4j.core.model.iface.OsmRelation
import de.topobyte.osm4j.core.model.iface.OsmWay
import de.topobyte.osm4j.pbf.seq.PbfIterator
import mif.graph.osmdata.OsmData
import java.io.FileInputStream

class GraphProcessor : CliktCommand() {
    private val input by option("-i", "--input").required()
    private val output by option("-o", "--output")

    override fun run() {
        println(RoutingOsmFileReader().read(input))
    }
}

fun interface OsmFileReader {
    fun read(file: String): OsmData
}

class StandardOsmFileReader : OsmFileReader {
    override fun read(file: String): OsmData {
        val nodes = mutableListOf<OsmNode>()
        val ways = mutableListOf<OsmWay>()
        val relations = mutableListOf<OsmRelation>()

        PbfIterator(FileInputStream(file), true).forEach { container ->
            when (container.type) {
                EntityType.Node -> nodes.add(container.entity as OsmNode)
                EntityType.Way -> ways.add(container.entity as OsmWay)
                EntityType.Relation -> relations.add(container.entity as OsmRelation)
            }
        }

        return OsmData(nodes, ways, relations)
    }
}

class RoutingOsmFileReader : OsmFileReader {
    override fun read(file: String): OsmData {
        val ways = mutableListOf<OsmWay>()
        val neededNodes = mutableSetOf<Long>()
        PbfIterator(FileInputStream(file), true).forEach { container ->
            when (container.type) {
                EntityType.Way -> {
                    val way = container.entity as OsmWay
                    if (way.highwayType() != null) {
                        ways.add(way)
                        neededNodes.addAll(way.nodeIds())
                    }
                }
                else -> {}
            }
        }

        val nodes = mutableListOf<OsmNode>()
        val relations = mutableListOf<OsmRelation>()
        PbfIterator(FileInputStream(file), true).forEach { container ->
            when (container.type) {
                EntityType.Node -> {
                    val node = container.entity as OsmNode
                    if (node.id in neededNodes) {
                        nodes.add(node)
                    }
                }
                EntityType.Way -> {}
                EntityType.Relation -> relations.add(container.entity as OsmRelation)
            }
        }

        return OsmData(nodes, ways, relations)
    }

    private fun OsmWay.highwayType(): String? {
        for (i in 0 until numberOfTags) {
            val tag = getTag(i)
            if (tag.key == "highway") {
                return tag.value
            }
        }

        return null
    }

    private fun OsmWay.nodeIds(): List<Long> {
        val nodes = mutableListOf<Long>()

        for (i in 0 until numberOfNodes) {
            nodes.add(getNodeId(i))
        }

        return nodes
    }
}

fun main(args: Array<String>) = GraphProcessor().main(args)

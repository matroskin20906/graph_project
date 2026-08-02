package mif.graph

import mif.graph.geometry.PreciseDistanceCalculator
import mif.graph.osmdata.OsmData
import mif.graph.osmdata.oneway
import java.util.LinkedList

data class Edge(val from: Long, val to: Long, val weight: Double)

data class Vertex(val id: Long, val x: Double, val y: Double) {
    override fun toString() = "$id:$x,$y"
}

class Graph(data: OsmData) {
    val distanceCalculator = PreciseDistanceCalculator()

    val edges = data.ways().flatMap { way ->
        val oneway = way.oneway()
        buildList {
            for (i in 1 until way.numberOfNodes) {
                val fromId = way.getNodeId(i - 1)
                val toId = way.getNodeId(i)
                val weight = distanceCalculator.distance(data.node(fromId), data.node(toId))

                require(weight > 0.0) { "weight must be positive" }

                add(Edge(fromId, toId, weight))
                if (!oneway) {
                    add(Edge(toId, fromId, weight))
                }
            }
        }
    }
    val vertexes = data.nodes()
        .map { node -> Vertex(node.id, node.longitude, node.latitude) }
        .associateBy { it.id }

    fun dfs() {
        TODO("implement")
    }

    fun bfs() {
        TODO("implement")
    }

    /** @throws IllegalVertex */
    fun dijkstra(from: Vertex, to: Vertex): LinkedList<Vertex> {
        if (vertexes[from.id] == null) {
            throw IllegalVertex(from)
        }
        if (vertexes[to.id] == null) {
            throw IllegalVertex(to)
        }
        TODO("implement")
    }

    fun findBridges(): List<Edge> {
        TODO("implement")
    }

    fun kruskals() {

    }
}

class IllegalVertex(vertex: Vertex): RuntimeException("Vertex $vertex is not in the graph.")

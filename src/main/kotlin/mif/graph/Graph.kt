package mif.graph

import mif.graph.geometry.Node
import mif.graph.geometry.PreciseDistanceCalculator
import mif.graph.osmdata.OsmData
import mif.graph.osmdata.oneway
import java.util.LinkedList

interface Graph {
    val vertices: List<Vertex>
    val edges: List<Edge>
}

interface WeightedGraph : Graph {
    override val edges: List<WeightedEdge>
}

interface GeometricalWeightedGraph : WeightedGraph {
    val coordinates: Map<Long, Node>
}

sealed interface Edge {
    val from: Long
    val to: Long
}

data class SimpleEdge(
    override val from: Long,
    override val to: Long
) : Edge {
    override fun equals(other: Any?) =
        other != null && other is SimpleEdge && ((from == other.from && to == other.to) || (from == other.to && to == other.from))

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }
}

data class WeightedEdge(
    override val from: Long,
    override val to: Long,
    val weight: Double
) : Edge {
    override fun equals(other: Any?) =
        other != null && other is SimpleEdge && ((from == other.from && to == other.to) || (from == other.to && to == other.from))

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }
}

data class Vertex(val id: Long) {
    override fun toString() = "$id"
}

fun Graph.bfs(start: Long? = null): List<Vertex> {
    val vertices = vertices.associateBy { it.id }
    if (start != null && vertices[start] == null) {
        throw IllegalVertex(start)
    }

    val neighbours = edges
        .groupBy { it.from }
        .mapValues { (_, edges) -> edges.map { it.to } }
    val visited = mutableSetOf<Long>()
    val startVertex = start ?: vertices.keys.random()

    return buildList {
        addAll(bfsConnected(neighbours, visited, startVertex))
        neighbours.keys.forEach { vertex ->
            if (vertex !in visited) {
                addAll(bfsConnected(neighbours, visited, vertex))
            }
        }
    }.map { vertices[it]!! }
}

private fun bfsConnected(neighbours: Map<Long, List<Long>>, visited: MutableSet<Long>, start: Long): List<Long> {
    val q = LinkedList<Long>()
    visited.add(start)
    q.add(start)

    return buildList {
        while (q.isNotEmpty()) {
            val current = q.poll()
            add(current)

            for (neighbour in neighbours[current].orEmpty()) {
                if (neighbour !in visited) {
                    visited.add(neighbour)
                    q.add(neighbour)
                }
            }
        }
    }
}

/** @throws IllegalVertex **/
fun Graph.dfs(start: Long? = null): List<Vertex> {
    val vertices = vertices.associateBy { it.id }
    if (start != null && vertices[start] == null) {
        throw IllegalVertex(start)
    }

    val neighbours = edges
        .groupBy { it.from }
        .mapValues { (_, edges) -> edges.map { it.to } }
    val visited = mutableSetOf<Long>()
    val startVertex = start ?: vertices.keys.random()

    return buildList {
        visited.add(startVertex)
        addAll(dfsRec(neighbours, visited, startVertex))
        neighbours.keys.forEach { vertex ->
            if (vertex !in visited) {
                addAll(dfsRec(neighbours, visited, vertex))
            }
        }
    }.map { vertices[it]!! }
}

private fun dfsRec(adj: Map<Long, List<Long>>, visited: MutableSet<Long>, start: Long): List<Long> {
    visited.add(start)

    return listOf(start) + buildList {
        adj[start]?.forEach { neighbor ->
            if (neighbor !in visited) {
                addAll(dfsRec(adj, visited, neighbor))
            }
        }
    }
}

fun Graph.bridges(): List<Edge> {
    TODO()
}

/** @throws IllegalVertex **/
fun WeightedGraph.dijkstra(start: Long, end: Long): List<Edge> {
    val vertices = vertices.associateBy { it.id }
    if (vertices[start] == null) {
        throw IllegalVertex(start)
    }

    if (vertices[end] == null) {
        throw IllegalVertex(start)
    }

    TODO()
}

// here we should think about graph as unoriented
fun WeightedGraph.kruskals(): List<WeightedEdge> {
    TODO()
}

fun GeometricalWeightedGraph.dijkstra(start: Node, end: Node): List<Edge> {
    TODO()
}

class RoadGraph(
    override val vertices: List<Vertex>,
    override val edges: List<Edge>
) : Graph {
    companion object {
        fun from(data: OsmData): Graph {
            val edges = ArrayList<Edge>()

            for (way in data.ways()) {
                val oneway = way.oneway()

                for (i in 1 until way.numberOfNodes) {
                    val fromId = way.getNodeId(i - 1)
                    val toId = way.getNodeId(i)

                    edges.add(SimpleEdge(fromId, toId))

                    if (!oneway) {
                        edges.add(SimpleEdge(toId, fromId))
                    }
                }
            }

            return RoadGraph(data.nodes().map { node -> Vertex(node.id) }, edges)
        }
    }
}

class WeightedRoadGraph(
    override val vertices: List<Vertex>,
    override val edges: List<WeightedEdge>
) : WeightedGraph {
    companion object {
        fun from(data: OsmData): WeightedGraph {
            val distanceCalculator = PreciseDistanceCalculator()
            val edges = ArrayList<WeightedEdge>()

            for (way in data.ways()) {
                val oneway = way.oneway()

                for (i in 1 until way.numberOfNodes) {
                    val fromId = way.getNodeId(i - 1)
                    val toId = way.getNodeId(i)

                    val weight = distanceCalculator.distance(
                        requireNotNull(data.node(fromId)) {
                            "Missing node $fromId"
                        },
                        requireNotNull(data.node(toId)) {
                            "Missing node $toId"
                        }
                    )

                    require(weight > 0.0) {
                        "Invalid edge weight $weight for $fromId -> $toId"
                    }

                    edges.add(WeightedEdge(fromId, toId, weight))

                    if (!oneway) {
                        edges.add(WeightedEdge(toId, fromId, weight))
                    }
                }
            }

            return WeightedRoadGraph(data.nodes().map { node -> Vertex(node.id) }, edges)
        }
    }
}


class OsmRoadGraph(
    override val vertices: List<Vertex>,
    override val edges: List<WeightedEdge>,
    override val coordinates: Map<Long, Node>
) : GeometricalWeightedGraph {
    companion object {
        fun from(data: OsmData): GeometricalWeightedGraph {
            val weightedGraph = WeightedRoadGraph.from(data)

            return OsmRoadGraph(
                data.nodes().map { node -> Vertex(node.id) },
                weightedGraph.edges,
                data.nodes().associate { node -> node.id to Node(node.longitude, node.latitude) }
            )
        }
    }
}

class IllegalVertex(vertex: Long): RuntimeException("Vertex $vertex is not in the graph.")

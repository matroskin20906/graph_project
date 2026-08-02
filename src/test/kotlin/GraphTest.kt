import mif.graph.Edge
import mif.graph.IllegalVertex
import mif.graph.RoadGraph
import mif.graph.SimpleEdge
import mif.graph.Vertex
import mif.graph.WeightedRoadGraph
import mif.graph.bfs
import mif.graph.dfs
import mif.graph.dijkstra
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class GraphTest {
    @Test
    fun dfs() {
        val graph = RoadGraph(
            vertices = listOf(
                Vertex(0),
                Vertex(1),
                Vertex(2),
                Vertex(3),
                Vertex(4),
                Vertex(5),
            ),
            edges = listOf(
                SimpleEdge(0, 2),
                SimpleEdge(2, 0),
                SimpleEdge(0, 3),
                SimpleEdge(3, 0),
                SimpleEdge(1, 2),
                SimpleEdge(2, 1),
                SimpleEdge(5, 4),
                SimpleEdge(4, 5),
            )
        )

        val result = graph.dfs(0)

        assertEquals(6, result.size)
        assertEquals(listOf(Vertex(0), Vertex(2), Vertex(1), Vertex(3), Vertex(5), Vertex(4)), result)
    }

    @Test
    fun bfs() {
        val graph = RoadGraph(
            vertices = listOf(
                Vertex(0),
                Vertex(1),
                Vertex(2),
                Vertex(3),
                Vertex(4),
                Vertex(5),
            ),
            edges = listOf(
                SimpleEdge(0, 2),
                SimpleEdge(2, 0),
                SimpleEdge(0, 3),
                SimpleEdge(3, 0),
                SimpleEdge(1, 2),
                SimpleEdge(2, 1),
                SimpleEdge(5, 4),
                SimpleEdge(4, 5),
            )
        )

        val result = graph.bfs(0)

        assertEquals(6, result.size)
        assertEquals(listOf(Vertex(0), Vertex(2), Vertex(3), Vertex(1), Vertex(5), Vertex(4)), result)
    }

    @Test
    fun dijkstraFailForMissingVertices() {
        val graph = WeightedRoadGraph(edges = emptyList(), vertices = emptyList())
        assertFailsWith<IllegalVertex> { graph.dijkstra(1, 2) }
    }

    @Test
    fun dijkstra() {
    }

    @Test
    fun bridges() {
    }

    @Test
    fun kruskals() {
    }
}
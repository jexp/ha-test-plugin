package org.neo4j.test.ha;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

import java.util.Random;

@Description("Creates a part of a graph")
public class HaTestPlugin extends ServerPlugin {


    @Name("create_graph")
    @PluginTarget(GraphDatabaseService.class)
    public Integer createGraph(@Source GraphDatabaseService gds, @Parameter(name = "count") int count, @Parameter(name = "rels") int relsPerNode) {
        GraphCreator graphCreator = new GraphCreator(gds);
        int start = new Random().nextInt();
        return graphCreator.createNodes(start, count) + graphCreator.createRelationships(start, count, relsPerNode);
    }

    @Name("delete_nodes")
    @PluginTarget(GraphDatabaseService.class)
    public Integer deleteNodes(@Source GraphDatabaseService gds, @Parameter(name = "count") int count) {
        GraphCreator graphCreator = new GraphCreator(gds);
        return graphCreator.removeRandomNodes(count);
    }

}

package org.neo4j.test.ha;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

@Description("Creates a part of a graph")
public class HaTestPlugin extends ServerPlugin {


    @Name("create_graph")
    @PluginTarget(GraphDatabaseService.class)
    public Integer createGraph(@Source GraphDatabaseService gds, @Parameter(name = "start") int start, @Parameter(name = "count") int count, @Parameter(name = "rels") int relsPerNode) {
        GraphCreator graphCreator = new GraphCreator(gds);
        return graphCreator.createNodes(start, count) + graphCreator.createRelationships(start, count, relsPerNode);
    }

}

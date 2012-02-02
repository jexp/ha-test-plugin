package org.neo4j.test.ha;

import org.junit.After;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mh
 * @since 02.02.12
 */
public class GraphCreatorTest {

    private GraphCreator gc;
    private EmbeddedGraphDatabase gds;
    private Transaction tx;

    @org.junit.Before
    public void setUp() throws Exception {
        FileUtils.deleteRecursively(new File("out/db"));
        gds = new EmbeddedGraphDatabase("out/db");
        gc = new GraphCreator(gds);
    }

    @After
    public void tearDown() throws Exception {
        gds.shutdown();
    }

    @org.junit.Test
    public void testRemoveRandomNodes() throws Exception {
        gc.createNodes(0, 100);
        gc.removeRandomNodes(10);
        assertTrue(gc.indexedNodeCount() < 100);
    }

    @org.junit.Test
    public void testCreateNodes() {
        gc.createNodes(0, 100);
        assertEquals(100, gc.indexedNodeCount());
        assertEquals(101, gc.highestIdInUse());
    }

    @org.junit.Test
    public void testRemoveNode() throws Exception {
        tx = gds.beginTx();
        gc.createNodes(0, 2);
        final int removed = gc.removeNode(1);
        assertTrue(removed >= 1);
        assertEquals(1, gc.indexedNodeCount());
        tx.finish();
    }
}

package org.neo4j.test.ha;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.impl.nioneo.xa.NeoStoreXaDataSource;

import java.util.Random;

/**
 * @author mh
 * @since 02.02.12
 */
class GraphCreator {
    private final GraphDatabaseService gds;
    private Index<Node> index;
    private static final int BATCH_SIZE = 5000;

    public GraphCreator(GraphDatabaseService gds) {
        this.gds = gds;
        this.index = gds.index().forNodes("nodes");
    }

    int createRelationships(int start, int count, int relsPerNode) {
        final Random random = new Random();
        int created = 0;
        final int maxNodes = maxNodes();
        Transaction tx = gds.beginTx();
        for (int i = start; i < count; i++) {
            Node node = getNode(i);
            if (node==null) continue;
            for (int r = random.nextInt(relsPerNode / 2) + relsPerNode / 2; r >= 0; r--) {
                Node other = getNode(random.nextInt(maxNodes));
                if (other == null) continue;
                node.createRelationshipTo(other, Types.from(r));
                created++;
                tx = batch(tx, created);
            }
        }
        tx.success();
        tx.finish();
        return created;
    }

    private Transaction batch(Transaction tx, int count) {
        if (count % BATCH_SIZE != 0) return tx;
        tx.success();
        tx.finish();
        return gds.beginTx();
    }

    private int maxNodes() {
        return (int) ((NeoStoreXaDataSource) ((AbstractGraphDatabase) gds).getConfig().getTxModule()
                .getXaDataSourceManager().getXaDataSource(Config.DEFAULT_DATA_SOURCE_NAME))
                .getNeoStore().getNodeStore().getHighId();
    }

    int createNodes(int start, int count) {
        int created = 0;
        Transaction tx = gds.beginTx();
        gds.getReferenceNode().removeProperty("__lock__");
        for (int i = start; i < count; i++) {
            if (nodeExists(i)) {
                createNode();
                created++;
                tx = batch(tx,created);
            }
        }
        tx.success();
        tx.finish();
        return created;
    }

    private boolean nodeExists(int i) {
        return getNode(i) == null;
    }

    private Node getNode(int i) {
        return index.get("id", i).getSingle();
    }

    private Node createNode() {
        final Node node = gds.createNode();
        node.setProperty("id", node.getId());
        node.setProperty("text", String.valueOf(System.currentTimeMillis()));
        index.add(node, "id", node.getId());
        return node;
    }
}

package org.neo4j.test.ha;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.impl.nioneo.xa.NeoStoreXaDataSource;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author mh
 * @since 02.02.12
 */
class GraphCreator {
    private final GraphDatabaseService gds;
    private Index<Node> index;
    private static final int BATCH_SIZE = 50;
    private final Random random = new Random();

    public GraphCreator(GraphDatabaseService gds) {
        this.gds = gds;
        this.index = gds.index().forNodes("nodes");
    }

    int createRelationships(int start, int count, int relsPerNode) {
        int created = 0;
        final Batcher batcher = newBatcher();
        try {
            for (int i = 0; i < count; i++) {
                int id = start + i;
                Node node = getNode(id);
                if (node == null) continue;
                for (int r = random.nextInt(relsPerNode / 2) + relsPerNode / 2; r >= 0; r--) {
                    Node other = getNode(random.nextInt(count));
                    if (other == null) continue;
                    node.createRelationshipTo(other, Types.from(r));
                    created++;
                    batcher.batch();
                }
            }
        } finally {
            batcher.finish();
        }
        return created;
    }

    private Batcher newBatcher() {
        return new Batcher(BATCH_SIZE, gds);
    }

    int highestIdInUse() {
        return (int) ((NeoStoreXaDataSource) ((AbstractGraphDatabase) gds).getConfig().getTxModule()
                .getXaDataSourceManager().getXaDataSource(Config.DEFAULT_DATA_SOURCE_NAME))
                .getNeoStore().getNodeStore().getHighId();
    }

    int indexedNodeCount() {
        return IteratorUtil.count(index.query("id:*").iterator());
    }

    int createNodes(int start, int count) {
        int created = 0;
        final Batcher batcher = newBatcher();
        try {
            lock();
            for (int i = 0; i < count; i++) {
                final int id = start + i;
                if (!nodeExists(id)) {
                    createNode(id);
                    created++;
                    batcher.batch();
                }
            }
        } finally {
            batcher.finish();
        }
        return created;
    }

    private boolean nodeExists(int id) {
        return getNode(id) != null;
    }

    private Node getNode(int id) {
        final IndexHits<Node> hits = index.get("id", id);
        if (!hits.hasNext()) return null;
        final Node result = hits.next();
        hits.close();
        return result;
    }

    public int removeRandomNodes(final int count) {
        final int maxNodes = highestIdInUse();
        int deleted = 0;
        final Batcher batcher = newBatcher();
        try {
            lock();
            Set<Integer> removedIds = new HashSet<Integer>();
            for (int i = 0; i < count; i++) {
                final int id = random.nextInt(maxNodes);
                if (!removedIds.contains(id)) {
                    removedIds.add(id);
                    final int removedCount = removeNode(id);
                    deleted += removedCount;
                    batcher.batch(removedCount);
                }
            }
        } finally {
            batcher.finish();
        }
        return deleted;
    }

    private void lock() {
        gds.getReferenceNode().removeProperty("__lock__");
    }

    public int removeNode(int i) {
        final Node node = getNode(i);
        if (node == null) return 0;
        int count = 1;
        for (Relationship rel : node.getRelationships()) {
            rel.delete();
            count++;
        }
        index.remove(node);
        node.delete();
        return count;
    }

    private Node createNode(int id) {
        final Node node = gds.createNode();
        node.setProperty("id", id);
        node.setProperty("text", String.valueOf(System.currentTimeMillis()));
        index.add(node, "id", id);
        return node;
    }
}

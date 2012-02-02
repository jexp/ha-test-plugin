package org.neo4j.test.ha;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
* @author mh
* @since 02.02.12
*/
class Batcher {
    final int batchSize;
    final GraphDatabaseService gds;
    int count = 0;
    Transaction tx;

    Batcher(int batchSize, GraphDatabaseService gds) {
        this.batchSize = batchSize;
        this.gds = gds;
        start();
    }

    void start() {
        count = 0;
        tx = gds.beginTx();
    }

    void batch() {
        batch(1);
    }
    void batch(int offset) {
        count += offset;
        if (count > batchSize) {
            finish();
            start();
        }
    }

    void finish() {
        count = 0;
        tx.success();
        tx.finish();
        tx = null;
    }
}

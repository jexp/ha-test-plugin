package org.neo4j.test.ha;

import org.neo4j.graphdb.RelationshipType;

/**
* @author mh
* @since 02.02.12
*/
public enum Types implements RelationshipType {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN;

    public static Types from(int r) {
        return values()[r % 10];
    }
}

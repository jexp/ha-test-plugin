// it needs to have a copy of neo4j-enterprise-1.5.1 in its root directory (for libs)

curl -O http://dist.neo4j.org/neo4j-advanced-1.6.M01-unix.tar.gz
tar xzf neo4j-advanced-1.6.M01-unix.tar.gz

// build and run

ant
cp ha-test.jar neo4j-enterprise-1.5.1/plugins/
neo4j-enterprise-1.5.1/bin/neo4j restart

curl -H Content-Type:application/json -d '{"start":0, "count":1000, "rels": 100}' http://localhost:7474/db/data/ext/HaTestPlugin/graphdb/create_graph

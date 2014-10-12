// composite: mgmt.buildIndex('byName',Vertex.class).addKey(name).buildCompositeIndex()
// mixed: mgmt.buildIndex('nameAndAge',Vertex.class).addKey(name).addKey(age).buildMixedIndex("search")
// vertex: mgmt.buildEdgeIndex(battled,'battlesByTime',Direction.BOTH,Order.DESC,time)

// Boot
environment = System.getenv()["PLUTUS_ENV"];
if (environment == null) {
  environment = "dev";
}
println "Loading " + environment + " environment...";

// Load
println "Loading graph...";
TitanGraph g = TitanFactory.open("config/" + environment + "/plutus.properties");

// Delete edges
g.E.remove()

// Delete vertices
g.V.remove()

// Commit
println "Committing..."
g.commit()
println "Successful!"

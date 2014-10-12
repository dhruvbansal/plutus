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
mgmt         = g.getManagementSystem();

// Property Keys
println "Defining property keys..."

println "Defining bid..."
bid = mgmt.makePropertyKey("bid").dataType(Long.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byBid',Vertex.class).addKey(bid).unique().buildCompositeIndex();

println "Defining tid..."
tid = mgmt.makePropertyKey("tid").dataType(Long.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byTid',Vertex.class).addKey(tid).unique().buildCompositeIndex();

println "Defining hash..."
hash = mgmt.makePropertyKey("hash").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byHash',Vertex.class).addKey(hash).buildCompositeIndex();

println "Defining version..."
version = mgmt.makePropertyKey("version").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byVersion',Vertex.class).addKey(version).buildCompositeIndex();

println "Defining outputValue..."
outputValue = mgmt.makePropertyKey("outputValue").dataType(Float.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byOutputValue',Vertex.class).addKey(outputValue).buildMixedIndex("search");

println "Defining feesValue..."
feesValue = mgmt.makePropertyKey("feesValue").dataType(Float.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byFeesValue',Vertex.class).addKey(feesValue).buildMixedIndex("search");

println "Defining size..."
size = mgmt.makePropertyKey("size").dataType(Long.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('bySize',Vertex.class).addKey(size).buildMixedIndex("search");

println "Defining timestamp..."
timestamp = mgmt.makePropertyKey("timestamp").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byTimestamp',Vertex.class).addKey(timestamp).buildMixedIndex("search");

println "Defining nonce..."
nonce = mgmt.makePropertyKey("nonce").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byNonce',Vertex.class).addKey(hash).buildCompositeIndex();

println "Defining difficulty..."
difficulty = mgmt.makePropertyKey("difficulty").dataType(Float.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byDifficulty',Vertex.class).addKey(difficulty).buildMixedIndex("search");

println "Defining merkle..."
merkle = mgmt.makePropertyKey("merkle").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byMerkle',Vertex.class).addKey(merkle).buildCompositeIndex();

println "Defining numTransactions..."
numTransactions = mgmt.makePropertyKey("numTransactions").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byNumTransactions',Vertex.class).addKey(numTransactions).buildMixedIndex("search");

println "Defining numInputs..."
numInputs = mgmt.makePropertyKey("numInputs").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byNumInputs',Vertex.class).addKey(numInputs).buildMixedIndex("search");

println "Defining numOutputs..."
numOutputs = mgmt.makePropertyKey("numOutputs").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byNumOutputs',Vertex.class).addKey(numOutputs).buildMixedIndex("search");

println "Defining lockTime..."
lockTime = mgmt.makePropertyKey("lockTime").dataType(Long.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byLockTime',Vertex.class).addKey(lockTime).buildMixedIndex("search");

println "Defining balance..."
balance = mgmt.makePropertyKey("balance").dataType(Float.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byBalance',Vertex.class).addKey(balance).buildMixedIndex("search");

println "Defining index..."
index = mgmt.makePropertyKey("index").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byIndex',Edge.class).addKey(index).buildCompositeIndex();

println "Defining script..."
script = mgmt.makePropertyKey("script").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byScript',Edge.class).addKey(script).buildCompositeIndex();

println "Defining outputHash..."
outputHash = mgmt.makePropertyKey("outputHash").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byOutputHash',Edge.class).addKey(outputHash).buildCompositeIndex();

println "Defining outputIndex..."
outputIndex = mgmt.makePropertyKey("outputIndex").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byOutputIndex',Edge.class).addKey(outputIndex).buildCompositeIndex();

println "Defining inputHash..."
inputHash = mgmt.makePropertyKey("inputHash").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byInputHash',Edge.class).addKey(inputHash).buildCompositeIndex();

println "Defining inputIndex..."
inputIndex = mgmt.makePropertyKey("inputIndex").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byInputIndex',Edge.class).addKey(inputIndex).buildCompositeIndex();

println "Defining value..."
value = mgmt.makePropertyKey("value").dataType(Float.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byValue',Edge.class).addKey(value).buildMixedIndex("search");

println "Defining receiverAddress..."
receiverAddress = mgmt.makePropertyKey("receiverAddress").dataType(String.class).cardinality(Cardinality.SINGLE).make();
mgmt.buildIndex('byReceiverAddress',Edge.class).addKey(receiverAddress).buildCompositeIndex();

// Vertex labels
println "Defining vertex labels..."

println "Defining block..."
block       = mgmt.makeVertexLabel('block').make();;
println "Defining transaction..."
transaction = mgmt.makeVertexLabel('transaction').make();;
println "Defining address..."
address     = mgmt.makeVertexLabel('address').make();;

// Edge labels
println "Defining edge labels..."

println "Defining parent..."
parent = mgmt.makeEdgeLabel('parent').multiplicity(Multiplicity.SIMPLE).make();

println "Defining included..."
included = mgmt.makeEdgeLabel('included').multiplicity(Multiplicity.MANY2ONE).make();

println "Defining input..."
input = mgmt.makeEdgeLabel('input').multiplicity(Multiplicity.MULTI).make();
mgmt.buildEdgeIndex(input,'inputsByIndex',Direction.BOTH,Order.DESC,index)
mgmt.buildEdgeIndex(input,'inputsByScript',Direction.BOTH,Order.DESC,script)
mgmt.buildEdgeIndex(input,'inputsByOutputHash',Direction.BOTH,Order.DESC,outputHash)
mgmt.buildEdgeIndex(input,'inputsByOutputIndex',Direction.BOTH,Order.DESC,outputIndex)

println "Defining output..."
output = mgmt.makeEdgeLabel('output').multiplicity(Multiplicity.MULTI).make();
mgmt.buildEdgeIndex(output,'outputsByIndex',Direction.BOTH,Order.DESC,index)
mgmt.buildEdgeIndex(output,'outputsByScript',Direction.BOTH,Order.DESC,script)
mgmt.buildEdgeIndex(output,'outputsByInputHash',Direction.BOTH,Order.DESC,inputHash)
mgmt.buildEdgeIndex(output,'outputsByInputIndex',Direction.BOTH,Order.DESC,inputIndex)
// mgmt.buildEdgeIndex(output,'outputsByValue',Direction.BOTH,Order.DESC,value)
mgmt.buildEdgeIndex(output,'outputsByReceiverAddress',Direction.BOTH,Order.DESC,receiverAddress)

println "Committing..."
mgmt.commit();
println "Successful!"
quit
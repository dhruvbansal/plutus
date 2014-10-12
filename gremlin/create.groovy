// composite: mgmt.buildIndex('byName',Vertex.class).addKey(name).buildCompositeIndex()
// mixed: mgmt.buildIndex('nameAndAge',Vertex.class).addKey(name).addKey(age).buildMixedIndex("search")
// vertex: mgmt.buildEdgeIndex(battled,'battlesByTime',Direction.BOTH,Order.DESC,time)
log = { level, message ->
  println "[" + new Date() + "] " + level + " : " + message
}
info  = { message -> log("INFO",  message) }
warn  = { message -> log("WARN",  message) }
error = { message -> log("ERROR", message) }
debug = { message -> log("DEBUG", message) }

//
// Startup
// 
environment = System.getenv()["PLUTUS_ENV"];
if (environment == null) {
  environment = "dev";
}
info "Loading " + environment + " environment...";

info "Loading graph...";
TitanGraph g = TitanFactory.open("config/" + environment + "/plutus.properties");
mgmt         = g.getManagementSystem();

//
// Helper Functions
//
propertyKey = { name, dataType ->
  debug("Creating property key '$name' (${dataType})")
  return mgmt.makePropertyKey(name)
  .dataType(dataType)
  .cardinality(Cardinality.SINGLE)
  .make()
}

compositeIndex = { name, indexType, key, unique=false ->
  debug("Creating ${unique ? 'unique, ' : ''}composite index '$name'")
  thingSoFar = mgmt.buildIndex(name, indexType)
  .addKey(key)
  if (unique) {
    thingSoFar.unique().buildCompositeIndex()
  } else {
    thingSoFar.buildCompositeIndex()
  }
}

mixedIndex = { name, indexType, key ->
  debug("Creating mixed index '$name'")
  mgmt.buildIndex(name, indexType)
  .addKey(key)
  .buildMixedIndex("search")
}

propertyKeyWithCompositeIndex = { name, dataType, indexType, unique=false ->
  key = propertyKey(name, dataType)
  compositeIndex("by${name.capitalize()}", indexType, key, unique)
  return key
}

propertyKeyWithMixedIndex = { name, dataType, indexType ->
  key = propertyKey(name, dataType)
  mixedIndex("by${name.capitalize()}", indexType, key)
  return key
}

vertexLabel = { name ->
  debug("Creating vertex label '$name'")
  return mgmt.makeVertexLabel(name)
  .make();
}

edgeLabel = { name, multiplicity ->
  debug("Creating $multiplicity edge label '$name'")
  return mgmt.makeEdgeLabel(name)
  .multiplicity(Multiplicity."$multiplicity")
  .make()
}

vertexEdgeIndex = { label, key, name, direction="BOTH", order="DESC" ->
  debug("Creating vertex edge index '$name' for '${label}' edges on property key '${key}' (Direction.${direction}, Order.${order})")
  mgmt.buildEdgeIndex(label, name, Direction."$direction", Order."$order", key)
}

edgeLabelWithVertexIndexes = { edgeName, multiplicity, indexes=[] ->
  
  label = edgeLabel(edgeName, multiplicity)
  for (index in indexes) {
    (key, keyName, direction, order) = index
    if (direction && order) { vertexEdgeIndex(label, key, keyName, direction, order) ; return label }
    if (direction)          { vertexEdgeIndex(label, key, keyName, direction)        ; return label }
    if (order)              { vertexEdgeIndex(label, key, keyName, "BOTH", order)    ; return label }
    else                    { vertexEdgeIndex(label, key, keyName)                   ; return label }
  }
}

//
// Property Keys
//
info "Defining property keys..."

bkid            = propertyKeyWithCompositeIndex("bkid",            Long.class,    Vertex.class, true)
txid            = propertyKeyWithCompositeIndex("txid",            Long.class,    Vertex.class, true)
hash            = propertyKeyWithCompositeIndex("hash",            String.class,  Vertex.class, true)
bkHash          = propertyKeyWithCompositeIndex("bkHash",          String.class,  Vertex.class, true)
txHash          = propertyKeyWithCompositeIndex("txHash",          String.class,  Vertex.class, true)
version         = propertyKeyWithCompositeIndex("version",         String.class,  Vertex.class)
outputValue     = propertyKeyWithMixedIndex(    "outputValue",     Long.class,    Vertex.class)
feesValue       = propertyKeyWithMixedIndex(    "feesValue",       Long.class,    Vertex.class)
size            = propertyKeyWithMixedIndex(    "size",            Long.class,    Vertex.class)
timestamp       = propertyKeyWithMixedIndex(    "timestamp",       Long.class,    Vertex.class)
nonce           = propertyKeyWithCompositeIndex("nonce",           String.class,  Vertex.class)
difficulty      = propertyKeyWithMixedIndex(    "difficulty",      Float.class,   Vertex.class)
merkle          = propertyKeyWithCompositeIndex("merkle",          String.class,  Vertex.class)
numTx           = propertyKeyWithMixedIndex(    "numTx",           Integer.class, Vertex.class)
numInputs       = propertyKeyWithMixedIndex(    "numInputs",       Integer.class, Vertex.class)
numOutputs      = propertyKeyWithMixedIndex(    "numOutputs",      Integer.class, Vertex.class)
lockTime        = propertyKeyWithMixedIndex(    "lockTime",        Long.class,    Vertex.class)
balance         = propertyKeyWithMixedIndex(    "balance",         Long.class,    Vertex.class)
index           = propertyKey(                  "index",           Integer.class)
script          = propertyKeyWithCompositeIndex("script",          String.class,  Edge.class)
outputTxIndex   = propertyKey(                  "outputTxIndex",   Integer.class)
value           = propertyKeyWithMixedIndex(    "value",           Long.class,    Edge.class)
rcvrAddressHash = propertyKeyWithCompositeIndex("rcvrAddressHash", String.class,  Edge.class)

//
// Vertex.class labels
//
info "Defining vertex labels..."
block       = vertexLabel("block")
transaction = vertexLabel("transaction")
address     = vertexLabel("address")

//
// Edge.class labels & Vertex-Centric Indexes
//
info "Defining edge labels..."
parent   = edgeLabel("parent",   "MANY2ONE")
included = edgeLabel("included", "MANY2ONE")
input    = edgeLabelWithVertexIndexes("input", "MULTI", [
  [index,         "inputsByIndex",         "IN"],
  [script,        "inputsByScript",        "IN"],
  [outputTxIndex, "inputsByOutputTxIndex", "IN"]
])
output = edgeLabelWithVertexIndexes("output", "MULTI", [
  [index,           "outputsByIndex"],
  [script,          "outputsByScript"],
  [rcvrAddressHash, "outputsByRcvrAddressHash"],  
])
spent = edgeLabelWithVertexIndexes("spent", "MULTI", [
  [outputTxIndex,      "spentsByOutputTxIndex", "IN"]
])

//
// Commit
// 
info "Committing..."
mgmt.commit();
info "Successful!"

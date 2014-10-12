// Boot
environment = System.getenv()["PLUTUS_ENV"]
if (environment == null) {
  environment = "dev"
}
println "Loading $environment environment..."

// Load
println "Loading graph..."
TitanGraph g = TitanFactory.open("config/${environment}/plutus.properties")

// 
// Helper Functions
// 

logFrequency = 100
log = { level, message ->
  println "[" + new Date() + "] " + level + " : " + message
}
info  = { message -> log("INFO",  message) }
warn  = { message -> log("WARN",  message) }
error = { message -> log("ERROR", message) }

getOrCreateVertex = { vertexLabel, idField, id ->
  existing = g.V(idField, id)
  if (existing.hasNext()) {
    vertex = existing.next()
  } else {
    vertex = g.addVertex(vertexLabel)
  }
  vertex."$idField" = id
  return vertex
}

getOrCreateEdge = { edgeLabel, source, target ->
  existing = source.out(edgeLabel).retain([target])
  if (existing.hasNext()) {
    return existing.next()
  } else {
    return source.addEdge(edgeLabel, target)
  }
}

loadDataType = { name, lineProcessor ->
  lineNum = 0
  new File("data/${environment}/${name}.csv").eachLine({ line ->
    try {
      lineNum += 1
      lineProcessor(line)
    } catch (Throwable e) {
      error(e.toString())
    }
    if (lineNum % logFrequency == 0) { info("Loaded line ${lineNum} of ${name}...") }
  })
  println "Committing..."
  g.commit()
  info("Successfully loaded all " + name + "!")
}
  
//
// Blocks
//
loadBlocks = {
  lastBlock = null
  loadDataType("blocks", { line ->
    (bid,bkHash,version,timestamp,nonce,difficulty,merkle,numTx,outputValue,feesValue,size) = line.split(",")
    if (bid == "ID") { return };
    block = getOrCreateVertex("block", "bid", bid.toLong())
    
    block.bkHash      = bkHash
    block.version     = version
    block.timestamp   = timestamp
    block.nonce       = nonce
    block.difficulty  = difficulty.toFloat()
    block.merkle      = merkle
    block.numTx       = numTx.toInteger()
    block.outputValue = outputValue.toFloat()
    block.feesValue   = feesValue.toFloat()
    block.size        = size.toLong()
    
    if (lastBlock) {
      parentEdge = getOrCreateEdge("parent", block, lastBlock)
    }
    lastBlock = block
  })
}

//
// Transactions
//
loadTransactions = {
  loadDataType("transactions", { line ->
    (tid,txHash,version,bid,numInputs,numOutputs,outputValue,feesValue,lockTime,size) = line.split(",")
    
    if (tid == "ID") { return };
    transaction = getOrCreateVertex("transaction", "tid", tid.toLong())
    
    transaction.txHash      = txHash
    transaction.version     = version
    transaction.numInputs   = numInputs.toInteger()
    transaction.numOutputs  = numOutputs.toInteger()
    transaction.outputValue = outputValue.toFloat()
    transaction.feesValue   = feesValue.toFloat()
    transaction.lockTime    = lockTime.toLong()
    transaction.size        = size.toLong()
    
    block        = getOrCreateVertex("block", "bid", bid.toLong())
    includedEdge = getOrCreateEdge("included", transaction, block)
  })
}

//
// Run
//
loadBlocks()
loadTransactions()

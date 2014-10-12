// Boot
environment = System.getenv()["PLUTUS_ENV"]
if (environment == null) {
  environment = "dev"
}
println "Loading " + environment + " environment..."

// Load
println "Loading graph..."
TitanGraph g  = TitanFactory.open("config/" + environment + "/plutus.properties")
logFrequency = 100

getOrCreateVertex = { vertexLabel, idField, id ->
  existing = g.V(idField, id)
  if (existing.hasNext()) {
    return existing.next()
  } else {
    return g.addVertex(vertexLabel)
  }
}

getOrCreateEdge = { edgeLabel, source, target ->
  existing = source.out(edgeLabel).retain([target])
  if (existing.hasNext()) {
    return existing.next()
  } else {
    return source.addEdge(edgeLabel, target)
  }
}

// Log
log = { level, message ->
  println "[" + new Date() + "] " + level + " : " + message
}
info  = { message -> log("INFO",  message) }
warn  = { message -> log("WARN",  message) }
error = { message -> log("ERROR", message) }

// Blocks
loadBlocks = {
  lastBlock = null
  new File("data/" + environment + "/blocks.csv").eachLine({ line ->
    try {
      (bid,hash,version,timestamp,nonce,difficulty,merkle,numTransactions,outputValue,feesValue,size) = line.split(",")
    
      if (bid == "ID") { return };
      ibid  = bid.toLong()
      block = getOrCreateVertex("block", "bid", ibid)
    
      block.bid             = ibid
      block.hash            = hash
      block.version         = version
      block.timestamp       = timestamp
      block.nonce           = nonce
      block.difficulty      = difficulty.toFloat()
      block.merkle          = merkle
      block.numTransactions = numTransactions.toInteger()
      block.outputValue     = outputValue.toFloat()
      block.feesValue       = feesValue.toFloat()
      block.size            = size.toLong()
    
      if (lastBlock) {
	parentEdge = getOrCreateEdge("parent", block, lastBlock)
      } else {
	lastBlock = block
      }
    
      if (ibid % logFrequency == 0) { info("Loaded block " + bid) }
    } catch (Throwable e) {
      error(e.toString())
    }
  })
  g.commit()
  info("Loaded all blocks")
}

// Transactions
loadTransactions = {
  new File("data/" + environment + "/transactions.csv").eachLine({ line ->
    try {
      (tid,hash,version,blockId,numInputs,numOutputs,outputValue,feesValue,lockTime,size) = line.split(",")
    
      if (tid == "ID") { return };
      itid        = tid.toLong()
      transaction = getOrCreateVertex("transaction", "tid", itid)
    
      transaction.tid         = itid
      transaction.hash        = hash
      transaction.version     = version
      transaction.numInputs   = numInputs.toInteger()
      transaction.numOutputs  = numOutputs.toInteger()
      transaction.outputValue = outputValue.toFloat()
      transaction.feesValue   = feesValue.toFloat()
      transaction.lockTime    = lockTime.toLong()
      transaction.size        = size.toLong()

      block        = getOrCreateVertex("block", "tid", blockId.toInteger())
      includedEdge = getOrCreateEdge("included", transaction, block)
    
      if (itid % logFrequency == 0) { info("Loaded transaction " + tid) }
    } catch (Throwable e) {
      error(e.toString())
    }
  })
  g.commit()
  info("Loaded all transactions")
}

loadBlocks()
loadTransactions()

quit
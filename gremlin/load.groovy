// Boot
environment = System.getenv()["PLUTUS_ENV"]
if (environment == null) {
  environment = "dev"
}
println "Loading $environment environment..."

// Load
import javax.xml.bind.DatatypeConverter
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
  lineNum   = 0
  startedAt = (new Date()).getTime()
  new File("data/${environment}/${name}.csv").eachLine({ line ->
    try {
      lineNum += 1
      lineProcessor(line.split(",").collect(stripQuotes))
    } catch (Throwable e) {
      error(e.toString())
    }
    g.commit()
    if (lineNum % logFrequency == 0) {
      rate = (lineNum / (((new Date()).getTime() - startedAt) / 1000.0)).toInteger()
      info("Loaded line ${lineNum} of ${name} (${rate} lines / sec)...")
    }
  })
  info("Successfully loaded all " + name + "!")
}

parseTimestamp = { timestamp ->
  converted = DatatypeConverter.parseDateTime(timestamp).getTime().time
  return converted
}

stripQuotes = { string ->
  string.replace('"', '')
}
  
//
// Blocks
//
loadBlocks = {
  lastBlock = null
  loadDataType("blocks", { fields ->
    (bkid,bkHash,version,timestamp,nonce,difficulty,merkle,numTx,outputValue,feesValue,size) = fields
    if (bkid == "ID") { return };
    block = getOrCreateVertex("block", "bkid", bkid.toLong())
    
    block.bkHash      = bkHash
    block.version     = version
    block.timestamp   = parseTimestamp(timestamp)
    block.nonce       = nonce
    block.difficulty  = difficulty.toFloat()
    block.merkle      = merkle
    block.numTx       = numTx.toInteger()
    block.outputValue = outputValue.toLong()
    block.feesValue   = feesValue.toLong()
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
  loadDataType("transactions", { fields ->
    (txid,txHash,version,bkid,numInputs,numOutputs,outputValue,feesValue,lockTime,size) = fields
    
    if (txid == "ID") { return };
    
    transaction = getOrCreateVertex("transaction", "txid", txid.toLong())
    transaction.txHash      = txHash
    transaction.version     = version
    transaction.numInputs   = numInputs.toInteger()
    transaction.numOutputs  = numOutputs.toInteger()
    transaction.outputValue = outputValue.toLong()
    transaction.feesValue   = feesValue.toLong()
    transaction.lockTime    = lockTime.toLong()
    transaction.size        = size.toLong()
    
    block        = getOrCreateVertex("block", "bkid", bkid.toLong())
    includedEdge = getOrCreateEdge("included", transaction, block)
  })
}

//
// Outputs
// 
loadOutputs = {
  loadDataType("outputs", { fields ->
    (txid,index,value,script,rcvrAddressHash,inputTxHash,inputTxIndex) = fields
    
    if (txid == "TransactionId") { return };
    
    thisTransaction = getOrCreateVertex("transaction", "txid", txid.toLong())
    rcvrAddress     = getOrCreateVertex("address", "hash", rcvrAddressHash)
    output          = getOrCreateEdge("output", thisTransaction, rcvrAddress)

    lValue          = value.toLong()

    output.index           = index.toInteger()
    output.script          = script
    output.value           = lValue
    output.rcvrAddressHash = rcvrAddressHash

    if (rcvrAddress.balance == null) {
      rcvrAddress.balance = lValue
    } else {
      rcvrAddress.balance += lValue
    }
  })
}

//
// Inputs
// 
loadInputs = {
  loadDataType("inputs", { fields ->
    (txid,index,script,outputTxHash,outputTxIndex) = fields
    
    if (txid == "TransactionId") { return };
    
    thisTransaction     = getOrCreateVertex("transaction", "txid",   txid.toLong())
    inputTransaction    = getOrCreateVertex("transaction", "txHash", outputTxHash)
    try {
      input               = getOrCreateEdge("input", inputTransaction, thisTransaction)
    } catch (Throwable e) {
      if (e.message ==~ /out-unique/) {
	warn "POSSIBLE DUPLICATE INPUT: ${thisTransaction.hash}"
	throw e
      }
    }
    input.index         = index.toInteger()
    input.script        = script

    iOutputTxIndex      = outputTxIndex.toInteger()
    input.outputTxIndex = iOutputTxIndex
    
    potentialInputTransactionOutputsToSpend = inputTransaction.outE("output").has("index", iOutputTxIndex)
    if (potentialInputTransactionOutputsToSpend.hasNext()) {
      unspentAddressHash  = potentialInputTransactionOutputsToSpend.next().rcvrAddressHash
      unspentAddress      = getOrCreateVertex("address", "hash", unspentAddressHash)
      try {
	spent               = getOrCreateEdge("spent", unspentAddress, thisTransaction)
      } catch (Throwable e) {
	warn "POSSIBLE DUPLICATE INPUT: ${unspentAddress.hash}"
	throw e
      }
      spent.outputTxIndex = iOutputTxIndex
    }
  })
}

//
// Run
//
loadBlocks()
loadTransactions()
loadOutputs()
loadInputs()

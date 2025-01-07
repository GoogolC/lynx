import org.grapheco.lynx.KeyValueStore

/**
 * This is a demo application showing how to use KeyValueStore.
 * It demonstrates creating a KeyValueStore, writing, and reading key-value pairs.
 */
object DemoKeyValueStoreApp extends App {

  // Define a test file path for the demo
  val demoFile = "demo_key_value_store.txt"

  // Create an instance of KeyValueStore
  val store = new KeyValueStore(demoFile)

  // Write some data into the KeyValueStore
  val data = Map(
    "project" -> "Lynx",
    "type" -> "Demo",
    "language" -> "Scala",
    "version" -> "1.0.0"
  )
  store.write(data) match {
    case scala.util.Success(_) =>
      println("Data written successfully!")
    case scala.util.Failure(exception) =>
      println(s"Failed to write data: ${exception.getMessage}")
  }

  // Read the data back from the KeyValueStore
  val readData = store.read()
  println("\nData read from KeyValueStore:")
  readData.foreach { case (key, value) =>
    println(s"$key -> $value")
  }

  // Append a single key-value pair to the store
  KeyValueStore.writeKeyValue(demoFile, "author", "Grapheco") match {
    case scala.util.Success(_) =>
      println("\nAppended a key-value pair successfully!")
    case scala.util.Failure(exception) =>
      println(s"Failed to append data: ${exception.getMessage}")
  }

  // Read all data again
  println("\nAll data after appending:")
  KeyValueStore.readKeyValue(demoFile).foreach { case (key, value) =>
    println(s"$key -> $value")
  }
}

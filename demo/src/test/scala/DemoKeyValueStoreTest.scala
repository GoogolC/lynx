package demo

import org.grapheco.lynx.KeyValueStore

object DemoKeyValueStoreTest extends App {
  val testFilePath = "test_key_value_store.txt"

  // Test writeKeyValue
  println("Testing writeKeyValue...")
  KeyValueStore.writeKeyValue(testFilePath, "testKey", "testValue") match {
    case scala.util.Success(_) =>
      println("Successfully wrote key-value pair.")
    case scala.util.Failure(exception) =>
      println(s"Failed to write key-value pair: ${exception.getMessage}")
  }

  // Test readKeyValue
  println("\nTesting readKeyValue...")
  val data = KeyValueStore.readKeyValue(testFilePath)
  println("Key-Value pairs:")
  data.foreach { case (key, value) =>
    println(s"$key -> $value")
  }
}


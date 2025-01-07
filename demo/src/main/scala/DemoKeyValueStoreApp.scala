package demo

import org.grapheco.lynx.KeyValueStore

import scala.util.{Try, Success, Failure}
import java.io.File

object DemoKeyValueStoreApp extends App {
  val demoFile = "demo_key_value_store.txt"

  println(s"Using demo file: $demoFile")

  // Write key-value pairs to the file
  println("\nWriting key-value pairs:")
  KeyValueStore.writeKeyValue(demoFile, "name", "Lynx") match {
    case Success(_) => println("Successfully wrote: name -> Lynx")
    case Failure(ex) => println(s"Failed to write: ${ex.getMessage}")
  }

  KeyValueStore.writeKeyValue(demoFile, "version", "1.0") match {
    case Success(_) => println("Successfully wrote: version -> 1.0")
    case Failure(ex) => println(s"Failed to write: ${ex.getMessage}")
  }

  KeyValueStore.writeKeyValue(demoFile, "author", "Grapheco") match {
    case Success(_) => println("Successfully wrote: author -> Grapheco")
    case Failure(ex) => println(s"Failed to write: ${ex.getMessage}")
  }

  // Read key-value pairs from the file
  println("\nReading key-value pairs:")
  KeyValueStore.readKeyValue(demoFile).foreach { case (key, value) =>
    println(s"$key -> $value")
  }
}


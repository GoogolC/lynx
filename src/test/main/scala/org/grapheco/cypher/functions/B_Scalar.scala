package org.grapheco.cypher.functions

import org.grapheco.lynx.TestBase
import org.grapheco.lynx.physical.{NodeInput, RelationshipInput, StoredNodeInputRef}
import org.grapheco.lynx.types.LynxValue
import org.grapheco.lynx.types.composite.LynxList
import org.grapheco.lynx.types.property.{LynxNull, LynxString}
import org.grapheco.lynx.types.structural._
import org.grapheco.lynx.types.time.LynxLocalTime
import org.grapheco.lynx.util.LynxTemporalParser
import org.junit.jupiter.api.{Assertions, BeforeEach, Test}
import java.time.{LocalDateTime, LocalTime, ZoneId}
import java.util.Date
import scala.collection.mutable.ArrayBuffer

/**
 * @program: lynx
 * @description:
 * @author: Wangkainan
 * @create: 2022-08-30 14:15
 */

/*
create ({name:'Alice',eyes:'brown',age:38})
create ({name:'Charlie',eyes:'green',age:53})
create ({name:'Bob',eyes:'blue',age:25})
create ({name:'Daniel',eyes:'brown',age:54})
create ({name:'Eskil',eyes:'blue',age:41,array:['one','two','three']})

match (a),(b)
where a.name='Alice' and b.name='Bob'
create (a)-[r:KNOWS]->(b)

match (a),(c)
where a.name='Alice' and c.name='Charlie'
create (a)-[r:KNOWS]->(c)

match (b),(d)
where b.name='Bob' and d.name='Daniel'
create (b)-[r:KNOWS]->(d)

match (d),(c)
where c.name='Charlie' and d.name='Daniel'
create (c)-[r:KNOWS]->(d)

match (b),(e)
where b.name='Bob' and e.name='Eskil'
create (b)-[r:MARRIED]->(e)

  */

class B_Scalar extends TestBase {
  val nodesInput = ArrayBuffer[(String, NodeInput)]()
  val relationsInput = ArrayBuffer[(String, RelationshipInput)]()

  val n1 = TestNode(TestId(1), Seq(LynxNodeLabel("Developer")),
    Map(LynxPropertyKey("name") -> LynxValue("Alice"),
      LynxPropertyKey("age") -> LynxValue("38"),
      LynxPropertyKey("eyes") -> LynxValue("brown")))
  val n2 = TestNode(TestId(2), Seq.empty,
    Map(LynxPropertyKey("name") -> LynxValue("Charlie"),
      LynxPropertyKey("age") -> LynxValue("53"),
      LynxPropertyKey("eyes") -> LynxValue("green")))
  val n3 = TestNode(TestId(3), Seq.empty,
    Map(LynxPropertyKey("name") -> LynxValue("Bob"),
      LynxPropertyKey("age") -> LynxValue("25"),
      LynxPropertyKey("eyes") -> LynxValue("blue")))
  val n4 = TestNode(TestId(4), Seq.empty,
    Map(LynxPropertyKey("name") -> LynxValue("Daniel"),
      LynxPropertyKey("age") -> LynxValue("54"),
      LynxPropertyKey("eyes") -> LynxValue("brown")))
  val n5 = TestNode(TestId(5), Seq.empty,
    Map(LynxPropertyKey("name") -> LynxValue("Eskil"),
      LynxPropertyKey("age") -> LynxValue("38"),
      LynxPropertyKey("eyes") -> LynxValue("brown"),
      LynxPropertyKey("array") -> LynxValue(Array("one", "two", "three"))))

  val r1 = TestRelationship(TestId(1), TestId(1), TestId(2), Option(LynxRelationshipType("KNOWS")), Map.empty)
  val r2 = TestRelationship(TestId(2), TestId(1), TestId(3), Option(LynxRelationshipType("KNOWS")), Map.empty)
  val r3 = TestRelationship(TestId(3), TestId(2), TestId(3), Option(LynxRelationshipType("KNOWS")), Map.empty)
  val r4 = TestRelationship(TestId(4), TestId(3), TestId(4), Option(LynxRelationshipType("KNOWS")), Map.empty)
  val r5 = TestRelationship(TestId(5), TestId(3), TestId(5), Option(LynxRelationshipType("MARRIED")), Map.empty)

  @BeforeEach
  def init(): Unit = {
    nodesInput.append(("n1", NodeInput(n1.labels, n1.props.toSeq)))
    nodesInput.append(("n2", NodeInput(n2.labels, n2.props.toSeq)))
    nodesInput.append(("n3", NodeInput(n3.labels, n3.props.toSeq)))
    nodesInput.append(("n4", NodeInput(n4.labels, n4.props.toSeq)))
    nodesInput.append(("n5", NodeInput(n5.labels, n5.props.toSeq)))

    relationsInput.append(("r1", RelationshipInput(Seq(r1.relationType.get), Seq.empty, StoredNodeInputRef(r1.startNodeId), StoredNodeInputRef(r1.endNodeId))))
    relationsInput.append(("r2", RelationshipInput(Seq(r2.relationType.get), Seq.empty, StoredNodeInputRef(r2.startNodeId), StoredNodeInputRef(r2.endNodeId))))
    relationsInput.append(("r3", RelationshipInput(Seq(r3.relationType.get), Seq.empty, StoredNodeInputRef(r3.startNodeId), StoredNodeInputRef(r3.endNodeId))))
    relationsInput.append(("r4", RelationshipInput(Seq(r4.relationType.get), Seq.empty, StoredNodeInputRef(r4.startNodeId), StoredNodeInputRef(r4.endNodeId))))
    relationsInput.append(("r5", RelationshipInput(Seq(r5.relationType.get), Seq.empty, StoredNodeInputRef(r5.startNodeId), StoredNodeInputRef(r5.endNodeId))))

    model.write.createElements(nodesInput, relationsInput,
      (nodesCreated: Seq[(String, LynxNode)], relsCreated: Seq[(String, LynxRelationship)]) => {
        nodesCreated.toMap ++ relsCreated
      }
    )
    model.write.commit
  }

  @Test
  def coalesce(): Unit = {
    val records = runOnDemoGraph(
      """
        |MATCH (a)
        |WHERE a.name = 'Alice'
        |RETURN coalesce(a.hairColor, a.eyes)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(LynxValue("brown"), records(0)("coalesce(a.hairColor, a.eyes)"))
  }

  @Test
  def endNode(): Unit = {

    val records = runOnDemoGraph(
      """
        |MATCH (x:Developer)-[r]-()
        |RETURN endNode(r)
        |""".stripMargin).records().map(f => f("endNode(r)").asInstanceOf[TestNode]).toArray

    Assertions.assertEquals(2, records.length)
    records.sortBy(_.id.value).zip(List(n2, n3)).foreach(item => Assertions.assertEquals(item._1, item._2))
  }


  @Test
  def head(): Unit = {

    val records = runOnDemoGraph(
      """
        |MATCH (a)
        |WHERE a.name = 'Eskil'
        |RETURN a.array, head(a.array)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(List(LynxString("one"), LynxString("two"), LynxString("three")), records.head("a.array").asInstanceOf[LynxList].value.toList)
    Assertions.assertEquals("one", records.head("head(a.array)").asInstanceOf[LynxValue].value)
  }

  /*
  two errors
    1.   id should be Integer
    2.   expected id from 0 to 4, actual id from 1 to 5
   */
  @Test
  def id(): Unit = {

    val records = runOnDemoGraph(
      """
        |MATCH (a)
        |RETURN id(a)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(5, records.length)
    Array(1,2,3,4,5)
      .map(LynxValue.apply)
      .zip(records.map(_.getAsInt("id(a)")).sortBy(_.get.v))
      .foreach{ case (id, value) => Assertions.assertEquals(id, value.get)}
  }

  @Test
  def last(): Unit = {

    val records = runOnDemoGraph(
      """
        |MATCH (a)
        |WHERE a.name = 'Eskil'
        |RETURN a.array, last(a.array)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(List(LynxString("one"), LynxString("two"), LynxString("three")), records.head("a.array").asInstanceOf[LynxList].value.toList)
    Assertions.assertEquals("three", records.head("last(a.array)").asInstanceOf[LynxValue].value)
  }


  @Test
  def length(): Unit = {

    val records = runOnDemoGraph(
      """
        |MATCH p =(a)-->(b)-->(c)
        |WHERE a.name = 'Alice'
        |RETURN length(p)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(3, records.length)
    Assertions.assertEquals(LynxValue(2), records(0)("length(p)"))
    Assertions.assertEquals(LynxValue(2), records(1)("length(p)"))
    Assertions.assertEquals(LynxValue(2), records(2)("length(p)"))
  }

  /*
  returns should be Integer
   */
  @Test
  def properties(): Unit = {
    val num = nodesInput.length
    val records = runOnDemoGraph(
      """
        |CREATE (p:Person { name: 'Stefan', city: 'Berlin' })
        |RETURN properties(p)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(Map("name" -> LynxString("Stefan"), "city" -> LynxString("Berlin")), records(0)("properties(p)").asInstanceOf[LynxValue].value)
    //TODO   Labels added: 1
    Assertions.assertEquals(num + 1, all_nodes.size)
  }

  @Test
  def randomUUID(): Unit = {
    val records0 = runOnDemoGraph(
      """
        |RETURN randomUUID() AS uuid
        |""".stripMargin).records().toArray

    val records1 = runOnDemoGraph(
      """
        |RETURN randomUUID() AS uuid
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records0.length)
    Assertions.assertEquals(1, records1.length)
    Assertions.assertEquals(records1.head("uuid").value.asInstanceOf[String].length,
      records0.head("uuid").value.asInstanceOf[String].length)
    Assertions.assertNotEquals(records1.head("uuid"), records0.head("uuid"))
  }

  /*
  returns should be Integer
   */
  @Test
  def size(): Unit = {
    val records = runOnDemoGraph(
      """
        |RETURN size(['Alice', 'Bob'])
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(LynxValue(2), records.head("size(['Alice', 'Bob'])"))
  }

  @Test
  def sizeAppliedToPatternExpression(): Unit = {
    val records = runOnDemoGraph(
      """
        |MATCH (a)
        |WHERE a.name = 'Alice'
        |RETURN size((a)-->()) AS fof
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(LynxValue(2), records.head("fof"))
  }

  /*
  size(a.name) should be Integer
   */
  @Test
  def sizeAppliedToString(): Unit = {
    val records = runOnDemoGraph(
      """
        |MATCH (a)
        |WHERE size(a.name)> 6
        |RETURN size(a.name)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(LynxValue(7), records.head("size(a.name)"))
  }

  @Test
  def startNode(): Unit = {
    val records = runOnDemoGraph(
      """
        |MATCH (x:Developer)-[r]-()
        |RETURN startNode(r)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(2, records.length)
    Assertions.assertEquals(n1, records(0)("startNode(r)"))
    Assertions.assertEquals(n1, records(1)("startNode(r)"))
  }

  @Test
  def timestamp(): Unit = {
    val records0 = runOnDemoGraph(
      """
        |RETURN timestamp()
        |""".stripMargin).records().toArray

    val date_System = new Date()
    val date_Cypher = new Date(records0(0)("timestamp()").value.asInstanceOf[Long])

    val localTime_Cypher = LynxLocalTime(LocalDateTime.ofInstant(date_System.toInstant, ZoneId.systemDefault()).toLocalTime)
    val localTime_System = LynxLocalTime(LocalDateTime.ofInstant(date_Cypher.toInstant, ZoneId.systemDefault()).toLocalTime)

    Assertions.assertEquals(1, records0.length)
    Assertions.assertTrue(LynxTemporalParser.isSameCurrentTime(localTime_System, localTime_Cypher))
  }

  @Test
  def toBoolean(): Unit = {
    val records = runOnDemoGraph(
      """
        |RETURN toBoolean('TRUE'), toBoolean('not a boolean')
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(true, records(0)("toBoolean('TRUE')").asInstanceOf[LynxValue].value)
    Assertions.assertEquals(null, records(0)("toBoolean('not a boolean')").asInstanceOf[LynxValue].value)
  }


  @Test
  def toFloat(): Unit = {
    val records = runOnDemoGraph(
      """
        |RETURN toFloat('11.5'), toFloat('not a number')
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(11.5, records(0)("toFloat('11.5')").asInstanceOf[LynxValue].value)
    Assertions.assertEquals(null, records(0)("toFloat('not a number')").asInstanceOf[LynxValue].value)
  }

  /*
  returns should be Integer
   */
  @Test
  def toInteger(): Unit = {
    val records = runOnDemoGraph(
      """
        |RETURN toInteger('42'), toInteger('not a number')
        |""".stripMargin).records().toArray

    Assertions.assertEquals(1, records.length)
    Assertions.assertEquals(LynxValue(42), records(0)("toInteger('42')"))
    Assertions.assertEquals(LynxNull, records(0)("toInteger('not a number')"))
  }

  @Test
  def typeRelationship(): Unit = {
    val records = runOnDemoGraph(
      """
        |MATCH (n)-[r]->()
        |WHERE n.name = 'Alice'
        |RETURN type(r)
        |""".stripMargin).records().toArray

    Assertions.assertEquals(2, records.length)
    Assertions.assertEquals("KNOWS", records(0)("type(r)").asInstanceOf[LynxValue].value)
    Assertions.assertEquals("KNOWS", records(1)("type(r)").asInstanceOf[LynxValue].value)

  }
}


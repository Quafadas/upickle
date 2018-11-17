import mill._, mill.scalalib._, mill.scalalib.publish._, mill.scalajslib._

trait CommonModule extends ScalaModule {

  def platformSegment: String

  def sources = T.sources(
    millSourcePath / "src",
    millSourcePath / s"src-$platformSegment"
  )
}
trait CommonPublishModule extends CommonModule with PublishModule with CrossScalaModule{
  def publishVersion = "0.6.7"
  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "com.lihaoyi",
    url = "https://github.com/lihaoyi/upickle",
    licenses = Seq(License.MIT),
    scm = SCM(
      "git://github.com/lihaoyi/upickle.git",
      "scm:git://github.com/lihaoyi/upickle.git"
    ),
    developers = Seq(
      Developer("lihaoyi", "Li Haoyi","https://github.com/lihaoyi")
    )
  )
}

object core extends Module {

  trait CoreTests extends TestModule{
    def ivyDeps = Agg(ivy"com.lihaoyi::utest::0.6.4", ivy"com.lihaoyi::acyclic:0.1.5")
    def testFrameworks = Seq("upickle.core.UTestFramework")
  }
  object js extends Cross[CoreJsModule]("2.11.12", "2.12.6")

  class CoreJsModule(val crossScalaVersion: String) extends CommonPublishModule with ScalaJSModule {
    def artifactName = "upickle-core"
    def millSourcePath = build.millSourcePath / "core"
    def scalaJSVersion = "0.6.22"

    def platformSegment = "js"
    object test extends Tests with CoreTests
  }

  object jvm extends Cross[CoreJvmModule]("2.11.12", "2.12.6")
  class CoreJvmModule(val crossScalaVersion: String) extends CommonPublishModule {
    def platformSegment = "jvm"
    def artifactName = "upickle-core"
    def millSourcePath = build.millSourcePath / "core"
    object test extends Tests with CoreTests
  }
}
object upack extends Module {

  object js extends Cross[PackJsModule]("2.11.12", "2.12.6")

  class PackJsModule(val crossScalaVersion: String) extends CommonPublishModule with ScalaJSModule {
    def moduleDeps = Seq(core.js())
    def artifactName = "upack"
    def millSourcePath = build.millSourcePath / "upack"
    def scalaJSVersion = "0.6.22"

    def platformSegment = "js"
    object test extends Tests with CommonModule with TestScalaJSModule  {
      def moduleDeps = super.moduleDeps ++ Seq(ujson.js().test)
      def scalaJSVersion = "0.6.22"
      def platformSegment = "js"
      def testFrameworks = Seq("upickle.core.UTestFramework")
    }
  }

  object jvm extends Cross[PackJvmModule]("2.11.12", "2.12.6")
  class PackJvmModule(val crossScalaVersion: String) extends CommonPublishModule {
    def moduleDeps = Seq(core.js())
    def platformSegment = "jvm"
    def artifactName = "upack"
    def millSourcePath = build.millSourcePath / "upack"
    object test extends Tests with CommonModule  {
      def moduleDeps = super.moduleDeps ++ Seq(ujson.jvm().test)
      def platformSegment = "jvm"
      def testFrameworks = Seq("upickle.core.UTestFramework")
    }
  }
}

trait JsonModule extends CommonPublishModule{
  def artifactName = "ujson"
  def millSourcePath = build.millSourcePath / "ujson"
  trait JawnTestModule extends Tests with CommonModule{
    def platformSegment = JsonModule.this.platformSegment
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest::3.0.3",
      ivy"org.scalacheck::scalacheck::1.13.5"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

object ujson extends Module{
  object js extends Cross[JsonJsModule]("2.11.12", "2.12.6")
  class JsonJsModule(val crossScalaVersion: String) extends JsonModule with ScalaJSModule {
    def moduleDeps = Seq(core.js())
    def scalaJSVersion = "0.6.22"
    def platformSegment = "js"

    object test extends JawnTestModule with TestScalaJSModule{
      def scalaJSVersion = "0.6.22"
    }
  }

  object jvm extends Cross[JsonJvmModule]("2.11.12", "2.12.6")
  class JsonJvmModule(val crossScalaVersion: String) extends JsonModule{
    def moduleDeps = Seq(core.jvm())
    def platformSegment = "jvm"
    object test extends JawnTestModule
  }

  object argonaut extends Cross[ArgonautModule]("2.11.12", "2.12.6")
  class ArgonautModule(val crossScalaVersion: String) extends CommonPublishModule{
    def artifactName = "ujson-argonaut"
    def platformSegment = "jvm"
    def moduleDeps = Seq(ujson.jvm())
    def ivyDeps = Agg(ivy"io.argonaut::argonaut:6.2")
  }
  object json4s extends Cross[Json4sModule]("2.11.12", "2.12.6")
  class Json4sModule(val crossScalaVersion: String) extends CommonPublishModule{
    def artifactName = "ujson-json4s"
    def platformSegment = "jvm"
    def moduleDeps = Seq(ujson.jvm())
    def ivyDeps = Agg(
      ivy"org.json4s::json4s-ast:3.5.2",
      ivy"org.json4s::json4s-native:3.5.2"
    )
  }

  object circe extends Cross[CirceModule]("2.11.12", "2.12.6")
  class CirceModule(val crossScalaVersion: String) extends CommonPublishModule{
    def artifactName = "ujson-circe"
    def platformSegment = "jvm"
    def moduleDeps = Seq(ujson.jvm())
    def ivyDeps = Agg(ivy"io.circe::circe-parser:0.9.1")
  }

  object play extends Cross[PlayModule]("2.11.12", "2.12.6")
  class PlayModule(val crossScalaVersion: String) extends CommonPublishModule{
    def artifactName = "ujson-play"
    def platformSegment = "jvm"
    def moduleDeps = Seq(ujson.jvm())
    def ivyDeps = Agg(
      ivy"com.typesafe.play::play-json:2.6.9",
      ivy"com.fasterxml.jackson.core:jackson-databind:2.9.4"
    )
  }
}

trait UpickleModule extends CommonPublishModule{
  def artifactName = "upickle"
  def millSourcePath = build.millSourcePath / "upickle"
  def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
    ivy"com.lihaoyi::acyclic:0.1.5"
  )
  def compileIvyDeps = Agg(
    ivy"com.lihaoyi::acyclic:0.1.5",
    ivy"org.scala-lang:scala-reflect:${scalaVersion()}",
    ivy"org.scala-lang:scala-compiler:${scalaVersion()}"
  )
  def scalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8",
    "-feature"
  )


  def generatedSources = T{
    val dir = T.ctx().dest
    val file = dir / "upickle" / "Generated.scala"
    ammonite.ops.mkdir(dir / "upickle")
    val tuples = (1 to 22).map{ i =>
      def commaSeparated(s: Int => String) = (1 to i).map(s).mkString(", ")
      val writerTypes = commaSeparated(j => s"T$j: Writer")
      val readerTypes = commaSeparated(j => s"T$j: Reader")
      val typeTuple = commaSeparated(j => s"T$j")
      val implicitWriterTuple = commaSeparated(j => s"implicitly[Writer[T$j]]")
      val implicitReaderTuple = commaSeparated(j => s"implicitly[Reader[T$j]]")
      val lookupTuple = commaSeparated(j => s"x(${j-1})")
      val fieldTuple = commaSeparated(j => s"x._$j")
      val caseReader =
        if(i == 1) s"f(readJs[Tuple1[T1]](x)._1)"
        else s"f.tupled(readJs[Tuple$i[$typeTuple]](x))"
        s"""
        implicit def Tuple${i}Writer[$writerTypes]: TupleNWriter[Tuple$i[$typeTuple]] =
          new TupleNWriter[Tuple$i[$typeTuple]](Array($implicitWriterTuple), x => if (x == null) null else Array($fieldTuple))
        implicit def Tuple${i}Reader[$readerTypes]: TupleNReader[Tuple$i[$typeTuple]] =
          new TupleNReader(Array($implicitReaderTuple), x => Tuple$i($lookupTuple).asInstanceOf[Tuple$i[$typeTuple]])
        """
    }

    ammonite.ops.write(file, s"""
      package upickle
      package api
      import acyclic.file
      import language.experimental.macros
      /**
       * Auto-generated picklers and unpicklers, used for creating the 22
       * versions of tuple-picklers and case-class picklers
       */
      trait Generated extends upickle.core.Types{
        ${tuples.mkString("\n")}
      }
    """)
    Seq(PathRef(dir))
  }
}



object upickle extends Module{
  object jvm extends Cross[UpickleJvmModule]("2.11.12", "2.12.6")
  class UpickleJvmModule(val crossScalaVersion: String) extends UpickleModule{
    def platformSegment = "jvm"
    def moduleDeps = Seq(ujson.jvm(), upack.jvm())

    object test extends Tests with CommonModule{
      def platformSegment = "jvm"
      def moduleDeps = super.moduleDeps ++ Seq(
        ujson.argonaut(),
        ujson.circe(),
        ujson.json4s(),
        ujson.play(),
        core.jvm().test
      )
      def ivyDeps = super.ivyDeps() ++ bench.jvm.ivyDeps()
      def testFrameworks = Seq("upickle.core.UTestFramework")
    }
  }

  object js extends Cross[UpickleJsModule]("2.11.12", "2.12.6")
  class UpickleJsModule(val crossScalaVersion: String) extends UpickleModule with ScalaJSModule {
    def moduleDeps = Seq(ujson.js(), upack.jvm())
    def platformSegment = "js"

    def scalaJSVersion = "0.6.22"
    def scalacOptions = T{
      super.scalacOptions() ++ Seq({
        val a = build.millSourcePath.toString.replaceFirst("[^/]+/?$", "")
        val g = "https://raw.githubusercontent.com/lihaoyi/upickle"
        s"-P:scalajs:mapSourceURI:$a->$g/v${publishVersion()}/"
      })
    }
    object test extends Tests with CommonModule{
      def platformSegment = "js"
      def moduleDeps = super.moduleDeps ++ Seq(core.js().test)
      def scalaJSVersion = "0.6.22"
      def testFrameworks = Seq("upickle.core.UTestFramework")
    }
  }
}

trait BenchModule extends CommonModule{
  def scalaVersion = "2.12.6"
  def millSourcePath = build.millSourcePath / "bench"
  def ivyDeps = Agg(
    ivy"io.circe::circe-core::0.9.1",
    ivy"io.circe::circe-generic::0.9.1",
    ivy"io.circe::circe-parser::0.9.1",
    ivy"com.typesafe.play::play-json::2.6.7",
    ivy"io.argonaut::argonaut:6.2",
    ivy"org.json4s::json4s-ast:3.5.2",
    ivy"com.lihaoyi::sourcecode::0.1.4",
    ivy"com.avsystem.commons::commons-core::1.26.3",
  )
}

object bench extends Module {
  object js extends BenchModule with ScalaJSModule {
    def scalaJSVersion = "0.6.22"
    def platformSegment = "js"
    def moduleDeps = Seq(upickle.js("2.12.6").test)
    def run(args: String*) = T.command {
      finalMainClassOpt() match{
        case Left(err) => mill.eval.Result.Failure(err)
        case Right(_) =>
          ScalaJSWorkerApi.scalaJSWorker().run(
            toolsClasspath().map(_.path),
            nodeJSConfig(),
            fullOpt().path.toIO
          )
          mill.eval.Result.Success(())
      }

    }
  }

  object jvm extends BenchModule {
    def platformSegment = "jvm"
    def moduleDeps = Seq(upickle.jvm("2.12.6").test)
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"com.fasterxml.jackson.module::jackson-module-scala:2.9.4",
      ivy"com.fasterxml.jackson.core:jackson-databind:2.9.4",
    )
  }
}

name            := "companion"
version         := "1.0-SNAPSHOT"
scalaVersion    := "2.11.12"
organization    := "com.verizon.itanalytics.dataengineering"
resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)


libraryDependencies ++= {
  lazy val akkaHttpVersion = "10.1.0"
  lazy val akkaVersion = "2.5.11"
  lazy val alpakkaVersion = "0.17"
  lazy val jacksonVersion = "2.9.5"
  lazy val scalaTestVersion = "3.0.1"
  lazy val slickVersion = "3.2.1"
  Seq(
    "com.typesafe.akka"           %% "akka-http"                  % akkaHttpVersion,
    "com.typesafe.akka"           %% "akka-http-spray-json"       % akkaHttpVersion,
    "com.typesafe.akka"           %% "akka-http-xml"              % akkaHttpVersion,
    
    "com.typesafe.akka"           %% "akka-stream"                % akkaVersion,
    "com.typesafe.akka"           %% "akka-slf4j"                 % akkaVersion,

    "com.typesafe.slick"          %% "slick"                      % slickVersion,

    "com.lightbend.akka"          %% "akka-stream-alpakka-csv"    % alpakkaVersion,
    "com.lightbend.akka"          %% "akka-stream-alpakka-slick"  % alpakkaVersion,

    "ch.qos.logback"              % "logback-classic"             % "1.1.7",
    "de.heikoseeberger"           %% "accessus"                   % "0.1.0",

    "com.fasterxml.jackson.core"  % "jackson-core"                % jacksonVersion,

    "com.typesafe.akka"           %% "akka-http-testkit"          % akkaHttpVersion   % Test,
    "com.typesafe.akka"           %% "akka-testkit"               % akkaVersion       % Test,
    "com.typesafe.akka"           %% "akka-stream-testkit"        % akkaVersion       % Test,
    "org.scalatest"               %% "scalatest"                  % scalaTestVersion  % Test
  )
}


libraryDependencies += "com.h2database" % "h2" % "1.4.192"

libraryDependencies ++= {
  val jpmmlVersion = "1.4.1"
  Seq(
    "org.jpmml" % "pmml-evaluator" % jpmmlVersion,
    "org.jpmml" % "pmml-evaluator-extension" % jpmmlVersion
  )
}

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
libraryDependencies += "junit" % "junit" % "4.12" % Test
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

val scalaTestVersion = "3.0.5"
def scalastic = Def.setting {
  scalaBinaryVersion.value match {
    case "2.10" => Nil
    case _      => ("org.scalactic" %% "scalactic" % scalaTestVersion) :: Nil
  }
}

def scalatest = Def.setting {
  scalaBinaryVersion.value match {
    case "2.10" => Nil
    case _      => ("org.scalatest" %% "scalatest" % scalaTestVersion % Test) :: Nil
  }
}


val meta = """META.INF(.)*""".r

assemblyMergeStrategy in assembly := {
  case PathList("com", "typesafe", xs @ _*) => MergeStrategy.last
  case PathList("com", "lightbend", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case "overview.html" => MergeStrategy.last  // Added this for 2.1.0 I think
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}


mainClass in assembly := some("com.verizon.itanalytics.dataengineering.companion.Companion")
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true)
assemblyJarName in assembly := s"${name.value}-v${version.value}.jar"
fullClasspath in Runtime := (fullClasspath in (Compile, run)).value
ThisBuild / scalaVersion := "2.12.8"

lazy val server = (project in file("."))
  .settings(
    name := "Display"
  )

lazy val akkaVersion = "2.6.8"
lazy val akkaHttpVersion = "10.1.12"

libraryDependencies ++= Seq(
  "org.apache.poi" % "poi-ooxml" % "3.17",
  "com.typesafe.slick" %% "slick" % "3.3.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.1",
  "org.postgresql" % "postgresql" % "42.2.5",//org.postgresql.ds.PGSimpleDataSource dependency
  "joda-time"%"joda-time"%"2.10.5",
  "org.slf4j" % "slf4j-nop" % "1.7.26",
  "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed"         % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-pki" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % "0.10.7",
  "io.netty" % "netty-tcnative-boringssl-static" % "2.0.22.Final",
  "com.sun.mail" % "javax.mail" % "1.6.2",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "org.json4s" %% "json4s-native" % "3.6.0-M3",
  "com.aliyun" % "aliyun-java-sdk-dysmsapi" % "2.1.0",
  "com.aliyun" % "aliyun-java-sdk-core" % "4.5.4",
  "com.github.takezoe" %% "runtime-scaladoc-reader" % "1.0.1",  //读注释生成文档，注意到下面的addComplilerPlugin(com.github.takezoe)也需要加
  "ch.qos.logback" % "logback-classic" % "1.0.9"

)
addCompilerPlugin("com.github.takezoe" %% "runtime-scaladoc-reader" % "1.0.1")


scalacOptions += "-deprecation"
fork in run := true

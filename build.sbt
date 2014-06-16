name := "SmapApi-demo"

version := "0.1.1"

organization := "org.hittepit"

scalaVersion := "2.10.3"

libraryDependencies += "org.hittepit" %% "smapapi" % "0.1.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.7" % "test->*" excludeAll (ExclusionRule(organization="org.junit", name="junit"))

libraryDependencies += "org.mockito" % "mockito-core" % "1.9.5" % "test"

libraryDependencies += "com.h2database" % "h2" % "1.3.171" % "test"

libraryDependencies += "commons-dbcp" % "commons-dbcp" % "1.4"

libraryDependencies += "org.slf4j" % "jcl-over-slf4j" % "1.7.5"

scalacOptions ++= Seq("-feature", "-deprecation")

//(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/report")

parallelExecution in Test := false
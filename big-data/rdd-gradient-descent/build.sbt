name := "lin_reg"

version := "0.1"

scalaVersion := "2.13.10"
val sparkVersion = "3.2.1"

libraryDependencies  ++= Seq(
  // Last stable release
  "org.scalanlp" %% "breeze" % "1.2",

  // The visualization library is distributed separately as well.
  // It depends on LGPL code
  "org.scalanlp" %% "breeze-viz" % "1.2",
  "org.apache.spark" %% "spark-sql" % sparkVersion withSources(),
  "org.apache.spark" %% "spark-mllib" % sparkVersion withSources()
)

libraryDependencies += ("org.scalatest" %% "scalatest" % "3.2.14" % "test" withSources())

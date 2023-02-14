package vk.made.itmo.sigmadt.spark_ml

import breeze.linalg._
import org.apache.spark.sql.SparkSession


object Main {

  val Spark: SparkSession = SparkSession.builder
    .appName("sparkML")
    .config("spark.master", "local")
    .getOrCreate()

  def main(args: Array[String]): Unit = {
    import Spark.implicits._
    /*
    * remove all warnings in console.
    * If you want to see info about stages set it to "WARN"
    */
    Spark.sparkContext.setLogLevel("FATAL")

    val X = DenseMatrix.rand(10000, 3)
    val y: DenseVector[Double] = X * DenseVector(1.5, 0.3, -0.7)
    val data: DenseMatrix[Double] = DenseMatrix.horzcat(X, y.asDenseMatrix.t)

    val df = data(*, ::).iterator
      .map(x => (x(0), x(1), x(2), x(3)))
      .toSeq
      .toDF("x_1", "x_2", "x_3", "target")


    println(df.show())

    val linReg = new LinearRegression("linreg")
      .setIters(1000)
      .setLearningRate(0.1)
      .setBatch(500)
      .fit(df)

    linReg.transform(df)

    println("*******************************")
    println("Results are: ")
    val result = linReg.getWeights.toArray
    for (i <- 0 until 3) {
      println(s"x_$i = ${result(i)}")
    }
    println("*******************************")
  }
}

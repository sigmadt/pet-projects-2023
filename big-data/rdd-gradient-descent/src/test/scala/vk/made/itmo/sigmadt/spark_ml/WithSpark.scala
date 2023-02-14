package vk.made.itmo.sigmadt.spark_ml

import breeze.linalg.{*, DenseMatrix, DenseVector}
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}

object WithSpark {
  lazy val _spark: SparkSession = SparkSession.builder
    .appName("okay")
    .config("spark.master", "local")
    .getOrCreate()

  _spark.sparkContext.setLogLevel("FATAL")

  lazy val _sqlc: SQLContext = _spark.sqlContext

//  lazy val _df: DataFrame = createDF(100, DenseVector(17.13, -21.03, 21.01))
  lazy val _df: DataFrame = createDF(1000, DenseVector(1, 2, 3))

  private def createDF(rows: Int, weights: DenseVector[Double]) = {
    import _spark.implicits._
    val X = DenseMatrix.rand(rows, 3)
    val y: DenseVector[Double] = X * weights
    val data: DenseMatrix[Double] = DenseMatrix.horzcat(X, y.asDenseMatrix.t)

    data(*, ::).iterator
      .map(x => (x(0), x(1), x(2), x(3)))
      .toSeq
      .toDF("x_1", "x_2", "x_3", "target")
  }
}

trait WithSpark {
  lazy val spark: SparkSession = WithSpark._spark
  lazy val sqlc: SQLContext = WithSpark._sqlc
  lazy val df: DataFrame = WithSpark._df
}


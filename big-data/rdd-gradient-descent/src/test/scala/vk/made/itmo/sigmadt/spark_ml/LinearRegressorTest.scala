package vk.made.itmo.sigmadt.spark_ml

import breeze.linalg.DenseVector
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.sql.DataFrame
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should


class LinearRegressorTest extends AnyFlatSpec with WithSpark with should.Matchers {
  private val precision: Double = 0.05
  private val mse: Double = 10

  private def checkModel(model: LinearRegressor, df: DataFrame) = {
    val preds = model.transform(df)

    val pairRDD = preds
      .join(df.select("target"))
      .rdd
      .map(pair => (pair(0), pair(1)))

    val regScore = new RegressionMetrics(pairRDD)

    val scoreMSE = regScore.meanSquaredError
    println(s"SCORE: $scoreMSE")
    scoreMSE should be < mse
  }

  "Model" should "be precise" in {
    val linregModel = new LinearRegression("lr")
      .setIters(1000)
      .setLearningRate(0.1)
      .setBatch(1)
      .fit(df)
    checkModel(linregModel, df)
  }

  "Model" should "give right weights" in {
    val model = new LinearRegression("lr")
      .setIters(1000)
      .setLearningRate(0.1)
      .setBatch(10)
      .fit(df)

    val weights = model.getWeights
    val rightWeights: DenseVector[Double] = DenseVector(1, 2, 3)

    weights(0) should be  (rightWeights(0) +- precision)
    weights(1) should be  (rightWeights(1) +- precision)
    weights(2) should be  (rightWeights(2) +- precision)

  }
}

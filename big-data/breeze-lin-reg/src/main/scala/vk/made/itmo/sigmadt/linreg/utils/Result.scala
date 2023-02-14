package vk.made.itmo.sigmadt.linreg.utils

import breeze.linalg.DenseVector

class Result(val name: String) {
  val DASHES = "--------------------"
  val STARS  = "********************"

  def collect(yTrue: DenseVector[Double], yPred: DenseVector[Double]): Unit = {
    println(s"Results for $name")
    println(DASHES)

    val mae = Metric.MAE(yTrue, yPred)
    println(s"MAE  score is: $mae")
    println(DASHES)

    val rmse = Metric.RMSE(yTrue, yPred)
    println(s"RMSE score is: $rmse")
    println(DASHES)

    println(STARS)
  }
}

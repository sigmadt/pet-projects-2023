package vk.made.itmo.sigmadt.linreg.utils

import breeze.linalg.{DenseVector, sum}
import breeze.numerics.abs

object Metric {
  def MAE(yTrue: DenseVector[Double], yPred: DenseVector[Double]): Double = {
    sum(abs(yTrue - yPred)) / yTrue.length
  }

  def RMSE(yTrue: DenseVector[Double], yPred: DenseVector[Double]): Double = {
    val diff = yTrue - yPred
    val squaredDiff = diff *:* diff

    Math.sqrt(sum(squaredDiff) / yTrue.length)

  }
}

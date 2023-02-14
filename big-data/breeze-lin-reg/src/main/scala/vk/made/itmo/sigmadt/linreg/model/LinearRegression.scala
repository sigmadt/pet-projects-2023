package vk.made.itmo.sigmadt.linreg.model

import breeze.linalg._

class LinearRegression(val regularizationCoefficient: Double = 0.0) {
  var nRows: Int = 0
  var nCols: Int = 0

  var weights = new DenseVector[Double](0)

  var isFitted: Boolean = false

  def fit(X: DenseMatrix[Double], y: DenseVector[Double]): Unit = {
    nRows = X.rows
    nCols = X.cols

    val X_ = addColumnOfOnes(X)

    val Z = X_.t * X_ + regularizationCoefficient *:* DenseMatrix.eye[Double](nCols + 1)

    weights = inv(Z) * X_.t * y

    isFitted = true
  }

  def predict(X: DenseMatrix[Double]): DenseVector[Double] = {
    require(isFitted, "it looks like you didn't fit your model, use .fit() method")

    val pred = addColumnOfOnes(X) * weights
    pred
  }

  def addColumnOfOnes(X: DenseMatrix[Double]): DenseMatrix[Double] = {
    val x0 = DenseMatrix.ones[Double](X.rows, 1)
    DenseMatrix.horzcat(x0, X)
  }

}

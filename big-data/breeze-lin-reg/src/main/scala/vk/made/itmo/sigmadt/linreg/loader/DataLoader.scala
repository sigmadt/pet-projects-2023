package vk.made.itmo.sigmadt.linreg.loader

import breeze.linalg.{DenseMatrix, DenseVector, csvread}

import java.io.File

class DataLoader {
  def loadData(): (DenseMatrix[Double], DenseMatrix[Double]) = {
    val trainDataFrame: DenseMatrix[Double] = csvread(new File("data/uber_train.csv"), separator = ',')
    val (nRowsTrain, nColsTrain) = (trainDataFrame.rows, trainDataFrame.cols)

    val testDataFrame: DenseMatrix[Double] = csvread(new File("data/uber_test.csv"), separator = ',')
    val (nRowsTest, nColsTest) = (testDataFrame.rows, testDataFrame.cols)


    println(s"train data was loaded! n_rows: $nRowsTrain, n_cols: $nColsTrain")
    println(s"test  data was loaded! n_rows: $nRowsTest, n_cols: $nColsTest")

    (trainDataFrame, testDataFrame)
  }

  def getFeaturesAndTarget(data: DenseMatrix[Double], targetInd: Int): (DenseMatrix[Double], DenseVector[Double]) = {
    val (rows, cols) = (data.rows, data.cols)
    require(rows > 0, "empty data")
    require(targetInd <= cols, "invalid index was given for target")

    val (x, y) = (data(::, (0 until cols).filter(i => i != targetInd)), data(::, targetInd))

    (x.copy.toDenseMatrix, y)
  }

  def trainValidationSplit(data: DenseMatrix[Double], targetInd: Int, trainSize: Double):
  (DenseMatrix[Double], DenseVector[Double], DenseMatrix[Double], DenseVector[Double]) =
  {
    val (rows, cols) = (data.rows, data.cols)
    val threshold = (trainSize * cols).toInt

    val (x, y) = getFeaturesAndTarget(data, targetInd)

    val (trainX, trainY) = (x(0 until threshold, ::), y(0 until threshold))

    val (validationX, validationY) = (x(threshold until rows, ::), y(threshold until rows))

    (trainX.copy.toDenseMatrix, trainY, validationX.copy.toDenseMatrix, validationY)

  }
}

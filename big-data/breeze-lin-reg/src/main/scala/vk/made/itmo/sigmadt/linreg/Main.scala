package vk.made.itmo.sigmadt.linreg

import vk.made.itmo.sigmadt.linreg.loader.DataLoader
import vk.made.itmo.sigmadt.linreg.model.LinearRegression
import vk.made.itmo.sigmadt.linreg.utils.Result

object Main {
  def main(args: Array[String]): Unit = {
    val dataLoader = new DataLoader()
    val (train, test) = dataLoader.loadData()

    // 1. load train data and split on sets
    val (trainX, trainY, valX, valY) = dataLoader.trainValidationSplit(train, 0, 0.3)

    // 2. load test data
    val (testX, testY) = dataLoader.getFeaturesAndTarget(test, 0)

    val linearRegression = new LinearRegression(5)

    // 3. fit on train set
    linearRegression.fit(trainX, trainY)

    // 4. validation results
    val valResult = new Result("validation")
    valResult.collect(valY, linearRegression.predict(valX))

    // 5. test results
    val testResult = new Result("test")
    testResult.collect(testY, linearRegression.predict(testX))
  }
}

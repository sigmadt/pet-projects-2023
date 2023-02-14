package vk.made.itmo.sigmadt.spark_ml

import breeze.linalg.{DenseMatrix, DenseVector, sum}
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.ml.linalg.{Vector => LinVector}
import org.apache.spark.ml.param.shared.HasInputCol
import org.apache.spark.ml.param.{DoubleArrayParam, DoubleParam, Param, ParamMap, Params, StringArrayParam}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, Dataset, Encoder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import Main.Spark
import Spark.implicits._
import org.apache.spark.mllib.stat.MultivariateOnlineSummarizer
import org.apache.spark.mllib.linalg.Vectors


class LinearRegressor(override val uid: String)
  extends Model[LinearRegressor]
    with DefaultParamsWritable with LinearRegressorParams {
  override def copy(extra: ParamMap): LinearRegressor = defaultCopy(extra)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val df = dataset
      .select(getInputArrayCols.map(x => col(x)): _*)
      .map(r => r.toSeq.asInstanceOf[Seq[Double]].toArray)
      .collect()

    val X = DenseMatrix(df: _*)

    val predY = X * getWeights + getBias

    predY
      .toArray
      .toSeq
      .toDF(labelName)
  }

  override def transformSchema(schema: StructType): StructType = schema
}


trait LinearRegressorParams extends Params with HasInputCol {
  private final val inputArrayCols = new StringArrayParam(this, "inputArrayCols", "")
  private final val weights = new DoubleArrayParam(this, "weights", "")
  private final val bias = new DoubleParam(this, "bias", "")
  private final val batch = new Param[Int](this, "batch", "")
  private final val lr = new Param[Double](this, "learning rate", "")
  val iters = new Param[Int](this, "iters", "")
  final val labelName = "target"

  def setIters(value: Int): this.type = set(iters, value)

  def getIters: Int = $(iters)

  def setInputArrayCols(value: Array[String]): this.type = set(inputArrayCols, value)

  def getInputArrayCols: Array[String] = $(inputArrayCols)

  def setWeights(value: DenseVector[Double]): this.type = set(weights, value.toArray)

  def getWeights: DenseVector[Double] = DenseVector($(weights))

  def setBias(value: Double): this.type = set(bias, value)

  def getBias: Double = $(bias)

  def setBatch(value: Int): this.type = set(batch, value)

  def getBatch: Int = $(batch)

  def setLearningRate(value: Double): this.type = set(lr, value)

  def getLearningRate: Double = $(lr)

}

object LinearRegressor extends DefaultParamsReadable[LinearRegressor]


class LinearRegression(override val uid: String) extends Estimator[LinearRegressor]
  with DefaultParamsWritable with LinearRegressorParams {

  def this() = this(Identifiable.randomUID("linearRegression"))

  override def fit(dataset: Dataset[_]): LinearRegressor = {
    implicit val encoder: Encoder[LinVector] = ExpressionEncoder()

    // columns without target
    val columns = dataset
      .columns
      .filter(c => c != labelName)
    setInputArrayCols(columns)

    // initial 0-value weights
    val initWeights: DenseVector[Double] = DenseVector.zeros(getInputArrayCols.length)
    setWeights(initWeights)

    // bias is 0 and lr
    setBias(0.0)

    val label = "result"
    val result = new VectorAssembler()
      .setInputCols(getInputArrayCols ++ Array(labelName))
      .setOutputCol(label)

    val vectors: Dataset[LinVector] = result
      .transform(dataset)
      .select(label)
      .as[LinVector]

    val N = getIters

    for (_ <- 0 to N) {
      val (weights_, bias_) = gradientDescent(vectors)
      val currWeights = getWeights - getLearningRate * DenseVector(weights_.mean.toArray)
      val currBias = getBias - getLearningRate * bias_.mean.toArray(0)
      setWeights(currWeights)
      setBias(currBias)
    }

    copyValues(new LinearRegressor(uid + "_model")).setParent(this)
  }

  private def gradientDescent(vectors: Dataset[LinVector]): (MultivariateOnlineSummarizer, MultivariateOnlineSummarizer) = {
    val summary =
      vectors
        .rdd
        .mapPartitions(
          (iterator: Iterator[LinVector]) => {
            val weights = new MultivariateOnlineSummarizer()
            val bias = new MultivariateOnlineSummarizer()

            iterator
              .grouped(getBatch)
              .foreach(
                chunk => {
                  val size = chunk.size.asInstanceOf[Double]

                  val trainX =
                    DenseMatrix(
                      chunk
                        .toArray
                        .map(_.toArray.dropRight(1)): _*
                    )

                  val trainY = DenseVector(
                    chunk
                      .toArray
                      .map(_.toArray.last)
                  )

                  val eps = (trainX * getWeights + getBias) - trainY

                  val w_ = (eps.t * trainX).t / size
                  val b_ = sum(eps) / size

                  weights.add(Vectors.dense(w_.toArray))
                  bias.add(Vectors.dense(Array(b_)))
                }
              )

            Iterator((weights, bias))
          }
        )
        .reduce(
          (cols, target) => (cols._1.merge(cols._2), target._1.merge(target._2))
        )

    summary
  }

  override def copy(extra: ParamMap): Estimator[LinearRegressor] = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = schema
}

object LinearRegression extends DefaultParamsReadable[LinearRegression]
package cn.dmp

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Description: 
 *
 * @date 2022/8/22 11:41 PM
 * */
object Bzip2Parquet {

  def main(args: Array[String]): Unit = {
    println("Hello Scala")
    if (args.length != 2) {
      println(
        """
          |cn.dmp.tools.Bzip2Parquet
          |参数：
          | logInputPath
          | resultOutputPath
          |""".stripMargin)
      sys.exit(1)
    }
    val Array(logInputPath, resultOutputPath) = args
    val sparkConf = new SparkConf()
    sparkConf.setAppName(s"${this.getClass.getSimpleName}")
    sparkConf.setMaster("local[*]")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    val sc = new SparkContext(sparkConf)


    val sqlContext = new SQLContext(sc)

    val rawdata = sc.textFile(logInputPath)
    // 根据业务需求对数据进行ETL
    //    val dataRow = rawdata.map(line => line.split(",", line.length))
    //      .filter(_.length >= 85)
    //      .map(arr =>
    //        Row(arr(0),
    //          NBF.toInt(arr(1)))
    //      )

  }

}

package cn.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * Description: 
 *
 * @date 2022/8/22 11:43 PM
 * */
object SparkDemo_01 {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName(s"${this.getClass.getSimpleName}")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val df: DataFrame = spark.read.json("data/user.json")
    //df.show()

    df.createOrReplaceTempView("user")
    //    spark.sql("select * from user").show()
    //    spark.sql("select avg(age) from user").show()


    df.select("username", "age").show()
    //RDD=>DataFrame=>DataSet 转换需要引入隐式转换规则，否则无法转换
    import spark.implicits._
    //    df.select($"age" + 1).show()
    //    df.select('age + 1).show()

    spark.close()


  }
}

package cn.dmp.utils

/**
 * Description: 
 *
 * @date 2022/8/22 11:42 PM
 * */
object NBF {
  def toInt(str: String): Int = {
    try {
      str.toInt
    } catch {
      case _: Exception => 0
    }
  }

  def toDouble(str: String): Double = {
    try {
      str.toDouble
    } catch {
      case _: Exception => 0.0
    }
  }
}

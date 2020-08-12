package com.lfmunoz.utils


fun printResults(total: Int, diffMillis: Long) {
  println("--------------------------------------------------------------------------")
  println("Results: ")
  println("--------------------------------------------------------------------------")
  println("Published $total in ${diffMillis / 1000} seconds  (rate = ${ (total.toDouble() * 1000) / diffMillis })")
  println()
}


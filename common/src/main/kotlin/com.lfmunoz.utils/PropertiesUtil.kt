package com.lfmunoz.utils

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


fun loadResource(file: String) : String {
    println("reading file $file")
//    Resources.getResource("/html/file.html").readText()
    return {}::class.java.getResource(file).readText()
}

fun readFileAsLines(fileName: String): List<String>
        = File(fileName).bufferedReader().readLines()

fun writeTextFile(fileName: String, text: String)
        = File(fileName).writeText(text)


fun readPropertiesFile(fileName: String) : Map<String, String> {
//    val fileObject = File(fileName)
    // create a new file
//    val isNewFileCreated :Boolean = fileObject.createNewFile()
    return readFileAsLines(fileName).filter {it.isNotEmpty()}
            .associate {
                val arr = it.split("=")
                arr[0].trim() to arr[1].trim()
            }
}

fun writePropertiesFile(fileName: String, properties: Map<String, String>) {
    val text = properties.map {
        "${it.key}=${it.value}"
    }.joinToString("\n")
    writeTextFile(fileName, text)
}

fun createDir(pathName: String) : Boolean {
    return File(pathName).mkdirs()
}



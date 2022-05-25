package org.apache.hadoop.hdfs

import java.io.{BufferedReader, InputStreamReader}
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FileStatus, FileSystem, Path}
import org.apache.hadoop.io.IOUtils
import org.apache.spark.deploy.SparkHadoopUtil
import java.net.URI
import scala.collection.mutable.ArrayBuffer

/**
  * hdfs 文件系统操作类
  * Created by songgr on 2021/02/23.
  */
object HdfsOperator {

  lazy val hadoopConfiguration:Configuration = {
    val sparkHadoopUtil = SparkHadoopUtil.get
    if(sparkHadoopUtil != null){
      sparkHadoopUtil.conf
    } else new Configuration()
  }

  def isDir(path: String):Boolean = {
    val fs = FileSystem.get(URI.create(path), hadoopConfiguration)
    fs.isDirectory(new Path(path))
  }

  def isFile(path: String):Boolean = {
    val fs = FileSystem.get(URI.create(path), hadoopConfiguration)
    fs.isFile(new Path(path))
  }

  def fileExists(path: Path):Boolean = {
    val fs = FileSystem.get(path.toUri, hadoopConfiguration)
    fs.exists(path)
  }

  def fileExists(path: String):Boolean = {
    fileExists(new Path(path))
  }

  def createDir(path: String):Boolean = {
    val fs = FileSystem.get(URI.create(path), hadoopConfiguration)
    fs.mkdirs(new Path(path))
  }

  def deleteDir(path: String) = {
    val fs = FileSystem.get(URI.create(path), hadoopConfiguration)
    fs.delete(new Path(path), true)
  }

  def getFileStatus(path: String):FileStatus = {
    val fs = FileSystem.get(URI.create(path), hadoopConfiguration)
    val file = fs.getFileStatus(new Path(path))
    file
  }

  def listFiles(path: String): Seq[FileStatus] = {
    val fs = FileSystem.get(URI.create(path), hadoopConfiguration)
    fs.globStatus(new Path(path)).toSeq
  }

  def readFileToString(path: String): String = {
    val fs = FileSystem.get(URI.create(path), hadoopConfiguration)
    var br: BufferedReader = null
    var line: String = null
    val result = new ArrayBuffer[String]()
    try {
      br = new BufferedReader(new InputStreamReader(fs.open(new Path(path))))
      line = br.readLine()
      while (line != null) {
        result += line
        line = br.readLine()
      }
    } finally {
      if (br != null) br.close()
      if (fs != null) fs.close()
    }
    result.mkString("\n")
  }

  def readFileToBytes(fileName: String): Array[Byte] = {
    val fs = FileSystem.get(URI.create(fileName), hadoopConfiguration)
    val src: Path = new Path(fileName)
    var in: FSDataInputStream = null
    try {
      in = fs.open(src)
      val byteArrayOut = new ByteArrayOutputStream()
      IOUtils.copyBytes(in, byteArrayOut, 1024, true)
      byteArrayOut.toByteArray
    } finally {
      if (null != in) in.close()
      if (fs != null) fs.close()
    }
  }

}

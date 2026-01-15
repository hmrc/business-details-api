/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.yaml.snakeyaml.{LoaderOptions, Yaml}
import org.yaml.snakeyaml.constructor.SafeConstructor

import java.io.{File, FileInputStream, FileWriter}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

object OASBundler{

  private val yaml = new Yaml(new SafeConstructor(new LoaderOptions()))

  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Usage: SimpleOASBundler <rootDir> [outDir]")
      sys.exit(1)
    }

    val rootDir = Paths.get(args(0)).toAbsolutePath
    val outDir  = if (args.length > 1) Some(Paths.get(args(1)).toAbsolutePath) else None

    if (!Files.isDirectory(rootDir)) {
      System.err.println(s"Root directory not found: $rootDir")
      sys.exit(1)
    }

    Files.list(rootDir).iterator().asScala.filter(Files.isDirectory(_)).foreach { versionDir =>
      findApplicationYaml(versionDir).foreach { appPath =>
        println(s"[INFO] Processing: $appPath")
        val bundled = inlineTopLevelRefs(appPath.toFile)

        val outPath = outDir match {
          case Some(base) =>
            val rel = rootDir.relativize(versionDir)
            base.resolve(rel).resolve("application.bundled.yaml")
          case None =>
            versionDir.resolve("application.bundled.yaml")
        }

        Files.createDirectories(outPath.getParent)
        writeYaml(bundled, outPath.toFile)
        println(s"[OK] Wrote: $outPath")
      }
    }
  }

  private def findApplicationYaml(dir: Path): Option[Path] = {
    Seq("application.yaml", "application.yml").map(dir.resolve).find(Files.exists(_))
  }

  private def inlineTopLevelRefs(file: File): java.util.Map[String, Any] = {
    val rootDoc = loadYaml(file).asInstanceOf[java.util.Map[String, Any]]
    val scalaMap = rootDoc.asScala

    // Inline $ref files under paths only
    scalaMap.get("paths") match {
      case Some(pathsObj: java.util.Map[_, _]) =>
        val pathsMap = pathsObj.asScala.asInstanceOf[scala.collection.mutable.Map[String, Any]]
        pathsMap.foreach { case (pathKey, pathValue) =>
          pathValue match {
            case m: java.util.Map[_, _] =>
              val innerMap = m.asScala.asInstanceOf[scala.collection.mutable.Map[String, Any]]
              innerMap.get("$ref") match {
                case Some(refValue) =>
                  val refFile = new File(file.getParentFile, refValue.toString)
                  if (refFile.exists()) {
                    val inlined = loadYaml(refFile)
                    pathsMap(pathKey) = inlined // Replace $ref with actual content
                  } else {
                    println(s"[WARN] Ref file not found: $refFile")
                  }
                case None => // no $ref, leave as-is
              }
            case _ => // not a map, leave as-is
          }
        }
      case _ => println("[WARN] No paths section found")
    }

    rootDoc
  }

  private def loadYaml(file: File): Any = {
    val fis = new FileInputStream(file)
    try yaml.load(fis)
    finally fis.close()
  }

  private def writeYaml(obj: Any, file: File): Unit = {
    val fw = new FileWriter(file)
    try yaml.dump(obj, fw)
    finally fw.close()
  }
}

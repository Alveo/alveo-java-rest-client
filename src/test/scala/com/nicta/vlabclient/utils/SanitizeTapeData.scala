package com.nicta.vlabclient.utils

import scala.io.Source
import java.util.Random
import scala.util.matching.Regex.Match
import java.util
import com.github.jsonldjava.utils.JSONUtils
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Created by amack on 4/02/14.
 *
 * A utility class for creating testing data unencumbered by licensing issues.
 *
 * Replaces stored data from 'sensitive' fields with random strings of words (preserving case)
 *
 * Use this by manually editing the betamax tapes, and running 'sanitize' on the body
 * for any item list retrievals
 */
object Sanitizer {
  val wordReplacer = new WordReplacer()

  val needsSanitizingInItem = Seq("ausnc:Description", "ausnc:Contributorof", "ausnc:Participants",
    "dc:creator", "hcsvlab:full_text", "ausnc:Transcribers")

  def sanitizeItemJson(sourceJson: String): String = {
    val parsed = getMap(JSONUtils.fromString(sourceJson.replace("\n", "\\n")))
    val metadata = getMap(parsed("hcsvlab:metadata"))
    needsSanitizingInItem foreach { field =>
      metadata(field) = wordReplacer.replaceWords(metadata(field).asInstanceOf[String])
    }
    parsed("hcsvlab:metadata") = metadata.asJava
    JSONUtils.toString(parsed.asJava).replace("\\n", "\n")
  }

  def sanitizeAnnJson(sourceJson: String): String = {
    val parsed = getMap(JSONUtils.fromString(sourceJson.replace("\\n", """\\^""").replace("\n", "\\n")))
    val anns = getSeq(parsed("hcsvlab:annotations")).map(getMap)
    anns foreach { ann =>
      if (ann contains "label")
        ann("label") = wordReplacer.replaceWords(ann("label").asInstanceOf[String])
    }
    parsed("hcsvlab:annotations") = anns.map(_.asJava).asJava
    JSONUtils.toString(parsed.asJava).replace("\\n", "\n").replace("""\\^""", "\\n")
  }

  def sanitizeText(sourceText: String): String =
    wordReplacer.replaceWords(sourceText)

  def getMap(fromJson: Object) =
    fromJson.asInstanceOf[util.LinkedHashMap[String, Object]].asScala

  def getSeq(fromJson: Object) =
    fromJson.asInstanceOf[util.ArrayList[Object]].asScala

}

class WordReplacer {
  val replWordsByLen: Map[Int, Seq[String]] = {
    val inStream = getClass.getClassLoader.getResourceAsStream("randwords.txt")
    val src = Source.fromInputStream(inStream)
    val allWords = src.getLines
    val punctFree = allWords filter { wd => wd.forall(_.isLetter) }
    val punctFreeSeq = punctFree.toIndexedSeq
    src.close()
    punctFreeSeq.groupBy(_.length)
  }

  val rand = new Random(12121)


  def replaceWords(text: String) = {
    val re = """\p{IsL}+""".r // all sequences of ≥ 1 letters

    def newActualWordForLen(len: Int) = {
      val candidates = replWordsByLen(len)
      candidates(Math.abs(rand.nextInt()) % candidates.length)
    }

    def lenIsKnown(len: Int): Boolean = replWordsByLen.contains(len)

    def newWordForLen(len: Int): String = {
      if (replWordsByLen contains len) {
        newActualWordForLen(len)
      } else {
        val longerLen = (len to 1000).dropWhile(!lenIsKnown(_)).headOption
        if (longerLen.isDefined) {
          newActualWordForLen(longerLen.get).substring(0, len)
        } else {
          // find the closest shorter word length, and repeat them
          // (filling with a truncated version from that set of words)
          val longSubLen = (len to 1 by -1).dropWhile(!lenIsKnown(_)).head
          val numFullLengthWords = len / longSubLen
          val fullWords = (0 to numFullLengthWords).map(newActualWordForLen(longSubLen))
          val truncTgtLen = len % longSubLen
          val truncLen = (truncTgtLen to 1000).dropWhile(!lenIsKnown(_)).head
          val truncWord = newActualWordForLen(longSubLen).substring(0, truncLen)
          (fullWords ++ truncWord).mkString
        }
      }
    }

    def replaceWord(m: Match): String = {
      val oldWord = m.group(0)
      val newWord = newWordForLen(oldWord.length)
      (oldWord zip newWord) map { case (oc, nc) =>
        if (oc.isUpper) nc.toUpper else nc.toLower
      } mkString
    }
    re.replaceAllIn(text, replaceWord _)
  }

}

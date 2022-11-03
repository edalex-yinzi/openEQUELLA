/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.i18n

import java.util.Locale
import com.tle.common.i18n.LangUtils
import io.circe.{Decoder, Encoder}
import scala.jdk.CollectionConverters._

case class LocaleStrings(strings: Map[String, String]) {
  def stringsOrNone: Option[Map[String, String]] =
    if (strings.size > 1) {
      Some(strings)
    } else None

  def closest(locale: Locale): Option[String] = {
    Option(LangUtils.getClosestObjectForLocale(strings.asJava, locale))
  }
}

object LocaleStrings {
  val empty        = LocaleStrings(Map.empty)
  implicit val enc = Encoder.encodeMap[String, String].contramap[LocaleStrings](_.strings)
  implicit val dec = Decoder.decodeMap[String, String].map(LocaleStrings.apply)

  def fromStrings(
      single: String,
      map: Option[Map[String, String]],
      currentLocale: Locale
  ): Option[(String, LocaleStrings)] = map match {
    case None =>
      if (single.isEmpty) None
      else Some((single, LocaleStrings(Map(currentLocale.toLanguageTag -> single))))
    case Some(langMap) =>
      val nonEmpties = langMap.filter(_._2.nonEmpty)
      if (nonEmpties.isEmpty) None
      else
        Some(
          LangUtils.getClosestObjectForLocale(nonEmpties.asJava, currentLocale),
          LocaleStrings(nonEmpties)
        )
  }
}

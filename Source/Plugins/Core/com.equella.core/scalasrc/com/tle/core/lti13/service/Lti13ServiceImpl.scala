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

package com.tle.core.lti13.service

import com.tle.beans.lti.LtiPlatform
import com.tle.core.guice.Bind
import com.tle.core.lti13.dao.Lti13DAO
import org.springframework.transaction.annotation.Transactional
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Singleton
@Bind(classOf[Lti13Service])
class Lti13ServiceImpl extends Lti13Service {
  @Inject var lti13Dao: Lti13DAO = _

  override def getByPlatformID(platformID: String): Option[LtiPlatform] =
    lti13Dao.getByPlatformID(platformID)

  override def getAll: List[LtiPlatform] = lti13Dao.enumerateAll.asScala.toList

  @Transactional
  override def create(ltiPlatform: LtiPlatform): Long = lti13Dao.save(ltiPlatform)

  @Transactional
  override def update(ltiPlatform: LtiPlatform): Unit = lti13Dao.update(ltiPlatform)

  @Transactional
  override def delete(ltiPlatform: LtiPlatform): Unit = lti13Dao.delete(ltiPlatform)
}

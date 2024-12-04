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

package com.tle.core.qti.converter.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.core.qti.dao.QtiAssessmentItemDao;

@SuppressWarnings("nls")
public class QtiAssessmentItemXmlConverter implements Converter {
  private final QtiAssessmentItemDao questionDao;

  public QtiAssessmentItemXmlConverter(QtiAssessmentItemDao questionDao) {
    this.questionDao = questionDao;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(Class clazz) {
    return clazz == QtiAssessmentItem.class;
  }

  @Override
  public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
    final QtiAssessmentItem question = (QtiAssessmentItem) obj;
    if (question != null) {
      writer.addAttribute("uuid", question.getUuid());
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    final String uuidFromStream = reader.getAttribute("uuid");
    return questionDao.getByUuid(uuidFromStream);
  }
}

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

package com.tle.core.encryption.impl;

import com.tle.common.Check;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;

@Singleton
@Bind(EncryptionService.class)
@SuppressWarnings("nls")
public class EncryptionServiceImpl implements EncryptionService {
  private static final byte[] SHAREPASS =
      new byte[] {45, 123, -112, 2, 89, 124, 19, 74, 0, 24, -118, 98, 5, 100, 92, 7};
  private static final IvParameterSpec INITVEC = new IvParameterSpec("thisis16byteslog".getBytes());

  @Override
  public String encrypt(String value) {
    if (!Check.isEmpty(value)) {
      try {
        SecretKey key = new SecretKeySpec(SHAREPASS, "AES");
        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ecipher.init(Cipher.ENCRYPT_MODE, key, INITVEC);

        // Encrypt
        byte[] enc = ecipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(enc);
      } catch (Exception e) {
        throw new RuntimeException("Error encrypting", e);
      }
    }

    return value;
  }

  @Override
  public String decrypt(String value) {
    if (!Check.isEmpty(value)) {
      try {
        byte[] bytes = Base64.getDecoder().decode(value);
        SecretKey key = new SecretKeySpec(SHAREPASS, "AES");
        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ecipher.init(Cipher.DECRYPT_MODE, key, INITVEC);
        return new String(ecipher.doFinal(bytes));
      } catch (Exception e) {
        throw new RuntimeException("Error decrypting ", e);
      }
    }

    return value;
  }
}

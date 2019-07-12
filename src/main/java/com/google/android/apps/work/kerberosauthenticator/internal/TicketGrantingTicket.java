/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.apps.work.kerberosauthenticator.internal;

import static com.google.android.apps.work.kerberosauthenticator.Constants.TAG;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Set;
import javax.security.auth.Subject;
import krb.javax.security.auth.kerberos.KerberosTicket;

/** Represents a Ticket Granting Ticket */
public final class TicketGrantingTicket {
  private final Subject subject;

  public TicketGrantingTicket(Subject subject) {
    this.subject = subject;
  }

  public static TicketGrantingTicket fromSerializedSubject(byte[] serializedSubject) {
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(serializedSubject);
      ObjectInputStream ois = new ObjectInputStream(bis);
      Subject bareSubject = (Subject) ois.readObject();
      Set<?> privateCredentials = (Set<?>) ois.readObject();
      Set<?> publicCredentials = (Set<?>) ois.readObject();

      Subject subject =
          new Subject(
              bareSubject.isReadOnly(),
              bareSubject.getPrincipals(),
              publicCredentials,
              privateCredentials);
      return new TicketGrantingTicket(subject);

    } catch (ClassNotFoundException | IOException e) {
      Log.w(TAG, "Failed reading ticket", e);
    }

    return null;
  }

  public Subject asSubject() {
    return subject;
  }

  public byte[] asSerialized() {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(subject);
      oos.writeObject(subject.getPrivateCredentials());
      oos.writeObject(subject.getPublicCredentials());
      oos.close();
      return bos.toByteArray();
    } catch (IOException e) {
      Log.w(TAG, "Failed serializing subject", e);
    }

    return null;
  }

  public Date getExpiryDate() {
    Set<Object> privateCreds = subject.getPrivateCredentials();
    for (Object cred : privateCreds) {
      if (cred instanceof KerberosTicket) {
        return ((KerberosTicket) cred).getEndTime();
      }
    }
    return null;
  }

  public Date getIssuanceDate() {
    Set<Object> privateCreds = subject.getPrivateCredentials();

    for (Object cred : privateCreds) {
      if (cred instanceof KerberosTicket) {
        return ((KerberosTicket) cred).getAuthTime();
      }
    }
    return null;
  }
}

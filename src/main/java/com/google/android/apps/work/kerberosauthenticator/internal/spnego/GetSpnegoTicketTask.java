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
package com.google.android.apps.work.kerberosauthenticator.internal.spnego;

import static com.google.android.apps.work.kerberosauthenticator.Constants.TAG;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketRequestResult;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketRequestResult.ResultCode;
import com.google.common.base.Ascii;
import javax.security.auth.Subject;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.GSSCaller;
import sun.security.jgss.GSSManagerImpl;
import sun.security.jgss.GSSUtil;

/** Task for getting a SPNEGO ticket for the provided service. */
public class GetSpnegoTicketTask extends AsyncTask<String, Void, TicketRequestResult> {
  private final Subject subject;
  private final ServiceTicketResultListener listener;
  private final String domain;
  private final String domainController;
  private final boolean debugWithSensitiveData;
  private String service = null;
  private String serviceSpnegoTicket = null;

  public GetSpnegoTicketTask(
      Subject subject,
      String domain,
      String domainController,
      boolean debugWithSensitiveData, ServiceTicketResultListener listener) {
    this.subject = subject;
    this.domain = domain;
    this.domainController = domainController;
    this.debugWithSensitiveData = debugWithSensitiveData;
    this.listener = listener;
  }

  @Override
  protected TicketRequestResult doInBackground(String... services) {
    service = services[0];
    GSSUtil.setGlobalSubject(subject);
    System.setProperty("java.security.krb5.kdc", domainController);
    System.setProperty("java.security.krb5.realm", Ascii.toUpperCase(domain));

    System.setProperty("sun.security.jgss.debug", Boolean.toString(debugWithSensitiveData));

    GSSManager manager = new GSSManagerImpl(GSSCaller.CALLER_INITIATE, false);

    if (debugWithSensitiveData) {
      StringBuilder mechanismsSupported = new StringBuilder();
      for (Oid oid : manager.getMechs()) {
        mechanismsSupported.append(oid).append("   ");
      }
      Log.i(TAG, "Mechanisms supported: " + mechanismsSupported);
    }

    GSSName serverName;
    try {
      Oid spnegoOid = new Oid("1.3.6.1.5.5.2");

      serverName = manager.createName("HTTP@" + service, GSSName.NT_HOSTBASED_SERVICE, spnegoOid);

      GSSContext context =
          manager.createContext(serverName, spnegoOid, null, GSSContext.DEFAULT_LIFETIME);
      byte[] spnegoToken = new byte[0];
      spnegoToken = context.initSecContext(spnegoToken, 0, spnegoToken.length);

      Log.d(
          TAG,
          String.format(
              "GSS context established? %s service ticket is null? %s",
              context.isEstablished(), spnegoToken != null));

      if (spnegoToken != null) {
        serviceSpnegoTicket = Base64.encodeToString(spnegoToken, Base64.NO_WRAP);
      }
    } catch (GSSException e) {
      Log.e(TAG, "Error while getting service ticket", e);
      return new TicketRequestResult(ResultCode.ERROR_GSS_FAILURE, e.getMessage());
    }

    if (debugWithSensitiveData) {
      Log.i(TAG, "Spnego ticket: " + serviceSpnegoTicket);
    }

    return new TicketRequestResult(ResultCode.SUCCESS, "HTTP ticket for " + serverName);
  }

  @Override
  protected void onPostExecute(TicketRequestResult result) {
    super.onPostExecute(result);
    listener.onServiceTicketResult(service, result, serviceSpnegoTicket);
  }

  @Override
  protected void onCancelled(TicketRequestResult result) {
    super.onCancelled();
    listener.onServiceTicketResult(service, result, null);
  }
}

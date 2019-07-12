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
package com.google.android.apps.work.kerberosauthenticator.internal.kinit;

import static com.google.android.apps.work.kerberosauthenticator.Constants.TAG;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.apps.work.kerberosauthenticator.internal.KerberosAccountDetails;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketRequestResult;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketRequestResult.ResultCode;
import com.google.common.base.Ascii;
import com.sun.security.auth.module.Krb5LoginModule;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

/**
 * Performs the equivalent of kinit - logging in the user to the Kerberos KDC, producing a
 * ticket-granting-ticket for the user.
 */
public class UserAuthenticationTask extends AsyncTask<Void, Void, TicketRequestResult> {
  private static final String REFRESH_KRB5_CONFIG = "refreshKrb5Config";
  private static final String STORE_KEY = "storeKey";
  private static final String USE_FIRST_PASS = "useFirstPass";
  private static final String DEBUG = "debug";

  private final String username;
  private final String password;
  private final String adDomain;
  private final String domainController;
  private final boolean debugWithCredentials;
  private final UserAuthenticationResultListener listener;
  private Subject subject = null;

  public UserAuthenticationTask(
      UserAuthenticationResultListener listener,
      KerberosAccountDetails accountDetails,
      boolean debugWithCredentials) {
    this.listener = listener;
    this.username = accountDetails.getUsername();
    this.password = accountDetails.getPassword();
    this.adDomain = accountDetails.getActiveDirectoryDomain();
    this.domainController = accountDetails.getAdDomainController();
    this.debugWithCredentials = debugWithCredentials;
  }

  @Override
  protected TicketRequestResult doInBackground(Void... voids) {
    Log.i(TAG, String.format("Authenticating user %s to domain %s via %s",
        username, adDomain, domainController));
    System.setProperty("java.security.krb5.kdc", domainController);
    //NOTE: Domain MUST be upper-case.
    System.setProperty("java.security.krb5.realm", Ascii.toUpperCase(adDomain));
    System.setProperty("sun.security.jgss.debug", Boolean.toString(debugWithCredentials));

    Krb5LoginModule lm = new Krb5LoginModule();
    subject = new Subject();
    CallbackHandler handler = new UsernamePasswordCallbackHandler(username, password);
    Map<String, String> sharedState = new HashMap<>();
    sharedState.put(REFRESH_KRB5_CONFIG, "true");
    sharedState.put(STORE_KEY, "true");
    sharedState.put(USE_FIRST_PASS, "true");
    sharedState.put(DEBUG, Boolean.toString(debugWithCredentials));

    Map<String, Object> options = new HashMap<>();

    lm.initialize(subject, handler, sharedState, options);
    try {
      if (!lm.login()) {
        return new TicketRequestResult(ResultCode.ERROR_LOGIN_FAILED, "Login failed");
      }

      if (!lm.commit()) {
        return new TicketRequestResult(ResultCode.ERROR_COMMIT_FAILED, "Commit failed");
      }

      Log.i(TAG, String.format("Successfully authenticated %s to %s", username, adDomain));
      if (debugWithCredentials) {
        Log.i(TAG, String.format("Subject: %s", subject));
      }
    } catch (LoginException e) {
      Log.w(TAG, "Failure logging in", e);
      if (e.getMessage().contains("Pre-authentication information was invalid")) {
        return new TicketRequestResult(ResultCode.ERROR_BAD_PASSWORD, e.getMessage());
      } else {
        return new TicketRequestResult(ResultCode.ERROR_LOGIN_FAILED, e.getMessage());
      }
    }

    StringBuilder infoBuilder = new StringBuilder();

    for (Principal principal : subject.getPrincipals()) {
      infoBuilder.append("Principal: ").append(principal.getName()).append("\n");
    }

    for (Object credential : subject.getPrivateCredentials()) {
      infoBuilder.append("Credential type: ").append(credential.getClass()).append("\n");
    }

    return new TicketRequestResult(ResultCode.SUCCESS, infoBuilder.toString());
  }

  @Override
  protected void onPostExecute(TicketRequestResult result) {
    super.onPostExecute(result);
    listener.onTicketGrantingTicketResult(result, subject);
  }
}

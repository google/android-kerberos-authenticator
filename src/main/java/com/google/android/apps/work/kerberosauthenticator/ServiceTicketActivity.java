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

package com.google.android.apps.work.kerberosauthenticator;

import static com.google.android.apps.work.kerberosauthenticator.Constants.TAG;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketGrantingTicket;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketRequestResult;
import com.google.android.apps.work.kerberosauthenticator.internal.spnego.GetSpnegoTicketTask;
import com.google.android.apps.work.kerberosauthenticator.internal.spnego.ServiceTicketResultListener;
import java.util.Date;

/**
 * Obtains a service ticket for the given service, while displaying details about the current
 * account and its validity state, similar to the AuthenticatorStatusActivity .
 */
public class ServiceTicketActivity extends BaseAuthenticatorActivity
    implements ServiceTicketResultListener {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.authenticator);

    // See if we need to get a TGT or service ticket, or the activity is initialised by the user.
    Intent intent = getIntent();
    String serviceName = intent.getStringExtra(Constants.SERVICE_NAME);
    Log.d(TAG, String.format("Created service ticket activity, service name? %s", serviceName));

    // Initiatlise the UI with corresponding values.
    initUI(false, serviceName);
    showOkBtn(false);

    KerberosAccount account = KerberosAccount.getAccount(this);

    if (account == null) {
      setErrorResultAndFinish(
          AccountManager.ERROR_CODE_BAD_REQUEST, "No account for service ticket.");
      return;
    }

    if (TextUtils.isEmpty(serviceName)) {
      setErrorResultAndFinish(
          AccountManager.ERROR_CODE_BAD_ARGUMENTS, "Service name is missing.");
      return;
    }
    // Activity was created to generate a service ticket.
    setRefreshingStatus(getServiceTimestampTextviewId());
    setOkStatus(getTGTTimestampTextViewId());
    getServiceTicket(serviceName, account);
  }

  private void getServiceTicket(String serviceName, KerberosAccount account) {
    TicketGrantingTicket tgt =
        TicketGrantingTicket.fromSerializedSubject(account.getTicketGrantingTicket());
    GetSpnegoTicketTask spnego =
        new GetSpnegoTicketTask(
            tgt.asSubject(),
            account.getDomain(),
            account.getDomainController(),
            accountConfiguration.getDebugWithSensitiveData(),
            this);
    spnego.execute(serviceName);
  }

  @Override
  public void onServiceTicketResult(String service, TicketRequestResult requestResult,
      String serviceTicket) {
    if (accountConfiguration.getDebugWithSensitiveData()) {
      Log.d(
          TAG,
          String.format(
              "Result of attempt to obtain service ticket to %s: %s , valid ticket? %s.",
              service, requestResult, serviceTicket != null));
    }

    if (!requestResult.successful() || (serviceTicket == null)) {
      setErrorResultAndFinish(AccountManager.ERROR_CODE_BAD_AUTHENTICATION,
          requestResult.toString());
      return;
    }

    Bundle result = new Bundle();
    KerberosAccount account = KerberosAccount.getAccount(this);
    if (account != null || service != null) {
      result.putString(AccountManager.KEY_ACCOUNT_NAME, account.getName());
      result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.KERBEROS_ACCOUNT_TYPE);
      result.putString(AccountManager.KEY_AUTHTOKEN, serviceTicket);
      result.putInt("spnegoResult", 0);
      account.save(this);
      // Information about the most recent ticket to save in secure file storage.

      SharedPreferences sharedPref =
          getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE);
      ServiceTicketInfo.saveServiceTicketInfo(sharedPref, service, new Date().getTime(), null);

      // Return to the caller (e.g. Chrome)
      setResultAndFinish(result);
    } else {
      // Save information about the most recent ticket in secure file storage.
      SharedPreferences sharedPref =
          getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE);
      ServiceTicketInfo.saveServiceTicketInfo(
          sharedPref, null, new Date().getTime(), requestResult.toString());
      setErrorResultAndFinish(
          AccountManager.ERROR_CODE_BAD_AUTHENTICATION, requestResult.toString());
    }
 }

  /** Returns an intent that can be used to obtain a service ticket. */
  public static Intent getServiceTicketIntent(
      Context context, String serviceName, AccountAuthenticatorResponse response) {
    Intent intent = new Intent(context, ServiceTicketActivity.class);
    intent.putExtra(Constants.SERVICE_NAME, serviceName);
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
    return intent;
  }
}

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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.apps.work.kerberosauthenticator.internal.KerberosAccountDetails;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketGrantingTicket;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketRequestResult;
import com.google.android.apps.work.kerberosauthenticator.internal.kinit.UserAuthenticationResultListener;
import com.google.android.apps.work.kerberosauthenticator.internal.kinit.UserAuthenticationTask;
import javax.security.auth.Subject;

/** Obtains a ticket granting ticket for the user, while displaying authentication status and
 * details on the most recently issued tickets.
 * If the user launches the Kerberos authenticator via the launcher manually, this activity will
 * only show status.
 */
public class LoginActivity extends BaseAuthenticatorActivity implements
    UserAuthenticationResultListener {

  boolean isPasswordRetry = false;

  /** Returns an intent that can be used to authenticate an account. */
  public static Intent getAuthenticateIntent(
      Context context, AccountAuthenticatorResponse response) {
    return getAuthenticateIntent(context, response, null);
  }

  /** Returns an intent that can be used to authenticate an account. */
  public static Intent getAuthenticateIntent(
      Context context, AccountAuthenticatorResponse response, String serviceName) {
    Intent intent = new Intent(context, LoginActivity.class);
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
    if (serviceName != null) {
      intent.putExtra(Constants.SERVICE_NAME, serviceName);
    }
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Let the user know an account cannot be added because managed config is missing.
    if (!accountConfiguration.hasManagedConfigs()) {
      startActivity(DeclineAddingAccountActivity.getDeclineIntentDueToConfigMissing(this, null));
      finish();
      return;
    }

    isPasswordRetry = false;

    setContentView(R.layout.authenticator);

    Intent intent = getIntent();
    String serviceName = intent.getStringExtra(Constants.SERVICE_NAME);
    boolean shouldAddAccount = TextUtils.isEmpty(serviceName);
    Log.d(
        Constants.TAG,
        String.format(
            "Starting login activity, service name? %s, add new account? %s.",
            serviceName, shouldAddAccount));

    // Initialise the UI with corresponding values.
    initUI(false /*isUserInitiated*/, serviceName);

    // Generate or renew a TGT. Do not attempt recovering from a bad password if we are in the
    // process of adding a new account.
    authenticateAccount(shouldAddAccount);

    // If only the status is shown, the activity remains open until the user taps the dismiss
    // button to finish it.
    Log.d(Constants.TAG, "Finished creating login activity.");
  }

  @Override
  public void onTicketGrantingTicketResult(
      TicketRequestResult ticketRequestResult, Subject ticket) {
    if (accountConfiguration.getDebugWithSensitiveData()) {
      Log.d(TAG, String.format("Result of attempt to authenticate user: %s , valid ticket? %s",
          ticketRequestResult, ticket != null));
    }
    Bundle result = new Bundle();
    KerberosAccount account = KerberosAccount.getAccount(this);
    boolean successGettingTgt = ticketRequestResult.successful() && ticket != null;

    if (successGettingTgt && account != null) {
      TicketGrantingTicket tgt = new TicketGrantingTicket(ticket);
      account.setTicketGrantingTicket(tgt.asSerialized());
      account.save(this);
      isPasswordRetry = false;
    } else {
      if (ticketRequestResult.isPasswordBad() && !isPasswordRetry) {
        Log.i(
            Constants.TAG,
            String.format(
                "Bad password for user %s, removing and attempting re-authentication.",
                account.getName()));
        KerberosAccount.removeAccount(this);
        isPasswordRetry = true;
        authenticateAccount(true /*shouldAddAccount*/);
      } else {
        setErrorResultAndFinish(
            AccountManager.ERROR_CODE_BAD_AUTHENTICATION, ticketRequestResult.toString());
      }
      return;
    }

    String serviceName = getIntent().getStringExtra(Constants.SERVICE_NAME);
    if (serviceName == null) {
      result.putString(AccountManager.KEY_ACCOUNT_NAME, account.getName());
      result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.KERBEROS_ACCOUNT_TYPE);
      // Change UI to show the TGT is obtained correctly.
      setResultAndFinish(result);
    } else {
      // A service name was provided - meaning the TGT had to be renewed before getting a service
      // ticket. As the TGT was renewed successfully, launch the service ticket activity.
      AccountAuthenticatorResponse response =
          getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
      Intent serviceTicketIntent =
          ServiceTicketActivity.getServiceTicketIntent(this, serviceName, response);
      serviceTicketIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
      startActivity(serviceTicketIntent);
      finish();
    }
  }

  private void authenticateAccount(boolean shouldAddAccount) {
    KerberosAccountDetails accountDetails = accountConfiguration.getAccountDetails();
    if (accountDetails == null) {
      Log.e(TAG, "Missing details for new account, erroring out.");
      setErrorResultAndFinish(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "Account details missing");
      return;
    }

    KerberosAccount account = KerberosAccount.getAccount(this);
    // Remove the old account if the username in the from the managed configuration is different
    // to the saved account.
    if (account != null) {
      boolean accountNameDiffers = !account.getName().equals(accountDetails.getUsername());
      if (accountNameDiffers) {
        Log.i(
            Constants.TAG,
            String.format("Removing obsolete account for user" + " %s.", account.getName()));
        KerberosAccount.removeAccount(this);
        account = null;
      }      // Fall through to the logic for creating and saving a new account as the old
      // one was just removed.
    } else if (!shouldAddAccount) {
      throw new IllegalStateException(
          "No account is defined and not in a flow for adding accounts.");
    }

    boolean hasUserPassword = account != null && !TextUtils.isEmpty(account.getPassword());
    if (accountConfiguration.hasManagedConfigPassword() || hasUserPassword) {
      // We have all data required to authenticate.
      initiateUserAuthenticationTask(buildKerberosAccountDetails(accountDetails, account));
    } else {
      showPasswordEntryPrompt(account);
    }
  }

  private void saveUserCredentials() {
    hideUserLoginUI();
    KerberosAccountDetails detailsWithoutPassword = accountConfiguration.getAccountDetails();
    String password = ((TextView) findViewById(R.id.editTextPw)).getText().toString();
    KerberosAccountDetails detailsWithPassword =
        new KerberosAccountDetails(
            detailsWithoutPassword.getUsername(),
            password,
            detailsWithoutPassword.getActiveDirectoryDomain(),
            detailsWithoutPassword.getAdDomainController());
    initiateUserAuthenticationTask(detailsWithPassword);
  }

  private void initiateUserAuthenticationTask(KerberosAccountDetails accountDetails) {
    setRefreshingStatus(getTGTTimestampTextViewId());
    KerberosAccount account = KerberosAccount.getAccount(this);
    if (account == null) {
      account = new KerberosAccount(accountDetails);
    }

    account.save(this);
    UserAuthenticationTask kinit =
        new UserAuthenticationTask(
            this,
            new KerberosAccountDetails(
                account.getName(),
                account.getPassword(),
                account.getDomain(),
                account.getDomainController()),
            accountConfiguration.getDebugWithSensitiveData());
    kinit.execute();
  }

  private void showUserLoginUI() {
    View pwField = findViewById(R.id.editTextPw);
    pwField.setVisibility(View.VISIBLE);
    Button loginBtn = findViewById(R.id.ok_btn);
    loginBtn.setText(R.string.login_btn);
    loginBtn.setVisibility(View.VISIBLE);
    loginBtn.setOnClickListener(v -> saveUserCredentials());
  }

  private void hideUserLoginUI() {
    findViewById(R.id.editTextPw).setVisibility(View.INVISIBLE);
    findViewById(R.id.ok_btn).setVisibility(View.INVISIBLE);
    setText(getTGTTimestampTextViewId(), getText(R.string.not_available).toString());
    setRefreshingStatus(getTGTTimestampTextViewId());
  }

  private void showPasswordEntryPrompt(KerberosAccount account) {
    // Activity was created to generate a TGT.
    // Prepare to check if we have the user password available.
    boolean missingAccountPassword = account == null || TextUtils.isEmpty(account.getPassword());
    if (!accountConfiguration.hasManagedConfigPassword() && missingAccountPassword) {
      // User needs to log in if there is no managed config password and the Account Manager
      // does not hold any password.
      showUserLoginUI();
    }
    if (account != null) {
      // If the TGT is being renewed for the same account, check if there is any service ticket
      // information available already.
      showLastServiceAuth();
    }
  }

  private static KerberosAccountDetails buildKerberosAccountDetails(
      KerberosAccountDetails accountDetails, KerberosAccount account) {
    String username;
    String password;
    String domain;
    String controller;
    if (account == null) {
      username = accountDetails.getUsername();
      password = accountDetails.getPassword();
      domain = accountDetails.getActiveDirectoryDomain();
      controller = accountDetails.getAdDomainController();
    } else {
      username = account.getName();
      password = account.getPassword();
      domain = account.getDomain();
      controller = account.getDomainController();
    }

    if (TextUtils.isEmpty(password)) {
      throw new IllegalStateException("No valid password.");
    }
    return new KerberosAccountDetails(username, password, domain, controller);
  }
}
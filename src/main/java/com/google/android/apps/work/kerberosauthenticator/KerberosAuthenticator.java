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

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.apps.work.kerberosauthenticator.internal.TicketGrantingTicket;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;

/** Kerberos account authenticator. */
public class KerberosAuthenticator extends AbstractAccountAuthenticator {
  private final Context context;

  KerberosAuthenticator(Context context) {
    super(context);
    this.context = context;
  }

  @Override
  public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
    return unsupportedOperationBundle("editProperties");
  }

  private Bundle unsupportedOperationBundle(String opName) {
    Bundle result = new Bundle();
    result.putInt(
        AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION);
    result.putString(AccountManager.KEY_ERROR_MESSAGE, "Unsupported method: " + opName);
    return result;
  }

  @Override
  public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
      String authTokenType, String[] requiredFeatures, Bundle options) {
    Bundle bundle = new Bundle();

    Intent intentToReturn;

    // Request comes from Chrome, add account.
    if (requiredFeatures != null && Arrays.asList(requiredFeatures).contains(Constants.SPNEGO)) {
      if (hasValidAccountConfiguration()) {
        intentToReturn = LoginActivity.getAuthenticateIntent(context, response);
      } else {
        intentToReturn =
            DeclineAddingAccountActivity.getDeclineIntentDueToConfigMissing(context, response);
      }
    } else {
      // User cannot add account themselves.
      intentToReturn =
          DeclineAddingAccountActivity.getDeclineIntentDueToUserAdded(context, response);
    }
    bundle.putParcelable(AccountManager.KEY_INTENT, intentToReturn);

    return bundle;
  }

  @Override
  public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
      Bundle options) {
    return unsupportedOperationBundle("confirmCredentials");
  }

  @Override
  public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
      String authTokenType, Bundle options) {
    Log.d(
        TAG,
        String.format(
            "Received request to obtain token %s with account %s.", authTokenType, account));

    // Request does not come from Chrome, deny access.
    if (options == null
        || !options.containsKey(AccountManager.KEY_ANDROID_PACKAGE_NAME)
        || !Constants.CHROME_PACKAGE_NAME.equals(
            options.get(AccountManager.KEY_ANDROID_PACKAGE_NAME))) {
      return unsupportedOperationBundle("Unsupported caller app.");
    }

    Bundle result = new Bundle();
    Matcher matcher = Constants.AUTH_TOKEN_PATTERN.matcher(authTokenType);
    String serviceName;
    if (matcher.matches() && matcher.groupCount() == 5) {
      serviceName = matcher.group(4);
    } else {
      // Cannot obtain service name.
      result.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_ARGUMENTS);
      result.putString(
          AccountManager.KEY_ERROR_MESSAGE,
          String.format("Invalid auth token format for %s.", authTokenType));
      return result;
    }

    KerberosAccount krbAccount = KerberosAccount.getAccount(context);
    // No account, this is a deviation from the protocol, return an error.
    if (krbAccount == null) {
      result.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_REQUEST);
      result.putString(AccountManager.KEY_ERROR_MESSAGE, "No account configured?");
      return result;
    }

    // Mismatch between the username in the account provided and the existing Kerberos account,
    // this is a deviation from the protocol, return an error.
    if (!krbAccount.getName().equals(account.name)) {
      result.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_REQUEST);
      result.putString(AccountManager.KEY_ERROR_MESSAGE,
          String.format("Account names mismatch: %s vs %s", krbAccount.getName(), account.name));
      return result;
    }

    // Check if the account details via managed config have changed from what's stored in the
    // AccountManager. If there's a mismatch and the account needs to be updated, also call
    // getAuthenticateIntent as it will remove the old account and add a new one.
    boolean needReAuthentication = !krbAccount.getName().equals(getManagedConfigurationUsername());

    // Before requesting a service ticket, check if the TGT for the current account needs renewal.
    TicketGrantingTicket tgt =
        TicketGrantingTicket.fromSerializedSubject(krbAccount.getTicketGrantingTicket());

    needReAuthentication |=
        tgt == null || tgt.getExpiryDate() == null || tgt.getExpiryDate().before(new Date());
    if (needReAuthentication) {
      Log.d(
          TAG,
          String.format("Ticket-granting-ticket for %s will be renewed.", krbAccount.getName()));
      Intent intent =
          LoginActivity.getAuthenticateIntent(context, response, serviceName);
      result.putParcelable(AccountManager.KEY_INTENT, intent);
      return result;
    }

    Log.d(TAG, String.format("Will request service ticket for %s, account %s.",
        serviceName, krbAccount.getName()));
    Intent intent =
        ServiceTicketActivity.getServiceTicketIntent(context, serviceName, response);
    result.putParcelable(AccountManager.KEY_INTENT, intent);
    return result;
  }

  @Override
  public String getAuthTokenLabel(String authTokenType) {
    return "Spnego" + authTokenType;
  }

  @Override
  public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
      String authTokenType, Bundle options) {
    return unsupportedOperationBundle("updateCredentials");
  }

  @Override
  public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
      String[] features) {
    Bundle result = new Bundle();
    for (String feature : features) {
      if (!feature.equals("SPNEGO")) {
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
      }
    }
    result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
    return result;
  }

  @Override
  public Bundle addAccountFromCredentials(AccountAuthenticatorResponse response, Account account,
      Bundle accountCredentials)
      throws NetworkErrorException {
    return super.addAccountFromCredentials(response, account, accountCredentials);
  }

  /**
   * An interface to operate on an {@code AccountConfiguration} instance and get some data
   * out ot it.
   * @param <T> return type for the data extracted.
   */
  interface AccountConfigurationOperator<T> {
    T operate(AccountConfiguration config);
  }

  // Runs the provided operator on an AccountConfiguration instance, unregistering it when
  // done.
  private <T> T getFromAccountConfiguration(AccountConfigurationOperator<T> operator) {
    AccountConfiguration config = null;
    try {
      config = new AccountConfiguration(context);
      return operator.operate(config);
    } finally {
      config.unregisterReceiver(context);
    }
  }

  private boolean hasValidAccountConfiguration() {
    return getFromAccountConfiguration(AccountConfiguration::hasManagedConfigs);
  }

  private String getManagedConfigurationUsername() {
    return getFromAccountConfiguration(
        (config) -> {
          if (!config.hasManagedConfigs()) {
            return "";
          }

          return config.getAccountDetails().getUsername();
        });
  }
}

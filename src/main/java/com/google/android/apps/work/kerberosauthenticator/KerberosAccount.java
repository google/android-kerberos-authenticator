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

import static com.google.android.apps.work.kerberosauthenticator.Constants.KERBEROS_ACCOUNT_TYPE;
import static com.google.android.apps.work.kerberosauthenticator.Constants.TAG;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Base64;
import android.util.Log;
import com.google.android.apps.work.kerberosauthenticator.BaseAuthenticatorActivity.ServiceTicketInfo;
import com.google.android.apps.work.kerberosauthenticator.internal.KerberosAccountDetails;

/** Kerberos account functionality. */
public class KerberosAccount {
  @VisibleForTesting static final String KEY_AD_DOMAIN = "ad_domain";
  @VisibleForTesting static final String KEY_AD_DC = "domain_controller";
  @VisibleForTesting static final String KEY_TGT = "ticket_granting_ticket";

  private final String name;
  private final String password;
  private final Bundle userData = new Bundle();

  KerberosAccount(String name, String password, String adDomain, String domainController) {
    this(name, password, adDomain, domainController, "");
  }

  private KerberosAccount(
      String name, String password, String adDomain, String domainController, String base64Tgt) {
    this.name = name;
    this.password = password;
    userData.putString(KEY_AD_DOMAIN, adDomain);
    userData.putString(KEY_AD_DC, domainController);
    userData.putString(KEY_TGT, base64Tgt);
  }

  KerberosAccount(KerberosAccountDetails accountDetails) {
    this(
        accountDetails.getUsername(),
        accountDetails.getPassword(),
        accountDetails.getActiveDirectoryDomain(),
        accountDetails.getAdDomainController());
  }

  static void removeAccount(Context context) {
    AccountManager am = AccountManager.get(context);
    Account[] accounts = am.getAccountsByType(KERBEROS_ACCOUNT_TYPE);
    if (accounts.length > 0) {
      am.removeAccountExplicitly(accounts[0]);
    }
    ServiceTicketInfo.clearServiceTicketInfo(
        context.getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE));
  }

  /**
   * Returns the Kerberos account currently configured in the AccountMAnager.
   * @return the account, or null if none is configured.
   * @throws IllegalStateException if more than one account is configured.
   */
  static KerberosAccount getAccount(Context context) {
    AccountManager am = AccountManager.get(context);
    Account[] accounts = am.getAccountsByType(KERBEROS_ACCOUNT_TYPE);
    if (accounts.length > 1) {
      throw new IllegalStateException(
          "More than one Kerberos account available in the Account Manager");
    } else if (accounts.length < 1) {
      // Indicate there's no account.
      return null;
    }
    Account account = accounts[0];
    String password = am.getPassword(account);
    String adDomain = am.getUserData(account, KEY_AD_DOMAIN);
    String domainController = am.getUserData(account, KEY_AD_DC);
    String base64Tgt = am.getUserData(account, KEY_TGT);
    return new KerberosAccount(account.name, password, adDomain, domainController, base64Tgt);
  }

  public String getName() {
    return name;
  }

  byte[] getTicketGrantingTicket() {
    return Base64.decode(userData.getString(KEY_TGT), Base64.NO_WRAP);
  }

  void setTicketGrantingTicket(byte[] tgt) {
    userData.putString(KEY_TGT, Base64.encodeToString(tgt, Base64.NO_WRAP));
  }

  void save(Context context) {
    AccountManager am = AccountManager.get(context);
    Account[] accounts = am.getAccountsByType(KERBEROS_ACCOUNT_TYPE);
    boolean hasAccountWithIncorrectName = (accounts.length > 0) && !accounts[0].name.equals(name);
    boolean hasNoAccount = (accounts.length == 0);

    if (hasAccountWithIncorrectName) {
      throw new IllegalStateException(
          String.format(
              "Cannot save account details for user %s when the existing account is for user %s",
              name, accounts[0].name));
    }

    if (hasNoAccount) {
      Log.i(TAG, String.format("Adding account %s.", name));
      am.addAccountExplicitly(new Account(name, KERBEROS_ACCOUNT_TYPE), password, userData);
      return;
    }

    final Account account = accounts[0];
    Log.i(TAG, String.format("Updating TGT for account %s.", account.name));
    am.setUserData(account, KEY_TGT, userData.getString(KEY_TGT));

    if (password != null && !password.equals(am.getPassword(account))) {
      Log.v(TAG, String.format("Updating password for account %s.", account.name));
      am.setPassword(account, password);
    }

    final String domain = userData.getString(KEY_AD_DOMAIN);
    final String currentDomain = am.getUserData(account, KEY_AD_DOMAIN);
    if (!currentDomain.equals(domain)) {
      Log.i(TAG, String.format("Updating domain for account %s from %s to %s.", account.name,
          currentDomain, domain));
      am.setUserData(account, KEY_AD_DOMAIN, domain);
    }

    final String domainController = userData.getString(KEY_AD_DC);
    String currentDomainController = am.getUserData(account, KEY_AD_DC);
    if (!currentDomainController.equals(domainController)) {
      Log.i(
          TAG,
          String.format(
              "Updating domain controller for account %s from %s to %s",
              account.name, currentDomainController, domainController));
      am.setUserData(account, KEY_AD_DC, domainController);
    }
  }

  String getDomainController() {
    return userData.getString(KEY_AD_DC);
  }

  String getDomain() {
    return userData.getString(KEY_AD_DOMAIN);
  }

  String getPassword() {
    return password;
  }
}

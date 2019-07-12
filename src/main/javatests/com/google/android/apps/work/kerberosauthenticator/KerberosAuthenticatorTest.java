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
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.AD_DC;
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.AD_DOMAIN;
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.PASSWORD;
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.TEST_AD_CONTROLLER;
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.TEST_AD_DOMAIN;
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.TGT_B64;
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.USERNAME;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAccountManager;

@RunWith(RobolectricTestRunner.class)
@Config(
    sdk = 26,
    shadows = {ShadowAccountManager.class})
public class KerberosAuthenticatorTest {

  private ContextWrapper context;
  private AccountManager accountManager;
  private KerberosAuthenticator authenticator;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    accountManager = AccountManager.get(context);
    authenticator = new KerberosAuthenticator(context);
  }

  @After
  public void tearDown() {
    shadowOf(accountManager).removeAllAccounts();
  }

  @Test
  public void testAuthenticatorUnsupportedOperationError() {
    Bundle result = authenticator.editProperties(null, null);
    assertThat(result.get(AccountManager.KEY_ERROR_CODE)).isEqualTo(6);
    assertThat(result.get(AccountManager.KEY_ERROR_MESSAGE))
        .isEqualTo("Unsupported method: editProperties");
  }

  @Test
  public void testAddAccount() {
    setTestRestrictions();
    Bundle result =
        authenticator.addAccount(
            null, null, null, new String[] {Constants.SPNEGO}, getTestOptions());
    assertIsAuthenticationActivity((Intent) result.get("intent"));
  }

  @Test
  public void testDeclineAddAccountWhenMissingManagedConfigs() {
    // SPNEGO requests are approved.
    Bundle result =
        authenticator.addAccount(
            null, null, null, new String[] {Constants.SPNEGO}, getTestOptions());
    assertThat(((Intent) result.get("intent")).getComponent().getClassName())
        .isEqualTo(DeclineAddingAccountActivity.class.getName());
  }

  @Test
  public void testDeclineAddAccountWhenNotSPNEGORequest() {
    // Other request types except for SPNEGO are declined as users should not add Kerberos accounts
    // manually.
    authenticator = new KerberosAuthenticator(context);
    Bundle result = authenticator.addAccount(null, null, null, new String[] {}, getTestOptions());
    assertThat(((Intent) result.get("intent")).getComponent().getClassName())
        .isEqualTo(DeclineAddingAccountActivity.class.getName());
  }

  @Test
  public void testDeclineCallerNotChrome() {
    setTestRestrictions();

    Bundle options = new Bundle();
    options.putString(AccountManager.KEY_ANDROID_PACKAGE_NAME, "test_wrong_caller_app");
    Bundle result =
        authenticator.getAuthToken(
            null, null, "SPNEGO:HOSTBASED:HTTP@test-server.example.com", options);
    assertThat(result.get("errorCode")).isEqualTo(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION);
    assertThat(result.get("errorMessage")).isEqualTo("Unsupported method: Unsupported caller app.");
  }

  @Test
  public void testGetAuthTokenBadServiceName() {
    String badTestToken = "SPNEGO:HOSTBASED:HTTP@.com";
    authenticator = new KerberosAuthenticator(context);
    Bundle result = authenticator.getAuthToken(null, null, badTestToken, getTestOptions());
    assertThat(result.get(AccountManager.KEY_ERROR_CODE))
        .isEqualTo(AccountManager.ERROR_CODE_BAD_ARGUMENTS);
    assertThat(result.get(AccountManager.KEY_ERROR_MESSAGE))
        .isEqualTo(String.format("Invalid auth token format for %s.", badTestToken));
  }

  @Test
  public void testGetAuthTokenNoKerberosAccount() {
    authenticator = new KerberosAuthenticator(context);

    Bundle result =
        authenticator.getAuthToken(
            null, null, "SPNEGO:HOSTBASED:HTTP@test-server.example.com", getTestOptions());
    assertThat(result.get(AccountManager.KEY_ERROR_CODE))
        .isEqualTo(AccountManager.ERROR_CODE_BAD_REQUEST);
    assertThat(result.get(AccountManager.KEY_ERROR_MESSAGE)).isEqualTo("No account configured?");
  }

  @Test
  public void testGetAuthTokenRenewTGT() {
    Account testAccount = new Account(TestHelper.USERNAME, KERBEROS_ACCOUNT_TYPE);
    shadowOf(accountManager).addAccount(testAccount);
    accountManager.setUserData(testAccount, KerberosAccount.KEY_AD_DC, TEST_AD_CONTROLLER);
    accountManager.setUserData(testAccount, KerberosAccount.KEY_AD_DOMAIN, TEST_AD_DOMAIN);
    accountManager.setUserData(testAccount, KerberosAccount.KEY_TGT, TGT_B64);

    Account[] accounts = AccountManager.get(context).getAccounts();
    // There should be exactly one account.
    assertThat(accounts.length).isEqualTo(1);
    Account account = accounts[0];
    Bundle result =
        authenticator.getAuthToken(
            null, account, "SPNEGO:HOSTBASED:HTTP@test-server.example.com", getTestOptions());
    // The auth token is invalid so we renew the TGT.
    Intent resultIntent = result.getParcelable(AccountManager.KEY_INTENT);
    assertThat(resultIntent.getExtras().getString(Constants.SERVICE_NAME)).isEqualTo("test-server");
    assertIsAuthenticationActivity(resultIntent);
  }

  @Test
  public void testGetAuthTokenValidTGT() {
    Account testAccount = new Account(TestHelper.USERNAME, Constants.KERBEROS_ACCOUNT_TYPE);
    shadowOf(accountManager).addAccount(testAccount);
    accountManager.setUserData(testAccount, KerberosAccount.KEY_AD_DC, TEST_AD_CONTROLLER);
    accountManager.setUserData(testAccount, KerberosAccount.KEY_AD_DOMAIN, TEST_AD_DOMAIN);
    accountManager.setUserData(
        testAccount, KerberosAccount.KEY_TGT, TestHelper.B64_SUBJECT);

    // Set application restrictions to match the account added to the account manager so that the
    // flow of triggering a change will not be invoked.
    RestrictionsManager restrictionsManager =
        (RestrictionsManager)
            context.getSystemService(context.getSystemServiceName(RestrictionsManager.class));
    Bundle restrictionsBundle = TestHelper.makeRestrictionsBundle();
    restrictionsBundle.putString(AccountConfiguration.USERNAME_KEY, USERNAME);
    shadowOf(restrictionsManager).setApplicationRestrictions(restrictionsBundle);

    Account[] accounts = AccountManager.get(context).getAccounts();
    // There should be exactly one account.
    assertThat(accounts).hasLength(1);
    Account account = accounts[0];
    Bundle result =
        authenticator.getAuthToken(
            null, account, "SPNEGO:HOSTBASED:HTTP@test-server.example.com", getTestOptions());

    // The auth token is invalid so we renew the TGT.
    Intent resultIntent = result.getParcelable(AccountManager.KEY_INTENT);
    assertThat(resultIntent.getExtras().getString(Constants.SERVICE_NAME)).isEqualTo("test-server");
    assertThat(resultIntent.getComponent().getClassName())
        .isEqualTo(ServiceTicketActivity.class.getName());
  }

  @Test
  public void testGetAuthTokenAccountDetailsChange() {
    Account testAccount = new Account(TestHelper.USERNAME, Constants.KERBEROS_ACCOUNT_TYPE);
    shadowOf(accountManager).addAccount(testAccount);
    accountManager.setUserData(testAccount, KerberosAccount.KEY_AD_DC, TEST_AD_CONTROLLER);
    accountManager.setUserData(testAccount, KerberosAccount.KEY_AD_DOMAIN, TEST_AD_DOMAIN);
    accountManager.setUserData(
        testAccount, KerberosAccount.KEY_TGT, TestHelper.B64_SUBJECT);

    // Set application restrictions with a different account name to test that re-authentication
    // flow is triggered.
    RestrictionsManager restrictionsManager =
        (RestrictionsManager)
            context.getSystemService(context.getSystemServiceName(RestrictionsManager.class));
    Bundle restrictionsBundle = TestHelper.makeRestrictionsBundle();
    shadowOf(restrictionsManager).setApplicationRestrictions(restrictionsBundle);

    Account[] accounts = AccountManager.get(context).getAccounts();
    // There should be exactly one account.
    assertThat(accounts).hasLength(1);
    Account account = accounts[0];
    Bundle result =
        authenticator.getAuthToken(
            null, account, "SPNEGO:HOSTBASED:HTTP@test-server.example.com", getTestOptions());

    // The auth token is invalid so we renew the TGT.
    Intent resultIntent = result.getParcelable(AccountManager.KEY_INTENT);
    assertThat(resultIntent.getExtras().getString("ServiceName")).isEqualTo("test-server");
    assertIsAuthenticationActivity(resultIntent);
  }

  @Test
  public void testHasFeatures() {
    Account account = new Account(USERNAME, KERBEROS_ACCOUNT_TYPE);
    shadowOf(accountManager).addAccount(account);
    accountManager.setPassword(account, PASSWORD);
    accountManager.setUserData(account, "ad_domain", AD_DOMAIN);
    accountManager.setUserData(account, "domain_controller", AD_DC);
    accountManager.setUserData(account, "ticket_granting_ticket", TGT_B64);
    accountManager.setUserData(account, "service_ticket_name", Constants.SERVICE_NAME);

    Bundle response = authenticator.hasFeatures(null, account, new String[] {});
    assertThat(response.get(AccountManager.KEY_BOOLEAN_RESULT)).isEqualTo(true);
    response = authenticator.hasFeatures(null, account, new String[] {"SPNEGO"});
    assertThat(response.get(AccountManager.KEY_BOOLEAN_RESULT)).isEqualTo(true);
  }

  private void setTestRestrictions() {
    RestrictionsManager restrictionsManager =
        (RestrictionsManager)
            context.getSystemService(context.getSystemServiceName(RestrictionsManager.class));
    Bundle restrictionsBundle = TestHelper.makeRestrictionsBundle();
    shadowOf(restrictionsManager).setApplicationRestrictions(restrictionsBundle);
  }

  private static void assertIsAuthenticationActivity(Intent intent) {
    assertThat(intent.getComponent().getClassName()).isEqualTo(LoginActivity.class.getName());
  }

  private static Bundle getTestOptions() {
    Bundle options = new Bundle();
    options.putString(AccountManager.KEY_ANDROID_PACKAGE_NAME, Constants.CHROME_PACKAGE_NAME);
    return options;
  }
}

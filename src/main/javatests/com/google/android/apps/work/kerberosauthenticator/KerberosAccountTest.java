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
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.TGT;
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.TGT_B64;
import static com.google.android.apps.work.kerberosauthenticator.TestHelper.USERNAME;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.apps.work.kerberosauthenticator.BaseAuthenticatorActivity.ServiceTicketInfo;
import java.util.Base64;
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
public final class KerberosAccountTest {
  private ContextWrapper context;
  private AccountManager accountManager;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    accountManager = AccountManager.get(context);
  }

  @After
  public void tearDown() {
    shadowOf(accountManager).removeAllAccounts();
  }

  private void assertKerberosAccount(KerberosAccount account) {
    assertThat(account.getName()).isEqualTo(USERNAME);
    assertThat(account.getPassword()).isEqualTo(PASSWORD);
    assertThat(account.getDomain()).isEqualTo(AD_DOMAIN);
    assertThat(account.getDomainController()).isEqualTo(AD_DC);
    assertThat(account.getTicketGrantingTicket()).isEqualTo(TGT);
  }

  @Test
  public void testAccountGetters() {
    KerberosAccount account = TestHelper.createKerberosAccount();
    assertKerberosAccount(account);
  }

  @Test
  public void testGetAccount_nullByDefault() {
    assertThat(KerberosAccount.getAccount(context)).isNull();
  }

  @Test
  public void testGetAccount_positive() {
    shadowOf(accountManager).addAccount(new Account(USERNAME, KERBEROS_ACCOUNT_TYPE));
    assertThat(KerberosAccount.getAccount(context)).isNotNull();
  }

  @Test
  public void testRemoveAccount() {
    shadowOf(accountManager).addAccount(new Account(USERNAME, KERBEROS_ACCOUNT_TYPE));

    // Save test service ticket info data. This gets deleted when removing an account.
    SharedPreferences sharedPref =
        context.getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE);
    String testServiceName = "test-name";
    ServiceTicketInfo.saveServiceTicketInfo(sharedPref, testServiceName, 1, null);
    // Verify the service ticket info is saved correctly, before testing its removal.
    ServiceTicketInfo info = ServiceTicketInfo.getServiceTicketInfo(sharedPref);
    assertThat(info.getServiceName()).isEqualTo(testServiceName);
    assertThat(info.getError()).isEmpty();
    assertThat(info.getObtainedAtMillis()).isEqualTo(1);

    // Remove account
    KerberosAccount.removeAccount(context);

    // Verify all account data has been successfully removed.
    assertThat(KerberosAccount.getAccount(context)).isNull();
    info =
        ServiceTicketInfo.getServiceTicketInfo(
            context.getSharedPreferences(Constants.PREFERENCE_NAME, Activity.MODE_PRIVATE));
    assertThat(info.getServiceName()).isEmpty();
    assertThat(info.getObtainedAtMillis()).isEqualTo(-1);
    assertThat(info.getError()).isEmpty();
  }

  @Test
  public void testGetAccount() {
    Account account = new Account(USERNAME, KERBEROS_ACCOUNT_TYPE);
    shadowOf(accountManager).addAccount(account);
    accountManager.setPassword(account, PASSWORD);
    accountManager.setUserData(account, "ad_domain", AD_DOMAIN);
    accountManager.setUserData(account, "domain_controller", AD_DC);
    accountManager.setUserData(account, "ticket_granting_ticket", TGT_B64);

    KerberosAccount krbAccount = KerberosAccount.getAccount(context);
    assertKerberosAccount(krbAccount);
  }

  @Test
  public void testSaveAccount_noPreviousAccount() {
    TestHelper.createKerberosAccount().save(context);
    KerberosAccount loadedAccount = KerberosAccount.getAccount(context);
    assertKerberosAccount(loadedAccount);
  }

  @Test
  public void testSaveAccount_PreviousAccountSameName() {
    Account account = new Account(USERNAME, KERBEROS_ACCOUNT_TYPE);
    shadowOf(accountManager).addAccount(account);
    accountManager.setPassword(account, PASSWORD);
    accountManager.setUserData(account, "ad_domain", AD_DOMAIN);
    accountManager.setUserData(account, "domain_controller", AD_DC);
    accountManager.setUserData(account, "ticket_granting_ticket", TGT_B64);

    final String anotherPassword = "hunter123";
    final String anotherDomain = "another_domain.example.com";
    final String anotherDc = "secondary_dc.example.com";
    final byte[] anotherTgt = {'d', 'e', 'f'};
    final String b64Tgt = Base64.getEncoder().encodeToString(anotherTgt);
    KerberosAccount modifiedAccount =
        new KerberosAccount(USERNAME, anotherPassword, anotherDomain, anotherDc);
    modifiedAccount.setTicketGrantingTicket(anotherTgt);
    modifiedAccount.save(context);

    Account readAccount = accountManager.getAccountsByType(KERBEROS_ACCOUNT_TYPE)[0];
    assertThat(readAccount).isNotNull();
    assertThat(readAccount.name).isEqualTo(USERNAME);
    assertThat(accountManager.getPassword(readAccount)).isEqualTo(anotherPassword);
    assertThat(accountManager.getUserData(readAccount, "ad_domain")).isEqualTo(anotherDomain);
    assertThat(accountManager.getUserData(readAccount, "domain_controller")).isEqualTo(anotherDc);
    assertThat(accountManager.getUserData(readAccount, "ticket_granting_ticket")).isEqualTo(b64Tgt);
  }

  @Test
  public void testSaveAccount_PreviousAccountDifferentName() {
    Account account = new Account("anotherUser", KERBEROS_ACCOUNT_TYPE);
    shadowOf(accountManager).addAccount(account);
    accountManager.setPassword(account, "dummyPassword");
    accountManager.setUserData(account, "ad_domain", "dummyDomain");
    accountManager.setUserData(account, "domain_controller", "dummyAd");
    accountManager.setUserData(account, "ticket_granting_ticket", "");

    KerberosAccount krbAccount = TestHelper.createKerberosAccount();
    // This call will throw expected exception IllegalStateException
    try {
      krbAccount.save(context);
      fail("Overwriting account should throw IllegalStateException.");
    } catch (IllegalStateException expected) {
    }
    // Exception was expected, continue with checking that there were no changes
    Account readAccount = accountManager.getAccountsByType(KERBEROS_ACCOUNT_TYPE)[0];
    assertThat(readAccount).isNotNull();
    assertThat(readAccount.name).isEqualTo("anotherUser");
    assertThat(accountManager.getPassword(readAccount)).isEqualTo("dummyPassword");
    assertThat(accountManager.getUserData(readAccount, "ad_domain")).isEqualTo("dummyDomain");
    assertThat(accountManager.getUserData(readAccount, "domain_controller")).isEqualTo("dummyAd");
    assertThat(accountManager.getUserData(readAccount, "ticket_granting_ticket")).isEmpty();
    return;
  }
}

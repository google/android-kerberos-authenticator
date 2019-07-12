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

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.BroadcastReceiver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.apps.work.kerberosauthenticator.internal.KerberosAccountDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * Tests {@link BaseAuthenticatorActivity}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class BaseAuthenticatorActivityTest {

  private RestrictionsManager restrictionsManager;
  private Bundle restrictionsBundle;
  private ContextWrapper context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    restrictionsManager = (RestrictionsManager) context.getSystemService(
        context.getSystemServiceName(RestrictionsManager.class));
    restrictionsBundle = TestHelper.makeRestrictionsBundle();
  }

  @Test
  public void testGetConfigs_allValues() {
    shadowOf(restrictionsManager).setApplicationRestrictions(restrictionsBundle);

    ActivityController<BaseAuthenticatorActivity> controller = Robolectric.buildActivity(
        BaseAuthenticatorActivity.class).create().start();
    BaseAuthenticatorActivity baseAuthenticatorActivity = controller.get();
    AccountConfiguration accConfigs = baseAuthenticatorActivity.accountConfiguration;

    KerberosAccountDetails accountDetails = accConfigs.getAccountDetails();
    assertThat(accountDetails).isNotNull();
    assertThat(accountDetails.getUsername()).isEqualTo(TestHelper.TEST_USERNAME);
    assertThat(accountDetails.getPassword()).isEqualTo(TestHelper.TEST_PASSWORD);
    assertThat(accountDetails.getActiveDirectoryDomain()).isEqualTo(TestHelper.TEST_AD_DOMAIN);
    assertThat(accountDetails.getAdDomainController()).isEqualTo(TestHelper.TEST_AD_CONTROLLER);
  }

  @Test
  public void testGetConfigs_missingValues() {
    restrictionsBundle.remove(AccountConfiguration.USERNAME_KEY);
    shadowOf(restrictionsManager).setApplicationRestrictions(restrictionsBundle);

    ActivityController<BaseAuthenticatorActivity> controller = Robolectric.buildActivity(
        BaseAuthenticatorActivity.class).create().start();
    BaseAuthenticatorActivity baseAuthenticatorActivity = controller.get();
    AccountConfiguration accConfigs = baseAuthenticatorActivity.accountConfiguration;
    assertThat(accConfigs.getAccountDetails()).isNull();
  }

  @Test
  public void testGetConfigs_noValuesSet() {
    // This tests the default behaviour initialised by the app's restrictions xml config.
    //restrictionsBundle.clear();
    restrictionsBundle.clear();
    shadowOf(restrictionsManager).setApplicationRestrictions(restrictionsBundle);

    ActivityController<BaseAuthenticatorActivity> controller = Robolectric.buildActivity(
        BaseAuthenticatorActivity.class).create().start();
    BaseAuthenticatorActivity baseAuthenticatorActivity = controller.get();
    AccountConfiguration accConfigs = baseAuthenticatorActivity.accountConfiguration;

    assertThat(accConfigs.getAccountDetails()).isNull();
  }

  @Test
  public void testBroadcastConfigs() {
    // Set original restrictions
    shadowOf(restrictionsManager).setApplicationRestrictions(restrictionsBundle);
    ActivityController<BaseAuthenticatorActivity> controller = Robolectric.buildActivity(
        BaseAuthenticatorActivity.class).create().start();
    BaseAuthenticatorActivity baseAuthenticatorActivity = controller.get();
    AccountConfiguration accConfigs = baseAuthenticatorActivity.accountConfiguration;

    // Change one restriction
    assertThat(accConfigs.getAccountDetails().getUsername()).isEqualTo(TestHelper.TEST_USERNAME);
    restrictionsBundle.putString(AccountConfiguration.USERNAME_KEY, TestHelper.TEST_USERNAME + "1");

    // Broadcast the restriction change
    BroadcastReceiver receiver = accConfigs.getReceiver();
    LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(
        Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED));
    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(
        Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED));

    // Check the username restriction is updated.
    assertThat(accConfigs.getAccountDetails().getUsername())
        .isEqualTo(TestHelper.TEST_USERNAME + "1");

    // Unregister receiver.
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
  }
}

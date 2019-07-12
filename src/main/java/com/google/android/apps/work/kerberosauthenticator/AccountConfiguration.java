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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.google.android.apps.work.kerberosauthenticator.internal.KerberosAccountDetails;
import com.google.common.base.Strings;

/**
 * This class obtains and updates Kerberos account details from managed restrictions.
 *
 * <p>When a password is not supplied through managed restrictions, this class will obtain it from
 * the user interface, which will prompt the user to enter a password.
 *
 * <p>A DPC can set account details in managed restrictions and this is the only way through which
 * the Kerberos Authenticator will obtain credentials to generate tickets. Users cannot add their
 * own authentication credentials.
 */
public class AccountConfiguration {

  // Managed configs keys
  static final String AD_DOMAIN_KEY = "adDomain";
  static final String AD_CONTROLLER_KEY = "adController";
  static final String USERNAME_KEY = "username";
  static final String PASSWORD_KEY = "password";
  static final String SENSITIVE_DEBUG_DATA_KEY = "sensitiveDebugData";
  // Managed configuration
  private final RestrictionsManager restrictionsManager;
  private final ManagedConfigsBroadcastReceiver restrictionsReceiver;
  // Manage configs fields
  private String username;
  private String password;
  private String adDomain;
  private String adController;
  private boolean debugWithSensitiveData = false;

  AccountConfiguration(@NonNull Context context) {
    // Managed configs initialisation and listener definition
    restrictionsManager = (RestrictionsManager) context.getSystemService(
        Context.RESTRICTIONS_SERVICE);
    IntentFilter restrictionsFilter =
        new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);
    restrictionsReceiver = new ManagedConfigsBroadcastReceiver();
    context.registerReceiver(restrictionsReceiver, restrictionsFilter);
    setManagedConfigs();
  }

  private void setManagedConfigs() {
    Bundle restrictionsBundle = restrictionsManager.getApplicationRestrictions();
    if (restrictionsBundle == null) {
      restrictionsBundle = new Bundle();
    }
    // Obtain managed configs.
    if (restrictionsBundle.containsKey(AD_DOMAIN_KEY)
        && restrictionsBundle.containsKey(AD_CONTROLLER_KEY)
        && restrictionsBundle.containsKey(USERNAME_KEY)) {
      adDomain = restrictionsBundle.getString(AD_DOMAIN_KEY);
      adController = restrictionsBundle.getString(AD_CONTROLLER_KEY);
      username = restrictionsBundle.getString(USERNAME_KEY);
    }
    if (restrictionsBundle.containsKey(PASSWORD_KEY)) {
      // Password may either be supplied by managed config or user input.
      password = restrictionsBundle.getString(PASSWORD_KEY);
    }

    debugWithSensitiveData = restrictionsBundle.getBoolean(SENSITIVE_DEBUG_DATA_KEY, false);
  }

  KerberosAccountDetails getAccountDetails() {
    if (!hasManagedConfigs()) {
      return null;
    }
    return new KerberosAccountDetails(username, password, adDomain, adController);
  }

  boolean getDebugWithSensitiveData() {
    return debugWithSensitiveData;
  }

  @VisibleForTesting
  BroadcastReceiver getReceiver() {
    return restrictionsReceiver;
  }

  void unregisterReceiver(@NonNull Context context) {
    context.unregisterReceiver(restrictionsReceiver);
  }

  boolean hasManagedConfigs() {
    // If any restriction string is empty, the configs are assumed to be missing.
    boolean emptyUsername = Strings.isNullOrEmpty(username);
    boolean emptyDomain = Strings.isNullOrEmpty(adDomain);
    boolean emptyDomainController = Strings.isNullOrEmpty(adController);
    boolean hasManagedConfigs = !(emptyUsername || emptyDomain || emptyDomainController);
    if (!hasManagedConfigs) {
      Log.d(
          Constants.TAG,
          String.format(
              "Missing managed configuration: username? %s, domain? %s, domain"
                  + " controller? %s.",
              emptyUsername, emptyDomain, emptyDomainController));
    }
    return hasManagedConfigs;
  }

  boolean hasManagedConfigPassword() {
    return !Strings.isNullOrEmpty(password);
  }

  class ManagedConfigsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
      Log.d(Constants.TAG, "New managed configuration received.");
      setManagedConfigs();
    }
  }
}

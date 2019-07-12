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

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * This contains the response to a user trying to add a new Kerberos account themselves. The account
 * should only be added through managed configs on a managed user account.
 */
public class DeclineAddingAccountActivity extends AccountAuthenticatorActivity {
  private static final String ACCOUNT_CONFIGURATION_MISSING = "accountConfigMissing";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.no_account_support);

    boolean accountConfigsMissing =
        getIntent().getBooleanExtra(ACCOUNT_CONFIGURATION_MISSING, false);

    if (accountConfigsMissing) {
      ((TextView) findViewById(R.id.decline_explanation))
          .setText(getString(R.string.adding_account_missing_config_title));
    }

    Button ok = findViewById(R.id.ok_btn);

    ok.setOnClickListener(v -> {

      Bundle bundle = new Bundle();
      bundle.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION);
      bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "User cannot add account");
      setAccountAuthenticatorResult(bundle);
      finish();
    });
  }

  private static Intent getDeclineIntent(
      Context context, AccountAuthenticatorResponse response, boolean accountConfigMissing) {
    Intent intent = new Intent(context, DeclineAddingAccountActivity.class);
    intent.putExtra(ACCOUNT_CONFIGURATION_MISSING, accountConfigMissing);
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
    return intent;
  }

  public static Intent getDeclineIntentDueToUserAdded(
      Context context, AccountAuthenticatorResponse response) {
    return getDeclineIntent(context, response, false);
  }

  public static Intent getDeclineIntentDueToConfigMissing(
      Context context, AccountAuthenticatorResponse response) {
    return getDeclineIntent(context, response, true);
  }
}

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

import android.util.Log;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import krb.javax.security.auth.callback.NameCallback;

/**
 * A handler to provide the username and the password to the Krb5LoginModule
 */
public class UsernamePasswordCallbackHandler implements CallbackHandler {
  private final String username;
  private final String password;

  UsernamePasswordCallbackHandler(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public void handle(Callback[] callbacks) {
    for (Callback callback : callbacks) {
      if (callback instanceof NameCallback) {
        ((NameCallback) callback).setName(username);
      } else if (callback instanceof PasswordCallback) {
        ((PasswordCallback) callback).setPassword(password.toCharArray());
      } else {
        Log.w(TAG, String.format("Unknown callback: %s", callback.getClass()));
      }
    }
  }
}

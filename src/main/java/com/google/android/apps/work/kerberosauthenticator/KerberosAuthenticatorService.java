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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Kerberos authenticator service.
 * Upon binding, creates a KerberosAuthenticator instance that interfaces with the AccountManager.
 *
 * Part of the requirement of being an authenticator is to have a service that, upon binding by
 * the AccountManager, returns the result of {@code getIBinder()} of an object that implements
 * the {@code AbstractAccountAuthenticator} class.
 */
public class KerberosAuthenticatorService extends Service {
  private KerberosAuthenticator authenticator;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    if (authenticator == null) {
      authenticator = new KerberosAuthenticator(this);
    }
    return authenticator.getIBinder();
  }
}

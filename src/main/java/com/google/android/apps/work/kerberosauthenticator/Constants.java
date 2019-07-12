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

import java.util.regex.Pattern;

/**
 * Constants common to all Kerberos authenticator classes.
 */
public class Constants {
  // =========== Constants for public use ===========
  // Account type - MUST match the value in res/xml/kerberos_authenticator.xml
  static final String KERBEROS_ACCOUNT_TYPE = "AndroidEnterpriseKerberos";
  static final String SPNEGO = "SPNEGO";

  // =========== Constants for internal use ===========
  // Only allow authentication requests from Chrome.
  static final String CHROME_PACKAGE_NAME = "com.android.chrome";
  // Key of the activity extra indicating whether the activity is allowed to add an account.
  static final String ADD_ACCOUNT = "AddAccount";
  // Key of the activity extra indicating the service name for which a service ticket should be
  // obtained.
  static final String SERVICE_NAME = "ServiceName";
  // Key for the servicet ticket information key-value store.
  static final String PREFERENCE_NAME = "service_ticket_info_storage";
  // Tag for logging
  public static final String TAG = "AFW_KerberosAuth";
  // Regex pattern to match the format of a service ticket type description.
  // Matches the formats word:word:...@serviceName.domain. ... .tld
  static final Pattern AUTH_TOKEN_PATTERN =
      Pattern.compile("([\\w-]+):([\\w-]+):([\\w-]+)@([\\w-]+)\\.(.)*");
}

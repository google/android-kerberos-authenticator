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

import android.os.Bundle;
import java.util.Base64;
import java.util.Date;

public class TestHelper {
  static final String TEST_USERNAME = "TEST_USERNAME";
  static final String TEST_PASSWORD = "TEST_PASSWORD";
  static final String TEST_AD_DOMAIN = "TEST_AD_DOMAIN";
  static final String TEST_AD_CONTROLLER = "TEST_AD_CONTROLLER";

  static final String USERNAME = "test_user";
  static final String PASSWORD = "test_password";
  static final String AD_DOMAIN = "test_domain.example.com";
  static final String AD_DC = "controller.test_domain.example.com";
  static final byte[] TGT = {'a', 'b', 'c'};
  static final String TGT_B64 = Base64.getEncoder().encodeToString(TGT);

  static final String TEST_SERVICE_NAME = "test_service_name";

  static final String B64_SUBJECT =
      "rO0ABXNyABtqYXZheC5zZWN1cml0eS5hdXRoLlN1YmplY3SMsjKTADP6aAMAAloACHJlYWRPbmx5TA"
      + "AKcHJpbmNpcGFsc3QAD0xqYXZhL3V0aWwvU2V0O3hwAHNyACVqYXZhLnV0aWwuQ29sbGVjdGlvbnMk"
      + "U3luY2hyb25pemVkU2V0BsPCeQLu3zwCAAB4cgAsamF2YS51dGlsLkNvbGxlY3Rpb25zJFN5bmNocm"
      + "9uaXplZENvbGxlY3Rpb24qYfhNCZyZtQMAAkwAAWN0ABZMamF2YS91dGlsL0NvbGxlY3Rpb247TAAF"
      + "bXV0ZXh0ABJMamF2YS9sYW5nL09iamVjdDt4cHNyACVqYXZheC5zZWN1cml0eS5hdXRoLlN1YmplY3"
      + "QkU2VjdXJlU2V0bcwygBdVficDAANJAAV3aGljaEwACGVsZW1lbnRzdAAWTGphdmEvdXRpbC9MaW5r"
      + "ZWRMaXN0O0wABnRoaXMkMHQAHUxqYXZheC9zZWN1cml0eS9hdXRoL1N1YmplY3Q7eHAAAAABc3IAFG"
      + "phdmEudXRpbC5MaW5rZWRMaXN0DClTXUpgiCIDAAB4cHcEAAAAAXNyADJrcmIuamF2YXguc2VjdXJp"
      + "dHkuYXV0aC5rZXJiZXJvcy5LZXJiZXJvc1ByaW5jaXBhbJmnfV0PHjMpAwAAeHB1cgACW0Ks8xf4Bg"
      + "hU4AIAAHhwAAAAEjAQoAMCAQGhCTAHGwVlcmFubXVxAH4AEAAAABIbEEFCQ0QuRVhBTVBMRS5DT014"
      + "eHEAfgACeHEAfgAHeHhzcQB+AANzcQB+AAgAAAADc3EAfgAMdwQAAAABc3IAL2tyYi5qYXZheC5zZW"
      + "N1cml0eS5hdXRoLmtlcmJlcm9zLktlcmJlcm9zVGlja2V0ZqGBbXB3w7sCAApbAAxhc24xRW5jb2Rp"
      + "bmd0AAJbQkwACGF1dGhUaW1ldAAQTGphdmEvdXRpbC9EYXRlO0wABmNsaWVudHQANExrcmIvamF2YX"
      + "gvc2VjdXJpdHkvYXV0aC9rZXJiZXJvcy9LZXJiZXJvc1ByaW5jaXBhbDtbAA9jbGllbnRBZGRyZXNz"
      + "ZXN0ABdbTGphdmEvbmV0L0luZXRBZGRyZXNzO0wAB2VuZFRpbWVxAH4AGFsABWZsYWdzdAACW1pMAA"
      + "lyZW5ld1RpbGxxAH4AGEwABnNlcnZlcnEAfgAZTAAKc2Vzc2lvbktleXQAKkxrcmIvamF2YXgvc2Vj"
      + "dXJpdHkvYXV0aC9rZXJiZXJvcy9LZXlJbXBsO0wACXN0YXJ0VGltZXEAfgAYeHB1cQB+ABAAAAQBYY"
      + "ID/TCCA/mgAwIBBaESGxBBQkNELkVYQU1QTEUuQ09NoiUwI6ADAgECoRwwGhsGa3JidGd0GxBBQkNE"
      + "LkVYQU1QTEUuQ09No4IDtTCCA7GgAwIBEqEDAgECooIDowSCA5/g1nhWhXUEBil0yJ0HcY+QgRX+St"
      + "00L74v8nod+PInn9wzQtRjb2MOISMbkEVWlRJIu6cdwaeskcUeP9QFZBlwyPOmhgcDBRgTo55wLHRR"
      + "BtuA8xQeUr1hOx/HXjA4AiYlJ/7ywrylnVy6fOTSEiwateQ6QWa9hTTYkZESBsQXQe/ib2SXb+Ve9m"
      + "OxzdKNP15GF1dilDXUgWCUlT13WX9qiLE+PMf96jCEkCF46a0YiacrA9i2MG3RckCf9jg8ohWVxDp9"
      + "hF2tsnSu+U8+emV1LQefm+f0s0GgyOQzSvtcd6OKs/UepGo7/gMLTLT7yclh7qaeInAkjxLP14epFc"
      + "oB9Lv2PXtOP5fwHppi1PtGGW3Aq/fDp8ALQKGPmP9QNH+K/TR+5LtbOnZjFuBqGFH5B7JdlMOqBh5B"
      + "CFPl7AhxekEvVHTqraSyussQnSLOvoqHXEEza75M+6qodJZRA/taCNWrRYJy+8c4FyfmlEc/mtQtvd"
      + "ckqYSnMFrdem4Fb6kEy/uIakd8VyB6Kyzv1HlUpe+tN7S5Sw8JB/5LU3jkGr6/KYNXArpX/tfnHqTE"
      + "sEtUXptO7axbXjXK0ONzpCz4wjAW9aPz+62z9+2naTxr0ZXdCvUAhL+ydaepMayuGTlmJmRhJwffOO"
      + "rPwaxOdvNaOkFbsIeJrZUVdgkCICk98Mp2aOGN/Pg1cIMC5l8WO90mttOmCnh65aBsMpa7XrkmIk6a"
      + "k/jJYpCqS0Ulo8LQ6hU+JLyK25muFPlqKM8thVbtjhvk41AxjRfcTGsoru/osvs+QGoI8te/EChK4X"
      + "27J9W6lCAso7GZl/k9B8zfv7UZPrtOlWJrw7dtkKMGRrqeumRSdHSAKfuH7QgLTZDiUb84fsni7fLu"
      + "Qzs6Tl+11sRsDrlUIdFY0HhBjtTUbcI7Zir3B4RUUiBsj0FXNmTfzLLsYnYnFfA8OhgOiGLLwZQhU1"
      + "oUh1MGgDwfntCOBfCvqB9h7jBIzeaGDCyQQPhOZHXwoq8e4cDdF1S532XZbvop0afdnAOdGehJkR93"
      + "SBFR0jEKElwoDCKdgctZFX5KwPAw674eUxjA81xDLi6daLYDfG5VLB4peXy52R2I29Dofh0RGq1EDy"
      + "q7ibsTssoG9kmzAHdmgq4U0vARwQf4hAN+BEYxNurJ0K2vVo9264F5SQZBVPm7dHcik+yjr403UsJw"
      + "jlBQBHgBZvb5842bKXw6e1g4MekSaqyR2H7yiohzcgAOamF2YS51dGlsLkRhdGVoaoEBS1l0GQMAAH"
      + "hwdwgAAAFp6ICSMHhzcQB+AA51cQB+ABAAAAASMBCgAwIBAaEJMAcbBWVyYW5tdXEAfgAQAAAAEhsQ"
      + "QUJDRC5FWEFNUExFLkNPTXhwc3EAfgAfdwgAAAH83j0fMHh1cgACW1pXjyA5FLhd4gIAAHhwAAAAIA"
      + "AAAAAAAAAAAAEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAcHNxAH4ADnVxAH4AEAAAACUwI6ADAgECoRww"
      + "GhsGa3JidGd0GxBBQkNELkVYQU1QTEUuQ09NdXEAfgAQAAAAEhsQQUJDRC5FWEFNUExFLkNPTXhzcg"
      + "Aoa3JiLmphdmF4LnNlY3VyaXR5LmF1dGgua2VyYmVyb3MuS2V5SW1wbJKDhug8r0vXAwAAeHB1cQB+"
      + "ABAAAAArMCmgAwIBEqEiBCCk3oQ+cqywuAJkoGX2oV6aqojXC6mZr6zMA5jItWNc+3hzcQB+AB93CA"
      + "AAAWnogJIweHhxAH4AAnhxAH4AE3hzcQB+AANzcQB+AAgAAAACc3EAfgAMdwQAAAAAeHEAfgACeHEA"
      + "fgAueA==";

  // Mon Apr 04 16:18:54 PDT 2039
  static final Date EXPECTED_DATE = new Date(2185571934000L);

  static Bundle makeRestrictionsBundle() {
    Bundle restrictionsBundle = new Bundle();
    restrictionsBundle.putString(AccountConfiguration.USERNAME_KEY, TEST_USERNAME);
    restrictionsBundle.putString(AccountConfiguration.PASSWORD_KEY, TEST_PASSWORD);
    restrictionsBundle.putString(AccountConfiguration.AD_DOMAIN_KEY, TEST_AD_DOMAIN);
    restrictionsBundle.putString(AccountConfiguration.AD_CONTROLLER_KEY, TEST_AD_CONTROLLER);
    return restrictionsBundle;
  }

  static KerberosAccount createKerberosAccount() {
    KerberosAccount account = new KerberosAccount(USERNAME, PASSWORD, AD_DOMAIN, AD_DC);
    account.setTicketGrantingTicket(TGT);
    return account;
  }
}

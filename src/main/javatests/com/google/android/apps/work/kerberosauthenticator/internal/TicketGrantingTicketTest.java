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
package com.google.android.apps.work.kerberosauthenticator.internal;

import static com.google.common.truth.Truth.assertThat;

import java.security.Principal;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import javax.security.auth.Subject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public final class TicketGrantingTicketTest {
  static final String B64_SUBJECT =
      "rO0ABXNyABtqYXZheC5zZWN1cml0eS5hdXRoLlN1YmplY3SMsjKTADP6aAMAAloACHJlYWRPbmx"
          + "5TAAKcHJpbmNpcGFsc3QAD0xqYXZhL3V0aWwvU2V0O3hwAHNyACVqYXZhLnV0aWwuQ29sbGVjdG"
          + "lvbnMkU3luY2hyb25pemVkU2V0BsPCeQLu3zwCAAB4cgAsamF2YS51dGlsLkNvbGxlY3Rpb25zJ"
          + "FN5bmNocm9uaXplZENvbGxlY3Rpb24qYfhNCZyZtQMAAkwAAWN0ABZMamF2YS91dGlsL0NvbGxl"
          + "Y3Rpb247TAAFbXV0ZXh0ABJMamF2YS9sYW5nL09iamVjdDt4cHNyACVqYXZheC5zZWN1cml0eS5"
          + "hdXRoLlN1YmplY3QkU2VjdXJlU2V0bcwygBdVficDAANJAAV3aGljaEwACGVsZW1lbnRzdAAWTG"
          + "phdmEvdXRpbC9MaW5rZWRMaXN0O0wABnRoaXMkMHQAHUxqYXZheC9zZWN1cml0eS9hdXRoL1N1Y"
          + "mplY3Q7eHAAAAABc3IAFGphdmEudXRpbC5MaW5rZWRMaXN0DClTXUpgiCIDAAB4cHcEAAAAAXNy"
          + "ADJrcmIuamF2YXguc2VjdXJpdHkuYXV0aC5rZXJiZXJvcy5LZXJiZXJvc1ByaW5jaXBhbJmnfV0"
          + "PHjMpAwAAeHB1cgACW0Ks8xf4BghU4AIAAHhwAAAAEjAQoAMCAQGhCTAHGwVlcmFubXVxAH4AEA"
          + "AAABIbEEFCQ0QuRVhBTVBMRS5DT014eHEAfgACeHEAfgAHeHhzcQB+AANzcQB+AAgAAAADc3EAf"
          + "gAMdwQAAAABc3IAL2tyYi5qYXZheC5zZWN1cml0eS5hdXRoLmtlcmJlcm9zLktlcmJlcm9zVGlj"
          + "a2V0ZqGBbXB3w7sCAApbAAxhc24xRW5jb2Rpbmd0AAJbQkwACGF1dGhUaW1ldAAQTGphdmEvdXR"
          + "pbC9EYXRlO0wABmNsaWVudHQANExrcmIvamF2YXgvc2VjdXJpdHkvYXV0aC9rZXJiZXJvcy9LZX"
          + "JiZXJvc1ByaW5jaXBhbDtbAA9jbGllbnRBZGRyZXNzZXN0ABdbTGphdmEvbmV0L0luZXRBZGRyZ"
          + "XNzO0wAB2VuZFRpbWVxAH4AGFsABWZsYWdzdAACW1pMAAlyZW5ld1RpbGxxAH4AGEwABnNlcnZl"
          + "cnEAfgAZTAAKc2Vzc2lvbktleXQAKkxrcmIvamF2YXgvc2VjdXJpdHkvYXV0aC9rZXJiZXJvcy9"
          + "LZXlJbXBsO0wACXN0YXJ0VGltZXEAfgAYeHB1cQB+ABAAAAQBYYID/TCCA/mgAwIBBaESGxBBQk"
          + "NELkVYQU1QTEUuQ09NoiUwI6ADAgECoRwwGhsGa3JidGd0GxBBQkNELkVYQU1QTEUuQ09No4IDt"
          + "TCCA7GgAwIBEqEDAgECooIDowSCA5/g1nhWhXUEBil0yJ0HcY+QgRX+St00L74v8nod+PInn9wz"
          + "QtRjb2MOISMbkEVWlRJIu6cdwaeskcUeP9QFZBlwyPOmhgcDBRgTo55wLHRRBtuA8xQeUr1hOx/"
          + "HXjA4AiYlJ/7ywrylnVy6fOTSEiwateQ6QWa9hTTYkZESBsQXQe/ib2SXb+Ve9mOxzdKNP15GF1"
          + "dilDXUgWCUlT13WX9qiLE+PMf96jCEkCF46a0YiacrA9i2MG3RckCf9jg8ohWVxDp9hF2tsnSu+"
          + "U8+emV1LQefm+f0s0GgyOQzSvtcd6OKs/UepGo7/gMLTLT7yclh7qaeInAkjxLP14epFcoB9Lv2"
          + "PXtOP5fwHppi1PtGGW3Aq/fDp8ALQKGPmP9QNH+K/TR+5LtbOnZjFuBqGFH5B7JdlMOqBh5BCFP"
          + "l7AhxekEvVHTqraSyussQnSLOvoqHXEEza75M+6qodJZRA/taCNWrRYJy+8c4FyfmlEc/mtQtvd"
          + "ckqYSnMFrdem4Fb6kEy/uIakd8VyB6Kyzv1HlUpe+tN7S5Sw8JB/5LU3jkGr6/KYNXArpX/tfnH"
          + "qTEsEtUXptO7axbXjXK0ONzpCz4wjAW9aPz+62z9+2naTxr0ZXdCvUAhL+ydaepMayuGTlmJmRh"
          + "JwffOOrPwaxOdvNaOkFbsIeJrZUVdgkCICk98Mp2aOGN/Pg1cIMC5l8WO90mttOmCnh65aBsMpa"
          + "7XrkmIk6ak/jJYpCqS0Ulo8LQ6hU+JLyK25muFPlqKM8thVbtjhvk41AxjRfcTGsoru/osvs+QG"
          + "oI8te/EChK4X27J9W6lCAso7GZl/k9B8zfv7UZPrtOlWJrw7dtkKMGRrqeumRSdHSAKfuH7QgLT"
          + "ZDiUb84fsni7fLuQzs6Tl+11sRsDrlUIdFY0HhBjtTUbcI7Zir3B4RUUiBsj0FXNmTfzLLsYnYn"
          + "FfA8OhgOiGLLwZQhU1oUh1MGgDwfntCOBfCvqB9h7jBIzeaGDCyQQPhOZHXwoq8e4cDdF1S532X"
          + "Zbvop0afdnAOdGehJkR93SBFR0jEKElwoDCKdgctZFX5KwPAw674eUxjA81xDLi6daLYDfG5VLB"
          + "4peXy52R2I29Dofh0RGq1EDyq7ibsTssoG9kmzAHdmgq4U0vARwQf4hAN+BEYxNurJ0K2vVo926"
          + "4F5SQZBVPm7dHcik+yjr403UsJwjlBQBHgBZvb5842bKXw6e1g4MekSaqyR2H7yiohzcgAOamF2"
          + "YS51dGlsLkRhdGVoaoEBS1l0GQMAAHhwdwgAAAFp6ICSMHhzcQB+AA51cQB+ABAAAAASMBCgAwI"
          + "BAaEJMAcbBWVyYW5tdXEAfgAQAAAAEhsQQUJDRC5FWEFNUExFLkNPTXhwc3EAfgAfdwgAAAFp6q"
          + "XjMHh1cgACW1pXjyA5FLhd4gIAAHhwAAAAIAAAAAAAAAAAAAEBAAAAAAAAAAAAAAAAAAAAAAAAA"
          + "AAAcHNxAH4ADnVxAH4AEAAAACUwI6ADAgECoRwwGhsGa3JidGd0GxBBQkNELkVYQU1QTEUuQ09N"
          + "dXEAfgAQAAAAEhsQQUJDRC5FWEFNUExFLkNPTXhzcgAoa3JiLmphdmF4LnNlY3VyaXR5LmF1dGg"
          + "ua2VyYmVyb3MuS2V5SW1wbJKDhug8r0vXAwAAeHB1cQB+ABAAAAArMCmgAwIBEqEiBCCk3oQ+cq"
          + "ywuAJkoGX2oV6aqojXC6mZr6zMA5jItWNc+3hzcQB+AB93CAAAAWnogJIweHhxAH4AAnhxAH4AE"
          + "3hzcQB+AANzcQB+AAgAAAACc3EAfgAMdwQAAAAAeHEAfgACeHEAfgAueA==";

  static final String B64_EMPTY_SUBJECT =
      "rO0ABXNyABtqYXZheC5zZWN1cml0eS5hdXRoLlN1YmplY3SMsjKTADP6aAMAAloACHJlYWRPbmx"
          + "5TAAKcHJpbmNpcGFsc3QAD0xqYXZhL3V0aWwvU2V0O3hwAHNyACVqYXZhLnV0aWwuQ29sbGVjdG"
          + "lvbnMkU3luY2hyb25pemVkU2V0BsPCeQLu3zwCAAB4cgAsamF2YS51dGlsLkNvbGxlY3Rpb25zJ"
          + "FN5bmNocm9uaXplZENvbGxlY3Rpb24qYfhNCZyZtQMAAkwAAWN0ABZMamF2YS91dGlsL0NvbGxl"
          + "Y3Rpb247TAAFbXV0ZXh0ABJMamF2YS9sYW5nL09iamVjdDt4cHNyACVqYXZheC5zZWN1cml0eS5"
          + "hdXRoLlN1YmplY3QkU2VjdXJlU2V0bcwygBdVficDAANJAAV3aGljaEwACGVsZW1lbnRzdAAWTG"
          + "phdmEvdXRpbC9MaW5rZWRMaXN0O0wABnRoaXMkMHQAHUxqYXZheC9zZWN1cml0eS9hdXRoL1N1Y"
          + "mplY3Q7eHAAAAABc3IAFGphdmEudXRpbC5MaW5rZWRMaXN0DClTXUpgiCIDAAB4cHcEAAAAAHhx"
          + "AH4AAnhxAH4AB3h4c3EAfgADc3EAfgAIAAAAA3NxAH4ADHcEAAAAAHhxAH4AAnhxAH4ADnhzcQB"
          + "+AANzcQB+AAgAAAACc3EAfgAMdwQAAAAAeHEAfgACeHEAfgAReA==";

  static final Date EXPECTED_DATE = new Date(1554419934000L);
  static final Date EXPECTED_AUTH_DATE = new Date(1554383934000L);

  byte[] encodedSubject = null;
  byte[] encodedEmptySubject = null;

  @Before
  public void setUp() {
    encodedSubject = Base64.getDecoder().decode(B64_SUBJECT);
    encodedEmptySubject = Base64.getDecoder().decode(B64_EMPTY_SUBJECT);
  }

  @Test
  public void testCreatingTgtFromSubject() {
    TicketGrantingTicket tgt = TicketGrantingTicket.fromSerializedSubject(encodedSubject);

    assertThat(tgt).isNotNull();
    Subject subject = tgt.asSubject();

    Set<Principal> principals = subject.getPrincipals();
    assertThat(principals).isNotNull();
    assertThat(principals).hasSize(1);
    Principal username = subject.getPrincipals().iterator().next();
    assertThat(username).isNotNull();
    assertThat(username.getName()).isEqualTo("eranm@ABCD.EXAMPLE.COM");

    assertThat(subject.getPrivateCredentials()).isNotEmpty();
    assertThat(subject.getPrivateCredentials().iterator().next()).isNotNull();
    assertThat(subject.getPublicCredentials()).isEmpty();
  }

  @Test
  public void testEncodingSubject() {
    TicketGrantingTicket tgt = TicketGrantingTicket.fromSerializedSubject(encodedSubject);
    // DER encoding must be identical
    assertThat(tgt.asSerialized()).isEqualTo(encodedSubject);
  }

  @Test
  public void testGettingExpiryDate() {
    TicketGrantingTicket tgt = TicketGrantingTicket.fromSerializedSubject(encodedSubject);
    Date expiryDate = tgt.getExpiryDate();
    assertThat(expiryDate).isNotNull();
    assertThat(expiryDate).isEqualTo(EXPECTED_DATE);
  }

  @Test
  public void testCreatingTgtFromEmptySubject() {
    TicketGrantingTicket tgt = TicketGrantingTicket.fromSerializedSubject(encodedEmptySubject);
    assertThat(tgt).isNotNull();
    assertThat(tgt.getExpiryDate()).isNull();
    assertThat(tgt.asSubject().getPrincipals()).isEmpty();
    assertThat(tgt.asSubject().getPrivateCredentials()).isEmpty();
    assertThat(tgt.asSubject().getPublicCredentials()).isEmpty();
  }

  @Test
  public void testGetIssuanceDate() {
    TicketGrantingTicket tgt = TicketGrantingTicket.fromSerializedSubject(encodedSubject);
    Date issuanceDate = tgt.getIssuanceDate();
    assertThat(issuanceDate).isNotNull();
    assertThat(issuanceDate).isEqualTo(EXPECTED_AUTH_DATE);
  }
}

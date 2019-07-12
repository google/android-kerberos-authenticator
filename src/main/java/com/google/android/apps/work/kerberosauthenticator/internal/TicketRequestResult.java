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

/**
 * The result from attempting to get a ticket (either a ticket-granting-ticket or a service
 * ticket).
 */
public class TicketRequestResult {
  /**
   * The result code of getting a ticket.
   */
  public enum ResultCode {
    SUCCESS,
    ERROR_BAD_PASSWORD,
    ERROR_LOGIN_FAILED,
    ERROR_COMMIT_FAILED,
    ERROR_GSS_FAILURE
  };

  private final ResultCode resultCode;
  private final String message;

  public TicketRequestResult(ResultCode resultCode, String message) {
    this.resultCode = resultCode;
    this.message = message;
  }

  public boolean successful() {
    return resultCode == ResultCode.SUCCESS;
  }

  public boolean isPasswordBad() {
    return resultCode == ResultCode.ERROR_BAD_PASSWORD;
  }

  @Override
  public String toString() {
    if (successful()) {
      return String.format("Success: %s", message);
    }

    return String.format("Failure (%s): %s", resultCode, message);
  }
}

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
package com.google.android.apps.work.kerberosauthenticator.internal.spnego;

import com.google.android.apps.work.kerberosauthenticator.internal.TicketRequestResult;

/**
 * Interface for reporting the result of getting a service ticket.
 */
public interface ServiceTicketResultListener {

  /**
   * Called when the attempt to get a service ticket for the {@code service} concluded.
   * @param service The name of the service for which the ticket was attempted.
   * @param result The result, including a human-readable status message.
   * @param serviceTicket The actual service ticket, base64-encoded without line wraps, for use
   * by the browser. If a service ticket could not be obtained, this would be null.
   */
  void onServiceTicketResult(String service, TicketRequestResult result, String serviceTicket);
}

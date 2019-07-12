# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
load("@build_bazel_rules_android//android:rules.bzl", "android_local_test")

EXTERNAL_DEPS = [
    "@com_google_guava_guava//jar",
    "@maven//:junit_junit",
    "@maven//:androidx_localbroadcastmanager_localbroadcastmanager",
    "@maven//:androidx_annotation_annotation",
    "@maven//:androidx_test_rules",
    "@maven//:androidx_test_espresso_espresso_core",
    "@maven//:androidx_test_runner",
    "@maven//:androidx_test_monitor",
    "@maven//:androidx_test_core",
    "@maven//:androidx_test_espresso_espresso_idling_resource",
    "@com_google_truth_truth//jar",
    "@robolectric//bazel:android-all",
    "@maven//:org_robolectric_robolectric",
    "@maven//:org_robolectric_shadows_framework",
    "@maven//:org_robolectric_annotations",
]

android_local_test(
    name = "BaseAuthenticatorActivityTest",
    size = "small",
    test_class = "com.google.android.apps.work.kerberosauthenticator.BaseAuthenticatorActivityTest",
    srcs = [
        "com/google/android/apps/work/kerberosauthenticator/BaseAuthenticatorActivityTest.java",
        "com/google/android/apps/work/kerberosauthenticator/TestHelper.java",
    ],
    manifest_values = {
        "minSdkVersion": "26",
        "targetSdkVersion": "27",
    },
    deps = EXTERNAL_DEPS + [
        "//src/main/java:lib",
    ],
)

android_local_test(
    name = "AccountConfigurationTest",
    size = "small",
    test_class = "com.google.android.apps.work.kerberosauthenticator.AccountConfigurationTest",
    srcs = [
        "com/google/android/apps/work/kerberosauthenticator/AccountConfigurationTest.java",
        "com/google/android/apps/work/kerberosauthenticator/TestHelper.java",
    ],
    manifest_values = {
        "minSdkVersion": "26",
        "targetSdkVersion": "27",
    },
    deps = EXTERNAL_DEPS + [
        "//src/main/java:lib",
    ],
)

android_local_test(
    name = "KerberosAccountTest",
    size = "small",
    test_class = "com.google.android.apps.work.kerberosauthenticator.KerberosAccountTest",
    srcs = [
        "com/google/android/apps/work/kerberosauthenticator/KerberosAccountTest.java",
        "com/google/android/apps/work/kerberosauthenticator/TestHelper.java",
    ],
    manifest_values = {
        "minSdkVersion": "26",
        "targetSdkVersion": "27",
    },
    deps = EXTERNAL_DEPS + [
        "//src/main/java:lib",
    ],
)

android_local_test(
    name = "KerberosAuthenticatorTest",
    size = "small",
    test_class = "com.google.android.apps.work.kerberosauthenticator.KerberosAuthenticatorTest",
    srcs = [
        "com/google/android/apps/work/kerberosauthenticator/KerberosAuthenticatorTest.java",
        "com/google/android/apps/work/kerberosauthenticator/TestHelper.java",
    ],
    manifest_values = {
        "minSdkVersion": "26",
        "targetSdkVersion": "27",
    },
    deps = EXTERNAL_DEPS + [
        "//src/main/java:lib",
    ],
)

android_local_test(
    name = "TicketGrantingTicketTest",
    size = "small",
    test_class = "com.google.android.apps.work.kerberosauthenticator.internal.TicketGrantingTicketTest",
    srcs = [
        "com/google/android/apps/work/kerberosauthenticator/internal/TicketGrantingTicketTest.java",
    ],
    manifest_values = {
        "minSdkVersion": "26",
        "targetSdkVersion": "27",
    },
    deps = EXTERNAL_DEPS + [
        "//src/main/java:lib",
    ],
)

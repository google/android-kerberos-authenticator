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
load("@build_bazel_rules_android//android:rules.bzl", "android_binary", "android_library")

licenses(["restricted"])

exports_files(["LICENSE"])

android_binary(
    name = "kerberosauthenticator",
    manifest = "AndroidManifest.xml",
    multidex = "native",
    deps = [
        ":lib",
    ],
)

android_library(
    name = "lib",
    srcs = glob([
        "com/google/android/apps/work/kerberosauthenticator/*.java",
        "com/google/android/apps/work/kerberosauthenticator/internal/*.java",
        "com/google/android/apps/work/kerberosauthenticator/internal/kinit/*.java",
        "com/google/android/apps/work/kerberosauthenticator/internal/spnego/*.java",
    ]),
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = ["//src/main/javatests:__pkg__"],
    deps = [
        "@androidsdk//com.android.support:appcompat-v7-25.3.1",
        "@com_google_guava_guava//jar",
        "@openjdk-kerberos//:openjdk_kerberos",
    ],
)

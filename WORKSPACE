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
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

http_archive(
    name = "build_bazel_rules_android",
    urls = ["https://github.com/bazelbuild/rules_android/archive/v0.1.1.zip"],
    sha256 = "cd06d15dd8bb59926e4d65f9003bfc20f9da4b2519985c27e190cddc8b7a7806",
    strip_prefix = "rules_android-0.1.1",
)

http_archive(
   name = "google_bazel_common",
   strip_prefix = "bazel-common-b3778739a9c67eaefe0725389f03cf821392ac67",
   sha256 = "4ae0fd0af627be9523a166b88d1298375335f418dcc13a82e9e77a0089a4d254",
   urls = ["https://github.com/google/bazel-common/archive/b3778739a9c67eaefe0725389f03cf821392ac67.zip"],
)

http_archive(
    name = "robolectric",
    urls = ["https://github.com/robolectric/robolectric-bazel/archive/4.1.tar.gz"],
    sha256 = "2ee850ca521288db72b0dedb9ecbda55b64d11c470435a882f8daf615091253d",
    strip_prefix = "robolectric-bazel-4.1",
)

git_repository(
  name = "openjdk-kerberos",
  remote = "https://github.com/google/openjdk-kerberos",
  commit = "ff3fae18565506dbdaf5e104cb61f7e8210c71c5",
  shallow_since = "1560940223 +0100",
)

maven_jar(
    name = "com_google_truth_truth",
    artifact = "com.google.truth:truth:0.45",
)

android_sdk_repository(
    name = "androidsdk",
    # Replace with path to Android SDK on your system.
    path = "/path/to/sdk",
    api_level = 27,
    build_tools_version = "27.0.3",
)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")
robolectric_repositories()

# The following dependencies were calculated from:
# generate_workspace --maven_project=/tmp --artifact=com.google.guava:guava:28.0-android --repositories=https://jcenter.bintray.com
load("//:generate_workspace.bzl", "generated_maven_jars")
generated_maven_jars()

# Maven repo for installing test support libraries
RULES_JVM_EXTERNAL_TAG = "2.2"
RULES_JVM_EXTERNAL_SHA = "f1203ce04e232ab6fdd81897cf0ff76f2c04c0741424d192f28e65ae752ce2d6"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

# Test support libraries
load("@rules_jvm_external//:defs.bzl", "maven_install")
maven_install(
    artifacts = [
        "junit:junit:4.12",
        "javax.inject:javax.inject:1",
        "org.hamcrest:java-hamcrest:2.0.0.0",
        "androidx.lifecycle:lifecycle-extensions:2.0.0",
        "androidx.test:core:1.1.0",
        "androidx.test.espresso:espresso-core:3.1.1",
        "androidx.test:rules:aar:1.1.1",
        "androidx.test:runner:aar:1.1.1",
        "org.robolectric:annotations:4.1",
        "org.robolectric:robolectric:4.1",
        "org.robolectric:shadows-framework:4.1",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)

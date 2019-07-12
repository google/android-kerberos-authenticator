"""
Copyright 2019 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

The following dependencies were calculated from:

generate_workspace --maven_project=/tmp --artifact=com.google.guava:guava:28.0-android
--repositories=https://jcenter.bintray.com
"""

def generated_maven_jars():
    """Maven artifacts for generated deps."""

    # com.google.guava:guava:bundle:28.0-android
    native.maven_jar(
        name = "com_google_code_findbugs_jsr305",
        artifact = "com.google.code.findbugs:jsr305:3.0.2",
        repository = "https://jcenter.bintray.com/",
        sha1 = "25ea2e8b0c338a877313bd4672d3fe056ea78f0d",
    )

    # com.google.guava:guava:bundle:28.0-android
    native.maven_jar(
        name = "org_checkerframework_checker_compat_qual",
        artifact = "org.checkerframework:checker-compat-qual:2.5.5",
        repository = "https://jcenter.bintray.com/",
        sha1 = "435dc33e3019c9f019e15f01aa111de9d6b2b79c",
    )

    # com.google.guava:guava:bundle:28.0-android
    native.maven_jar(
        name = "org_codehaus_mojo_animal_sniffer_annotations",
        artifact = "org.codehaus.mojo:animal-sniffer-annotations:1.17",
        repository = "https://jcenter.bintray.com/",
        sha1 = "f97ce6decaea32b36101e37979f8b647f00681fb",
    )

    # com.google.guava:guava:bundle:28.0-android
    native.maven_jar(
        name = "com_google_errorprone_error_prone_annotations",
        artifact = "com.google.errorprone:error_prone_annotations:2.3.2",
        repository = "https://jcenter.bintray.com/",
        sha1 = "d1a0c5032570e0f64be6b4d9c90cdeb103129029",
    )

    # com.google.guava:guava:bundle:28.0-android
    native.maven_jar(
        name = "com_google_guava_failureaccess",
        artifact = "com.google.guava:failureaccess:1.0.1",
        repository = "https://jcenter.bintray.com/",
        sha1 = "1dcf1de382a0bf95a3d8b0849546c88bac1292c9",
    )

    native.maven_jar(
        name = "com_google_guava_guava",
        artifact = "com.google.guava:guava:28.0-android",
        repository = "https://jcenter.bintray.com/",
        sha1 = "3679f8e9a5e0544c361a3225a3e7bbd5febe5f29",
    )

    # com.google.guava:guava:bundle:28.0-android
    native.maven_jar(
        name = "com_google_guava_listenablefuture",
        artifact = "com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava",
        repository = "https://jcenter.bintray.com/",
        sha1 = "b421526c5f297295adef1c886e5246c39d4ac629",
    )

    # com.google.guava:guava:bundle:28.0-android
    native.maven_jar(
        name = "com_google_j2objc_j2objc_annotations",
        artifact = "com.google.j2objc:j2objc-annotations:1.3",
        repository = "https://jcenter.bintray.com/",
        sha1 = "ba035118bc8bac37d7eff77700720999acd9986d",
    )

def generated_java_libraries():
    """Jars for generated deps."""
    native.java_library(
        name = "com_google_code_findbugs_jsr305",
        visibility = ["//visibility:public"],
        exports = ["@com_google_code_findbugs_jsr305//jar"],
    )

    native.java_library(
        name = "org_checkerframework_checker_compat_qual",
        visibility = ["//visibility:public"],
        exports = ["@org_checkerframework_checker_compat_qual//jar"],
    )

    native.java_library(
        name = "org_codehaus_mojo_animal_sniffer_annotations",
        visibility = ["//visibility:public"],
        exports = ["@org_codehaus_mojo_animal_sniffer_annotations//jar"],
    )

    native.java_library(
        name = "com_google_errorprone_error_prone_annotations",
        visibility = ["//visibility:public"],
        exports = ["@com_google_errorprone_error_prone_annotations//jar"],
    )

    native.java_library(
        name = "com_google_guava_failureaccess",
        visibility = ["//visibility:public"],
        exports = ["@com_google_guava_failureaccess//jar"],
    )

    native.java_library(
        name = "com_google_guava_guava",
        visibility = ["//visibility:public"],
        exports = ["@com_google_guava_guava//jar"],
        runtime_deps = [
            ":com_google_code_findbugs_jsr305",
            ":com_google_errorprone_error_prone_annotations",
            ":com_google_guava_failureaccess",
            ":com_google_guava_listenablefuture",
            ":com_google_j2objc_j2objc_annotations",
            ":org_checkerframework_checker_compat_qual",
            ":org_codehaus_mojo_animal_sniffer_annotations",
        ],
    )

    native.java_library(
        name = "com_google_guava_listenablefuture",
        visibility = ["//visibility:public"],
        exports = ["@com_google_guava_listenablefuture//jar"],
    )

    native.java_library(
        name = "com_google_j2objc_j2objc_annotations",
        visibility = ["//visibility:public"],
        exports = ["@com_google_j2objc_j2objc_annotations//jar"],
    )

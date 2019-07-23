# Kerberos Authenticator

A stand-alone app that enables Android apps such as Google Chrome to obtain the
necessary credentials for accessing web services that require Kerberos SPNEGO
authentication.

## NOTE

This is not an officially supported Google product. It is provided as a
reference implementation of a Kerberos authentication solution on Android. 
This app can only be configured in conjuction with
[managed configurations](https://developer.android.com/work/managed-configurations).

## About

The code in this package is an Android app that uses the
[OpenJDK Kerberos](https://github.com/google/openjdk-kerberos) library to
provide SPNEGO authentication for Chrome.

## Building

A .apk file suitable to be used for Kerberos authentication can be built using
[Bazel](https://docs.bazel.build/versions/master/bazel-overview.html) and then
installed on an Android device.

## Using

The pre-compiled app from this repository is available for download at
https://android-kerberos-authenticator.appspot.com. In order to use this app,
you need to provide the following managed configurations:

*   username (compulsory).
*   password (optional): if the password is not provided, the user will be
    prompted to enter it upon authentication.
*   adDomain: the domain name that the user belogs to.
*   adController: the domain name for the Active Directory Domain Controller
*   sensitiveDebugData (optional): set to true to see sensitive debug data such
    as the raw tickets and password.

You may also want to configure Chrome to allow it to talk to the Authenticator:

*   [AuthServerWhitelist](http://dev.chromium.org/administrators/policy-list-3#AuthServerWhitelist)
    should be set to a comma-separated list of FQDNs of the web servers that
    will be accessed.
*   [AuthAndroidNegotiateAccountType](http://dev.chromium.org/administrators/policy-list-3#AuthAndroidNegotiateAccountType)
    must be set to "AndroidEnterpriseKerberos".

### Ticket storage

Upon obtaining a Ticket-Granting-Ticket for authentication, this will be stored
securely in
[Account Manager](https://developer.android.com/reference/android/accounts/AccountManager)
and re-used whenever requested through Chrome authentication. If this ticket
expires and a valid ticket is needed, it will automatically get renewed and
stored again in the Account Manager. Some debug service ticket information will
be stored securely in a file.

## Participating

There is a public mailing list for discussing Kerberos authentication in
Android in general, asking questions on the codebase or suggesting improvements:
[https://groups.google.com/forum/#!forum/android-kerberos-authenticator](https://groups.google.com/forum/#!forum/android-kerberos-authenticator)
It is suggested to direct technical questions first to this group, and open
a GitHub issue after there is agreement in the group that the issue is
one that should be addressed in the codebase.

## Deployment

This section provides a method for building your own apk from the existing code
that will be ready to be deployed. It does not cover custom development or
specific integrated development environment setup.

We will walk you through the following steps:

*   Downloading the source code
*   Choosing a new applicationId
*   Compiling the application
*   Signing the new APK

Note: The examples below illustrating the applicationId update process are taken
from an Ubuntu Linux desktop. Please consult the documentation for each tool
used for instructions specific to your chosen operating system.

#### Pre-requisites

You will need to install a minimal set of development tools in order to get
started

*   Git - A version control system used to download the source code.
    Installation instructions are here:
    https://git-scm.com/book/en/v2/Getting-Started-Installing-Git
*   Android SDK Platform Tools - Command line tools used to compile the
    authenticator: https://developer.android.com/studio/releases/platform-tools
*   Bazel - A build and test tool used on the authenticator project:
    https://docs.bazel.build/versions/master/install.html

The Bazel website has an optional tutorial covering steps for building an
Android app which might be useful:
https://docs.bazel.build/versions/master/tutorial/android-app.html

Once all the above tools are installed, the ANDROID_HOME environment variable
should be set to the path that the Android SDK is installed. Adjust for your
installation if it is different:

```shell
export ANDROID_HOME=$HOME/Android/Sdk/
```

Use the
[sdkmanager](https://developer.android.com/studio/command-line/sdkmanager) to
download the Android platform. We’re using version 27:

```shell
$ANDROID_HOME/tools/bin/sdkmanager "platforms;android-27"
```

#### Download the source code

We now need the source code. Use git to download the latest copy:

```shell
git clone https://github.com/google/android-kerberos-authenticator/
```

Browse to the newly created directory called “kerberosauthenticator”. There are
a few parameters that need to be set in the WORKSPACE file for the Bazel tool.

*   Open the WORKSPACE file for editing and search for the section named
    android_sdk_repository.
*   Set api_level to 27
*   Set build_tools_version to the current version installed on the system
*   There may be a parameter labeled path. You can add the location of the SDK
    which should be the same as the $ANDROID_HOME environment variable but the
    line can safely be deleted

```shell
android_sdk_repository(
    name = "androidsdk",
    api_level = 27,
    build_tools_version = "28.0.3.",
)
```

You can get your current build tools number by using the
[sdkmanager](https://developer.android.com/studio/command-line/sdkmanager)
command:

```shell
$ANDROID_HOME/tools/bin/sdkmanager --list
```

#### Choose a new applicationId

We are now ready to assign a new applicationId.

Open the authenticator BUILD file for editing . It should be located at
`src/main/java/BUILD` from the kerberosauthenticator directory. Look for the
section called android_library location and edit the applicationId value.

There are some limitations on the format of the applicationId. Best practices
for choosing and setting one can be found here:
https://developer.android.com/studio/build/application-id

Choose an id that is unique and descriptive. Once your version of the
authenticator is published, you should not change the id again.

#### Compile the application

We are now ready to compile the authenticator with the updated applicationId.
Browse to the kerberosauthenticator directory and run the bazel command:

```shell
bazel -c opt build //src/main/java:kerberosauthenticator
```

The "-c opt" argument compiles a version with debugging information stripped out
of the generated APK. If you would like to install on a connected device with
debugging enabled or a virtual device, use this command instead:

```shell
bazel mobile-install //src/main/java:kerberosauthenticator
```

The files generated by these commands are located in `bazel-bin/src/main/java`.

#### Sign the new APK

To verify the authenticity of the app, you can sign it with your own private
key. Subsequent updates need to be signed with the same key. You can generate a
private key with keytool:

```shell
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048
-validity 10000 -alias my-alias
```

The example above prompts you for a keystore and key password, and for the
"Distinguished Name" fields for your key. It then generates the keystore as a
file called my-release-key.jks, saving it in the current directory. The keystore
contains a single key that is valid for 10,000 days.

Signing the authenticator app requires zipalign and apksigner tools:

*   Align the unsigned APK using zipalign

```shell
zipalign -v -p 4 ./bazel-bin/src/main/java/kerberosauthenticator_unsigned.apk
kerberosauthenticator_unsigned.apk-aligned.apk
```

*   Sign your APK with your private key using apksigner:

```shell
apksigner sign --ks my-release-key.jks --out kerberosauthenticator.apk
kerberosauthenticator_unsigned.apk-aligned.apk
```

zipalign ensures that all uncompressed data starts with a particular byte
alignment relative to the start of the file, which may reduce the amount of RAM
consumed by the app.

This example generates a signed APK called kerberosauthenticator.apk after
signing it with a private key and certificate that are stored in a single
KeyStore file: my-release-key.jks.

Details and instructions for building and signing apps can be found here:
[Build your app from the command line](https://developer.android.com/studio/build/building-cmdline).

## Contributing

The [CONTRIBUTING.md](CONTRIBUTING.md) file contains instructions on how to
submit the Contributor License Agreement before sending any pull requests (PRs).
Of course, if you're new to the project, it's usually best to discuss any
proposals and reach consensus before sending your first PR.

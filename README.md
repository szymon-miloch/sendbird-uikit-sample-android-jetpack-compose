# Issue Report - NetworkCallback

## Summary
After integrating Sendbird into our application, we have observed consistent crash reports in Crashlytics with the following stacktrace:

FATAL EXCEPTION: WM.task-1
Process: dev.bright.tms.driver.dev, PID: 19064
android.net.ConnectivityManager$TooManyRequestsException
at android.net.ConnectivityManager.convertServiceException(ConnectivityManager.java:4468)
at android.net.ConnectivityManager.sendRequestForNetwork(ConnectivityManager.java:4753)
at android.net.ConnectivityManager.registerDefaultNetworkCallbackForUid(ConnectivityManager.java:5433)
at android.net.ConnectivityManager.registerDefaultNetworkCallback(ConnectivityManager.java:5400)
at android.net.ConnectivityManager.registerDefaultNetworkCallback(ConnectivityManager.java:5374)
at androidx.work.impl.utils.NetworkApi24.registerDefaultNetworkCallbackCompat(NetworkApi24.kt:28)
at androidx.work.impl.constraints.trackers.NetworkStateTracker24.startTracking(NetworkStateTracker.kt:138)
at androidx.work.impl.constraints.trackers.ConstraintTracker.addListener(ConstraintTracker.kt:56)
at androidx.work.impl.constraints.controllers.ConstraintController$track$1.invokeSuspend(ContraintControllers.kt:54)
at androidx.work.impl.constraints.controllers.ConstraintController$track$1.invoke(Unknown Source:8)
at androidx.work.impl.constraints.controllers.ConstraintController$track$1.invoke(Unknown Source:4)
at kotlinx.coroutines.flow.ChannelFlowBuilder.collectTo$suspendImpl(Builders.kt:316)
at kotlinx.coroutines.flow.ChannelFlowBuilder.collectTo(Unknown Source:0)
at kotlinx.coroutines.flow.CallbackFlowBuilder.collectTo(Builders.kt:330)
at kotlinx.coroutines.flow.internal.ChannelFlow$collectToFun$1.invokeSuspend(ChannelFlow.kt:56)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:101)
at androidx.work.impl.utils.SerialExecutorImpl$Task.run(SerialExecutorImpl.java:96)
at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:644)
at java.lang.Thread.run(Thread.java:1012)
Suppressed: kotlinx.coroutines.internal.DiagnosticCoroutineContextException: [StandaloneCoroutine{Cancelling}@cc84c7d, androidx.work.impl.utils.SerialExecutorImpl@e44d772]

## Root Cause Analysis
Our investigation has identified that the **direct** cause of this issue is WorkManager attempting to register a
ConnectivityManager.NetworkCallback for monitoring network constraints when scheduling a WorkRequest. The crash occurs
because the system has already reached the maximum allowed number of NetworkCallback registrations.

Android enforces a limit of 100 ConnectivityManager.NetworkCallback registrations per application.
[See requestNetwork JavaDoc documentation for details on this limitation](https://android.googlesource.com/platform//frameworks/base/%2B/c51dc08958b203619c48b8631290e7f5772df348/packages/Connectivity/framework/src/android/net/ConnectivityManager.java?utm_source=chatgpt.com).

## Reproduction Steps
Further investigation revealed that the following sequence of actions with Sendbird Chat triggers excessive NetworkCallback registrations:

1. Turn off network connectivity (both WiFi and cellular) on the device
2. Navigate to Sendbird Chat and attempt to send image messages
3. Sendbird queues these image messages due to the lack of network connectivity

## Technical Details
Our testing indicates that approximately 10 queued image messages can generate around 40+ NetworkCallback registrations,
though this number varies. Even subsequent text message attempts increase the number of registered callbacks.

Analysis using Android Profiler's heap dump functionality confirms that Sendbird appears to be registering NetworkCallbacks
with exception handling. When the number of registered callbacks exceeds 100, any subsequent attempt to register a
NetworkCallback (such as from WorkManager) results in the TooManyRequestsException shown in the stacktrace above.

## System Behavior
The sequence of events leading to the crash can be summarized as follows:

1. The application registers 1-3 network callbacks during startup
2. Sendbird registers multiple callbacks (with try-catch exception handling) when queuing messages during network unavailability
3. When the application subsequently schedules a WorkRequest, WorkManager attempts to register its own NetworkCallback
4. This registration fails with TooManyRequestsException because the system-wide limit of 100 has been exceeded

Either the MessageSyncManagerImpl component or Coil usage from Sendbird appears to be involved in registering additional
callbacks (according to Android Profiler tool analysis).

In this Fork - there is a floating button added to register additional callback during runtime. Feel
free to use it to reproduce this issue after registration of 100+ NetworkCallbacks.

## Recommendation
We recommend that this issue be addressed by the Sendbird team, as the current implementation of network callback
registration in the Sendbird SDK can lead to system resource exhaustion and application crashes.

# Sendbird UIKit Sample for Android Jetpack Compose

Sendbird UIKit for Android Jetpack Compose is a development kit with a declarative user interface that enables an easy and fast integration of standard chat features into new or existing client apps. This repository hosts the sample app that demonstrates the usage of Sendbird UIKit for Android Jetpack Compose.

You can find out more about Sendbird UIKit for Android Jetpack Compose at [UIKit for Android Jetpack Compose doc](https://sendbird.com/docs/uikit/v3/jetpack-compose/overview). If you need any help in resolving any issues or have questions, visit [our community](https://community.sendbird.com).

## Screenshots

<img src="screenshots/screenshots.jpeg" alt="Screenshot">

## How to use the sample app

To open the sample in Android Studio, simply clone the repository.

```
git clone git@github.com:sendbird/sendbird-uikit-sample-android-jetpack-compose.git
```

Then, check out from the main branch and open the `uikit-compose-sample/` directory in Android Studio.

### Try the sample app with your data

If you would like to try the sample app specifically fit to your usage, you can do so by replacing the default sample app ID with yours, which you can obtain by [creating your Sendbird application from the dashboard](https://sendbird.com/docs/chat/v4/android/quickstart/send-first-message#3-install-and-configure-the-chat-sdk-4-step-1-create-a-sendbird-application-from-your-dashboard). Furthermore, you could also add data of your choice on the dashboard to test. This will allow you to experience the sample app with data from your Sendbird application.

## License

```
MIT License

Copyright (c) 2021 Sendbird

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

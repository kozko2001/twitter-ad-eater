# Twitter Ad Eater

Twitter Ad Eater is a module for the Xposed/EdXPosed/[LSPosed](https://github.com/LSPosed/LSPosed) framework that attempts to remove ads from the Twitter Android application.

# Installation

1. Download the module from the [Releases](https://github.com/kozko2001/twitter-ad-eater/releases) page.
2. Install the APK.
3. Open the LSPosed app.
4. Enable the module and whitelist the Twitter app on the module page.
5. Reboot the device.

# Background

I just forked the  [Twitter Ad Eater](https://github.com/ppawel/twitter-ad-eater), to try a different
approach, instead on hook in the `addView` try to get the request that contains the tweets and
remove the promotions.

I did not test this properly, more than in my phone. But it works! Yeeey!

Also, a lot of code is copied from somebody else... https://github.com/shuwenyouxi/XposeDemo/blob/master/app/src/main/java/com/dsw/xposeddemo/hook/ModifyOkHttpRequestHook.kt
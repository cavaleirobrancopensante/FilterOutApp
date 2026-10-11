# FilterOut

Add banned words to your phone. If one appears on screen — on a web page, in an app, inside a picture, or because you typed it — the screen is covered so you can think twice.

## How it works

- **Reads the text on screen** through Android's Accessibility service: web pages, apps, search boxes, what you type.
- **Reads text inside images and video frames** (optional, Android 11+) with on-device text recognition (Google ML Kit, model bundled inside the app).
- When a banned word is found, a full-screen cover appears. After a 3-second pause you can tap **Leave**, which sends you to the home screen.
- FilterOut's own screen is never blocked, so you can edit your list.

## Privacy: no internet, at all

The app has **no INTERNET permission**, so Android itself prevents it from sending or receiving anything. The text-recognition library asks for that permission by default; the manifest strips it out, and the automatic build checks the finished APK and refuses to publish it if the permission ever comes back. Your word list is stored privately on the phone and excluded from backups.

## Banned words

| You add  | It blocks                                   |
|----------|---------------------------------------------|
| `sex`    | "sex" as a whole word — not "Essex"          |
| `porn*`  | words starting with it — "pornography"       |
| `*porn*` | the letters anywhere — "xxpornxx"            |
| `only fans` | the phrase, including "only-fans"         |

Accents and look-alike characters are handled automatically: `porno` catches "pornô", `porn` catches "p0rn", `sex` catches "$EX".

## Install

1. Open the **Actions** tab of this repository, tap the latest successful **Build APK** run, and download **FilterOut-apk** (a zip containing `FilterOut.apk`).
2. Open `FilterOut.apk` on the phone and allow installing from that source.
3. Open FilterOut, add your words, tap **Turn on**, find FilterOut in Accessibility settings and switch it on.

**If the switch is greyed out** (Android 13+ "restricted setting"): go to *Settings → Apps → FilterOut*, tap the **⋮** menu at the top right, choose **Allow restricted settings**, then try again.

**If it stops working after a while**, the phone may be killing it to save battery: *Settings → Apps → FilterOut → Battery → Unrestricted*.

Every new build is signed with the same key (`debug.keystore` in this repo), so new versions install over old ones without losing your list.

## Build it yourself

```
./gradlew assembleRelease
```

The APK is at `app/build/outputs/apk/release/app-release.apk`.

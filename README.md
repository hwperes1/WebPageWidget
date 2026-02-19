# Web Page Widget

An Android home screen widget that displays a screenshot of any website, refreshed every hour.

## How to get the APK (3 steps, ~10 minutes)

### Step 1 — Create a GitHub repository

1. Go to [github.com](https://github.com) and sign in (or create a free account)
2. Click the **+** button (top right) → **New repository**
3. Name it `WebPageWidget`, set it to **Public**, click **Create repository**

### Step 2 — Upload all these files

1. In your new repo, click **uploading an existing file** (or drag-and-drop)
2. Upload **all files and folders** from this `WebPageWidget` folder on your computer
   - Make sure the `.github` folder is included (it may be hidden — in Windows Explorer, enable "Show hidden items")
3. Click **Commit changes**

GitHub Actions will automatically start building the APK (you can watch it under the **Actions** tab).

### Step 3 — Download the APK

1. Go to the **Actions** tab in your GitHub repo
2. Click the latest workflow run (green checkmark = done, ~5 min)
3. Scroll down to **Artifacts** → click **WebPageWidget-debug** to download a ZIP
4. Unzip it — inside is `app-debug.apk`

### Install on your phone

1. Transfer the APK to your phone (email, Google Drive, USB, etc.)
2. On your phone: **Settings → Apps → Special app access → Install unknown apps**
   Enable it for whichever app you'll use to open the APK (Files, Chrome, etc.)
3. Tap the APK file and install

### Using the widget

1. Open the **Web Page Widget** app and enter your URL → tap **Save & Refresh Widget**
2. Long-press your home screen → **Widgets** → find **Web Page Widget** → drag it onto your screen
3. The widget will show a screenshot of the site and refresh it every hour automatically
4. Tap the widget anytime to go back to settings or change the URL

## Notes

- The widget shows a static screenshot (not a live interactive page) — this is an Android limitation for home screen widgets
- First screenshot may take 30–60 seconds after saving the URL
- HTTP sites (non-HTTPS) are supported
- Widget is resizable on the home screen

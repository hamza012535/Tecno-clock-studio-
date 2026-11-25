# Publishing & Release Guide (GitHub Releases + Komi Store)

This guide walks maintainers through setting up automated signed APK releases and publishing **Tecno Clock Studio** to **Komi Store**.

---

## 1. Initial Setup: Signing Keystore & GitHub Secrets

> **Security Warning:** **NEVER commit your `.jks` or `.keystore` file to the Git repository.** Always store it locally in a secure password manager / vault and upload it only as an encrypted GitHub Secret.

### Generate a Release Keystore
Run `keytool` locally (included with JDK 17):

```bash
keytool -genkeypair -v \
  -keystore tecno-clock-studio-upload.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### Base64-Encode the Keystore
- **macOS:**
  ```bash
  base64 -i tecno-clock-studio-upload.jks | tr -d '\n' | pbcopy
  ```
- **Linux:**
  ```bash
  base64 -w 0 tecno-clock-studio-upload.jks
  ```
- **Windows (PowerShell):**
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("tecno-clock-studio-upload.jks")) | Set-Clipboard
  ```

### Add GitHub Secrets
In your GitHub repository, go to **Settings -> Secrets and variables -> Actions** and add:
1. `ANDROID_KEYSTORE_FILE`: Paste the single-line Base64 output.
2. `ANDROID_KEYSTORE_PASSWORD`: Your keystore password.
3. `ANDROID_KEY_ALIAS`: `upload` (or the alias you chose).
4. `ANDROID_KEY_PASSWORD`: Your key password.

---

## 2. Automatic Version Code Management

`app/build.gradle.kts` reads `VERSION_CODE` and `VERSION_NAME` from environment variables injected by `.github/workflows/android-release.yml`.
When you push a semantic version tag (for example `v1.2.3`):
- `VERSION_NAME` becomes `1.2.3`
- `VERSION_CODE` becomes `10203` (`MAJOR * 10000 + MINOR * 100 + PATCH`)

This guarantees monotonically increasing `versionCode` values across releases and prevents update conflicts on Komi Store.

---

## 3. Release Process

You can publish a public non-draft release in either of two ways:

### Option A: Push a Version Tag
1. Ensure `main` is up to date and tested.
2. Create and push a semantic version tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

### Option B: One-Click Manual Release in GitHub UI
1. Open your repository on GitHub and go to **Actions -> Android Build & Komi Store Release**.
2. Click **Run workflow**, leave **Publish a public non-draft GitHub Release** checked, enter `v1.0.0`, and click **Run workflow**.

In both cases:
- The `build` job compiles the APK (signed release APK when `ANDROID_KEYSTORE_FILE` secrets are configured).
- The `release` job creates a **published, non-draft** GitHub Release with auto-generated release notes and attaches the `.apk`.

---

## 4. Komi Store Verification

Komi Store automatically discovers apps through GitHub's public Search API when all four conditions are met:
1. **Public Repository**: Confirm the GitHub repository visibility is **Public**.
2. **Relevant Topics**: Add `android`, `lock-screen`, `clock`, `tecno`, `hios`, `komi-store` under **About -> Topics**.
3. **Non-Draft Release**: Open the **Releases** tab and confirm the release does **not** have a `Draft` badge.
4. **APK Asset Attached**: Confirm `tecno-clock-studio-v1.0.0-release.apk` is listed under **Assets**.

---

## 5. Manual Komi CLI Usage (Optional)

If you prefer registering or updating a release manually from your local machine:

```bash
# Authenticate using a GitHub token with public_repo scope
export GITHUB_TOKEN="ghp_your_token_here"

# Publish / sync the latest tagged release from the current git repository
komi publish --tag v1.0.0

# Or verify metadata and release assets before publishing
komi verify
```

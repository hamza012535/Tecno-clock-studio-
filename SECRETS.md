# Required GitHub Repository Secrets

Configure these secrets under **Settings -> Secrets and variables -> Actions -> New repository secret** in your GitHub repository before pushing a release tag (`v*`):

| Secret Name | Required | Description |
| :--- | :---: | :--- |
| `ANDROID_KEYSTORE_FILE` | Yes (for Release) | Base64-encoded contents of your `.jks` / `.keystore` signing file (single line, no wrapping). |
| `ANDROID_KEYSTORE_PASSWORD` | Yes (for Release) | Password protecting the keystore file (`-storepass`). |
| `ANDROID_KEY_ALIAS` | Yes (for Release) | Key alias inside the keystore (recommended: `upload`). |
| `ANDROID_KEY_PASSWORD` | Yes (for Release) | Password protecting the private key alias (`-keypass`). |
| `GITHUB_TOKEN` | Automatic | Provided automatically by GitHub Actions with `contents: write` permission to publish Releases and register with Komi Store. |

> **Note:** On pushes to `main`/`master` without signing secrets configured, the workflow falls back cleanly to building a debug snapshot artifact so CI still passes while keeping your signing keys private.

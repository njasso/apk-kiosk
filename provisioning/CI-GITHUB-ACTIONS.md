# Construire l'APK via GitHub Actions (sans rien installer localement)

Le workflow `.github/workflows/build-apk.yml` compile l'APK sur les serveurs
GitHub (JDK 17 + SDK Android déjà présents) et le publie en artefact.

## 1. Pousser le projet
Place le dossier `apk-kiosk/` (avec `.github/`) dans un dépôt GitHub, puis
`git push`. Le build se lance seul (ou via l'onglet **Actions → Run workflow**).

## 2. Récupérer l'APK
Onglet **Actions → dernier run → Artifacts → `na2quiz-kiosk-apk`**. Décompresse :
tu obtiens `app-release.apk`.

- **Sans secrets configurés** : l'APK est signé en *debug* → installable pour un
  test pilote, mais PAS pour une distribution officielle.
- **Avec secrets** (ci-dessous) : l'APK est signé *release*, prêt pour le parc.

## 3. Activer la signature release (recommandé)
Génère le keystore une fois (voir `SIGNATURE.md`), puis encode-le :
```
base64 -w0 keystore.jks > keystore.b64
```
Dans le dépôt GitHub → **Settings → Secrets and variables → Actions → New secret**,
crée :

| Secret | Valeur |
|--------|--------|
| `KEYSTORE_B64` | contenu de `keystore.b64` |
| `KEYSTORE_PASSWORD` | mot de passe du keystore |
| `KEY_ALIAS` | `na2quiz` (ou ton alias) |
| `KEY_PASSWORD` | mot de passe de la clé |

Au prochain push, l'APK sera signé release automatiquement.

⚠️ Ne commite JAMAIS `keystore.jks` ni `keystore.b64` (déjà couverts par
`.gitignore`). Sauvegarde le keystore hors dépôt : sa perte empêche toute
future mise à jour de l'app.

# Générer la clé et signer l'APK

## 1. Créer un keystore (une seule fois, à CONSERVER précieusement)
```
keytool -genkeypair -v -keystore keystore.jks -keyalg RSA -keysize 4096 \
  -validity 10000 -alias na2quiz
```
⚠️ Sauvegardez `keystore.jks` hors du dépôt Git. Sa perte = impossibilité de
publier une mise à jour de l'app.

## 2. Exposer les secrets à Gradle (jamais commités)
```
export KIOSK_KEYSTORE=/chemin/keystore.jks
export KIOSK_STORE_PASSWORD='...'
export KIOSK_KEY_ALIAS=na2quiz
export KIOSK_KEY_PASSWORD='...'
```

## 3. Construire l'APK release signé
```
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

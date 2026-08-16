# Mise en Device Owner (kiosque non contournable)

Le Lock Task Mode devient **non désactivable par l'élève** uniquement si l'app
est **Device Owner**. À faire sur chaque tablette, une fois, sur un appareil
**neuf ou réinitialisé** (aucun compte Google configuré).

## Méthode ADB (recommandée pour un parc maîtrisé)

1. Réinitialiser la tablette (aucun compte ajouté au premier démarrage).
2. Activer le débogage USB (Options développeur).
3. Installer l'APK signé :
   ```
   adb install app-release.apk
   ```
4. Promouvoir l'app en Device Owner :
   ```
   adb shell dpm set-device-owner uk.na2quiz.kiosk/.KioskDeviceAdminReceiver
   ```
5. Vérifier :
   ```
   adb shell dumpsys device_policy | grep -i "device owner"
   ```

À partir de là, l'épinglage démarre seul, l'élève ne peut ni quitter l'app
ni la désinstaller. Pour retirer le mode (maintenance) :
```
adb shell dpm remove-active-admin uk.na2quiz.kiosk/.KioskDeviceAdminReceiver
```

## Méthode QR / NFC (parc plus large)
Pour déployer en masse, utilisez le provisioning par QR code Android Enterprise
(Android Management API) : le QR contient le checksum de l'APK et l'URL de
téléchargement. Voir provisioning/qr-provisioning.json (modèle).

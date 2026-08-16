# NA²QUIZ — Application terminal (kiosque WebView, double mode)

Application Android verrouillée qui n'affiche que l'interface élève de NA²QUIZ.
Elle fonctionne **en ligne** (production) **et en local** (serveur de salle LAN).

## Double connexion en ligne / local

- **Par défaut** : la tablette démarre sur le serveur **en ligne**
  `https://summative.na2quizappschool.uk`.
- **Mode salle locale** : un opérateur bascule vers un serveur LAN en `http://`
  (ex. `http://192.168.1.10:3000`) via l'écran de configuration caché.
- Le HTTPS est **forcé** pour la production ; le HTTP en clair n'est autorisé
  que vers les **adresses privées** (10.x, 172.16–31.x, 192.168.x) et localhost.
- La WebView refuse toute URL hors production et hors LAN privé.

## Accéder à la configuration (opérateur)

1. Taper **7 fois rapidement** dans le **coin haut-gauche** de l'écran.
2. Saisir le **code superviseur** (`246810` par défaut — À CHANGER dans
   `MainActivity.kt`, constante `CODE_SUPERVISEUR`).
3. Choisir **En ligne** ou **Local**, saisir l'URL locale si besoin, Enregistrer.
   Le choix est **persistant** (conservé au redémarrage).

## Avant de compiler — à adapter dans `MainActivity.kt`
- `URL_EN_LIGNE` : URL de production (déjà renseignée).
- `URL_LOCALE_DEFAUT` : IP:port de votre serveur de salle par défaut.
- `CODE_SUPERVISEUR` : code d'accès à la configuration.
- `domainesProd` : domaines de production autorisés.

## Build & déploiement
Voir `provisioning/SIGNATURE.md` (clé + build signé) et
`provisioning/DEVICE_OWNER.md` (mise en Device Owner = kiosque non contournable).

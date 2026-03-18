# Delivery Backend API

## Installation
```bash
cd delivery-backend
npm install
```

## Démarrage
```bash
npm start
# ou pour le développement
npm run dev
```

## Configuration Réseau pour Android

### ✅ Connexion réussie avec l'IP : `192.168.2.112:3000`

Le serveur écoute sur toutes les interfaces (`0.0.0.0`) pour permettre la connexion depuis l'émulateur Android.

### Configuration Android requise :

1. **ApiClient.kt** :
```kotlin
private const val BASE_URL = "http://192.168.2.112:3000/"
```

2. **network_security_config.xml** :
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">192.168.2.112</domain>
    <domain includeSubdomains="false">10.0.2.2</domain>
    <domain includeSubdomains="false">localhost</domain>
</domain-config>
```

3. **AndroidManifest.xml** :
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

### Test de connexion dans l'application :
- **Bouton** : "Test connexion avec base de donner" dans SettingsScreen
- **Endpoint testé** : `GET /api/tables`
- **Timeout** : 30 secondes
- **Résultat** : Affiche le nombre de tables trouvées en cas de succès

## Endpoints Disponibles

### Exploration de la Base de Données
- `GET /api/tables` - Liste toutes les tables ✅ **Testé et fonctionnel**
- `GET /api/tables/:tableName/structure` - Structure d'une table
- `GET /api/:tableName` - Données de n'importe quelle table

### Endpoints Métier
- `GET /api/deliveries` - Livraisons
- `GET /api/deliveries/drivers` - Chauffeurs
- `GET /api/user/:email` - Utilisateur par email
- `GET /api/user/profile?email=...` - Profil utilisateur actuel

## Comment la connexion fonctionne :

### 1. **Configuration du Backend** :
```javascript
// server.js
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Server running on http://0.0.0.0:${PORT}`);
  console.log(`🌐 Accessible from Android emulator at: http://10.0.2.2:${PORT}`);
});
```

### 2. **Configuration Android** :
- **Permissions** : `INTERNET` et `ACCESS_NETWORK_STATE`
- **Sécurité réseau** : Autorise HTTP pour les IPs de développement
- **Timeout** : 30 secondes pour éviter les blocages

### 3. **Flow de connexion** :
1. L'app Android appelle `GET /api/tables`
2. Le backend se connecte à PostgreSQL Neon
3. Retourne la liste des tables si la connexion réussit
4. L'app affiche "✅ Connexion réussie! X tables trouvées"

### 4. **Gestion des erreurs** :
- **Timeout** : "⏱️ Timeout: Le serveur ne répond pas"
- **Connexion refusée** : "🔌 Connexion refusée: Vérifiez que le backend tourne"
- **Hôte inconnu** : "🌐 Hôte inconnu: Vérifiez l'URL de l'API"
- **Erreur HTTP** : "❌ Erreur HTTP: XXX XXX"

## Base de Données
- **Type** : PostgreSQL (Neon)
- **Connection** : Automatique au démarrage du serveur
- **Sécurité** : SSL requis (configuré dans DATABASE_URL)

⚠️ **Important**: Cette API ne MODIFIE PAS votre base de données existante.
Elle fait seulement de la lecture pour explorer la structure existante.

## Dépannage

### Si la connexion ne fonctionne pas :
1. **Vérifiez que le backend tourne** : `npm start`
2. **Vérifiez l'IP** : `ipconfig` et mettez à jour `ApiClient.kt`
3. **Vérifiez le firewall** : Autorisez le port 3000
4. **Testez manuellement** : `http://192.168.2.112:3000/api/tables` dans le navigateur

### IPs alternatives pour émulateur :
- `10.0.2.2` : IP par défaut de l'hôte pour émulateur Android
- `192.168.x.x` : IP locale de votre machine (recommandé)
- `localhost` : Ne fonctionne généralement pas depuis l'émulateur

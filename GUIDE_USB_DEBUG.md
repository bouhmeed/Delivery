# 🔄 Guide Retour au Débogage USB

## 📋 Étapes Rapides pour Revenir au Mode USB

### 1️⃣ **Reconnecter le Câble USB**
- Branchez votre téléphone au PC avec le câble USB-C
- Assurez-vous que le débogage USB est activé sur le téléphone

### 2️⃣ **Vérifier la Connexion**
```bash
# Dans PowerShell (sur votre PC)
adb devices
# Devrait afficher votre appareil
```

### 3️⃣ **Lancer le Backend** (si pas déjà lancé)
```bash
cd delivery-backend
npm start
```

### 4️⃣ **Lancer l'Application**
- Ouvrez Android Studio
- Cliquez sur "Run 'app'"
- Sélectionnez votre appareil USB dans la liste

---

## 🔧 **Configuration Actuelle (Fonctionne pour USB ET WiFi)**

### ✅ **NetworkConfig.kt** - DÉJÀ CONFIGURÉ
```kotlin
val BASE_URLS = listOf(
    "http://192.168.2.145:3000/",  // IP WiFi (principal)
    "http://192.168.2.131:3000/",  // Backup
    "http://10.0.2.2:3000/",       // Émulateur
    "http://localhost:3000/"        // Local
)
```

### ✅ **Network Security Config** - DÉJÀ CONFIGURÉ
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain>192.168.2.145</domain>  // WiFi
    <domain>192.168.2.131</domain>  // Backup
    <domain>10.0.2.2</domain>       // Émulateur
</domain-config>
```

---

## 🎯 **CE QUE VOUS DEVEZ FAIRE**

### **ÉTAPE 1 - Branchement USB**
1. Connectez le câble USB-C
2. Sur téléphone : Options développeur → Débogage USB → Activer
3. Autorisez la connexion sur le téléphone si demandé

### **ÉTAPE 2 - Vérification**
```bash
# Test dans PowerShell
adb devices
# Résultat attendu :
# List of devices attached
# XXXXXXXXXX    device
```

### **ÉTAPE 3 - Lancement**
1. Backend : `cd delivery-backend && npm start`
2. Android Studio : Run → Select USB Device
3. Application : Testez la connexion

---

## 🚨 **SI ÇA NE MARCHE PAS**

### **Problème 1 : Appareil non détecté**
```bash
# Redémarrer ADB
adb kill-server
adb start-server
adb devices
```

### **Problème 2 : Connexion API échoue**
- L'application utilisera automatiquement `192.168.2.145:3000`
- Si problème, utilisez le bouton "Test connexion" dans Settings

### **Problème 3 : ADB non reconnu**
- Installez/reinstallez les drivers USB de votre téléphone
- Essayez un autre câble USB
- Essayez un autre port USB

---

## 📊 **État de Configuration**

| Élément | État | Action |
|---------|------|--------|
| **Backend** | ✅ Prêt | `npm start` |
| **App Android** | ✅ Configurée | Lancer depuis Android Studio |
| **Réseau** | ✅ Configuré | `192.168.2.145:3000` |
| **USB Debug** | 🔄 À connecter | Branchez le câble |

---

## 🎉 **RÉSUMÉ RAPIDE**

1. **Branchez USB-C**
2. **Activez débogage USB** sur téléphone
3. **Vérifiez** avec `adb devices`
4. **Lancez backend** : `npm start`
5. **Lancez app** depuis Android Studio

**C'est tout ! Votre configuration est déjà prête pour USB !** 🚀

---

## 📝 **Notes**

- **Aucune modification de code nécessaire**
- **Configuration réseau fonctionne pour USB et WiFi**
- **IP `192.168.2.145` reste la même** (votre PC)
- **Backend écoute sur `0.0.0.0:3000`** (toutes interfaces)

## 🔍 **Vérification Finale**

```bash
# Test API depuis PC
curl http://192.168.2.145:3000/api/tables

# Test depuis téléphone (dans l'app)
Settings → "Test connexion avec base de données"
```

---

## 📱 **PASSER DU USB AU SANS FIL**

### **Option 1 : Débogage Sans Fil Android 11+**
1. **Avec USB connecté** :
   ```bash
   adb devices  # Vérifiez que l'appareil est connecté
   ```
2. **Activer le débogage sans fil** :
   ```bash
   adb tcpip 5555
   ```
3. **Trouver l'IP du téléphone** :
   - Paramètres → À propos du téléphone → État → Adresse IP
   - Ou : `adb shell ip addr show wlan0`
4. **Se connecter sans fil** :
   ```bash
   adb connect 192.168.2.XXX:5555  # Remplacez XXX par l'IP de votre téléphone
   ```
5. **Débrancher le câble USB**
6. **Vérifier** :
   ```bash
   adb devices  # Devrait montrer l'appareil en sans fil
   ```

### **Option 2 : Pairing Android 11+**
1. **Sur le téléphone** :
   - Paramètres → Options pour les développeurs → Débogage sans fil
   - Activer "Débogage sans fil"
   - Scanner le QR code ou noter le code d'appairage
2. **Dans Android Studio** :
   - Run → Edit Configurations
   - Target Device → "Pair devices using Wi-Fi"
   - Scanner le QR code ou entrer le code
3. **Sélectionner l'appareil** dans la liste des dispositifs sans fil

### **Option 3 : Ancienne Méthode (Root requis)**
```bash
# Activer le serveur ADB sur le téléphone
adb shell setprop service.adb.tcp.port 5555
adb shell stop adbd
adb shell start adbd

# Se connecter depuis PC
adb connect 192.168.2.XXX:5555
```

---

## 🔄 **RETOUR AU USB (depuis sans fil)**
```bash
# Se reconnecter en USB
adb usb
# Branchez le câble
adb devices
```

---

## 📊 **TABLEAU RÉCAPITULATIF**

| Mode | Commande | État | Avantages |
|------|----------|------|-----------|
| **USB** | `adb devices` | ✅ Stable | Fiable, rapide |
| **WiFi TCP/IP** | `adb tcpip 5555` | 🔄 Moyen | Sans câble |
| **WiFi Pairing** | QR code | ✅ Moderne | Sécurisé, simple |

---

## 🚨 **DÉPANNAGE SANS FIL**

### **Connexion échoue**
```bash
# Vérifier le réseau
adb connect 192.168.2.XXX:5555

# Si erreur : retry
adb kill-server
adb start-server
adb connect 192.168.2.XXX:5555
```

### **Appareil non trouvé**
- Vérifiez que téléphone et PC sont sur **même WiFi**
- Désactivez/activez le WiFi du téléphone
- Redémarrez ADB : `adb kill-server && adb start-server`

### **Android Studio ne voit pas l'appareil**
- Refresh la liste des appareils
- Redémarrez Android Studio
- Utilisez `adb devices` pour vérifier

---

**Préparé le :** $(date)  
**IP PC :** 192.168.2.145  
**Port Backend :** 3000  
**Port ADB WiFi :** 5555

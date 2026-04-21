# Comparaison des Calendriers - Delivery vs Tournée

## 🚚 Écran "Suivi mes Tournées" (DeliveryTrackingScreen.kt)

### Localisation
- **Fichier**: `app/src/main/java/com/example/delivery/presentation/screens/DeliveryTrackingScreen.kt`
- **Lignes**: 361-371 (DateFilterRow)
- **Composant principal**: `DateFilterRow`

### Fonctionnalités du Calendrier

#### 1. DateFilterRow (Barre de filtrage par date)
```kotlin
DateFilterRow(
    selectedDate = selectedDate,
    onDateSelected = { newDate ->
        viewModel.setSelectedDate(newDate)
    },
    onPreviousDay = { viewModel.goToPreviousDay(driverId) },
    onNextDay = { viewModel.goToNextDay(driverId) },
    onTodayClick = { viewModel.goToToday(driverId) },
    shipmentDates = shipmentDates
)
```

**Caractéristiques:**
- **Affichage**: Barre horizontale avec navigation jour précédent/suivant
- **Date cliquable**: Ouvre le calendrier popup quand on clique sur la date
- **Indicateur vert**: Point vert (8.dp) si la date a des expéditions
- **Bouton "Aujourd'hui"**: Apparaît seulement si ce n'est pas aujourd'hui
- **Format date**: `yyyy/MM/dd` (ex: 2025/04/15)
- **Type de date**: "Aujourd'hui", "Hier", "Demain", "Passé", "Futur"

#### 2. CustomCalendar (Popup/Dialog)
- **Fichier**: `app/src/main/java/com/example/delivery/presentation/components/CustomCalendar.kt`
- **Composant**: `CustomCalendar` (lignes 386-499)
- **Déclenchement**: Clic sur la date dans DateFilterRow

**Caractéristiques:**
- **Type**: Dialog (popup modal)
- **Navigation mois**: Boutons précédent/suivant pour changer de mois
- **Jours de la semaine**: Lun, Mar, Mer, Jeu, Ven, Sam, Dim (français)
- **Grille**: 7 colonnes x 6 lignes (42 cellules)
- **Indicateurs rouges**: Points rouges (4.dp) sur les jours avec expéditions
- **Sélection**: Fond vert (0xFF4CAF50) pour la date sélectionnée
- **Jours avec expéditions**: Fond vert clair (0xFFE8F5E9)
- **Boutons**: "Annuler" et "OK" pour fermer

**Code des indicateurs rouges:**
```kotlin
if (hasShipments && !isSelected) {
    Spacer(modifier = Modifier.height(2.dp))
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.Red)
    )
}
```

#### 3. Données chargées
- **shipmentDates**: Liste de dates ISO (yyyy-MM-dd) avec expéditions
- **Source API**: `getShipmentDates(driverId)`
- **Chargement**: Via ViewModel dans `loadShipmentDates(driverId)`
- **État**: `_shipmentDates` MutableStateFlow dans ViewModel

#### 4. Comportement au clic
1. Utilisateur clique sur la date dans DateFilterRow
2. CustomCalendar s'ouvre en popup
3. Utilisateur sélectionne une date dans le calendrier
4. La popup se ferme
5. `selectedDate` est mis à jour
6. `LaunchedEffect` détecte le changement
7. `loadTripForDate()` est appelé avec la nouvelle date
8. Les expéditions sont rechargées pour la nouvelle date

---

## 🚗 Écran "Tournée" (TourneeScreen.kt)

### Localisation
- **Fichier**: `app/src/main/java/com/example/delivery/presentation/screens/TourneeScreen.kt`
- **Lignes**: 9275-9281 (TourneeCalendarCard)
- **Composant principal**: `TourneeCalendarCard`

### Fonctionnalités du Calendrier

#### 1. TourneeCalendarCard (Carte de calendrier intégrée)
```kotlin
TourneeCalendarCard(
    currentMonth = currentMonth,
    selectedDate = selectedDate,
    toursByDate = realToursByDate,
    onDateSelected = { date -> selectedDate = date },
    onMonthChanged = { month -> currentMonth = month }
)
```

**Caractéristiques:**
- **Affichage**: Carte intégrée dans l'écran (pas de popup)
- **Position**: En haut de l'écran, dans LazyColumn
- **Navigation mois**: Boutons avec fond semi-transparent (primaryContainer alpha 0.3f)
- **Style**: Carte avec ombre (6.dp) et coins arrondis (16.dp)
- **Couleur de fond**: MaterialTheme.colorScheme.surface

#### 2. TourneeCalendarGrid (Grille du calendrier)
- **Fichier**: `app/src/main/java/com/example/delivery/presentation/screens/HomeScreen.kt`
- **Composant**: `TourneeCalendarGrid` (lignes 9492-9559)

**Caractéristiques:**
- **Grille**: Affichage semaine par semaine (7 colonnes)
- **Calcul**: Premier jour de la semaine ajusté pour commencer par lundi
- **Jours vides**: Remplissage avec null pour les jours hors mois
- **Taille cellule**: 40.dp x 40.dp

#### 3. TourneeCalendarDay (Cellule de jour)
- **Composant**: `TourneeCalendarDay` (lignes 9562-9636)

**Caractéristiques:**
- **Fond sélectionné**: MaterialTheme.colorScheme.primary
- **Fond avec tournée**: MaterialTheme.colorScheme.primaryContainer
- **Fond normal**: MaterialTheme.colorScheme.surface
- **Bordure**: 2.dp si sélectionné
- **Texte**: Numéro du jour + nombre de tournées
- **Indicateur rouge**: Point rouge (10.dp) en haut à droite si des tournées existent

**Code de l'indicateur rouge:**
```kotlin
if (hasTour && tourCount > 0) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(10.dp)
            .background(
                color = Color.Red,
                shape = RoundedCornerShape(5.dp)
            )
            .padding(1.dp)
    )
}
```

#### 4. Données chargées
- **toursByDate**: Map<LocalDate, Int> comptant les tournées par jour
- **Source**: Calculé localement depuis la liste des trips
- **Calcul**: `trips.groupBy { getTripDate(trip) }.mapValues { it.value.size }`
- **Filtrage**: Trips filtrés par date sélectionnée ET recherche

#### 5. Comportement au clic
1. Utilisateur clique directement sur un jour dans le calendrier
2. `selectedDate` est mis à jour immédiatement
3. La liste des trips est filtrée pour la nouvelle date
4. `LaunchedEffect` précharge les expéditions pour les trips affichés
5. L'UI se met à jour avec les trips du nouveau jour

---

## 🔍 Comparaison des Deux Calendriers

| Caractéristique | Delivery (🚚) | Tournée (🚗) |
|----------------|---------------|-------------|
| **Type d'affichage** | Popup/Dialog | Carte intégrée |
| **Composant** | DateFilterRow + CustomCalendar | TourneeCalendarCard |
| **Navigation** | Flèches jour + popup calendrier | Flèches mois dans la carte |
| **Indicateur rouge** | 4.dp (petit) | 10.dp (plus grand) |
| **Position indicateur** | Sous le numéro de jour | En haut à droite de la cellule |
| **Données** | shipmentDates (API) | toursByDate (calculé localement) |
| **Affichage du nombre** | Non (juste indicateur) | Oui (nombre de tournées affiché) |
| **Fond sélectionné** | Vert (0xFF4CAF50) | Primary (MaterialTheme) |
| **Fond avec données** | Vert clair (0xFFE8F5E9) | PrimaryContainer |
| **Taille cellule** | AspectRatio(1f) | 40.dp fixe |
| **Boutons** | "Annuler" / "OK" | Navigation mois intégrée |
| **Rechargement** | API call (loadTripForDate) | Filtrage local + préchargement |
| **Recherche** | Non | Oui (barre de recherche) |

---

## 📊 Différences Techniques

### DeliveryTrackingScreen (🚚)
```kotlin
// État
val selectedDate by viewModel.selectedDate.collectAsState()
val shipmentDates by viewModel.shipmentDates.collectAsState()

// Comportement
LaunchedEffect(driverId, selectedDate) {
    viewModel.loadTripForDate(driverId, selectedDate)
    viewModel.loadShipmentDates(driverId)
}

// Composant
DateFilterRow(
    selectedDate = selectedDate,
    shipmentDates = shipmentDates,
    onDateSelected = { newDate ->
        viewModel.setSelectedDate(newDate)
    }
)
```

### TourneeScreen (🚗)
```kotlin
// État
var selectedDate by remember { mutableStateOf(LocalDate.now()) }
var currentMonth by remember { mutableStateOf(YearMonth.from(LocalDate.now())) }

// Calcul local
val tripsByDate = remember(trips) {
    trips.groupBy { trip -> getTripDate(trip) }
        .mapValues { it.value.size }
        .filterKeys { it != null }
        .mapKeys { it.key!! }
}

// Composant
TourneeCalendarCard(
    currentMonth = currentMonth,
    selectedDate = selectedDate,
    toursByDate = realToursByDate,
    onDateSelected = { date -> selectedDate = date },
    onMonthChanged = { month -> currentMonth = month }
)
```

---

## 🎯 Cas d'Usage

### Delivery (🚚) - Suivi des livraisons
- **Objectif**: Voir les livraisons d'un jour spécifique
- **Workflow**: Sélectionner une date → Voir les livraisons du jour
- **Données**: Expéditions (shipments) avec statuts
- **Indicateur**: Jours avec des livraisons planifiées

### Tournée (🚗) - Gestion des tournées
- **Objectif**: Voir les tournées planifiées par jour
- **Workflow**: Sélectionner un jour → Voir les tournées + leurs expéditions
- **Données**: Trips avec leurs expéditions associées
- **Indicateur**: Jours avec des tournées (avec compteur)
- **Recherche**: Possibilité de filtrer les trips

---

## 🔧 Implémentation Technique

### CustomCalendar (Delivery)
- **Fichier**: `CustomCalendar.kt`
- **Lignes**: 386-499
- **Type**: Dialog avec DialogProperties
- **Grille**: LazyVerticalGrid avec GridCells.Fixed(7)
- **Génération**: `generateCalendarDays(month)` avec padding pour 42 cellules

### TourneeCalendarCard (Tournée)
- **Fichier**: `HomeScreen.kt` (composant partagé)
- **Lignes**: 9400-9489
- **Type**: Card avec Column
- **Grille**: Column avec Rows manuelles (7 colonnes par row)
- **Génération**: Calcul manuel des semaines avec padding

---

## 💡 Points Clés à Retenir

### Delivery (🚚)
1. **Calendrier popup** - S'ouvre quand on clique sur la date
2. **Indicateurs rouges** - Petits points (4.dp) sous le jour
3. **API-based** - Les dates viennent de l'API
4. **Rechargement API** - Chaque changement de date déclenche un appel API
5. **Navigation jour** - Flèches pour jour précédent/suivant
6. **Bouton "Aujourd'hui"** - Raccourci pour revenir à aujourd'hui

### Tournée (🚗)
1. **Calendrier intégré** - Toujours visible en haut de l'écran
2. **Indicateurs rouges** - Points plus grands (10.dp) en coin
3. **Calcul local** - Les données sont calculées depuis les trips chargés
4. **Filtrage local** - Pas d'appel API au changement de date
5. **Navigation mois** - Flèches pour mois précédent/suivant
6. **Compteur visible** - Nombre de tournées affiché dans chaque cellule
7. **Recherche** - Barre de recherche pour filtrer les trips

---

## 📝 Résumé

**Delivery (🚚)** utilise un calendrier **popup** avec des indicateurs **subtils** pour filtrer les livraisons par date, avec un rechargement **API** à chaque changement.

**Tournée (🚗)** utilise un calendrier **intégré** avec des indicateurs **visibles** et un compteur pour gérer les tournées, avec un filtrage **local** des données déjà chargées.

Les deux approches sont adaptées à leurs cas d'usage respectifs :
- Delivery: Focus sur les livraisons d'un jour spécifique
- Tournée: Vue d'ensemble des tournées avec possibilité de navigation rapide

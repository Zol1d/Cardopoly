# Cardopoly

Cardopoly ir Android aplikācija galda spēlei **Monopoly**, kur naudas aprite notiek ar NFC kartēm, nevis papīra naudā.

### Galvenās iespējas

- NFC karšu reģistrācija un piesaiste spēlētāju krāsām
- Spēli var uzsākt ar vai bez kartēm
- Bankas darbības ar NFC (pieskaitīt, atņemt, pārskaitīt starp spēlētājiem)
- Aizdevumu pārvaldība (summa, atmaksas summa, nosacījumi, piezīmes)
- Darījumu vēsture ar laiku un apļu informāciju
- Spēles automātiska atkopšana pēc aplikācijas aizvēršanas
- Kalkulatori (starpība, procenti, nekustamā īpašuma nodokļa kalkulators)
- Iestatījumi: sākuma nauda, komisijas procenti, nekustamā īpašuma nodoklis, tumšais/gaišais režīms

Aplikācijā tika integrēti arī dažādi pašizdomāti spēles noteikumi, piemēram aizņemšanās un dažādu veido nodokļi, no tiem var pilnībā atteikties.

### Izmantotās tehnoloģijas

- Kotlin + Jetpack Compose
- AndroidX DataStore (kartes, iestatījumi, spēles atkopšana)
- Kotlinx Serialization
- Compose Navigation 
- Firebase Crashlytics (jābūt savam `google-services.json`, vai pašam jāatslēdz)

### Prasības

- Android 9+ (minSdk 28)
- NFC atbalsts ierīcē
- Android Studio ar AGP 8.2 vai jaunāku
- JDK 17

### Palaišana lokāli

1. Atver projektu Android Studio.
2. Ļauj Gradle sinhronizēt depus.
3. Pievieno `app/google-services.json` (ja izmanto savu Firebase projektu).
4. Palaid aplikāciju.

#### Būvēšanas komandas
Windows:
```
gradlew.bat assembleDebug
gradlew.bat installDebug
```
macOS/Linux:
```
./gradlew assembleDebug
./gradlew installDebug
```

### Kvik-starts

1. Ekrānā **Kartes** reģistrē NFC kartes un piešķir tām krāsas.
2. Izvēlies **Jauna spēle ar kartēm**.
3. Pievieno spēlētājus, nolasot katra spēlētāja karti.
4. Spēlē!
5. Aizdevumus un to atmaksu pārvaldi sadaļā **Aizdevumi**.

### Piezīmes

- Maksimālais aktīvo spēlētāju skaits spēlē: 8.
- Karšu krāsu komplekts pašlaik paredzēts 6 kartēm.
- Ekrāna orientācija ir fiksēta portreta režīmā.


#### "Monopoly" ir "Hasbro, Inc." preču zīme. Šis projektam nav nekāda saistība ar to.

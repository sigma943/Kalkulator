# Kalkulator

Kalkulator na Androida napisany w Kotlinie i Jetpack Compose. Działa lokalnie,
obsługuje historię, motywy, funkcje naukowe i przeliczanie jednostek.

## Obliczenia

- Kolejność działań, nawiasy i mnożenie domyślne: `2(3+4) = 14`.
- Liczby ujemne: `2×−3 = −6`, `2^−3 = 0,125`, `−2^2 = −4`.
- Procenty: `200+10% = 220`, `200−10% = 180`, `200×10% = 20`, `50% = 0,5`.
  Przy dodawaniu i odejmowaniu samodzielny prawy argument procentowy jest liczony
  względem lewego argumentu. W iloczynach i ilorazach `%` oznacza dzielenie przez 100.
- Funkcje trygonometryczne w DEG/RAD, logarytmy, pierwiastki, silnia oraz stałe π i e.
- Przecinek i kropka dziesiętna; zapis naukowy w postaci `1E-13` lub `1×10^−13`.
- Po `=` cyfra rozpoczyna nowe działanie, operator kontynuuje z pełną wartością
  `Double`, niezależnie od ustawionego zaokrąglenia ekranu. Ponowne `=` zachowuje wynik.
- Brakujące nawiasy zamykające są uzupełniane przy `=` i w podglądzie.
- Przytrzymanie `=` uruchamia easter egg.

Silnik używa arytmetyki `Double` (około 15–17 cyfr znaczących); nie jest przeznaczony
do rozliczeń wymagających dokładnej arytmetyki dziesiętnej. Zaokrąglanie ekranu
stosuje HALF_UP. Historia zapisuje wynik w formie wyświetlanej.

## Uruchomienie i testy

Wymagania: JDK 21, Android SDK i Android Studio obsługujące konfigurację projektu.
Otwórz katalog projektu w Android Studio i pozwól pobrać wymagane SDK.
Gradle 9.3.1 jest pobierany przez dołączony Gradle Wrapper.

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Windows: użyj `gradlew.bat` zamiast `./gradlew`.
APK: `app/build/outputs/apk/debug/app-debug.apk`.
Build debug używa standardowego klucza Android debug. Klucz API i plik `.env`
nie są potrzebne do funkcji kalkulatora.

Wersja release wymaga własnego klucza: `KEYSTORE_PATH`, `STORE_PASSWORD`,
`KEY_PASSWORD`; alias klucza: `upload`. Nie dodawaj kluczy do repozytorium.

## GitHub Actions

Workflow **Build Android APK** uruchamia testy i buduje APK po pushu oraz dla PR.
Można go też uruchomić ręcznie przez **Actions → Build Android APK → Run workflow**.
Gotowy APK znajduje się w artefakcie **Kalkulator-Debug-APK**, a raporty testów
w **Unit-test-reports**. Tag `v*` dodatkowo tworzy GitHub Release z debug APK.
APK debug służy do testowania; wydanie produkcyjne wymaga podpisu release.

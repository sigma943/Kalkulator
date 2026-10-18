# Kalkulator

Kalkulator na Androida napisany w Kotlinie i Jetpack Compose. Działa lokalnie,
obsługuje historię, motywy, funkcje naukowe i przeliczanie jednostek.
Kursy walut mogą być pobierane z NBP przez internet.

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

## Niezawodność przelicznika

Przelicznik używa dziesiętnego `BigDecimal`: współczynniki są zapisane dokładnie,
ilorazy liczone z 34 cyframi znaczącymi, a ekran pokazuje do 16 cyfr. Zmiana
aktywnego wiersza przekazuje pełny wynik. Błędne dane otrzymują komunikat zamiast
pozornego zera. Temperatura jest sprawdzana względem zera bezwzględnego.

- Dane: kB/MB/GB są dziesiętne, KiB/MiB/GiB binarne; bajt i bit są rozróżniane.
- Objętość: oddzielne US cup (236,5882365 ml), szklanka 250 ml i miarka 240 ml.
- Systemy liczbowe: zapis całkowity BIN/OCT/DEC/HEX jest przeliczany przez `BigInteger`;
  dostępne są przyciski A–F. Niedozwolone cyfry nie są przyjmowane.
- Finanse: jednostką bazową jest netto/kwota bazowa; wyniki z VAT lub napiwkiem
  zawierają ten dodatek. Rabaty mają osobną cenę bazową przed obniżką.
- BMI ma osobny formularz masy w kg i wzrostu w cm; nie jest konwersją jednostek.
- Miesiące i lata oznaczają średnie okresy: 30,4375 i 365,25 dnia, nie odstępy
  między konkretnymi datami. Mach jest przybliżeniem dla powietrza przy 15°C,
  a radiany używają przybliżenia π.
- Waluty: tabela A NBP jest pobierana przez HTTPS wraz z datą publikacji.
  W razie braku sieci można użyć zapisanej tabeli, maksymalnie sprzed 7 dni.
  Starsza lub niekompletna tabela blokuje konwersję. Są to kursy średnie,
  bez spreadu i prowizji. Kwoty i historia nie są wysyłane do NBP.

Źródła definicji i danych:
[NBP Web API](https://api.nbp.pl/),
[NIST: przedrostki binarne](https://physics.nist.gov/cuu/Units/binary.html),
[NIST: współczynniki konwersji](https://www.nist.gov/pml/special-publication-811/nist-guide-si-appendix-b-conversion-factors/nist-guide-si-appendix-b9).

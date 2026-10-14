# backend/ — instrukcja dla modeli (i ludzi)

Źródło prawdy aktualizacji: `backend/api/version.json`.
Aplikacja (`com.bscompanion.app`) pobiera go przez raw.githubusercontent:

```
https://raw.githubusercontent.com/royallorde-maker/bs-companion/main/backend/api/version.json
```

URL jest wbudowany w apk przez `buildConfigField UPDATE_URL` w `app/build.gradle.kts`.
Nie hardkoduj URL w Kotlin — tylko przez BuildConfig.

## Schemat version.json

```json
{
  "versionCode": 6,
  "versionName": "1.2.0",
  "apkUrl": "https://github.com/.../releases/latest/download/app-release.apk",
  "changelog": "krótki opis",
  "force": false,
  "publishedAt": "2026-10-13T00:00:00Z"
}
```

- `versionCode` MUSI rosnąć o +1 przy każdym wydaniu (apka porównuje kody, nie nazwy).
- `apkUrl` MUSI wskazywać na asset z GitHub Release (nie commituj apk do repo).
- `force=true` tylko gdy stara wersja jest łamana (apka pokazuje mocniejszy baner).

## Jak wydać nową wersję (krok po kroku, sprawdzone do 1.43.0)

Zasada: KAŻDA zmiana w apce = osobny release + wpis w instrukcji (InfoCard poniżej).
Pomijanie tego psuje OTA (version.json wskazuje w próżnię albo na stary build).

1. Podbij `versionCode` (+1) + `versionName` w `app/build.gradle.kts`.
2. Zaktualizuj `backend/api/version.json` (ten sam kod/nazwa, nowy changelog, aktualne `publishedAt` UTC).
3. Dopisz kartę `InfoCard("x.y.z — ...")` w `ChangelogScreen` w `ui/BsApp.kt` NA GÓRZE historii (powyżej poprzedniej wersji).
4. Zbuduj podpisany release — WYMAGA SIECI (lint dociąga zależności, `--offline` pada):
   ```powershell
   $g = "C:\Users\skraba\.gradle\wrapper\dists\gradle-8.7-bin\bhs2wmbdwecv87pi65oeuq5iu\gradle-8.7\bin\gradle.bat"
   & $g :app:assembleRelease :app:testDebugUnitTest
   # Wynik: app/build/outputs/apk/release/app-release.apk (podpisany)
   ```
   W tym projekcie brak `gradle` na PATH i bootstrapu wrappera — wołaj pełną ścieżkę jw.
   (`./gradlew` nie działa — policzy wrappera którego nie ma.)
5. Zweryfikuj podpis (musi być TEN SAM co poprzedni release, inaczej update się nie zainstaluje):
   ```powershell
   & "C:\Users\skraba\AppData\Local\Android\Sdk\build-tools\34.0.0\apksigner.bat" verify --print-certs app/build/outputs/apk/release/app-release.apk
   # Oczekiwane: CN=BS Companion, SHA-256 0fd77ec8684c3dbc2c2917f612f127013184cd35726b94a8929698ab2ff88228
   ```
6. Push + tag + Release (asset MUSI nazywać się `app-release.apk` — `latest/download` na to liczy):
   ```powershell
   git add <konkretne pliki — NIGDY gołe -A, wciąga .concord/>; git commit -m "1.44.0 code 50: ..."; git push origin main
   git tag -a v1.44.0 -m "v1.44.0 ..."; git push origin v1.44.0
   gh release create v1.44.0 app/build/outputs/apk/release/app-release.apk --title "v1.44.0" --notes "..."
   ```
   `gh` wymaga jednorazowo `gh auth login` (https). Bez Release `latest/download` serwuje STARY apk
   (objaw: "build identyczny" mimo nowego version.json).
7. Sprawdź stronę statusu `backend/site/index.html`: wiersz "tag ⇔ version.json" ma być ZGODNE.
8. W apce: zakładka Zmiany → karta Aktualizacja ma pokazać "dostępna".

## Podpisywanie (nie ruszać bez zrozumienia)

- Klucz: `C:\Users\skraba\bscompanion-release.keystore`, alias `bscompanion` (POZA repo).
  Kopia + hasło MUSZĄ mieć backup — zgubienie = koniec OTA. Hasło żyje tylko w `keystore.properties`.
- `app/build.gradle.kts` wymaga klucza TYLKO przy taskach pakujących release
  (`packageRelease`/`bundleRelease` padają z jawnym błędem); debug/testy działają bez niego.
- Historia kluczy: ≤1.4.1 oryginalny (niedostępny), 1.5.0 DesignNest (porzucony), od 1.6.0 `bscompanion`.
  Zmiana klucza = użytkownicy muszą odinstalować apkę (Android odrzuca update).
- Poprzedni klucz DesignNest (`designnest-release.keystore`, hasło utracone) jest martwy — nie używać.

## Jak edytować stronę statusu

- Plik: `backend/site/index.html` (statyczny, zero dependencji, fetch ABSOLUTNEGO raw URL
  `backend/api/version.json` + Releases API — `../api/` nie istnieje przy deployu Pages).
- Podgląd lokalnie: `python3 -m http.server` w root repo → `http://localhost:8000/backend/site/`.
- Deploy (docelowo): GitHub Pages przez Actions z `path: backend/site`. Do tego czasu wystarczy raw + Release.

## Jak pushować (git)

```powershell
git status --short --branch
git add -A
git commit -m "krótko co i dlaczego"
git push
```

- `main` to jedyny branch. Bezpośredni push jest OK (projekt 1-osobowy).
- Nie commituj: `local.properties`, `keystore.properties`, `*.jks`, `*.keystore`, `app/build/`,
  `build/`, `.gradle/`, `.idea/`, `.kotlin/`, `.local/`, `.concord/`, `*.iml`, `.DS_Store`.
- Duże binaria (apk/aab) TYLKO przez Releases, nigdy do gita.

## Zachowanie offline (kontrakt z apką)

- Apka MUSI działać bez internetu: plan, posiłki, alarmy — wszystko lokalne.
- Brak sieci = subtelna ikona wifi-off w nagłówku + karta Aktualizacja pokazuje "Nie można sprawdzić — brak połączenia", bez crashy i bez blokowania UI.
- Sprawdzanie aktualizacji: przy otwarciu Zmiany + ręczny przycisk "Sprawdź". BezWorkManager — 1-osobowy projekt, nie dokładaj tła.
- Instalacja apk: `DownloadManager` → `FileProvider` (`res/xml/filepaths.xml`) → `ACTION_VIEW` + `REQUEST_INSTALL_PACKAGES`. Ten sam keystore co poprzedni apk, inaczej update się nie zainstaluje.

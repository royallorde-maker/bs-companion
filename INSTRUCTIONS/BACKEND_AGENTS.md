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

## Jak wydać nową wersję (krok po kroku, sprawdzone 2026-10-13 na v1.5.0)

1. Podbij `versionCode` (+1, apka porównuje kody, nie nazwy) + `versionName` w `app/build.gradle.kts`.
2. Zaktualizuj `backend/api/version.json` (ten sam kod/nazwa, nowy changelog, aktualne `publishedAt` UTC).
3. Karta `InfoCard` w `ChangelogScreen`: POMIŃ — ekran Zmiany/Changelog nie istnieje od rebuildu DesignNest (v1.5.0). Gdy wróci ekran z historią, dopisz kartę PONIŻEJ karty "O aplikacji".
4. Zbuduj podpisany release (wymaga sieci — lint dociąga zależności, `--offline` pada):
   ```powershell
   $g = "C:\Users\skraba\.gradle\wrapper\dists\gradle-8.7-bin\bhs2wmbdwecv87pi65oeuq5iu\gradle-8.7\bin\gradle.bat"
   & $g :app:assembleRelease
   # Wynik: app/build/outputs/apk/release/app-release.apk (podpisany)
   # Bez keystore.properties: app-release-unsigned.apk (tylko do testów, nie do dystrybucji)
   ```
   UWAGA: w tym projekcie brak `gradle` na PATH i bootstrap wrappera — wołaj pełną ścieżkę jw.
5. Zweryfikuj podpis (build-tools 34.0.0):
   ```powershell
   & "C:\Users\skraba\AppData\Local\Android\Sdk\build-tools\34.0.0\apksigner.bat" verify --print-certs app/build/outputs/apk/release/app-release.apk
   ```
6. Push + tag + Release:
   ```powershell
   git add app/build.gradle.kts backend/api/version.json
   git commit -m "1.5.0 code 12: ..."
   git push origin main
   git tag -a v1.5.0 -m "v1.5.0 ..."
   git push origin v1.5.0
   gh release create v1.5.0 app/build/outputs/apk/release/app-release.apk --title "v1.5.0" --notes "..."
   ```
   Asset MUSI nazywać się `app-release.apk` (stała nazwa — `latest/download` na nią liczy).
   Wymiana assetu w istniejącym Release: `gh release upload vX.Y.Z <plik> --clobber`.
   `gh` wymaga jednorazowo `gh auth login` (protokół https, keyring). Bez tego Release nie powstanie
   i `latest/download` nadal serwuje STARY apk (objaw: "build identyczny").
7. Sprawdź stronę statusu `backend/site/index.html`: wiersz "tag ⇔ version.json" ma być ZGODNE.
8. W apce: zakładka Zmiany → karta Aktualizacja ma pokazać "dostępna". (Brak od v1.5.0 — brak ekranu.)

## Podpisywanie (klucz, dakik)

- Klucz release: `C:\Users\skraba\designnest-release.keystore` (POZA repo, alias `designnest`, RSA-2048).
  Kopia zapasowa obowiązkowa — ZGUBIENIE KLUCZA = koniec aktualizacji OTA.
- `keystore.properties` w root repo (gitignored, nigdy nie commituj):
  `storeFile=C:/Users/skraba/designnest-release.keystore` + `storePassword`/`keyAlias`/`keyPassword`.
- `app/build.gradle.kts` podpina signing DEFENSYWNIE (tylko gdy plik istnieje).
  Ten sam keystore co poprzedni apk, inaczej update się nie zainstaluje
  (`INSTALL_FAILED_UPDATE_INCOMPATIBLE` → wymagany ręczny uninstall).
  Nowy klucz (jak w v1.5.0 vs v1.4.1) = użytkownicy muszą odinstalować starą apkę.
- Cert v1.5.0: `SHA-256 92958d46a3059b2b6c1818493bc76e5fa1d8fa48cee080da357893fcffaa5ed3`.

## Jak edytować stronę statusu

- Plik: `backend/site/index.html` (statyczny, zero dependencji, fetch do `../api/version.json` + Releases API).
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
- Nie commituj: `local.properties`, `keystore.properties`, `*.jks`, `app/build/`, `.gradle/`, `.idea/`.
- Duże binaria (apk/aab) TYLKO przez Releases, nigdy do gita.

## Zachowanie offline (kontrakt z apką)

- Apka MUSI działać bez internetu: plan, posiłki, alarmy — wszystko lokalne.
- Brak sieci = subtelna ikona wifi-off w nagłówku + karta Aktualizacja pokazuje "Nie można sprawdzić — brak połączenia", bez crashy i bez blokowania UI.
- Sprawdzanie aktualizacji: przy otwarciu Zmiany + ręczny przycisk "Sprawdź". BezWorkManager — 1-osobowy projekt, nie dokładaj tła.
- Instalacja apk: `DownloadManager` → `FileProvider` (`res/xml/filepaths.xml`) → `ACTION_VIEW` + `REQUEST_INSTALL_PACKAGES`. Ten sam keystore co poprzedni apk, inaczej update się nie zainstaluje.

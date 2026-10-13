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

## Jak wydać nową wersję (krok po kroku)

1. Podbij `versionCode` + `versionName` w `app/build.gradle.kts`.
2. Zaktualizuj `backend/api/version.json` (ten sam kod/nazwa, nowy changelog, data).
3. Dopisz kartę `InfoCard("x.y.z — ...")` w `ChangelogScreen` w `ui/BsApp.kt` PONIŻEJ karty "O aplikacji".
4. Zbuduj: `./gradlew :app:assembleRelease` (wymaga `keystore.properties`, nigdy go nie commituj).
5. Push na `main`, potem na GitHub: Release z tagiem `v<versionName>` (np. `v1.2.0`) + załącz `app-release.apk` jako `app-release.apk` (stała nazwa — `latest/download` na nią liczy).
6. Sprawdź stronę statusu `backend/site/index.html`: wiersz "tag ⇔ version.json" ma być ZGODNE.
7. W apce: zakładka Zmiany → karta Aktualizacja ma pokazać "dostępna".

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

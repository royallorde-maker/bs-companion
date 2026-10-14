# Przykładowa sesja: 1.45.0 (Ustawienia + slide motywu)

Spisany przykład tego, jak wyglądała prawdziwa sesja — łącznie z wpadkami.
Nie jest to dosłowny transkrypt, tylko przebieg decyzji i incydentów.

## Zadanie wejściowe

1. Usunąć tekst "Motyw aktywny od razu." i kartę "Więcej wkrótce" z Ustawień.
2. Zostawić sam wybór motywu.
3. Dodać animację zmiany motywu (wjazd z lewej).
4. Przenieść "O aplikacji" ze Zmian do Ustawień.

## Incydent 1: wycięty środek pliku (splice złą kotwicą)

Przy wymianie pigułki nawigacji skrypt ciął od markera `/** P` — a taki prefiks
ma też komentarz `Ring` kilkaset linii wcześniej. Efekt: zniknęły `Ring`,
`Appear`, `BsApp`, część nawigacji.

Wykrycie: policzenie `fun` przed/po (spadek = alarm).
Naprawa: odtworzenie bloku z gita + ponowne nałożenie zmiany.
Zasada: kotwice długie i unikalne, po każdym cięciu inwentaryzacja funkcji.

## Incydent 2: edycje-widma (najgroźniejszy)

Seria edycji zgłaszała sukces, odczyty je "potwierdzały" — a plik na dysku
ich nie miał. Build przechodził (kompilował stary kod), release 1.45.0 wyszedł
bez obiecanej zawartości.

Wykrycie: dopiero inspekcja dex (`strings` w APK) — brak nowych ciągów
mimo zielonego builda.
Prawdopodobna przyczyna: zapis na nieaktualnym snapshocie + cache odczytów.
Zasada: po edycji struktury ZAWSZE niezależny odczyt powłoką
(`Select-String`, `Get-Content`), nigdy tylko cached reader. Przed releasem
dex-grep na ciągi danej zmiany.

## Incydent 3: walka z klamrami na ślepo

Kilka rund "liczenia klamer z pamięci" — za każdym razem wniosek był zły,
bo plik na dysku nie był tym, co zakładałem. Lekcja: nie diagnozuj struktury
z pamięci; diff `git diff HEAD` + kompilator są wyrocznią.

## Naprawa release 1.45.0

Tag i release istniały kilka minut, jeden user, nic nie zainstalowane:
release + tag skasowane, commit z prawdziwą zawartością, tag i release
od nowa. Gdyby ktokolwiek zdążył pobrać — byłaby 1.45.1 zamiast rewrite.

## Checklista końcowa (działa)

1. `Select-String` na żywo po każdym edycie struktury.
2. `aapt dump badging` — wersja/kod/nazwa w binarce.
3. dex-grep na nowe ciągi — zawartość naprawdę w środku.
4. `apksigner verify` — ten sam klucz.
5. `git add` jawną listą, `git status` czysty, tag na właściwym commicie.

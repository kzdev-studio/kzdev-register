# KZDev Register

Lekki i bezpieczny plugin rejestracji oraz logowania dla serwerów **Spigot / Paper (1.20+)** z obsługą **muzyki podczas logowania** (zarówno wbudowanych dźwięków Minecrafta, jak i własnych utworów z Resource Packa).

## Funkcje
- `/register <hasło> <powtórz_hasło>` — rejestracja nowego konta gracza
- `/login <hasło>` — logowanie do istniejącego konta
- Bezpieczne hashowanie haseł (`SHA-256` z solą w `PasswordUtil`) oraz zapis w `plugins/KZDevRegister/passwords.yml`
- Blokada ruchu i interakcji do czasu zalogowania (`MovementFreezer`)
- Automatyczny kick po przekroczeniu czasu na zalogowanie (`auth.kick-seconds`)
- Opcjonalne cząsteczki podczas oczekiwania na logowanie (`auth.particles-enabled`)
- **Konfigurowalna muzyka logowania** (`music.*`):
  - Możliwość włączenia/wyłączenia muzyki
  - Obsługa nazw dźwięków z Bukkit `Sound` (np. `MUSIC_DISC_CAT`, `MUSIC_DISC_PIGSTEP`)
  - Obsługa własnych kluczy z Resource Packa (np. `custom.moja_piosenka`)
  - Opcjonalny BossBar z nazwą odtwarzanego utworu
  - Konfigurowalna głośność, ton (`pitch`), opóźnienie startu i zapętlenie

## Konfiguracja muzyki (`config.yml`)
```yaml
music:
  enabled: true
  # Możesz użyć nazwy Bukkit Sound (np. MUSIC_DISC_CAT) lub klucza z Resource Packa (np. custom.moja_piosenka)
  sound: "MUSIC_DISC_CAT"
  display-name: "&dTeraz gra: &fDomyślna muzyka logowania"
  bossbar: true
  volume: 1.0
  pitch: 1.0
  delay-ticks: 10
  loop-ticks: 3600
```

### Jak podłączyć własną piosenkę (Resource Pack)?
1. Dodaj plik `.ogg` do swojego Resource Packa (np. `assets/minecraft/sounds/custom/moja_piosenka.ogg`) i zarejestruj go w `sounds.json` pod kluczem `custom.moja_piosenka`.
2. W `plugins/KZDevRegister/config.yml` ustaw:
   ```yaml
   music:
     enabled: true
     sound: "custom.moja_piosenka"
     display-name: "&dTeraz gra: &fNazwa Twojego Utworu"
   ```
3. Po zalogowaniu lub wyjściu gracza plugin automatycznie zatrzymuje odtwarzany utwór.

## Budowanie z kodu źródłowego
```bash
./gradlew clean build
```
Gotowy plik pluginu zostanie wygenerowany w `build/libs/KZDevRegister.jar`.

## Autor
**KZDev Studio** — [kzdev.pl](https://kzdev.pl) | [GitHub](https://github.com/kzdev-studio)

# LISAN

App Android nativa per imparare l'arabo in italiano. La versione iniziale include interfaccia Jetpack Compose, navigazione, lezioni dimostrative in arabo egiziano e MSA, sintesi vocale Android, riconoscimento vocale e progressi locali.

## Compilazione automatica
Ogni push su `main` avvia GitHub Actions. Il workflow compila `app-debug.apk` e lo pubblica come artefatto denominato **LISAN-debug-apk** per 14 giorni. È disponibile anche l'avvio manuale da Actions → Build LISAN APK → Run workflow.

## Stato e limiti
Questa è una prima versione da verificare con la pipeline CI. Il corso di 90 giorni completo, tutor AI con backend protetto, contenuti audio madrelingua, sincronizzazione cloud e valutazione fonetica non sono ancora implementati. Il riconoscimento vocale trascrive la voce e non assegna un voto alla pronuncia. Le funzioni vocali dipendono dai servizi installati sul dispositivo.

## Requisiti
JDK 17, Android SDK 36, Android Gradle Plugin 8.13.2 e Gradle 8.13. Package: `it.lisan.app`.

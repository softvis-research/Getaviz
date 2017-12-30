# README #

Der EvaluationsServer ist eine Rails-Applikation um Evaluationen von X3DOM-Dateien zu ermöglichen

### Zweck ###

* Verwaltung verschiedener Experimente
* Erstellung unterschiedlicher Fragetypen
* Hochladen von X3DOM-Szenen
* Nutzung einer zufälligen Zuweisung von Probanden zu verschiedenen Szenen
* Auswertung der Ergebnisse
* Keine Anmeldung notwendig (außer Adminfunktionen)

### Setup ###

* Ruby in Version ab 2.0 installieren (mit ruby, bundler, rake-Binaries)
* mySQL oder äquivalentes Drop-In-Replacement installieren (z.B. MariaDB)
* mySQL-Benutzer erstellen
* git checkout in ein Verzeichnis
* config/database.yml anpassen
* bundle install
* bundle exec rake db:create
* bundle exec rake db:migrate
* bundle exec rails server -p [port]
* Deployment wahrscheinlich bald mit Capistrano
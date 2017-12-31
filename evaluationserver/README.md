# README #

The evaluation server is a rails application capable to support the evaluation of websites. Currently its main use is the evaluation of x3dom software visualizations.  

### features ###

* administration of different experiments
* creation of different question types
* random assignment of participants to different scenes
* export of results
* login only required fpr administrative functions

### Setup ###

#### Docker ####

* Run `docker-compose up` in the evaluationserver directory
* Open `localhost:8081` in your web browser

#### manual setup ####

* install Ruby in version 2.0 or above (along with ruby, bundler, rake)
* install mySQL oder equivalent drop-in-replacement (e.g. MariaDB)
* create mySQL user
* git clone
* create source.sh in main directory with creddentials for mysql-database to for config/database.yml
* bundle install
* bundle exec rake db:create
* bundle exec rake db:migrate
* bundle exec rails server -p [port]

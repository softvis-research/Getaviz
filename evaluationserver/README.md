# README

The evaluation server is a rails application capable to support the evaluation of websites. Currently its main use is the evaluation of x3dom software visualizations.  

## Features

* administration of different experiments
* creation of different question types
* random assignment of participants to different scenes
* export of results
* login only required fpr administrative functions


## Setup ##

* install docker-compose
* docker-compose build
* docker-compose up
* the server is reachable under http://localhost:3000
* Admin-access without master.key-File is admin/admin 




<!--
#### manual setup ####-->
<!--
* install Ruby in version 2.0 or above (along with ruby, bundler, rake)
* install mySQL oder equivalent drop-in-replacement (e.g. MariaDB)
* create mySQL user
* git clone
* create source.sh in main directory with creddentials for mysql-database to for config/database.yml
* bundle install
* bundle exec rake db:create
* bundle exec rake db:migrate
* bundle exec rails server -p [port]
-->

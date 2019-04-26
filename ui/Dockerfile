FROM php:7.2-apache
COPY . /var/www/html/ui
RUN apt-get update && \
    curl -sL https://deb.nodesource.com/setup_8.x | bash - &&\
    apt-get install -y build-essential nodejs git &&\
    mv "$PHP_INI_DIR/php.ini-production" "$PHP_INI_DIR/php.ini" &&\
    cd /var/www/html/ui &&\
    npm install
LABEL maintainer="david.baum@uni-leipzig.de" \
      version="1.0"

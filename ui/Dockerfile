FROM php:7.2-apache
COPY . /var/www/html/ui
RUN mv "$PHP_INI_DIR/php.ini-production" "$PHP_INI_DIR/php.ini"
LABEL maintainer="david.baum@uni-leipzig.de" \
      version="1.0"

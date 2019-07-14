FROM ruby:2.4.2-slim



RUN apt-get update && apt-get install -qq -y netcat-openbsd build-essential nodejs libxslt-dev libxml2-dev zlib1g-dev libmysqlclient-dev --fix-missing --no-install-recommends

RUN mkdir /app

WORKDIR /app

ADD Gemfile /app/Gemfile
ADD Gemfile.lock /app/Gemfile.lock

RUN bundle install


RUN rm -fr $(dirname $(dirname $(gem which libv8)))/vendor \
  && apt-get -y autoremove --purge build-essential \
  && apt-get clean \
  && rm -rf /var/cache/apt/archives/* /var/lib/apt/lists/*

ADD . /app

COPY docker-entrypoint.sh /usr/local/bin

ENTRYPOINT ["docker-entrypoint.sh"]
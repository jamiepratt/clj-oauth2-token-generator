FROM clojure:tools-deps

COPY . /usr/src/clj_oauth2_token_generator
WORKDIR /usr/src/clj_oauth2_token_generator

RUN clj -T:build uber

CMD [ "java", "-jar", "target/app-standalone.jar" ]


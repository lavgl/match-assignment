FROM clojure:tools-deps as builder

WORKDIR /usr/src/app
COPY ./deps.edn .
RUN clj -P

COPY . .
RUN clj -T:build uber && \
mv target/match-*.jar target/match.jar

#====

FROM eclipse-temurin:17-jre-alpine

WORKDIR /root/
COPY --from=builder /usr/src/app/target/match.jar ./match.jar

EXPOSE 5001

CMD ["java", "-jar", "match.jar"]
# Dockerfile

FROM maven:3.9.9-eclipse-temurin-17

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 복사
COPY target/movinfo*.jar messenger.jar

COPY build/check-log-and-compress.sh /usr/local/bin/check-log-and-compress.sh
RUN chmod +x /usr/local/bin/check-log-and-compress.sh
RUN echo "0 0 * * * root /usr/local/bin/check-log-and-compress.sh" > /etc/cron.d/check-log-and-compress
RUN chmod 0644 /etc/cron.d/check-log-and-compress

# 실행 및 모니터링
CMD touch /var/log/messenger.log && \
    java -jar messenger.jar > /var/log/messenger.log 2>&1 & tail -F /var/log/messenger.log

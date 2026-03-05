FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
# Cloud Run은 PORT 환경변수로 포트를 지정하므로 쉘 형식으로 확장
ENTRYPOINT ["sh", "-c", "java -jar -Dspring.profiles.active=dev -Duser.timezone=Asia/Seoul app.jar"]
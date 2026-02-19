FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Baixar todas as dependências
RUN mvn dependency:go-offline

# Copiar o código fonte e compilar
COPY src ./src
RUN mvn package -DskipTests

# Imagem de execução
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instalar curl para healthcheck
RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar

# Variáveis de ambiente
ENV DB_URL=jdbc:postgresql://postgres:5432/erp
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

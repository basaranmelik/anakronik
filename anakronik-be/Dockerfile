# --- STAGE 1: Build Aşaması ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn/ .mvn/
RUN mvn dependency:go-offline
COPY src/ src/
RUN mvn package -DskipTests

# --- STAGE 2: Production Aşaması ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 'root' kullanıcısı ile çalışacağı için özel kullanıcı oluşturma,
# yetkilendirme (chown) ve kullanıcı değiştirme (USER) adımları kaldırıldı.

# Derlenmiş .jar dosyasını kopyala
COPY --from=builder /app/target/*.jar app.jar

# Portu belirt
EXPOSE 8080

# Uygulamayı çalıştır
# USER komutu olmadığı için bu komut varsayılan olarak 'root' ile çalışacaktır.
ENTRYPOINT ["java", "-jar", "app.jar"]

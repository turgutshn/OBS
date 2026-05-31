#  Öğrenci Yönetim Sistemi

Java 21 / Spring Boot 3 backend ve React + Vite + Tailwind frontend ile kurumsal kalitede bir öğrenci yönetim platformu.

## Özellikler

### İşlevsel
- **Öğrenci yönetimi**: ekleme, düzenleme, silme, arama, sayfalama
- **Öğretmen yönetimi**: aynı operasyonlar + ders ataması
- **Ders yönetimi**: ders tanımı, kredi, dönem, öğretmen ataması
- **Ders işlemleri**: öğrenciyi derse kayıt, kayıttan düşme, not girişi (vize/final)
- **Otomatik harf notu & GANO**: %40 vize + %60 final hesabı, 4'lük sisteme dönüşüm
- **Katkı payı menüsü**: tahakkuk, ödeme, kısmi ödeme, muafiyet, gecikme takibi
- **Transkript üretimi**: dönemlik döküm + GANO + kredi özetleri, yazdırma
- **Raporlar**: özet metrikler, bölüm dağılımları, mali durum, denetim kayıtları
- **Yönetici / Öğrenci panelleri**: rol bazlı menüler, ayrı rotalar
- **Şifre yönetimi**: değiştirme, oturum açma/kapama, tüm cihazlardan çıkış

### Güvenlik (endüstri standardının üzerinde)
- **Spring Security** + **JWT** access token (15 dk) + opaque **Refresh Token** (7 gün, dönüşümlü)
- **BCrypt(strength=12)** ile parola hash
- **Failed-login lockout** (varsayılan 5 deneme → 15 dk kilit)
- **Rate limiting** (Bucket4j): auth uçları için 5/dk, diğerleri için 120/dk per-IP
- **Audit log** her güvenlik/CRUD işlemini DB'ye yazar (kullanıcı, IP, request-id, sonuç)
- **MDC tabanlı yapılandırılmış log**: `requestId` ve `user` her log satırına eklenir
- **Güvenlik başlıkları**: HSTS, CSP, X-Frame-Options:DENY, Referrer-Policy, X-Content-Type-Options
- **CORS** yapılandırması (whitelist)
- **Refresh token reuse detection**: bir refresh token tekrar kullanılırsa tüm tokenlar iptal edilir
- **Stateless session**, CSRF gerekmez (Bearer token)
- **Hata gizleme**: stacktrace/iç hatalar istemciye sızdırılmaz
- **Şifre politikası**: minimum 8 karakter, en az bir harf + rakam
- **Hibernate SQL parametrik bağlama** ile SQL injection koruması
- **Input validation** her uçta (`Jakarta Validation`)

### Loglama
- Konsol pattern: `timestamp [thread] level [requestId] [user] logger - msg`
- `AUDIT` etiketli iş logları + DB'de `audit_logs` tablosu
- Her isteğe otomatik `X-Request-Id` (yoksa üretilir)

## Mimari

```
turggut/
├── backend/                # Spring Boot 3.3 + Java 21
│   ├── src/main/java/com/turggut/sms
│   │   ├── config/         # Security, AppProperties, Bootstrap, OpenAPI
│   │   ├── security/       # JWT, filters, rate limit, request id
│   │   ├── domain/         # User, Student, Teacher, Course, Enrollment, Fee, Audit, Token
│   │   ├── service/        # Business logic (Auth, Student, Teacher, Course, Enrollment, Fee, Transcript, Report, Audit, Grade)
│   │   ├── controller/     # REST controllers
│   │   ├── dto/            # Request/Response records
│   │   └── exception/      # ApiException + GlobalExceptionHandler
│   └── src/main/resources/db/migration/V1__init_schema.sql  # Flyway
└── frontend/               # Vite + React + Tailwind
    └── src/
        ├── api/            # axios client + interceptor + endpoints
        ├── auth/           # AuthContext + ProtectedRoute
        ├── components/     # UI bileşenleri (Button, Input, Card, Table, Modal, ...)
        ├── pages/admin     # Admin sayfaları
        └── pages/student   # Öğrenci sayfaları
```

## Gereksinimler

- **Java 21** (`java -version` ile doğrulayın)
- **Maven 3.9+**
- **Node.js 20+** ve **npm**
- **PostgreSQL 14+**

## Kurulum

### 1. Veritabanı

PostgreSQL'de bir veritabanı ve kullanıcı oluşturun:

```sql
CREATE DATABASE sms;
CREATE USER sms WITH PASSWORD 'sms_password';
GRANT ALL PRIVILEGES ON DATABASE sms TO sms;
```

### 2. Backend

```bash
cd backend

# Geliştirme ortamı için varsayılanlar application.yml içinde tanımlı.
# İhtiyaca göre ortam değişkenleri ile override edebilirsiniz:
export JWT_SECRET=$(openssl rand -base64 64)        # opsiyonel
export DB_URL=jdbc:postgresql://localhost:5432/sms
export DB_USER=sms
export DB_PASSWORD=sms_password
export BOOTSTRAP_ADMIN_PASSWORD='ChangeMe!23'

mvn spring-boot:run
```

Backend `http://localhost:8080` üzerinde çalışır.
Swagger UI: `http://localhost:8080/swagger-ui.html`


> İlk girişten sonra parolayı mutlaka değiştirin.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend `http://localhost:5173` üzerinde çalışır. Vite, `/api` isteklerini backend'e proxy eder.

## Kullanım Akışı

1. `http://localhost:5173/login` adresine gidin, `admin / Admin!2345` ile giriş yapın.
2. Sol menüden **Öğretmenler** sayfasından öğretmen ekleyin (otomatik kullanıcı + parola).
3. **Dersler** sayfasından ders tanımlayın ve öğretmen atayın.
4. **Öğrenciler** sayfasından öğrenci ekleyin (otomatik kullanıcı + parola).
5. **Ders Atama / Kayıt** sayfasından öğrencileri derslere kaydedin.
6. Aynı sayfadan vize/final notlarını girin → harf notu ve GANO otomatik hesaplanır.
7. **Katkı Payı** sayfasından tahakkuk, ödeme ve muafiyet işlemlerini yönetin.
8. **Raporlar** ve **Denetim Kayıtları** ile sistemi izleyin.
9. Üretilen kullanıcı adı/parola ile öğrenci/öğretmen olarak giriş yapıp kendi panellerini test edin.

## Endpoint Özeti

| Yöntem | Yol | Yetki |
|--------|-----|-------|
| POST | `/api/auth/login` | herkese açık |
| POST | `/api/auth/refresh` | herkese açık (refresh token gerekir) |
| POST | `/api/auth/logout` | herkese açık |
| POST | `/api/auth/change-password` | giriş yapmış |
| GET | `/api/auth/me` | giriş yapmış |
| `*` | `/api/admin/**` | ADMIN |
| `*` | `/api/teacher/**` | ADMIN, TEACHER |
| `*` | `/api/student/**` | giriş yapmış |

## Sınama (Testler & Kapsam)

Proje hem **unit** hem de **integration** testlerle kaplanmıştır. Testler H2 (PostgreSQL uyumlu modda) üzerinde, ayrı bir DB kurulumu gerektirmeden çalışır.

```bash
cd backend

# Sadece testleri çalıştır
mvn test

# Testler + JaCoCo kapsam raporu + %80 kapsam kapısı (coverage gate)
mvn verify
```

- **Unit testler** (`src/test/.../unit`): `GradeCalculator` (harf notu/GANO bantları), `JwtTokenProvider` (token üretimi/parse/geçersiz/kısa secret), `RateLimitingFilter` (limit aşımı → 429, forwarded-for), `SecurityUtils`, `FeeService` (Mockito ile vade aşımı/eksik öğrenci).
- **Integration testler** (`src/test/.../integration`): `@SpringBootTest` + `MockMvc` ile uçtan uca akışlar — auth (login/lockout/refresh-rotation/reuse-detection/logout/change-password), öğrenci/öğretmen/ders/kayıt/katkı payı CRUD ve hata yolları, raporlar.
- Toplam **79 test**, ölçülen **satır kapsamı ~%95**.

### JaCoCo ile kod kapsamı (Neden?)

`pom.xml` içine **JaCoCo** eklendi. Sadece rapor üretmekle kalmaz, `verify` aşamasında bir **kapsam kapısı** uygular:

```xml
<rule>
  <element>BUNDLE</element>
  <limits><limit>
    <counter>LINE</counter><value>COVEREDRATIO</value>
    <minimum>0.80</minimum>   <!-- coverage.minimum -->
  </limit></limits>
</rule>
```

**Neden bunu yaptık?** Kapsam kapısı, hiçbir şey hariç tutulmadan (DTO/config dahil tüm `com.turggut.sms` paketi) **satır kapsamının %80'in altına düşmesini build'i kırarak engeller**. Böylece ileride eklenen kod test edilmeden ana dala giremez; kalite bir tercih değil, otomatik dayatılan bir kural olur. Rapor: `target/site/jacoco/index.html`.

> Not: Testler `maven-surefire-plugin` ile **UTC zaman diliminde** (`-Duser.timezone=UTC`) çalışır. Bu, `Instant` değerlerinin (ör. hesap kilidi `lockedUntil`) yerel saat dilimi kaymasından bağımsız, deterministik olmasını sağlar.

H2 ile geliştirme (Postgres olmadan):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## Docker ile Çalıştırma

Tüm yığın (PostgreSQL + backend + frontend) tek komutla ayağa kalkar:

```bash
docker compose up --build
```

- Frontend: `http://localhost` (nginx, 80)
- Backend API: `http://localhost:8080`
- PostgreSQL: konteyner içi `db:5432` (veriler `db-data` adlı kalıcı volume'da)

Varsayılanları `.env` veya ortam değişkeni ile override edebilirsiniz: `POSTGRES_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `BOOTSTRAP_ADMIN_PASSWORD`, `FRONTEND_PORT` …

```bash
docker compose up --build -d      # arka planda
docker compose logs -f backend    # logları izle
docker compose down               # durdur (volume korunur)
docker compose down -v            # volume dahil sıfırla
```

### Yapı (Neden böyle?)

- **`backend/Dockerfile` — çok aşamalı (multi-stage) build.** İlk aşamada `maven:3.9-eclipse-temurin-21` ile derleyip jar üretir, ikinci aşamada yalnızca `eclipse-temurin:21-jre` üzerine jar'ı kopyalar. **Neden?** Maven ve kaynak kodu final imaja sızmaz → imaj küçük, saldırı yüzeyi az. JDK **21'e sabitlendi** çünkü Lombok 1.18.36, Maven'in güncel varsayılanı olan JDK 25 ile uyumsuz. Konteyner **root olmayan** bir kullanıcıyla çalışır.
- **`frontend/Dockerfile` — build + nginx.** `node:20-alpine` ile `npm run build`, ardından statik çıktı `nginx:1.27-alpine` ile servis edilir. **Neden?** Üretimde Vite dev sunucusu kullanılmaz; nginx statik dosyaları hızlı ve güvenli sunar.
- **`frontend/nginx.conf`.** İki görevi var: (1) SPA fallback (`try_files … /index.html`) ile React Router'ın derin linklerde/sayfa yenilemede çalışması; (2) `/api/` isteklerini `backend:8080`'e proxy'lemek. **Neden?** Frontend göreli `/api` tabanını kullanır; geliştirmede Vite proxy'si, üretimde nginx aynı sözleşmeyi sağlar — kod değişmeden iki ortamda da çalışır.
- **`docker-compose.yml`.** `db` için `healthcheck` (`pg_isready`) tanımlı; backend `service_healthy` koşuluyla bekler. **Neden?** Backend, Flyway migration'larını çalıştırmadan önce veritabanının gerçekten hazır olmasını garantiler; "connection refused" yarış durumları engellenir. DB bağlantısı `SPRING_DATASOURCE_*` ortam değişkenleriyle verilir (Spring relaxed binding) — `application.yml`'deki localhost varsayılanlarını ezer.

## Üretim Notları

- `JWT_SECRET` mutlaka base64-kodlu, en az 32 byte rastgele bir değer olmalı.
- `BOOTSTRAP_ADMIN_PASSWORD` ilk açılış sonrası değiştirilmeli.
- HTTPS arkasında `forward-headers-strategy=framework` ile çalışır; reverse proxy gerekir.
- Cookie tabanlı session yerine Bearer JWT kullanıldığı için CSRF kapatılmıştır; tarayıcıda token `localStorage`'da tutulur. Daha sıkı bir profil için `HttpOnly`/`Secure` cookie içinde tutmak isterseniz `client.js` ve `SecurityConfig` küçük revizyonlarla uyarlanabilir.
- `application.yml` içindeki `app.security.*` değerleri ortam değişkenleri ile override edilebilir.

## Lisans

Dahili kullanım için hazırlanmıştır.

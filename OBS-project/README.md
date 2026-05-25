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

## Sınama

```bash
cd backend && mvn test
```

H2 ile geliştirme (Postgres olmadan):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## Üretim Notları

- `JWT_SECRET` mutlaka base64-kodlu, en az 32 byte rastgele bir değer olmalı.
- `BOOTSTRAP_ADMIN_PASSWORD` ilk açılış sonrası değiştirilmeli.
- HTTPS arkasında `forward-headers-strategy=framework` ile çalışır; reverse proxy gerekir.
- Cookie tabanlı session yerine Bearer JWT kullanıldığı için CSRF kapatılmıştır; tarayıcıda token `localStorage`'da tutulur. Daha sıkı bir profil için `HttpOnly`/`Secure` cookie içinde tutmak isterseniz `client.js` ve `SecurityConfig` küçük revizyonlarla uyarlanabilir.
- `application.yml` içindeki `app.security.*` değerleri ortam değişkenleri ile override edilebilir.

## Lisans

Dahili kullanım için hazırlanmıştır.

# 🧼 HerbaWash - Secure E-Commerce Backend

**HerbaWash** is a secure backend system built using **Spring Boot**, **Spring Security**, **OAuth2**, **JWT**, **Redis**, and **Keycloak**.  
It powers a modern organic soap e-commerce platform with enterprise-grade features including rate limiting, audit logging, MFA, and session tracking.

---

## 🚀 Features

- 🔐 JWT & OAuth2 authentication with role-based access control (RBAC)
- 🛡️ API rate limiting with per-endpoint throttling and Redis blacklisting
- 🔄 Keycloak integration for user sync, session tracking, and scheduled metadata updates
- 🔒 Multi-Factor Authentication (MFA) using Keycloak
- 🧾 Audit logging, login history with Redis ZSet tracking
- 🕒 Session management (auto-logout, expiry, active session sync)
- 💳 Ready for payment gateway integration (coming in v2.0)


---

## 🛠️ Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security + OAuth2
- Redis
- Keycloak
- PostgreSQL / MySQL
- Maven or Gradle
- WebClient (for Keycloak Admin API)

---

## 📂 Project Structure

herbawash/
├── DTO/
├── controller/
├── service/
├── entity/
├── repository/
├── security/
│ └── jwt/
├── utils/
└── config/



---

## ⚙️ Setup & Run

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/herbawash.git
cd herbawash

# Configure application.yml or application.properties
# Add your DB, Redis, Keycloak credentials and endpoints

# Build and run
./mvnw clean install
./mvnw spring-boot:run
```

---
## 🧪 Run Tests
```bash
#run tests
./mvnw test


```

---
## 🧾 Versioning
This project uses Semantic Versioning (SemVer):
| Version  | Changes                                                               |
| -------- | --------------------------------------------------------------------- |
| `v1.0.0` | Initial release: JWT, OAuth2, RBAC, Redis config, and secure login    |
| `v1.1.0` | Audit logging, login history tracking                                 |
| `v1.2.0` | Redis-backed blacklist, per-API rate limiting, token revocation       |



---
## 🧪 API Testing (Postman)
- Postman collection: [`herbawash-collection.json`](docs/postman/herbawash-collection.json)
- Screenshots of sample flows are available in [`docs/screenshots/`](docs/screenshots/)
---
## 🔖 GitHub Release Workflow

🔖 GitHub Release Workflow
Use the GitHub UI or CLI:
```bash

git tag -a v1.0.0 -m "Initial stable release with security features"
git push origin v1.0.0


```

---
## 📄 License
This project is licensed under the MIT License.
---
## 👤 Author

Built with 💚 by Yogi 

Feel free to connect, fork, or contribute!

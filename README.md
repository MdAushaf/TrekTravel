# TrekTravel — Himalayan Trek Discovery & Slot Reservation Platform

> **Tagline:** Climb Higher. Travel Smarter.  
> **Default Application URL:** [http://localhost:8080/TrekTravel/](http://localhost:8080/TrekTravel/)  
> **Runtime Environment:** Apache Tomcat 10.1 • Java 17 LTS (JavaSE-17) • Jakarta EE 6.0 • MySQL 8.0 • Eclipse Dynamic Web Project  

---

## 1. Project Title
**TrekTravel** — Full-Featured High-Altitude Trek Discovery, Departure Scheduling, and Atomic Slot Reservation Web Platform.

---

## 2. Project Description
TrekTravel is an enterprise-grade Java web application engineered without bulky third-party application frameworks (no Spring, no Hibernate, no Maven/Gradle dependencies). Built purely on Jakarta Servlets, JavaServer Pages (JSP), and standard JDBC, it connects mountain outdoor enthusiasts with verified mountain expedition operators across the Indian Himalayas (Uttarakhand, Himachal Pradesh, West Bengal/Sikkim).

The platform guarantees zero-overbooking through transactional database row locking, supports government identity verification for safety and discounts, and provides distinct workspaces for Trekkers, Organizers, and Platform Administrators.

---

## 3. Main Features
- **Public Discovery & Multi-Parameter Filter**: Real-time filtering by state/region, difficulty tier (`EASY`, `MODERATE`, `DIFFICULT`), departure calendar month, and maximum budget threshold.
- **Dynamic Live Inventory & Itinerary Schedules**: Detailed overview of trail altitude, distance, base camp, inclusions, highlights, and dated departure batches with calculated duration endpoints (`start_date + duration_days - 1`).
- **Atomic Concurrency Slot Reservation**: Strict `SELECT ... FOR UPDATE` row locking inside atomic JDBC transactions preventing concurrent race conditions and capacity overselling.
- **Verified Trekker Discount Matrix**: Trekkers who submit and receive approval for their Government ID unlock automatic platform discounts on batch bookings.
- **Safety-First Emergency Profiling**: Mandatory pre-booking collection of trekker emergency contact names, emergency phone numbers, medical disclosures, and fitness self-assessments.
- **Magic-Byte Document Verification**: Government ID uploads (Aadhaar, Passport, Voter ID) inspected via binary magic bytes (`%PDF`, JPEG `FF D8 FF`, PNG `89 50 4E 47`) and streamed safely outside the public web root.
- **Strict 48-Hour Cancellation Window**: Self-service booking cancellation allowed up to 48 hours prior to departure, immediately restoring batch capacity.
- **Comprehensive CSRF & RBAC Defense**: Cryptographic session-scoped CSRF token validation on every state-altering POST, alongside real-time database active status verification.

---

## 4. User Roles

### A. TREKKER
- Discover upcoming Himalayan expeditions and filter departures by difficulty and date.
- Complete trekker profile: date of birth, emergency contact, fitness level, and medical disclosures.
- Upload Government ID proof (PDF, JPG, PNG under 2 MB) to request **Verified Trekker** status.
- Book 1 to 10 participants per batch with live pricing calculation.
- Manage personal bookings ledger and cancel reservations up to 48 hours prior to start date.

### B. ORGANIZER
- Manage commercial trek listings (itinerary, altitude, distance, season, inclusions, highlights).
- Publishing/editing any trek automatically routes it to `PENDING` status for administrative review.
- Schedule dated departure batches, set participant capacity (1–50 slots), price per person, and verified discount percentages.
- View live participant manifests and emergency contact information for scheduled departures.

### C. ADMIN
- Central platform operations dashboard with metrics across active users, treks, batches, and bookings.
- Review and verify/reject uploaded Trekker Government ID proofs with streaming document inspection.
- Authorize or reject Organizer trek listings before they become publicly bookable.
- Platform user management: inspect accounts and instantly activate or `BLOCK` offending accounts with real-time session invalidation.
- Platform-wide booking ledger oversight with emergency administrative cancellation.

---

## 5. Technology Stack
- **Backend / Language:** Java 17 LTS (OpenJDK / Oracle JDK 17.0.12+)
- **Server / Runtime:** Apache Tomcat 10.1 (Jakarta EE 6.0 Servlets & JSPs)
- **Database:** MySQL 8.0+ (InnoDB Engine, utf8mb4 charset, row-level locking)
- **Database Driver:** MySQL Connector/J 8.3.0 (`mysql-connector-j-8.3.0.jar` in `WEB-INF/lib/`)
- **Persistence Layer:** Plain JDBC via `PreparedStatement` (zero ORM overhead)
- **Security:** Standard Java Cryptography (`javax.crypto.SecretKeyFactory`, `PBKDF2WithHmacSHA256`, 120,000 iterations, constant-time hash comparison)
- **Frontend / Styling:** Bootstrap 5.3 (CDN), Custom CSS (Mountain Forest Palette), Vanilla JavaScript
- **IDE Metadata:** Eclipse IDE for Enterprise Java and Web Developers (Facet Dynamic Web Module 6.0, Java 17)

---

## 6. Project Architecture

The application adheres to a clean, decoupled MVC (Model-View-Controller) design pattern:

```
TrekTravel/
├── database/
│   └── trektravel.sql               # MySQL schema, table definitions & seed data
├── .settings/                       # Eclipse WST, JDT and Facet configuration
├── .classpath & .project            # Eclipse Dynamic Web Project definition
├── .gitignore                       # Clean Git configuration excluding compiled binaries
├── DECISIONS.md                     # Architectural rationale & sensible defaults
├── RUN.md                           # Operational build, run and test guide
├── README.md                        # Project documentation
└── src/
    └── main/
        ├── java/
        │   ├── db.properties        # Local database credentials & upload path
        │   ├── db.properties.example# Template configuration for version control
        │   └── com/trektravel/
        │       ├── model/           # POJO Data models (User, Trek, Batch, Booking, TrekkerProfile)
        │       ├── dao/             # Data Access Objects executing JDBC queries & transactions
        │       ├── filter/          # SecurityFilter (UTF-8, Headers, CSRF, RBAC, DB check)
        │       ├── servlet/         # Controllers handling HTTP requests & dispatching views
        │       └── util/            # Utilities (DBUtil, PasswordUtil, H, Validate, UploadUtil, PriceUtil)
        └── webapp/
            ├── css/style.css        # Mountain theme stylesheet
            ├── js/app.js            # Client-side validation & live modal price calculations
            └── WEB-INF/
                ├── web.xml          # Jakarta Servlet 6.0 deployment descriptor
                ├── lib/             # Third-party runtime jars (mysql-connector-j-8.3.0.jar)
                ├── jspf/            # Reusable header and footer JSP fragments
                └── views/           # Encapsulated JSP views (public, trekker, organizer, admin)
```

---

## 7. Database Setup Instructions

1. Launch your MySQL 8.0 server (CLI or MySQL Workbench).
2. Execute the provided setup script located at `database/trektravel.sql`:
   ```bash
   mysql -u root -p < database/trektravel.sql
   ```
3. This creates the `trektravel` database, establishes all 5 tables (`users`, `trekker_profiles`, `treks`, `batches`, `bookings`) with foreign keys, and seeds test accounts, approved Himalayan treks, upcoming departure batches, and sample bookings.
4. Verify the database:
   ```sql
   USE trektravel;
   SELECT email, role FROM users;
   SELECT COUNT(*) FROM treks;
   SELECT COUNT(*) FROM batches;
   ```

---

## 8. Eclipse Import Instructions

1. Open **Eclipse IDE for Enterprise Java and Web Developers**.
2. Navigate to **File → Import → General → Existing Projects into Workspace**.
3. Click **Browse** and select the `TrekTravel` directory.
4. Verify that `TrekTravel` is selected with the Dynamic Web Project nature, then click **Finish**.
5. Configure the Server Runtime in Eclipse:
   - Right-click `TrekTravel` → **Properties → Targeted Runtimes**.
   - Select **Apache Tomcat v10.1** and verify Java 17 is targeted.
6. Right-click the project → **Run As → Run on Server** → choose your Tomcat 10.1 instance.

---

## 9. JDK 17 Configuration

- Ensure JDK 17 (JavaSE-17) is installed on your operating system.
- Verify in your terminal:
  ```bash
  javac -version
  java -version
  ```
- In Eclipse: **Window → Preferences → Java → Installed JREs** must include JDK 17 as an execution environment.

---

## 10. Tomcat 10.1 Configuration

- Apache Tomcat 10.1 is required (Jakarta EE namespace `jakarta.servlet.*`).
- Set environment variables prior to running from terminal:
  ```cmd
  set "JAVA_HOME=C:\Program Files\Java\jdk-17"
  set "CATALINA_HOME=C:\Path\To\apache-tomcat-10.1.x"
  ```
- Port 8080 is the default HTTP connector in `conf/server.xml`.

---

## 11. How to Run the Project Locally

### Option A: From Command Line (Exploded WAR Build)
1. Configure `src/main/java/db.properties` with your local MySQL password.
2. Compile and package the exploded WAR:
   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File build_app.ps1
   ```
3. Copy `build/TrekTravel` into your Tomcat `webapps/` folder.
4. Start Tomcat:
   ```cmd
   catalina.bat run
   ```

### Option B: From Eclipse IDE
- Right-click `TrekTravel` → **Run As → Run on Server** → Select Tomcat 10.1.

---

## 12. Default Application URL
Navigate to the application home in any modern browser:
```
http://localhost:8080/TrekTravel/
```

### Seed Demo Logins:
| Role | Email | Password | Pre-configured State |
|---|---|---|---|
| **ADMIN** | `admin@trektravel.com` | `Admin@123` | Full dashboard access, ID review queue, trek approvals |
| **ORGANIZER** | `organizer@trektravel.com` | `Organizer@123` | Operator for Kedarkantha, Hampta Pass, Triund, Valley of Flowers |
| **ORGANIZER 2** | `organizer2@trektravel.com` | `Organizer@123` | Operator for Kheerganga, Sandakphu, Har Ki Dun, Dodital |
| **TREKKER (Verified)** | `trekker@trektravel.com` | `Trekker@123` | **APPROVED** Profile — qualifies for verified discounts |
| **TREKKER (Pending)** | `newbie@trektravel.com` | `Trekker@123` | **PENDING** Profile — books at standard rates |
| **TREKKER (Fresh)** | `fresh@trektravel.com` | `Trekker@123` | No profile yet — prompted to submit emergency contact before booking |

---

## 13. Important Configuration Details

- **Database Properties (`src/main/java/db.properties`)**:
  ```properties
  db.url=jdbc:mysql://localhost:3306/trektravel?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
  db.user=root
  db.password=CHANGE_ME
  upload.dir=
  ```
- **File Upload Directory**:
  By default, `upload.dir` defaults to `<user.home>/trektravel_uploads` outside the public web root. Uploaded documents are served strictly through `/admin/viewId` with role-based validation.
- **CSRF Token Header / Parameter**:
  All state-altering POST requests require the session-scoped CSRF token supplied via `<%= H.csrf(session) %>` or the `X-CSRF-Token` header.

---

## 14. Future Improvements
- Integration with third-party payment gateways (Razorpay / Stripe) for automated refund settlement on cancellation.
- Automated email and SMS notifications for departure schedule changes and booking confirmations.
- GPS trail GPX track visualization on interactive maps using Leaflet.js.
- Weather forecast API integration displaying live high-altitude conditions for active departures.

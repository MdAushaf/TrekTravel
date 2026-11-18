# TrekTravel — Operational Run & Deployment Guide (Windows)

> **Application URL:** [http://localhost:8080/TrekTravel/](http://localhost:8080/TrekTravel/)  
> **Environment:** Windows • Apache Tomcat 10.1.61 • Java 17 LTS (JDK 17.0.12) • MySQL 8.0  

---

## 1. Quick Service Control (Tomcat & MySQL)

### Start Tomcat 10.1
From PowerShell or Command Prompt, run:
```powershell
$env:CATALINA_HOME = "C:\Users\MD AUSHAF AHMED\apache-tomcat-10.1.61"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
& "$env:CATALINA_HOME\bin\catalina.bat" run
```
*Or execute the pre-configured root launcher:*
```cmd
run_catalina.bat
```

### Stop Tomcat 10.1
```cmd
stop_tomcat.bat
```
*Or terminate the process:*
```powershell
Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -match "Tomcat" -or $_.CommandLine -match "tomcat" } | Stop-Process -Force
```

### Check Port & Status
```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object LocalAddress, LocalPort, State
```

---

## 2. Rebuild & Redeploy Exploded WAR (Without Eclipse)

To compile all Java sources under Java 17 with Jakarta EE 6.0 APIs, sync assets, and deploy directly into Tomcat's `webapps/` folder, execute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File build_app.ps1
Copy-Item -Path "TrekTravel\build\TrekTravel\*" -Destination "C:\Users\MD AUSHAF AHMED\apache-tomcat-10.1.61\webapps\TrekTravel\" -Recurse -Force
```

### Manual CLI Step-by-Step Equivalent:
1. **Clean & create deploy target:**
   ```powershell
   Remove-Item -Recurse -Force "TrekTravel\build\TrekTravel" -ErrorAction SilentlyContinue
   New-Item -ItemType Directory -Path "TrekTravel\build\TrekTravel\WEB-INF\classes" -Force
   Copy-Item -Path "TrekTravel\src\main\webapp\*" -Destination "TrekTravel\build\TrekTravel\" -Recurse -Force
   ```
2. **Compile Java sources:**
   ```powershell
   & "C:\Program Files\Java\jdk-17\bin\javac.exe" --release 17 -encoding UTF-8 -cp "C:\Users\MD AUSHAF AHMED\apache-tomcat-10.1.61\lib\servlet-api.jar;C:\Users\MD AUSHAF AHMED\apache-tomcat-10.1.61\lib\jsp-api.jar;TrekTravel\build\TrekTravel\WEB-INF\lib\*" -d "TrekTravel\build\TrekTravel\WEB-INF\classes" (Get-ChildItem -Path "TrekTravel\src\main\java" -Filter "*.java" -Recurse).FullName
   ```
3. **Copy configuration:**
   ```powershell
   Copy-Item -Path "TrekTravel\src\main\java\db.properties" -Destination "TrekTravel\build\TrekTravel\WEB-INF\classes\" -Force
   ```
4. **Deploy to Tomcat:**
   ```powershell
   Copy-Item -Path "TrekTravel\build\TrekTravel\*" -Destination "C:\Users\MD AUSHAF AHMED\apache-tomcat-10.1.61\webapps\TrekTravel\" -Recurse -Force
   ```

---

## 3. Running Smoke Tests (12/12 Automated Suite)

An end-to-end regression and functional smoke test suite is available at `smoke_tests.ps1`. It exercises full browser workflows including session cookies, dynamic CSRF token extraction, price calculations, RBAC checks, and database mutations:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File smoke_tests.ps1
```

### Verified Test Cases:
| # | Test Case | Target Endpoint / Workflow | Expected Result |
|---|---|---|---|
| 1 | Public Brand Landing | `GET /TrekTravel/` | HTTP 200, renders hero & TrekTravel branding |
| 2 | Dynamic Trek Search | `GET /TrekTravel/search?difficulty=MODERATE` | HTTP 200, filters correctly |
| 3 | Trek Detail & Batches | `GET /TrekTravel/trek?id=1` | HTTP 200, lists upcoming departure batches |
| 4 | Authentication & Security | `POST /TrekTravel/login` | Bad password error; valid login redirects to role page |
| 5 | Verified Discount Display | `GET /TrekTravel/trek?id=1` | Verified trekker sees discount; pending profile sees full rate |
| 6 | Slot Booking | `POST /TrekTravel/trekker/book` | Creates confirmed booking row in database |
| 7 | Inventory Guard | `POST /TrekTravel/trekker/book` | Overbooking or participant cap (>10) rejected |
| 8 | 48h Cancellation | `POST /TrekTravel/trekker/cancel` | Status updated to `CANCELLED`, inventory released |
| 9 | Role-Based Access Control | `GET /TrekTravel/admin/dashboard` | Anonymous redirected to `/login`; Trekker returns HTTP 403 |
| 10 | Organizer Tenant Isolation | `GET /TrekTravel/organizer/trek/form?id=5` | Foreign trek ID returns HTTP 404 |
| 11 | Admin Approval Lifecycle | `POST /TrekTravel/admin/treks/review` | Admin approves PENDING trek; appears in public search |
| 12 | CSRF Defense | `POST /TrekTravel/login` (without token) | HTTP 403 Forbidden |

---

## 4. Optional: Working with Eclipse IDE

The project includes complete, pre-configured Eclipse IDE metadata:
- `.project` — Dynamic Web Project nature
- `.classpath` — JavaSE-17 and Apache Tomcat v10.1 targeted container
- `.settings/org.eclipse.wst.common.project.facet.core.xml` — Facets `jst.web 6.0` and `java 17`

### Steps to import into Eclipse:
1. Open Eclipse IDE for Enterprise Java and Web Developers.
2. Select **File → Import → General → Existing Projects into Workspace**.
3. In **Select root directory**, browse to:
   ```
   C:\Users\MD AUSHAF AHMED\OneDrive\Desktop\New folder (2)\TrekTravel
   ```
4. Click **Finish**.
5. Ensure Targeted Runtime is enabled:
   - Right-click `TrekTravel` → **Properties → Targeted Runtimes**.
   - Verify **Apache Tomcat v10.1** is checked.
6. Right-click `TrekTravel` → **Run As → Run on Server** → Select Tomcat 10.1 → **Finish**.

---

## 5. Demo Accounts

| Role | Email | Password | Details |
|---|---|---|---|
| **ADMIN** | `admin@trektravel.com` | `Admin@123` | Platform oversight, ID verification queue, trek approvals |
| **ORGANIZER** | `organizer@trektravel.com` | `Organizer@123` | Operator of Kedarkantha, Hampta Pass, Triund, Valley of Flowers |
| **ORGANIZER 2** | `organizer2@trektravel.com` | `Organizer@123` | Operator of Kheerganga, Sandakphu, Har Ki Dun, Dodital |
| **TREKKER (Verified)** | `trekker@trektravel.com` | `Trekker@123` | Approved Government ID — qualifies for verified discounts |
| **TREKKER (Pending)** | `newbie@trektravel.com` | `Trekker@123` | Pending ID review — books at standard rates |
| **TREKKER (Fresh)** | `fresh@trektravel.com` | `Trekker@123` | No profile yet — prompted to submit emergency details |

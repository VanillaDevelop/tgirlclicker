## TGirl.click Web-App

This is the web application for TGirl.click, a link shortening/redirect service. 
It is a relatively simple application written in Spring Boot with a Thymeleaf frontend. 
I built this application to play around with the ELK stack (Elasticsearch, Logstash, Kibana) I set up on my VPS, 
and to deploy a funny little application on this domain I've acquired a while ago.

## Features
**Link Shortening**  
Users can create redirect links to any URL. The links are stored using a unique alphanumeric identifier ("shorthand") 
which is between 4 and 8 characters long. The links are stored in a **PostgreSQL** database.

**Pride Theming 🏳️‍⚧️**  
The design of the web-app is of course heavily pride-themed, in line with the domain name. The on-the-nose theming 
probably makes this project less suitable for highlighting my professional skills, but I'm sure the target audience 
will appreciate the aesthetics.

**Comprehensive Logging**  
Since the main exercise of this project was to get a better feel for logging, this application heavily focuses on 
traceability. Logs are written using **SLF4J** and **Logback**, and are sent to a Logstash instance running on the 
same server. **Mapped Diagnostic Context (MDC)** is used via an interceptor to add additional information to the logs, 
such as the IP of the client as well as its user agent. These values are also stored in the database for a soft approach 
to spam prevention, by limiting the number of links an individual IP can create each day.

**...And More?** 

## Deployment
**For local development**, build the project using Gradle:

```bash
./gradlew clean build
```

Then run the spring boot application via your IDE. The two dependencies for local development are:
- A local PostgreSQL database, satisfying the connection parameters in `application-dev.properties`.
- A local Logstash instance, which accepts TCP connections as configured in `logback-spring.xml`.

**For production**, deployment is coordinated via a GitHub Actions workflow found in `.github/workflows/deploy.yml`. 
Similar to the development environment, it is assumed that a Logstash instance is accepting TCP connections as defined 
in `logback-spring.xml`. In production, the PostgreSQL database is configured via environment variables.  

Set environment variables as defined in `application-prod.properties`:

| Variable Name  | Description                        |
|----------------|------------------------------------|
| `DATABASE_URL` | Connection String for the database |
| `DB_USERNAME`  | Username for the database          |
| `DB_PASSWORD`  | Password for the database          |

The GitHub Actions workflow additionally requires the following parameters be set:

| Variable Name           | Type      | Description                           |
|-------------------------|-----------|---------------------------------------|
| `SSH_PRIVATE_KEY`       | Secret    | Private SSH key for deployment        |
| `SSH_IP_ADDRESS`        | Secret    | IP address of the server to deploy to |
| `SSH_PORT`              | Secret    | Known hosts file for SSH              |
| `SSH_SERVER_PUBLIC_KEY` | Variables | Public key of the server for SSH      |

The workflow expects some legwork to be done on the server beforehand:

- As application is not dockerized, Java must be set up (e.g. OpenJDK 17)
- deploy user must be set up with SSH access and user lingering
- PostgreSQL must be set up with a database and user matching the environment variables
- Logstash must be set up to accept TCP connections on the port defined in `logback-spring.xml` on the local server

Then set up the directory for the application: 
```bash
mkdir -p /opt/tgirlclicker
chown deploy:deploy /opt/tgirlclicker
```

Set up the systemd user service:
```bash
mkdir -p home/deploy/.config/systemd.user
```

Create the file `home/deploy/.config/systemd.user/tgirlclicker.service` with the following content:
```ini
[Unit]
Description=TGirl.click Web-App
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/tgirlclicker
User=deploy
EnvironmentFile=/opt/tgirlclicker/.env
ExecStart=/usr/bin/java -jar /opt/tgirlclicker/tgirlclicker.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=default.target
```

Then enable and start the service:
```bash
systemctl --user daemon-reload
systemctl --user enable tgirlclicker.service
systemctl --user start tgirlclicker.service
```
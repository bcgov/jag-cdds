# jag-cdds
[![Lifecycle:Stable](https://img.shields.io/badge/Lifecycle-Stable-97ca00)](https://github.com/bcgov/jag-cdds)
[![Maintainability](https://api.codeclimate.com/v1/badges/5a7027d5cc5800eeb2fe/maintainability)](https://codeclimate.com/github/bcgov/jag-cdds/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/5a7027d5cc5800eeb2fe/test_coverage)](https://codeclimate.com/github/bcgov/jag-cdds/test_coverage)

CDDS webMethods Retirement Project


### Recommended Tools
* Intellij
* Docker
* Docker Compose
* Maven
* Java 21
* Lombok

### Application Endpoints

Local Host: http://127.0.0.1:8080

Code Climate: https://codeclimate.com/github/bcgov/jag-cdds

### Required Environmental Variables

BASIC_AUTH_PASS: The password for the basic authentication. This can be any value for local.

BASIC_AUTH_USER: The username for the basic authentication. This can be any value for local.

ORDS_HOST: The url for ords rest package.

SCJ_HOST: The url for the SCJ CDDS endpoint.

SPLUNK_HTTP_URL: The url for the splunk hec. For local splunk this value should be 127.0.0.1:8088 for
remote do not include /services/collector.

SPLUNK_TOKEN: The bearer token to authenticate the application.

SPLUNK_INDEX: The index that the application will push logs to. The index must be created in splunk
before they can be pushed to.

### Building the Application
1) Set intellij to use java 21 for the project modals and sdk
2) Run ``mvn compile``
3) Make sure ```target/generated-sources/xjc``` folder in included in module path


### Running the application

Recommended Option) VSCode Dev Container
- Use the project's VSCode Dev Container

Option A) Intellij
1) Create intellij run configuration from CDDS Application
2) Set env variables. See the .env-template
3) Run the application

Option B) Jar
1) Run ```mvn package```
2) Run ```java -jar ./target/cdds-application.jar```

Option C) Docker
1) Run ```mvn package```
2) Run ```docker build -t cdds-civil-api .``` from root folder
3) Run ```docker run -p 8080:8080 cdds-civil-api```

Option D) Docker Compose
1) Run ```mvn package```
2) Run ```docker-compose up cdds-civil-api```

Option D) Eclipse
1) Clone the project into a local folder.
2) Import the Maven project using the Maven Project Import Wizard.
3) Set Variables either as Windows/Linux Environmental variables or POM goal Environment Variables:

BASIC_AUTH_PASS

BASIC_AUTH_USER

ORDS_HOST

SPLUNK_HTTP_URL

SPLUNK_TOKEN

SPLUNK_INDEX


4) Create POM goals: clean install, spring-boot:run  (when running locally).

### Pre Commit
1) Do not commit \CRLF use unix line enders
2) Run the linter ```mvn spotless:apply```

### JaCoCo Coverage Report
1) Run ```mvn clean verify```
3) Open ```target/site/jacoco/index.html``` in a browser

### Vunerability Scanning - Locally
1) Run ```docker build --no-cache -t cdds/jag-cdds:test .```
2) Run ```docker run -it -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/Library/Caches:/root/.cache/ aquasec/trivy image --insecure cdds/jag-cdds:test```

The above steps build the jag-cdds container and then use the Trivy docker container to scan the resulting images.  The output will look something like this:

```
cdds/jag-cdds:test (alpine 3.19.1)

Total: 10 (UNKNOWN: 0, LOW: 2, MEDIUM: 8, HIGH: 0, CRITICAL: 0)

┌───────────────┬────────────────┬──────────┬────────┬───────────────────┬───────────────┬───────────────────────────────────────────────────────────┐
│    Library    │ Vulnerability  │ Severity │ Status │ Installed Version │ Fixed Version │                           Title                           │
├───────────────┼────────────────┼──────────┼────────┼───────────────────┼───────────────┼───────────────────────────────────────────────────────────┤
│ busybox       │ CVE-2023-42363 │ MEDIUM   │ fixed  │ 1.36.1-r15        │ 1.36.1-r17    │ busybox: use-after-free in awk                            │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2023-42363                │
│               ├────────────────┤          │        │                   ├───────────────┼───────────────────────────────────────────────────────────┤
│               │ CVE-2023-42366 │          │        │                   │ 1.36.1-r16    │ busybox: A heap-buffer-overflow                           │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2023-42366                │
├───────────────┼────────────────┤          │        │                   ├───────────────┼───────────────────────────────────────────────────────────┤
│ busybox-binsh │ CVE-2023-42363 │          │        │                   │ 1.36.1-r17    │ busybox: use-after-free in awk                            │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2023-42363                │
│               ├────────────────┤          │        │                   ├───────────────┼───────────────────────────────────────────────────────────┤
│               │ CVE-2023-42366 │          │        │                   │ 1.36.1-r16    │ busybox: A heap-buffer-overflow                           │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2023-42366                │
├───────────────┼────────────────┤          │        ├───────────────────┼───────────────┼───────────────────────────────────────────────────────────┤
│ libcrypto3    │ CVE-2024-4603  │          │        │ 3.1.4-r5          │ 3.1.5-r0      │ openssl: Excessive time spent checking DSA keys and       │
│               │                │          │        │                   │               │ parameters                                                │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2024-4603                 │
│               ├────────────────┼──────────┤        │                   ├───────────────┼───────────────────────────────────────────────────────────┤
│               │ CVE-2024-2511  │ LOW      │        │                   │ 3.1.4-r6      │ openssl: Unbounded memory growth with session handling in │
│               │                │          │        │                   │               │ TLSv1.3                                                   │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2024-2511                 │
├───────────────┼────────────────┼──────────┤        │                   ├───────────────┼───────────────────────────────────────────────────────────┤
│ libssl3       │ CVE-2024-4603  │ MEDIUM   │        │                   │ 3.1.5-r0      │ openssl: Excessive time spent checking DSA keys and       │
│               │                │          │        │                   │               │ parameters                                                │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2024-4603                 │
│               ├────────────────┼──────────┤        │                   ├───────────────┼───────────────────────────────────────────────────────────┤
│               │ CVE-2024-2511  │ LOW      │        │                   │ 3.1.4-r6      │ openssl: Unbounded memory growth with session handling in │
│               │                │          │        │                   │               │ TLSv1.3                                                   │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2024-2511                 │
├───────────────┼────────────────┼──────────┤        ├───────────────────┼───────────────┼───────────────────────────────────────────────────────────┤
│ ssl_client    │ CVE-2023-42363 │ MEDIUM   │        │ 1.36.1-r15        │ 1.36.1-r17    │ busybox: use-after-free in awk                            │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2023-42363                │
│               ├────────────────┤          │        │                   ├───────────────┼───────────────────────────────────────────────────────────┤
│               │ CVE-2023-42366 │          │        │                   │ 1.36.1-r16    │ busybox: A heap-buffer-overflow                           │
│               │                │          │        │                   │               │ https://avd.aquasec.com/nvd/cve-2023-42366                │
└───────────────┴────────────────┴──────────┴────────┴───────────────────┴───────────────┴───────────────────────────────────────────────────────────┘
```

---

## Release Process

- The initial release is manually triggered using the pipeline defined in [main.yml](./.github/workflows/main.yml).
	- This can be triggered on any branch.
	- This pipeline deploys the code to the `dev` environment.

- The promotions to TEST and PROD are performed manually using the pipeline defined in [openshift-imagetagging.yml.](./.github/workflows/openshift-imagetagging.yml)
	- This pipeline can also be triggered on any branch.

### Steps
- Create a `release/<verion>` branch.
- Update the code in the branch of the release.  This could be as simple as a version bump.
- Open a PR titled `Release/<version>`.
- Trigger [main.yml](./.github/workflows/main.yml) on the `release/<verion>` branch to deploy to `dev`.
- Run [automationtestapi.yml](./.github/workflows/automationtestapi.yml) to test the deployment.
- Run [zap-scan-api.yml](./.github/workflows/zap-scan-api.yml) to scan the deployment for vulnerabilities.
- Promote to `test` using [openshift-imagetagging.yml.](./.github/workflows/openshift-imagetagging.yml).
- Wait for UAT to complete.
- Promote to `prod` using [openshift-imagetagging.yml.](./.github/workflows/openshift-imagetagging.yml).
- Approve and merge the PR.
- Generate a GitHub release with a tag.  The tag and title should be `<version>`.

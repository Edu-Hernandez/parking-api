# parking-api

API de estacionamiento en Spring Boot. Este documento recoge cómo preparar el entorno en Windows y cómo arrancar el proyecto en Cursor.

## Qué usa el proyecto

| Pieza | Valor |
| --- | --- |
| Herramienta de build | Maven (wrapper `./mvnw`) |
| Spring Boot | 4.1.2 |
| Java | 25 |
| Group | `com.edu` |
| Artifact | `parking-api` |
| Paquete | `com.edu.parking_api` |
| Base de datos | PostgreSQL 17 (Docker), puerto local `5433` |
| Esquema | Flyway (`db/migration`) |
| API | `http://localhost:8080` |

El paquete no es `com.edu.parking-api`: un nombre de paquete Java no admite guiones, así que el proyecto usa `com.edu.parking_api`.

## 1. Instalar el JDK en Windows

1. Instala un JDK 25 para Windows (por ejemplo [Eclipse Temurin 25](https://adoptium.net/) u Oracle JDK 25). La versión tiene que coincidir con `<java.version>25</java.version>` del `pom.xml`.
2. Anota la carpeta de instalación. `JAVA_HOME` apunta a la raíz del JDK, no a `bin`. Ejemplo: `C:\Program Files\Eclipse Adoptium\jdk-25`.

Comprueba la instalación en una terminal nueva:

```bash
java -version
javac -version
```

## 2. Variables de entorno

En Windows: **Configuración → Sistema → Acerca de → Configuración avanzada del sistema → Variables de entorno**.

1. Crea o edita la variable de usuario o de sistema `JAVA_HOME` con la ruta raíz del JDK.
2. En `Path`, agrega la carpeta `bin` del JDK: `%JAVA_HOME%\bin`.
3. Cierra y vuelve a abrir la terminal (y Cursor) para que tomen los valores nuevos.

Comprueba:

```bash
echo $JAVA_HOME
java -version
```

En Git Bash, `echo $JAVA_HOME` debe mostrar la ruta del JDK 25.

## 3. Extensiones de Cursor

Instala estas extensiones para trabajar el proyecto Java desde Cursor:

- **IntelliJ IDEA Keybindings**, para usar atajos de IntelliJ.
- **Language Support for Java(TM) by Red Hat**, soporte del lenguaje.
- **Debugger for Java**, depuración.
- **Test Runner for Java**, ejecución de tests.
- **Maven for Java**, proyectos Maven.
- **Project Manager for Java**, vista de proyectos y dependencias.

El **Extension Pack for Java** agrupa lenguaje, depurador, tests, Maven y Project Manager. Si lo instalas, no hace falta repetir esas cinco por separado. La de atajos de IntelliJ se instala aparte.

Cuando Cursor pida el JDK, selecciona el JDK 25 de `JAVA_HOME`. Si hace falta fijarlo a mano, en ajustes de la extensión de Java indica esa misma ruta (`java.jdt.ls.java.home` o el runtime de Java 25).

## 4. Proyecto inicial en Spring Initializr

El esqueleto se generó en [start.spring.io](https://start.spring.io) con esta configuración:

| Campo | Valor |
| --- | --- |
| Project | Maven |
| Language | Java |
| Spring Boot | 4.1.2 |
| Group | `com.edu` |
| Artifact | `parking-api` |
| Name | `parking-api` |
| Package name | `com.edu.parking_api` |
| Packaging | Jar |
| Java | 25 |

Dependencias:

- Spring Web
- Spring Data JPA
- Validation
- PostgreSQL Driver
- Spring Boot DevTools
- Flyway (`flyway-core` y `flyway-database-postgresql`), agregada después en el `pom.xml`

Descarga el ZIP, descomprímelo y abre esa carpeta en Cursor (**File → Open Folder**).

## 5. Configuración dentro de Cursor

### Variables de entorno (`.env`)

En la raíz del proyecto crea `.env`. Docker Compose lo lee solo. Spring Boot no: hay que exportarlo en la terminal antes de arrancar la API.

```properties
POSTGRES_DB=parking_db
POSTGRES_USER=parking_user
POSTGRES_PASSWORD=tu_contraseña
DB_PASSWORD=tu_contraseña
JWT_SECRET=una_clave_de_al_menos_32_caracteres
```

`POSTGRES_PASSWORD` es la clave del contenedor. `DB_PASSWORD` es la que usa la aplicación. Deben ser la misma. `JWT_SECRET` firma los tokens y debe tener al menos 32 caracteres; sin ella la API no arranca. `.env` está en `.gitignore` y no se sube al repositorio.

### Docker

Hace falta [Docker Desktop](https://www.docker.com/products/docker-desktop/) en ejecución. `docker-compose.yml` levanta PostgreSQL 17:

- Contenedor: `parking-postgres`
- Imagen: `postgres:17-alpine`
- Puerto del host: `5433` (dentro del contenedor sigue siendo `5432`)
- Volumen: `parking_data`
- Base, usuario y contraseña: los de `.env`

### `application.properties`

`src/main/resources/application.properties` conecta la API a esa base y publica el servidor en el puerto 8080:

```properties
spring.application.name=parking-api

spring.datasource.url=jdbc:postgresql://localhost:5433/${POSTGRES_DB}
spring.datasource.username=${POSTGRES_USER}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

server.port=8080

# JWT
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-minutes=15
app.jwt.refresh-expiration-days=14
```

`ddl-auto=validate` solo comprueba que las entidades coincidan con las tablas. Quien crea y cambia el esquema es Flyway. El puerto `5433` tiene que coincidir con el mapeo de `docker-compose.yml`.

## 6. Esquema de la base con Flyway

En `application.properties`, `spring.jpa.hibernate.ddl-auto` pasa de `update` a `validate`. El esquema deja de depender de Hibernate y pasa a Flyway.

### Por qué hacemos este cambio

- **`update`:** Hibernate intenta modificar la estructura de la base de datos automáticamente, a partir de las entidades Java.
- **`validate`:** Hibernate solamente comprueba que las entidades coincidan con las tablas existentes. Si no coinciden, la API no arranca.
- **Flyway:** crea y modifica las tablas mediante archivos SQL versionados en `db/migration`.

Si se dejara `update` junto con Flyway, los dos intentarían tocar el mismo esquema: Flyway con los `.sql` e Hibernate por su cuenta. Con `validate`, solo Flyway cambia las tablas.

### Estructura del proyecto

Cada módulo (`auth`, `users`, `companies`, `members`) tiene las mismas capas:

```text
presentation    controllers y DTOs de entrada/salida (validación con Bean Validation)
application     casos de uso y comandos
domain          modelos, puertos (repositorios) y excepciones; sin dependencias de Spring
infrastructure  entidades JPA, repositorios Spring Data, mappers y adapters
```

```bash
Controller → XxxUseCase → XxxRepository (puerto)
                               ↑
                   XxxRepositoryAdapter → XxxJpaRepository → BD
```

Todas las rutas llevan el prefijo `/api/v1`, que se define una sola vez en `WebConfig`; los controllers no lo repiten.

### Carpetas

Dentro de `src/main/resources/`:

1. Clic derecho → **New Folder** → `db`.
2. Clic derecho sobre `db` → **New Folder** → `migration`.
3. Clic derecho sobre `migration` → **New File** → `V1__create_initial_schema.sql`.

```text
src/
└── main/
    └── resources/
        ├── application.properties
        └── db/
            └── migration/
                └── V1__create_initial_schema.sql
```

`spring.flyway.locations=classpath:db/migration` apunta a esa carpeta.

### Nombre del archivo

`V1__create_initial_schema.sql` se lee así:

- `V1`: versión 1 de la migración.
- `__`: dos guiones bajos. Separan la versión de la descripción. Con uno solo, Flyway no reconoce el archivo.
- `create_initial_schema`: qué hace esa versión.
- `.sql`: instrucciones SQL.

Cuando la estructura cambie, se agrega una versión nueva. Una migración ya ejecutada no se modifica.

Migraciones actuales:

```text
migration/
├── V1__create_initial_schema.sql        usuarios, empresas, roles y membresías
├── V2__create_refresh_tokens.sql        sesiones con refresh token
├── V3__add_platform_owner_to_users.sql  owner de la plataforma
└── V4__create_permissions.sql           permisos por rol y roles privilegiados
```

La siguiente migración será `V5__...sql`.

`V1__create_initial_schema.sql` tiene que llevar el `CREATE` del esquema inicial antes del primer arranque con Flyway. Si la API arranca con ese archivo vacío, Flyway lo marca como aplicado. El SQL ya no puede entrar en `V1`: iría en `V2__...sql`.

### Qué hace al arrancar

1. Busca los `.sql` en `db/migration`.
2. Compara con lo ya registrado.
3. Ejecuta solo las versiones pendientes, en orden.
4. Anota cada una en `flyway_schema_history` (versión, descripción, checksum y si terminó bien).

Las tablas no se crean a mano en PostgreSQL.

### Dependencia en `pom.xml`

Antes de arrancar con Flyway hace falta declararlo. Con Spring Boot 4 y PostgreSQL van los dos módulos. La versión la resuelve el parent del `pom.xml`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

`flyway-core` es el motor. `flyway-database-postgresql` es el soporte de PostgreSQL. Sin ese segundo módulo, Flyway no reconoce la base.

## 7. Arrancar el proyecto

Cada vez que vayas a trabajar, en la raíz del proyecto (`parking-api`):

1. Abre Docker Desktop y espera a que esté en marcha.
2. Levanta PostgreSQL:

```bash
docker compose up -d
```

3. En Git Bash, carga `.env` y arranca Spring Boot. `set -a` exporta las variables al hacer `source`; `set +a` vuelve a dejar de exportar todo lo que se defina después.

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

4. Comprueba la API:

[http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health)

La respuesta esperada es: `Los datos de la API están funcionando correctamente`.

Para detener la API, pulsa `Ctrl+C` en esa terminal. Para detener la base:

```bash
docker compose down
```

`docker compose down` no borra los datos: siguen en el volumen `parking_data`.

### Para qué sirve `./mvnw clean compile`

```bash
./mvnw clean compile
```

Comprueba que el proyecto compila. No arranca la API ni levanta PostgreSQL.

- `./mvnw` es el Maven incluido en el proyecto. No hace falta tener Maven instalado en Windows.
- `clean` borra la carpeta `target/`, donde quedó la compilación anterior.
- `compile` vuelve a copiar `src/main/resources` y compila `src/main/java` dentro de `target/classes`.

Sirve después de cambiar código, `application.properties` o el `pom.xml` (por ejemplo, al agregar Flyway). Si termina en `BUILD SUCCESS`, el código compila. Para ejecutar la API sigue haciendo falta `./mvnw spring-boot:run`, con Docker y el `.env` cargado.

## 8. API

Prefijo `/api/v1`. Salvo los públicos, todos los endpoints exigen `Authorization: Bearer <token>`.

| Método | Ruta | Quién puede |
| --- | --- | --- |
| GET | `/health` | Público |
| POST | `/users` | Público (registro) |
| POST | `/auth/login`, `/auth/refresh`, `/auth/logout` | Público |
| GET | `/auth/me` | Usuario autenticado |
| GET | `/auth/me/companies` | Usuario autenticado: sus empresas, rol y permisos |
| GET | `/roles` | Usuario autenticado: roles y sus permisos |
| POST | `/companies` | Owner de plataforma |
| GET | `/companies`, `/companies/{id}` | Owner de plataforma |
| PUT | `/companies/{id}` | Owner de plataforma |
| POST | `/companies/{id}/deactivate` | Owner de plataforma |
| GET | `/companies/{companyId}/members` | Permiso `MEMBERS_VIEW` |
| POST | `/companies/{companyId}/members` | Permiso `MEMBERS_ADD` |
| PATCH | `/companies/{companyId}/members/{userId}` | Permiso `MEMBERS_CHANGE_ROLE` |
| DELETE | `/companies/{companyId}/members/{userId}` | Permiso `MEMBERS_REMOVE` |
| POST | `/companies/{companyId}/users` | Owner de plataforma: crea el usuario y lo asigna a la empresa en una transacción |

El login devuelve un access token de 15 minutos y un refresh token de 14 días, que se guarda hasheado y se puede revocar.

### Owner de plataforma

Es el único usuario que administra todas las empresas (`users.is_platform_owner`). Solo puede existir uno y **no se asigna desde la API**: se promueve a un usuario ya registrado con SQL.

```bash
docker exec -it parking-postgres psql -U parking_user -d parking_db \
  -c "UPDATE users SET is_platform_owner = TRUE WHERE email = 'correo@ejemplo.com';"
```

### Roles y permisos de empresa

Los roles (`OWNER`, `ADMIN`, `EMPLOYEE`) y lo que puede hacer cada uno viven en la base (`roles`, `permissions`, `role_permissions`), no en el código. Cambiar un permiso es un `INSERT` o `DELETE` en `role_permissions` y se aplica sin reiniciar.

| Rol | Permisos de miembros |
| --- | --- |
| `ADMIN` | `MEMBERS_VIEW`, `MEMBERS_ADD`, `MEMBERS_CHANGE_ROLE`, `MEMBERS_REMOVE` |
| `OWNER` | Ninguno por defecto |
| `EMPLOYEE` | Ninguno |

- Los roles privilegiados (`OWNER`) solo los gestiona quien tenga `MEMBERS_MANAGE_PRIVILEGED`; hoy nadie lo tiene salvo el owner de plataforma, que no necesita permisos.
- Nadie puede modificar ni retirar su propia membresía (403).
- Un usuario puede pertenecer a varias empresas con roles distintos.

## 9. Pruebas

```bash
set -a; source .env; set +a
./mvnw test
```

Las pruebas unitarias no necesitan base de datos. `ParkingApiApplicationTests` arranca el contexto completo, por eso necesita Docker y el `.env` cargado. Los objetos de dominio de las pruebas salen de `src/test/.../support/Fixtures.java`.

## Referencias

- [Documentación de Apache Maven](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/4.1.2/maven-plugin)
- [Spring Web](https://docs.spring.io/spring-boot/4.1.2/reference/web/servlet.html)
- [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.2/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Validation](https://docs.spring.io/spring-boot/4.1.2/reference/io/validation.html)
- [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.1.2/reference/using/devtools.html)
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Flyway](https://documentation.red-gate.com/flyway)

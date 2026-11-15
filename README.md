# RentaDías - Sistema de renta de inmuebles por días

Proyecto final de Informática - Maestría en Ciencias de la Información y la Comunicación, Universidad Distrital.
Integrantes: Dilan Soler Monsalve y Yina Fernanda Castiblanco Pinilla.

Aplicación web en Java (programación orientada a objetos) para publicar, buscar y reservar inmuebles por noche,
con tres roles: cliente, propietario y administrador.

## Tecnologías

| Parte | Herramienta |
|---|---|
| Lenguaje | Java 17 |
| Web | Servlets + JSP (Jakarta EE 10) |
| Servidor | Apache Tomcat 10.1 |
| Base de datos | PostgreSQL |
| Proyecto | Maven, en Apache NetBeans |

## Qué necesitas instalar

1. **JDK 17 o superior**.
2. **Apache NetBeans**.
3. **Apache Tomcat 10.1**: descarga el "64-bit Windows zip" desde https://tomcat.apache.org y descomprímelo.
4. **PostgreSQL** con **pgAdmin**.
5. Internet la primera vez que compiles (Maven descarga el driver de PostgreSQL).

## Cómo ejecutarlo en tu computador

### 1. Descargar el proyecto
En GitHub: botón **Code > Download ZIP** (y descomprimir), o con Git:
```
git clone https://github.com/USUARIO/RentaInmuebles.git
```

### 2. Crear la base de datos
1. Abre pgAdmin y conéctate a tu servidor local.
2. Clic derecho en **Databases > Create > Database**. Nombre: `proyecto_renta_inmuebles`.
3. Clic derecho en la base nueva > **Query Tool** > botón de abrir archivo > selecciona
   `sql/proyecto_renta_inmuebles.sql` > ejecutar (F5).

### 3. Escribir tu contraseña de PostgreSQL
1. En `src/main/resources/` copia `db.properties.ejemplo` y nombra la copia `db.properties`.
2. Abre `db.properties` y cambia `ESCRIBE_AQUI_TU_CONTRASENA` por la contraseña de tu usuario `postgres`.

### 4. Agregar Tomcat a NetBeans (solo una vez)
1. **Tools > Servers > Add Server** > "Apache Tomcat or TomEE" > Next.
2. **Server Location**: la carpeta donde descomprimiste Tomcat.
3. Usuario y contraseña: `admin` / `admin`, marcando "Create user if it does not exist" > Finish.

### 5. Abrir y ejecutar
1. **File > Open Project** > selecciona la carpeta `RentaInmuebles`.
2. Clic derecho en el proyecto > **Clean and Build**.
3. Clic derecho > **Run**. Se abre `http://localhost:8080/RentaInmuebles/`.

### Usuarios de prueba
Contraseña de los tres: `1234`

| Correo | Rol |
|---|---|
| cliente@rentadias.com | Cliente |
| propietario@rentadias.com | Propietario |
| admin@rentadias.com | Administrador |

## Estructura

```
RentaInmuebles/
├── pom.xml                         configuración de Maven
├── sql/                            scripts de la base de datos
└── src/main/
    ├── java/com/rentainmuebles/
    │   ├── modelo/                 clases del diagrama: Usuario, Cliente, Propietario, Administrador, Inmueble, Renta, Banco...
    │   ├── datos/                  DAO: leen y escriben en PostgreSQL
    │   ├── controlador/            servlets: reciben los formularios
    │   └── util/                   apoyo: seguridad, imágenes, HTML
    ├── resources/                  db.properties (conexión)
    └── webapp/                     pantallas JSP y estilos
```

## Notas
- Las fotos de los inmuebles se guardan en `C:\Users\TU_USUARIO\RentaDias\imagenes` (fuera del proyecto),
  por eso no vienen en el repositorio.
- Los pagos son simulados: no se mueve dinero real.

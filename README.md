# LiterAlura

Este proyecto tiene la finalidad de hacer consultas API para obtener información de libros buscados así como de sus autores y persistir dicha información hacia una base de datos que posteriormente puede ser consultada por el usuario.

## Herramientas, recursos y tecnologías requeridas

***API Gutendex**

El consumo de esta API es completamente gratuito y no requiere registro ni KEY personalizada para su uso. La documentación de la API proporciona una URL para la consulta de los libros por titulo o nombre de autor:

"https://gutendex.com/books/?search="

***JDK 17**

***Spring Framework**
```java
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```
***Jackson 2.16**
```java
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>2.16.0</version>
</dependency>
```

***PostgreSQL**

La base de datos utilizada para este proyecto.
```java
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```
***Maven**

***Libreria icu4j 75.1**

Esta libreria permite convertir los codigos del ISO 639 al nombre de los idiomas correspondientes expresado en el idioma del sistema operativo en el que se usa. Es necesario añadir la dependencia correspondiente al pom.xml:
```java
<dependency>
  <groupId>com.ibm.icu</groupId>
  <artifactId>icu4j</artifactId>
  <version>75.1</version>
</dependency>
```
 
## Desarrollo y lógica del código



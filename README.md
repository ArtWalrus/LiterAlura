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

El programa tiene que iniciar varios procesos de forma automática justo cuando arranca:

```java
@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner {

	@Autowired
	private LibroRepository libroRepository;

	@Autowired
	private AutorRepository autorRepository;

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(libroRepository, autorRepository);
		principal.muestraElMenu();
	}
}
```

Una vez establecido esto, podemos abordar el proyecto a través de los siguientes puntos:

***Consumo de API**

El consumo API se reserva para una clase específicamente creada para esa tarea. En dicha clase, el método obtenerDatos recibe la URL formateada en la clase principal y retorna un String que contiene el JSON obtenido.

```java
public class ConsumoAPI {
    public String obtenerDatos(String url){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String json = response.body();
        return json;
    }
}
```

***Manejo e interpretación de JSON**

El JSON proporcionado por la API es una lista en la que el elmento que contiene la información que se requiere sobre el libro es también una lista y así mismo, esta segunda lista contiene dos listas más en las que se incluye la información del autor y del idioma.

De este modo, el primer manejo del JSON se realiza mediante el record DatosConsumo. Del cual cantidadDeResultadosDeAPI nos servirá para identificar la cantidad de libros encontrados y List<DatosLibro> libros recibirá la información de cada libro hayado.

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosConsumo(
        @JsonAlias("count") Integer cantidadDeResultadosDeAPI,
        @JsonAlias("results") List<DatosLibro> libros){}
```
El record DatosLibro será el siguiente paso en el manejo del JSON, pues servirá para extraer la información de la lista "results".

```java
public record DatosLibro(
        @JsonAlias("id") Integer id,
        @JsonAlias("title") String titulo,
        @JsonAlias("languages") List<String> idioma,
        @JsonAlias("authors") List<DatosAutor> autores,
        @JsonAlias("download_count") Integer descargas){}
```

Cuando el JSON llega a esta parte del "filtro" aún es necesario "desemvolver" la información de idioma y autores. 

Para el caso de los idiomas, estos son proporcionados en forma de codigo según el stardar ISO 639, lo cual resultaría poco práctico para el usuario que no esté familiarizado con dichos códigos, por lo que podria utilizarse un Enum para establecer el nombre de un idioma para cada código, pero eso limitaría nuestras posibilidades porque no sería práctico realizar un enum contemplando todos los idiomas existentes en la API y tampoco sería muy util limitar ese enum a 5 o 6 idiomas. La solución para lo anterior es aún más simple: La implementación de la libreria icu4j, cuya utilidad radita en la interpretación de los códigos ISO 639.

Así que en esta parte de la interpretación del JSON, la atención se centra en la lista de Autores, para lo cual se ha creado un record específico.

```java
public record DatosAutor (
    @JsonAlias("name") String nombreAutor,
    @JsonAlias("birth_year") Integer nacimiento,
    @JsonAlias("death_year") Integer muerte){}
```

***Conexión a la Base de datos, creación de entidades y relación entre ellas**

Para la conexión a la base de datos es indispensable la configuración de las propiedades de la aplicación. En el siguiente fragmento se observa la sustitución del nombre de la base de datos, el nombre del usuario y la contraseña por variables de entorno. Esto por aplicar buenas practicas referentes a la seguridad del proyecto.

```java
spring.application.name=literalura
spring.datasource.url=jdbc:postgresql://${DB_HOST}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
hibernate.dialect=org.hibernate.dialect.HSQLDialect
spring.jpa.hibernate.ddl-auto=update
```
Creación y relación de entidades:

```java
@Entity
@Table(name = "libros")

public class Libro {

    @Id
    private long id;
    private String titulo;
    private String idioma;
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private List<Autor> autores;
    private Integer descargas;
```

```java
@Entity
@Table(name = "autores")

public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    private String nombreAutor;
    private Integer nacimiento;
    private Integer muerte;
    @ManyToMany(mappedBy = "autores", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libro> libros;
```




***Persistencia y consulta datos**




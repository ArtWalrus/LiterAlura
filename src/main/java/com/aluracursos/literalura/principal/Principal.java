package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;
import com.ibm.icu.util.ULocale;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<Autor> autores;
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    ************************************************
                    1 - Buscar libro por título
                    2 - Mostrar libros registrados
                    3 - Mostrar autores registrados
                    4 - Mostrar autores vivos en un determinado año
                    5 - Mostrar libros por idioma
                    
                    0 - Salir
                    ************************************************
                    """;
            try {
                System.out.println(menu);
                opcion = teclado.nextInt();
                teclado.nextLine();

                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        mostrarLibrosRegistrados();
                        break;
                    case 3:
                        mostrarAutoresRegistrados();
                        break;
                    case 4:
                        mostrarAutoresVivosPorFecha();
                        break;
                    case 5:
                        mostrarLibrosPorIdioma();
                        break;
                    case 0:
                        System.out.println("CERRANDO APLICACIÓN ¡BYE!...");
                        break;
                    default:
                        System.out.println("ELIGE UNA OPCION VÁLIDA.");
                }

            } catch (InputMismatchException e) {
                System.out.println("ELIGE UNA OPCIÓN VÁLIDA MEDIANTE EL NÚMERO INDICADO EN EL MENÚ.\n");
                teclado.nextLine();
            } catch (IndexOutOfBoundsException e) {
                System.out.println("ELIGE UNA OPCIÓN DE LA LISTA PROPORCIONADA.\n");
                teclado.nextLine();
            } catch (DataIntegrityViolationException e){
                System.out.println("EL NOMBRE DEL LIBRO A GUARDAR ES DEMASIADO LARGO Y ME DA AMSIEDAD.\n");
                teclado.nextLine();
            }
        }
    }

    private DatosConsumo getDatosConsumo() {
        DatosConsumo datos;
        while (true) {
            System.out.println("\n¿QUÉ LIBRO O AUTOR QUIERES BUSCAR?");
            var busqueda = teclado.nextLine();
            var json = consumoApi.obtenerDatos(URL_BASE + busqueda.replace(" ", "%20"));
            datos = conversor.obtenerDatos(json, DatosConsumo.class);
            if (datos.cantidadDeResultadosDeAPI() > 0) {
                break;
            } else {
                System.out.println("NO SE ENCONTRARON RESULTADOS. INTENTA NUEVAMENTE.");
            }
        }
        return datos;
    }

    //OPCION UNO
    private void buscarLibroPorTitulo() {
        DatosConsumo datos = getDatosConsumo();

        List<Libro> libros = datos.libros().stream()
                .map(e -> new Libro(e))
                .collect(Collectors.toList());

        for (int i = 0; i < libros.size(); i++) {
            ULocale locale = new ULocale(libros.get(i).getIdioma());
            libros.get(i).setIdioma(locale.getDisplayName());
        }

        System.out.println("\nRESULTADOS DE BÚSQUEDA:");

        for (int i = 0; i < libros.size(); i++) {
            System.out.println("\n" + (i + 1) + ")\n" + libros.get(i));
        }

        System.out.println("¿QUÉ LIBRO QUIERES GUARDAR EN LA BASE DE DATOS?");

        Libro libroSeleccionado = libros.get(teclado.nextInt() - 1);
        List<Autor> autoresDeLibroSeleccionado = libroSeleccionado.getAutores();

        // Crear nuevos autores si no existen en la DB
        autores = autorRepository.findAll();

        List<Autor> nuevosAutores = autoresDeLibroSeleccionado.stream()
                .filter(a -> !autores.stream()
                        .anyMatch(autor -> autor.getNombreAutor().equalsIgnoreCase(a.getNombreAutor())))
                .collect(Collectors.toList());

        if (nuevosAutores.size() != 0) {
            autorRepository.saveAll(nuevosAutores);
        }

        autores.addAll(nuevosAutores);

        List<Autor> autoresExistentes = autoresDeLibroSeleccionado.stream()
                .map(a -> autores.stream()
                        .filter(autor -> autor.getNombreAutor().equalsIgnoreCase(a.getNombreAutor()))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        libroSeleccionado.setAutores(autoresExistentes);

        // Guardar el libro
        libroRepository.save(libroSeleccionado);
        System.out.println("""
        *****************************************************
        ¡¡¡LIBRO GUARDADO EXITOSAMENTE EN LA BASE DE DATOS!!!
        *****************************************************
        """);
    }

    // OPCION DOS
    private void mostrarLibrosRegistrados() {
        System.out.println("""
        *****************************************************
        ESTOS SON LOS LIBROS REGISTRADOS EN LA BASE DE DATOS:
        *****************************************************
        """);
        List <Libro> libros = libroRepository.findAll();
        libros.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .forEach(System.out::println);

        System.out.println("*** Presiona enter para continuar ***");
        var continuar = teclado.nextLine();
    }

    // OPCION TRES
    private void mostrarAutoresRegistrados() {
        System.out.println("""
        ******************************************************
        ESTOS SON LOS AUTORES REGISTRADOS EN LA BASE DE DATOS:
        ******************************************************
        """);

        autores = autorRepository.findAll();
        autores.stream()
                .sorted(Comparator.comparing(Autor::getNombreAutor))
                .forEach(System.out::println);
        System.out.println(" ");
        System.out.println("*** Presiona enter para continuar ***");
        var continuar = teclado.nextLine();
    }

    //OPCION CUATRO
    private void mostrarAutoresVivosPorFecha() {
        System.out.println("¿DE QUÉ AÑO QUIERES BUSCAR AUTORES?");
        var fechaDeAutoresVivos = teclado.nextInt();
        teclado.nextLine();
        List<Autor> autoresVivosEn = autorRepository
                .findAutorByNacimientoLessThanEqualAndMuerteGreaterThanEqual(fechaDeAutoresVivos, fechaDeAutoresVivos)
                .stream().sorted(Comparator.comparing(Autor::getNacimiento))
                .collect(Collectors.toList());

        if (autoresVivosEn.isEmpty()) {
            System.out.println("NO HAY REGITRO DE AUTORES VIVOS DURANTE ESE AÑO");
        } else {
            System.out.println("**********************************************************" +
                    "\nESTOS SON LOS AUTORES REGISTRADOS VIVOS DURANTE EL AÑO " + fechaDeAutoresVivos +
                    "\n**********************************************************\n");
            autoresVivosEn.forEach(System.out::println);
            System.out.println("\n*** Presiona enter para continuar ***");
            var continuar = teclado.nextLine();
        }

    }
    //OPCION CINCO
    private void mostrarLibrosPorIdioma() {

        List <Libro> libros = libroRepository.findAll();
        Set<String> idiomasEnDB = new HashSet<>();
        for (Libro libro : libros){
            idiomasEnDB.add(libro.getIdioma());
        }
        System.out.println("ESTOS SON LOS IDIOMAS EXISTENTES EN LA BASE DE DATOS:");

        for (String idioma : idiomasEnDB) {
            System.out.println(">> " + idioma);
        }

        System.out.println("¿DE QUÉ IDIOMA QUIERES BUSCAR LOS LIBROS?");
        String idiomaBusqueda = teclado.nextLine();

        List<Libro> librosPorIdioma = libroRepository.findBookByIdiomaContainingIgnoreCase(idiomaBusqueda);

        if (librosPorIdioma.isEmpty()) {
            System.out.println("NO EXISTEN REGISTROS DE LIBROS CON ESE IDIOMA.");
        } else {
            System.out.println("""
                            ************************************************************
                            ESTOS SON LOS LIBROS REGISTRADOS CON EL IDIOMA QUE ELEGISTE:
                            ************************************************************
                            """);
            librosPorIdioma.forEach(System.out::println);

            System.out.println("*** Presiona enter para continuar ***");
            var continuar = teclado.nextLine();
        }
    }
}
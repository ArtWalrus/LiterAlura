package com.aluracursos.literalura.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "libros")

public class Libro {

    @Id
    private long id;
    private String titulo;
    //    @Enumerated(EnumType.STRING)
    private String idioma;
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private List<Autor> autores;
    private Integer descargas;

    public Libro(){}

    public Libro(DatosLibro d){
        this.id = d.id();
        this.titulo = d.titulo();
        this.idioma = (d.idioma().get(0));
        this.autores = d.autores().stream()
                .map(Autor::new)
                .collect(Collectors.toList());
        this.descargas = d.descargas();
    }

    @Override
    public String toString() {
        return  "Id: " + id + '\n' +
                "Titulo: " + titulo + '\n' +
                "Autor/es: " + autores + '\n'+
                "Idioma: " + idioma + '\n' +
                "Descargas: " + descargas + '\n';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autor) {
        this.autores = autor;
    }

    public Integer getDescargas() {
        return descargas;
    }

    public void setDescargas(Integer descargas) {
        this.descargas = descargas;
    }
}

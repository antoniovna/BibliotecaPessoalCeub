package com.example.projetofinal.model;

import java.io.Serializable;

/**
 * Representa um livro na biblioteca pessoal.
 */
public class Livro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private String autor;
    private int ano;
    private boolean lido;

    /** Construtor vazio. */
    public Livro() {
    }

    /** Construtor para criação de novo livro (sem id). */
    public Livro(String titulo, String autor, int ano, boolean lido) {
        this.titulo = titulo;
        this.autor = autor;
        this.ano = ano;
        this.lido = lido;
    }

    /** Construtor completo usado pelo DAO na leitura do banco. */
    public Livro(Long id, String titulo, String autor, int ano, boolean lido) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.ano = ano;
        this.lido = lido;
    }

    /** Retorna o id do livro. */
    public Long getId() {
        return id;
    }

    /** Define o id do livro. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Retorna o título do livro. */
    public String getTitulo() {
        return titulo;
    }

    /** Define o título do livro. */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /** Retorna o autor do livro. */
    public String getAutor() {
        return autor;
    }

    /** Define o autor do livro. */
    public void setAutor(String autor) {
        this.autor = autor;
    }

    /** Retorna o ano de publicação. */
    public int getAno() {
        return ano;
    }

    /** Define o ano de publicação. */
    public void setAno(int ano) {
        this.ano = ano;
    }

    /** Retorna se o livro foi lido. */
    public boolean isLido() {
        return lido;
    }

    /** Define se o livro foi lido. */
    public void setLido(boolean lido) {
        this.lido = lido;
    }
}

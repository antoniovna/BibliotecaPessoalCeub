package com.example.projetofinal.persistencia;

import com.example.projetofinal.model.Livro;

import java.util.List;

/**
 * Interface DAO que define as operações de persistência de livros.
 */
public interface ILivroDAO {

    /** Salva um novo livro no banco. */
    boolean salvar(Livro livro);

    /** Atualiza um livro existente. */
    boolean atualizar(Livro livro);

    /** Exclui um livro do banco. */
    boolean excluir(Livro livro);

    /** Retorna todos os livros ordenados por título. */
    List<Livro> listarTodos();

    /** Conta livros: null = total, true = lidos, false = não lidos. */
    int contar(Boolean lido);
}

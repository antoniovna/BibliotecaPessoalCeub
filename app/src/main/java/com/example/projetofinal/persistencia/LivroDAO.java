package com.example.projetofinal.persistencia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.projetofinal.model.Livro;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do DAO para operações CRUD na tabela de livros.
 */
public class LivroDAO implements ILivroDAO {

    private static final String TAG = "LivroDAO";

    private final SQLiteDatabase escreve;
    private final SQLiteDatabase le;

    /** Inicializa as conexões de leitura e escrita com o banco. */
    public LivroDAO(Context context) {
        DbHelper db = new DbHelper(context);
        escreve = db.getWritableDatabase();
        le = db.getReadableDatabase();
    }

    /** Insere um novo livro no banco e define o id gerado. */
    @Override
    public boolean salvar(Livro livro) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DbHelper.COLUNA_TITULO, livro.getTitulo());
            cv.put(DbHelper.COLUNA_AUTOR, livro.getAutor());
            cv.put(DbHelper.COLUNA_ANO, livro.getAno());
            cv.put(DbHelper.COLUNA_LIDO, livro.isLido() ? 1 : 0);

            long id = escreve.insert(DbHelper.TABELA_LIVROS, null, cv);
            if (id != -1) {
                livro.setId(id);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar livro: " + e.getMessage());
        }
        return false;
    }

    /** Atualiza os dados de um livro existente pelo seu id. */
    @Override
    public boolean atualizar(Livro livro) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DbHelper.COLUNA_TITULO, livro.getTitulo());
            cv.put(DbHelper.COLUNA_AUTOR, livro.getAutor());
            cv.put(DbHelper.COLUNA_ANO, livro.getAno());
            cv.put(DbHelper.COLUNA_LIDO, livro.isLido() ? 1 : 0);

            int linhas = escreve.update(
                    DbHelper.TABELA_LIVROS,
                    cv,
                    "id = ?",
                    new String[]{livro.getId().toString()}
            );
            return linhas > 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar livro: " + e.getMessage());
        }
        return false;
    }

    /** Exclui um livro do banco pelo seu id. */
    @Override
    public boolean excluir(Livro livro) {
        try {
            int linhas = escreve.delete(
                    DbHelper.TABELA_LIVROS,
                    "id = ?",
                    new String[]{livro.getId().toString()}
            );
            return linhas > 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao excluir livro: " + e.getMessage());
        }
        return false;
    }

    /** Retorna todos os livros ordenados por título (case-insensitive). */
    @Override
    public List<Livro> listarTodos() {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM " + DbHelper.TABELA_LIVROS
                + " ORDER BY " + DbHelper.COLUNA_TITULO + " COLLATE NOCASE ASC";
        Cursor cursor = le.rawQuery(sql, null);
        try {
            while (cursor.moveToNext()) {
                Livro livro = new Livro(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COLUNA_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUNA_TITULO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUNA_AUTOR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUNA_ANO)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUNA_LIDO)) == 1
                );
                livros.add(livro);
            }
        } finally {
            cursor.close();
        }
        return livros;
    }

    /** Conta livros por status: null = total, true = lidos, false = não lidos. */
    @Override
    public int contar(Boolean lido) {
        String sql;
        String[] args;

        if (lido == null) {
            // Conta todos os livros
            sql = "SELECT COUNT(*) FROM " + DbHelper.TABELA_LIVROS;
            args = null;
        } else {
            // Conta filtrado pelo status de leitura
            sql = "SELECT COUNT(*) FROM " + DbHelper.TABELA_LIVROS
                    + " WHERE " + DbHelper.COLUNA_LIDO + " = ?";
            args = new String[]{lido ? "1" : "0"};
        }

        Cursor cursor = le.rawQuery(sql, args);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }
}

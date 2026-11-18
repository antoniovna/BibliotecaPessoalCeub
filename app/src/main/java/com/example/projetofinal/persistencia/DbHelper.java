package com.example.projetofinal.persistencia;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper para criação e migração do banco de dados SQLite.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";

    public static final String NOME_BANCO    = "biblioteca_pessoal.db";
    public static final int    VERSAO        = 1;
    public static final String TABELA_LIVROS = "livros";
    public static final String COLUNA_ID     = "id";
    public static final String COLUNA_TITULO = "titulo";
    public static final String COLUNA_AUTOR  = "autor";
    public static final String COLUNA_ANO    = "ano";
    public static final String COLUNA_LIDO   = "lido";
    public static final String COLUNA_NOTA   = "nota"; // usada somente na migração v2

    /** Construtor padrão que define o nome e a versão do banco. */
    public DbHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

    /** Cria a tabela de livros na primeira execução do app. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABELA_LIVROS + " ("
                + COLUNA_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUNA_TITULO + " TEXT NOT NULL, "
                + COLUNA_AUTOR  + " TEXT NOT NULL, "
                + COLUNA_ANO    + " INTEGER, "
                + COLUNA_LIDO   + " INTEGER DEFAULT 0"
                + ");";
        db.execSQL(sql);
    }

    /** Migração incremental sem DROP TABLE; adiciona coluna nota na v2. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABELA_LIVROS
                        + " ADD COLUMN " + COLUNA_NOTA + " INTEGER");
            } catch (Exception e) {
                Log.e(TAG, "Erro na migração v1 para v2: " + e.getMessage());
            }
        }
    }
}

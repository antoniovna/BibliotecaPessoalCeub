package com.example.projetofinal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.adapter.AdapterLivro;
import com.example.projetofinal.model.Livro;
import com.example.projetofinal.persistencia.ILivroDAO;
import com.example.projetofinal.persistencia.LivroDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity principal que exibe a listagem de livros cadastrados.
 */
public class ListaLivrosActivity extends AppCompatActivity {

    private RecyclerView recyclerLivros;
    private TextView txtListaVazia;
    private AdapterLivro adapter;
    private ILivroDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_livros);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavegacaoMenu.configurar(this, R.id.nav_livros);

        // Inicialização de componentes e persistência
        recyclerLivros = findViewById(R.id.recyclerLivros);
        txtListaVazia = findViewById(R.id.txtListaVazia);
        dao = new LivroDAO(this);

        // Configuração do RecyclerView
        adapter = new AdapterLivro(new ArrayList<>());
        recyclerLivros.setLayoutManager(new LinearLayoutManager(this));
        recyclerLivros.setHasFixedSize(true);
        recyclerLivros.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerLivros.setAdapter(adapter);
    }

    /**
     * Recarrega a lista de livros sempre que a Activity volta ao primeiro plano.
     */
    @Override
    protected void onResume() {
        super.onResume();
        carregarLivros();
    }

    /**
     * Consulta o banco de dados e atualiza a exibição no RecyclerView.
     */
    private void carregarLivros() {
        List<Livro> livros = dao.listarTodos();
        adapter.atualizarLista(livros);

        if (txtListaVazia != null) {
            txtListaVazia.setVisibility(livros.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}

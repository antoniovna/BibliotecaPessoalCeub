package com.example.projetofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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

        // Configuração de cliques simples (edição) e longo (exclusão com confirmação)
        recyclerLivros.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                recyclerLivros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (position == RecyclerView.NO_POSITION) {
                            return;
                        }
                        Livro livro = adapter.getLivro(position);
                        Intent intent = new Intent(ListaLivrosActivity.this, CadastroLivroActivity.class);
                        intent.putExtra(CadastroLivroActivity.EXTRA_LIVRO, livro);
                        NavegacaoMenu.irPara(ListaLivrosActivity.this, intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        if (position == RecyclerView.NO_POSITION) {
                            return;
                        }
                        Livro livro = adapter.getLivro(position);
                        exibirDialogoExclusao(livro);
                    }
                }
        ));
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

    /**
     * Exibe o diálogo de confirmação antes de excluir o livro selecionado.
     */
    private void exibirDialogoExclusao(Livro livro) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialogo_excluir_titulo)
                .setMessage(getString(R.string.dialogo_excluir_mensagem, livro.getTitulo()))
                .setPositiveButton(R.string.dialogo_sim, (dialog, which) -> confirmarExclusao(livro))
                .setNegativeButton(R.string.dialogo_nao, null)
                .show();
    }

    /**
     * Realiza a exclusão do livro no banco e atualiza a lista sem recriar a Activity.
     */
    private void confirmarExclusao(Livro livro) {
        if (dao.excluir(livro)) {
            Toast.makeText(this, getString(R.string.msg_excluido_sucesso), Toast.LENGTH_SHORT).show();
            carregarLivros();
        } else {
            Toast.makeText(this, getString(R.string.msg_excluido_erro), Toast.LENGTH_SHORT).show();
        }
    }
}

package com.example.projetofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projetofinal.model.Livro;
import com.example.projetofinal.persistencia.ILivroDAO;
import com.example.projetofinal.persistencia.LivroDAO;

import java.util.Calendar;

/**
 * Activity para inclusão e edição de livros na biblioteca pessoal.
 */
public class CadastroLivroActivity extends AppCompatActivity {

    public static final String EXTRA_LIVRO = "livroSelecionado";
    private static final int ANO_MINIMO = 1450;

    private EditText edtTitulo;
    private EditText edtAutor;
    private EditText edtAno;
    private CheckBox chkLido;
    private Button btnSalvar;
    private TextView txtCabecalho;

    private ILivroDAO dao;
    private Livro livroAtual;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_livro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavegacaoMenu.configurar(this, R.id.nav_novo);

        // Inicialização dos componentes de interface
        txtCabecalho = findViewById(R.id.txtCabecalho);
        edtTitulo = findViewById(R.id.edtTitulo);
        edtAutor = findViewById(R.id.edtAutor);
        edtAno = findViewById(R.id.edtAno);
        chkLido = findViewById(R.id.chkLido);
        btnSalvar = findViewById(R.id.btnSalvar);

        dao = new LivroDAO(this);

        // Verifica se veio livro para edição
        livroAtual = (Livro) getIntent().getSerializableExtra(EXTRA_LIVRO);
        if (livroAtual != null) {
            edtTitulo.setText(livroAtual.getTitulo());
            edtAutor.setText(livroAtual.getAutor());
            edtAno.setText(String.valueOf(livroAtual.getAno()));
            chkLido.setChecked(livroAtual.isLido());
            btnSalvar.setText(getString(R.string.botao_atualizar));
            txtCabecalho.setText(getString(R.string.titulo_edicao));
        }

        btnSalvar.setOnClickListener(v -> salvarLivro());
    }

    /**
     * Valida os campos do formulário antes de persistir.
     */
    private boolean validarCampos() {
        boolean valido = true;
        View primeiroInvalido = null;

        String titulo = edtTitulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            edtTitulo.setError(getString(R.string.erro_titulo_obrigatorio));
            valido = false;
            primeiroInvalido = edtTitulo;
        }

        String autor = edtAutor.getText().toString().trim();
        if (autor.isEmpty()) {
            edtAutor.setError(getString(R.string.erro_autor_obrigatorio));
            valido = false;
            if (primeiroInvalido == null) {
                primeiroInvalido = edtAutor;
            }
        }

        String anoStr = edtAno.getText().toString().trim();
        if (anoStr.isEmpty()) {
            edtAno.setError(getString(R.string.erro_ano_obrigatorio));
            valido = false;
            if (primeiroInvalido == null) {
                primeiroInvalido = edtAno;
            }
        } else {
            try {
                int ano = Integer.parseInt(anoStr);
                int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
                if (ano < ANO_MINIMO || ano > anoAtual) {
                    edtAno.setError(getString(R.string.erro_ano_intervalo, ANO_MINIMO, anoAtual));
                    valido = false;
                    if (primeiroInvalido == null) {
                        primeiroInvalido = edtAno;
                    }
                }
            } catch (NumberFormatException e) {
                edtAno.setError(getString(R.string.erro_ano_invalido));
                valido = false;
                if (primeiroInvalido == null) {
                    primeiroInvalido = edtAno;
                }
            }
        }

        if (!valido && primeiroInvalido != null) {
            primeiroInvalido.requestFocus();
        }

        return valido;
    }

    /**
     * Salva a inclusão ou atualização do livro no banco de dados.
     */
    private void salvarLivro() {
        if (!validarCampos()) {
            return;
        }

        String titulo = edtTitulo.getText().toString().trim();
        String autor = edtAutor.getText().toString().trim();
        int ano = Integer.parseInt(edtAno.getText().toString().trim());
        boolean lido = chkLido.isChecked();

        if (livroAtual == null) {
            // Inclusão de novo livro
            Livro novoLivro = new Livro(titulo, autor, ano, lido);
            boolean ok = dao.salvar(novoLivro);
            mostrarToast(ok ? R.string.msg_salvo_sucesso : R.string.msg_salvo_erro);
            if (ok) {
                NavegacaoMenu.irPara(this, new Intent(this, ListaLivrosActivity.class));
            }
        } else {
            // Edição de livro existente (preserva o id)
            livroAtual.setTitulo(titulo);
            livroAtual.setAutor(autor);
            livroAtual.setAno(ano);
            livroAtual.setLido(lido);

            boolean ok = dao.atualizar(livroAtual);
            mostrarToast(ok ? R.string.msg_atualizado_sucesso : R.string.msg_atualizado_erro);
            if (ok) {
                NavegacaoMenu.irPara(this, new Intent(this, ListaLivrosActivity.class));
            }
        }
    }

    /**
     * Exibe mensagem Toast a partir de um recurso de string.
     */
    private void mostrarToast(int resId) {
        Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
    }
}

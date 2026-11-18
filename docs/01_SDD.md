# SDD — App Biblioteca Pessoal (Android · Java · SQLite)

> **Documento de Especificação para Desenvolvimento (Spec-Driven Development).**
> Fonte da verdade: o PDF *"Exercício Avaliativo — App Biblioteca Pessoal"*. Este SDD transforma o PDF em
> especificação verificável. **Em caso de conflito, o PDF vence.**
>
> Convenções de ID:
> - `RFxx` / `RTxx` — requisitos copiados do PDF (mesma numeração).
> - `RNxx` — regras de negócio detalhadas a partir do PDF.
> - `DECxx` — decisões de projeto tomadas aqui onde o PDF é omisso (podem ser ajustadas, mas mantenha a coerência).
> - `CAxx` — critérios de aceitação (usados no checklist `04_CHECKLIST_VALIDACAO.md`).

---

## 1. Visão geral

App Android nativo **Biblioteca Pessoal**: CRUD completo de livros com SQLite, `RecyclerView` e menu inferior
(`BottomNavigationView`) presente nas três telas. Reaproveita a arquitetura em camadas do app "Gestão de Contatos":
`model` → `persistencia` (DbHelper + interface DAO + DAO) → `adapter` → Activities.

Três evoluções técnicas avaliadas:

1. **Persistência tipada** — `ano` (int → INTEGER) e `lido` (boolean → INTEGER 0/1).
2. **CRUD completo na UI** — lista ordenada, edição por clique simples, exclusão por clique longo com confirmação.
3. **Navegação unificada** — `BottomNavigationView` nas 3 telas, lógica centralizada em `NavegacaoMenu`.

### 1.1 Escopo

| Dentro do escopo | Fora do escopo (NÃO implementar) |
|---|---|
| 3 Activities, 1 entidade (`Livro`), 1 tabela (`livros`) | Room, Kotlin, Fragments, Navigation Component, ViewModel/LiveData |
| CRUD com SQLiteOpenHelper + DAO | ViewBinding/DataBinding (manter `findViewById`, padrão da aula) |
| Validação por campo com `setError` | Campo `nota` no modelo/tela (existe **apenas** na migração do `onUpgrade`) |
| Contadores via `SELECT COUNT(*)` | Busca, filtros, ordenação configurável, imagens de capa |
| Migração v1→v2 escrita (não executada na entrega) | Bibliotecas de terceiros além das do template |

---

## 2. Ambiente e stack

| Item | Valor |
|---|---|
| IDE de build/execução | Android Studio (versão atual estável) |
| IDE de desenvolvimento assistido | Google Antigravity (abrindo a mesma pasta do projeto) |
| Template | **Empty Views Activity** |
| Nome do app | `Biblioteca Pessoal` |
| Pacote | `com.example.bibliotecapessoal` |
| Linguagem | Java |
| Build | Gradle (o template atual usa `build.gradle.kts` + `gradle/libs.versions.toml`) |
| minSdk | o padrão do template (ex.: 24) |
| Dependências | as do template: `appcompat`, `material`, `activity`, `constraintlayout`. `RecyclerView` vem como dependência transitiva do `material`. |
| Tema | o do template (`Theme.Material3.DayNight.NoActionBar`) — **sem ActionBar**, por isso cada tela tem um cabeçalho próprio (TextView). |

**DEC01 — Dependência RecyclerView:** não adicionar dependência extra. Só adicionar
`androidx.recyclerview:recyclerview` se o import `androidx.recyclerview.widget.RecyclerView` não resolver.

---

## 3. Arquitetura

### 3.1 Camadas

```
┌──────────────────────────────────────────────────────────────┐
│ UI (Activities)                                              │
│  ListaLivrosActivity  CadastroLivroActivity  ResumoActivity  │
│        │  usa              │ usa                 │ usa       │
│        ▼                   ▼                     ▼           │
│  AdapterLivro  RecyclerItemClickListener   NavegacaoMenu     │
└────────┬───────────────────┬─────────────────────┬───────────┘
         │                   │                     │
         ▼                   ▼                     ▼
┌──────────────────────────────────────────────────────────────┐
│ Persistência: ILivroDAO ◄── LivroDAO ──► DbHelper (SQLite)   │
└──────────────────────────────────────────────────────────────┘
         ▲
         │ trafega
┌──────────────────────┐
│ model: Livro (Serializable) │
└──────────────────────┘
```

Regras de dependência:
- Activities falam com o banco **somente** via `ILivroDAO` (tipo declarado da variável) / `LivroDAO` (instância).
- `AdapterLivro` não acessa banco.
- `NavegacaoMenu` não acessa banco.
- Nenhuma classe usa variável `static` **mutável** para compartilhar estado (RT08). Constantes `static final` são permitidas.

### 3.2 Estrutura de arquivos (obrigatória — RT, seção 5 do PDF)

```
app/src/main/java/com/example/bibliotecapessoal/
├── ListaLivrosActivity.java          (LAUNCHER)
├── CadastroLivroActivity.java
├── ResumoActivity.java
├── NavegacaoMenu.java                (menu inferior + navegação — Anexo A completo)
├── RecyclerItemClickListener.java    (cópia fiel do Anexo B)
├── model/
│   └── Livro.java
├── persistencia/
│   ├── DbHelper.java
│   ├── ILivroDAO.java
│   └── LivroDAO.java
└── adapter/
    └── AdapterLivro.java

app/src/main/res/
├── drawable/
│   ├── ic_livros.xml                 (Vector Asset)
│   ├── ic_novo.xml                   (Vector Asset)
│   └── ic_resumo.xml                 (Vector Asset)
├── menu/
│   └── menu_bottom.xml
├── values/
│   ├── strings.xml
│   └── colors.xml
└── layout/
    ├── activity_lista_livros.xml
    ├── activity_cadastro_livro.xml
    ├── activity_resumo.xml
    └── adapter_livro.xml
```

**Não devem existir** ao final: `MainActivity.java`, `activity_main.xml` (renomeados), arquivos Java/XML não usados.

---

## 4. Modelo de dados

### 4.1 Classe `model/Livro.java`

| Atributo | Tipo Java | Coluna SQLite | Regra |
|---|---|---|---|
| `id` | `Long` | `INTEGER PRIMARY KEY AUTOINCREMENT` | gerado pelo SQLite; `null` = livro ainda não salvo |
| `titulo` | `String` | `TEXT NOT NULL` | obrigatório, não vazio, não só espaços |
| `autor` | `String` | `TEXT NOT NULL` | obrigatório, não vazio, não só espaços |
| `ano` | `int` | `INTEGER` | obrigatório; `1450 ≤ ano ≤ ano corrente` |
| `lido` | `boolean` | `INTEGER` (0/1) `DEFAULT 0` | `true`→1, `false`→0 |

Especificação:
- `public class Livro implements Serializable`
- `private static final long serialVersionUID = 1L;`
- Atributos `private`.
- Construtores: `Livro()` (vazio) e `Livro(String titulo, String autor, int ano, boolean lido)`.
  (Opcional: `Livro(Long id, String titulo, String autor, int ano, boolean lido)` usado pelo DAO na leitura.)
- Getters/setters: `getId/setId`, `getTitulo/setTitulo`, `getAutor/setAutor`, `getAno/setAno`, **`isLido`/`setLido`**.

### 4.2 Banco — `persistencia/DbHelper.java` (RT03)

`public class DbHelper extends SQLiteOpenHelper`

Constantes **`public static final`** (nomes exatos sugeridos — DEC02):

```java
public static final String NOME_BANCO   = "biblioteca_pessoal.db";
public static final int    VERSAO       = 1;            // ENTREGAR EM 1
public static final String TABELA_LIVROS = "livros";
public static final String COLUNA_ID     = "id";
public static final String COLUNA_TITULO = "titulo";
public static final String COLUNA_AUTOR  = "autor";
public static final String COLUNA_ANO    = "ano";
public static final String COLUNA_LIDO   = "lido";
public static final String COLUNA_NOTA   = "nota";      // usada só na migração v2
```

Construtor: `public DbHelper(Context context) { super(context, NOME_BANCO, null, VERSAO); }`

`onCreate(SQLiteDatabase db)` — monta o SQL com as constantes:

```sql
CREATE TABLE IF NOT EXISTS livros (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo TEXT NOT NULL,
    autor  TEXT NOT NULL,
    ano    INTEGER,
    lido   INTEGER DEFAULT 0
);
```

`onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)` — migração incremental, **sem DROP TABLE**:

```java
if (oldVersion < 2) {
    db.execSQL("ALTER TABLE " + TABELA_LIVROS + " ADD COLUMN " + COLUNA_NOTA + " INTEGER");
}
```

- Envolver `execSQL` em `try/catch (Exception e)` com `Log.e(...)` é aceitável (padrão da aula), mas **não** pode haver
  `DROP TABLE` no `onUpgrade`.
- **RN01:** `VERSAO` permanece `1` na entrega. O `onUpgrade` existe, compila e está correto, mas só roda quando a versão subir.

### 4.3 Contrato DAO — `persistencia/ILivroDAO.java` (RT01)

A interface declara **exatamente** estes 5 métodos (nem mais, nem menos):

```java
public interface ILivroDAO {
    boolean salvar(Livro livro);
    boolean atualizar(Livro livro);
    boolean excluir(Livro livro);
    List<Livro> listarTodos();
    int contar(Boolean lido);   // null = total; true = lidos; false = não lidos
}
```

### 4.4 Implementação — `persistencia/LivroDAO.java` (RT02, RF05)

`public class LivroDAO implements ILivroDAO`

Campos: `private final SQLiteDatabase escreve;` e `private final SQLiteDatabase le;`
Construtor: `public LivroDAO(Context context)` → `DbHelper db = new DbHelper(context); escreve = db.getWritableDatabase(); le = db.getReadableDatabase();`

| Método | Comportamento obrigatório | Retorno |
|---|---|---|
| `salvar(Livro)` | `ContentValues` com titulo, autor, ano, `lido ? 1 : 0` (sem id). `long id = escreve.insert(TABELA_LIVROS, null, cv)`. Se `id != -1`, faz `livro.setId(id)`. | `true` **somente se** `id != -1` |
| `atualizar(Livro)` | mesmos `ContentValues`; `int linhas = escreve.update(TABELA_LIVROS, cv, "id = ?", new String[]{ livro.getId().toString() })` | `true` **somente se** `linhas > 0` |
| `excluir(Livro)` | `int linhas = escreve.delete(TABELA_LIVROS, "id = ?", new String[]{ livro.getId().toString() })` | `true` **somente se** `linhas > 0` |
| `listarTodos()` | `SELECT * FROM livros ORDER BY titulo COLLATE NOCASE ASC` (DEC03); lê com `cursor.getColumnIndexOrThrow(...)`; `lido = cursor.getInt(idx) == 1`; fecha o cursor (`try/finally` ou try-with-resources) | `List<Livro>` (vazia se não houver registros, nunca `null`) |
| `contar(Boolean lido)` | `lido == null` → `SELECT COUNT(*) FROM livros`; caso contrário `SELECT COUNT(*) FROM livros WHERE lido = ?` com argumento `lido ? "1" : "0"`. Usa `rawQuery`, `moveToFirst()`, `getInt(0)`; fecha o cursor. | `int` |

Regras:
- **RN02:** conversão boolean↔INTEGER explícita: `livro.isLido() ? 1 : 0` na escrita e `cursor.getInt(...) == 1` na leitura.
- **RN03:** `contar` **não pode** chamar `listarTodos()` nem iterar cursor/lista com `for`/`while` para contar (RF05).
  Também não usar `DatabaseUtils.queryNumEntries` — o PDF exige `SELECT COUNT(*)` explícito.
- **RN04:** métodos de escrita capturam `Exception` (`try/catch`), registram com `Log.e(TAG, ...)` e retornam `false` em caso de erro.
- `TAG` é `private static final String TAG = "LivroDAO";` (constante, permitido).
- **DEC03:** `COLLATE NOCASE` mantém a ordenação por `titulo ASC` e evita que "a..." fique depois de "Z...".
  Se o professor exigir o texto literal, use `ORDER BY titulo ASC`.

---

## 5. Recursos (RT07)

**Regra absoluta:** nenhum texto visível hardcoded em layout, menu ou Java; nenhuma cor hex fora de `colors.xml`.
Em Java, textos via `getString(R.string...)` (inclusive Toast, `setError`, AlertDialog e "Atualizar").

### 5.1 `res/values/strings.xml` (catálogo — DEC04)

| name | valor |
|---|---|
| `app_name` | Biblioteca Pessoal |
| `menu_livros` | Livros |
| `menu_novo` | Novo |
| `menu_resumo` | Resumo |
| `titulo_lista` | Meus Livros |
| `titulo_cadastro` | Cadastro de Livro |
| `titulo_edicao` | Editar Livro *(opcional, DEC07)* |
| `titulo_resumo` | Resumo da Biblioteca |
| `hint_titulo` | Título do livro |
| `hint_autor` | Autor |
| `hint_ano` | Ano de publicação |
| `checkbox_lido` | Já li este livro |
| `botao_salvar` | Salvar |
| `botao_atualizar` | Atualizar |
| `status_lido` | Lido |
| `status_nao_lido` | Não lido |
| `formato_autor_ano` | `%1$s • %2$d` |
| `lista_vazia` | Nenhum livro cadastrado *(opcional, DEC08)* |
| `erro_titulo_obrigatorio` | Informe o título do livro |
| `erro_autor_obrigatorio` | Informe o autor |
| `erro_ano_obrigatorio` | Informe o ano de publicação |
| `erro_ano_invalido` | Ano inválido |
| `erro_ano_intervalo` | `O ano deve estar entre %1$d e %2$d` |
| `msg_salvo_sucesso` | Livro salvo com sucesso |
| `msg_salvo_erro` | Erro ao salvar o livro |
| `msg_atualizado_sucesso` | Livro atualizado com sucesso |
| `msg_atualizado_erro` | Erro ao atualizar o livro |
| `msg_excluido_sucesso` | Livro excluído com sucesso |
| `msg_excluido_erro` | Erro ao excluir o livro |
| `dialogo_excluir_titulo` | Excluir livro |
| `dialogo_excluir_mensagem` | `Deseja realmente excluir \"%1$s\"?` |
| `dialogo_sim` | Sim |
| `dialogo_nao` | Não |
| `resumo_total` | `Total de livros: %1$d` |
| `resumo_lidos` | `Livros lidos: %1$d` |
| `resumo_nao_lidos` | `Livros não lidos: %1$d` |
| `cd_status_leitura` | Status de leitura *(contentDescription, se usar ícone)* |

> Atenção XML: aspas dentro de string usam `\"`; apóstrofo usa `\'`.

### 5.2 `res/values/colors.xml` (catálogo — DEC05)

Manter `black` e `white` do template e acrescentar:

| name | valor sugerido | uso |
|---|---|---|
| `cor_primaria` | `#6A1B9A` | fundo do cabeçalho |
| `cor_texto_cabecalho` | `#FFFFFF` | texto do cabeçalho |
| `cor_status_lido` | `#2E7D32` | fundo da tag "Lido" |
| `cor_status_nao_lido` | `#C62828` | fundo da tag "Não lido" |
| `cor_texto_status` | `#FFFFFF` | texto das tags |
| `cor_texto_secundario` | `#616161` | linha autor • ano |
| `cor_resumo_destaque` | `#6A1B9A` | (opcional) números do resumo |

Qualquer `android:textColor`, `android:background`, `tint` etc. referencia `@color/...`.
Ícones vetoriais: se o Vector Asset gerar `android:fillColor="#FF000000"`, trocar por `@color/black`
(ou `@android:color/white` gerado pelo wizard pode ficar — não é hex).

### 5.3 `res/menu/menu_bottom.xml`

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_livros" android:icon="@drawable/ic_livros" android:title="@string/menu_livros" />
    <item android:id="@+id/nav_novo"   android:icon="@drawable/ic_novo"   android:title="@string/menu_novo" />
    <item android:id="@+id/nav_resumo" android:icon="@drawable/ic_resumo" android:title="@string/menu_resumo" />
</menu>
```

Ícones (Vector Asset > Clip Art): `ic_livros` = "menu_book" ou "library_books"; `ic_novo` = "add";
`ic_resumo` = "bar_chart" ou "insights".

---

## 6. Navegação (RT09)

### 6.1 `NavegacaoMenu.java` — Anexo A completo

- `public static void configurar(final Activity activity, final int itemAtual)`
  1. `BottomNavigationView nav = activity.findViewById(R.id.bottomNav);`
  2. `nav.setSelectedItemId(itemAtual);` **ANTES** do listener (senão entra em loop).
  3. `nav.setOnItemSelectedListener(...)`: se `id == itemAtual` → `return true`;
     `nav_livros` → `ListaLivrosActivity`; `nav_novo` → `CadastroLivroActivity`; `nav_resumo` → `ResumoActivity`;
     cada um via `irPara(activity, new Intent(activity, X.class))`. Retorna `true`.
- `public static void irPara(Activity origem, Intent intent)` — exatamente como no Anexo A:
  `addFlags(FLAG_ACTIVITY_NO_ANIMATION)`, `startActivity`, `finish()`.

### 6.2 Regras de navegação

- **RN05:** **toda** troca de tela usa `NavegacaoMenu.irPara(...)` — menu, clique para editar e retorno após salvar.
  Proibido `startActivity` direto nas Activities; proibido `finish()` sozinho após salvar.
- **RN06:** só existe uma Activity aberta por vez. Botão Voltar fecha o app em qualquer tela — **comportamento esperado**.
- **RN07:** cada Activity chama `NavegacaoMenu.configurar(this, R.id.nav_xxx)` no `onCreate`, depois do `setContentView`.
- **DEC06:** em modo edição, a `CadastroLivroActivity` continua marcando `nav_novo`. Tocar em "Novo" durante a edição não
  faz nada (é o item atual). Para criar outro livro: Livros → Novo. Aceitável e coerente com o Anexo A.

### 6.3 Diagrama de fluxo

```
            [Livros]                    [Novo]                     [Resumo]
ListaLivrosActivity  ◄──irPara──►  CadastroLivroActivity  ◄──irPara──►  ResumoActivity
     │  ▲                                  │                                   ▲
     │  └──── irPara (após salvar OK) ─────┘                                   │
     │                                                                          │
     └── clique simples: irPara(Intent + putExtra("livroSelecionado", livro)) ──► Cadastro (modo edição)
     └── clique longo: AlertDialog → dao.excluir → recarrega lista (sem recriar Activity)
     └─────────────────────────────── irPara ───────────────────────────────────┘
```

---

## 7. Telas

### 7.0 Regras comuns às 3 Activities

- Raiz de cada layout: `androidx.constraintlayout.widget.ConstraintLayout` com `android:id="@+id/main"`.
- Cabeçalho: `TextView` `@+id/txtCabecalho` no topo (largura `0dp`, presa a start/end/top do parent), texto `@string/titulo_...`,
  fundo `@color/cor_primaria`, texto `@color/cor_texto_cabecalho`.
- Último filho do ConstraintLayout: o `BottomNavigationView` do Anexo A (`@+id/bottomNav`, `layout_width="0dp"`,
  `app:menu="@menu/menu_bottom"`, preso a bottom/start/end do parent).
- `onCreate` (ordem):
  ```java
  super.onCreate(savedInstanceState);
  EdgeToEdge.enable(this);                       // já vem do template
  setContentView(R.layout.activity_xxx);
  ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);   // bottom = 0 !
      return insets;
  });
  NavegacaoMenu.configurar(this, R.id.nav_xxx);
  // ... inicialização específica
  ```

### 7.1 Tela 1 — `ListaLivrosActivity` (LAUNCHER) · RF02, RF03, RF04, RT04, RT05, RT06, RT08

**Layout `activity_lista_livros.xml`:**

| View | id | Restrições |
|---|---|---|
| ConstraintLayout | `main` | raiz |
| TextView | `txtCabecalho` | top/start/end → parent; texto `@string/titulo_lista` |
| RecyclerView | `recyclerLivros` | `layout_width="0dp"`, **`layout_height="0dp"`**, `top_toBottomOf="@id/txtCabecalho"`, `bottom_toTopOf="@id/bottomNav"`, start/end → parent |
| TextView *(opcional DEC08)* | `txtListaVazia` | centralizado na área da lista; `@string/lista_vazia`; `visibility` controlada no Java |
| BottomNavigationView | `bottomNav` | Anexo A |

**Layout do item `adapter_livro.xml`:** raiz `ConstraintLayout` (`layout_height="wrap_content"`, padding ~12–16dp) com:

| View | id | Conteúdo |
|---|---|---|
| TextView | `txtTitulo` | título (negrito, ~18sp), à esquerda; end → start da tag |
| TextView | `txtStatus` | tag "Lido"/"Não lido"; canto superior direito; texto `@color/cor_texto_status`; fundo definido no adapter |
| TextView | `txtAutorAno` | "Autor • Ano", abaixo do título, `@color/cor_texto_secundario` |

Separador entre itens: `new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)` na Activity (DEC09).

**`adapter/AdapterLivro.java`** (RT04):
- `public class AdapterLivro extends RecyclerView.Adapter<AdapterLivro.MyViewHolder>`
- Campo `private List<Livro> listaLivros;` · construtor `AdapterLivro(List<Livro> lista)`.
- `onCreateViewHolder`: infla `R.layout.adapter_livro`.
- `onBindViewHolder`:
  - `txtTitulo.setText(livro.getTitulo())`
  - `txtAutorAno.setText(ctx.getString(R.string.formato_autor_ano, livro.getAutor(), livro.getAno()))`
  - se `livro.isLido()` → texto `R.string.status_lido`, fundo `ContextCompat.getColor(ctx, R.color.cor_status_lido)`;
    senão → `R.string.status_nao_lido` + `R.color.cor_status_nao_lido`. (`ctx = holder.itemView.getContext()`)
- `getItemCount()` → `listaLivros.size()`.
- **Públicos obrigatórios:**
  - `public void atualizarLista(List<Livro> novaLista) { this.listaLivros = novaLista; notifyDataSetChanged(); }`
  - `public Livro getLivro(int position) { return listaLivros.get(position); }`
- `static class MyViewHolder extends RecyclerView.ViewHolder` com as 3 TextViews.

**`ListaLivrosActivity.java`:**
- Campos: `RecyclerView recyclerLivros; AdapterLivro adapter; ILivroDAO dao;` — **sem** campo `List<Livro>` (RT04).
- `onCreate`: regras comuns (`nav_livros`); `dao = new LivroDAO(this)`; `adapter = new AdapterLivro(new ArrayList<>())`;
  `recyclerLivros.setLayoutManager(new LinearLayoutManager(this))`; `setHasFixedSize(true)`; divider; `setAdapter(adapter)`;
  `addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerLivros, new RecyclerItemClickListener.OnItemClickListener() { ... }))`.
- `onResume()` (RT05): `super.onResume(); carregarLivros();`
- `private void carregarLivros()`: `adapter.atualizarLista(dao.listarTodos());` (+ visibilidade do estado vazio, se DEC08).
- `onItemClick(view, position)` (RF03, RT08):
  ```java
  if (position == RecyclerView.NO_POSITION) return;
  Livro livro = adapter.getLivro(position);
  Intent intent = new Intent(this, CadastroLivroActivity.class);
  intent.putExtra(CadastroLivroActivity.EXTRA_LIVRO, livro);
  NavegacaoMenu.irPara(this, intent);
  ```
- `onLongItemClick(view, position)` (RF04): guarda `NO_POSITION`; `Livro livro = adapter.getLivro(position)`;
  `new AlertDialog.Builder(this)` (androidx.appcompat) com título `dialogo_excluir_titulo`, mensagem
  `getString(R.string.dialogo_excluir_mensagem, livro.getTitulo())`, positivo `dialogo_sim` → `confirmarExclusao(livro)`,
  negativo `dialogo_nao` → `null`; `.show()`.
- `private void confirmarExclusao(Livro livro)`: `if (dao.excluir(livro)) { Toast sucesso; carregarLivros(); } else { Toast erro; }`
  — **sem** `recreate()`, sem `irPara`, sem reabrir a Activity.

### 7.2 Tela 2 — `CadastroLivroActivity` · RF01, RF03, RF06, RT08

**Layout `activity_cadastro_livro.xml`:**

| View | id | Detalhes |
|---|---|---|
| TextView | `txtCabecalho` | `@string/titulo_cadastro` |
| ScrollView *(recomendado)* | `scrollFormulario` | `0dp` x `0dp` entre `txtCabecalho` e `bottomNav`; contém um `LinearLayout` vertical com os campos |
| EditText | `edtTitulo` | `hint=@string/hint_titulo`, `inputType="textCapSentences"`, `maxLines=1` |
| EditText | `edtAutor` | `hint=@string/hint_autor`, `inputType="textPersonName\|textCapWords"` |
| EditText | `edtAno` | `hint=@string/hint_ano`, **`inputType="number"`**, `maxLength="4"` |
| CheckBox | `chkLido` | `text=@string/checkbox_lido` |
| Button | `btnSalvar` | `text=@string/botao_salvar`, largura total |
| BottomNavigationView | `bottomNav` | Anexo A |

**`CadastroLivroActivity.java`:**
- `public static final String EXTRA_LIVRO = "livroSelecionado";` (chave do Intent — constante, permitido)
- `private static final int ANO_MINIMO = 1450;`
- Campos de instância: `EditText edtTitulo, edtAutor, edtAno; CheckBox chkLido; Button btnSalvar; ILivroDAO dao; Livro livroAtual;`
  (`livroAtual == null` ⇒ inclusão; ≠ null ⇒ edição). **Nada `static` mutável.**
- `onCreate`: regras comuns (`nav_novo`); `findViewById`s; `dao = new LivroDAO(this)`;
  ```java
  livroAtual = (Livro) getIntent().getSerializableExtra(EXTRA_LIVRO);
  if (livroAtual != null) {
      edtTitulo.setText(livroAtual.getTitulo());
      edtAutor.setText(livroAtual.getAutor());
      edtAno.setText(String.valueOf(livroAtual.getAno()));
      chkLido.setChecked(livroAtual.isLido());
      btnSalvar.setText(getString(R.string.botao_atualizar));
      // opcional DEC07: txtCabecalho.setText(getString(R.string.titulo_edicao));
  }
  btnSalvar.setOnClickListener(v -> salvarLivro());
  ```
  (`getSerializableExtra(String)` está deprecated a partir da API 33, mas funciona e é o pedido no PDF. Pode-se
  anotar o método com `@SuppressWarnings("deprecation")`.)

- `private boolean validarCampos()` (RF01) — valida **todos** os campos e marca erro em cada um inválido:

  | Campo | Condição de erro | Mensagem (`setError`) |
  |---|---|---|
  | título | `trim().isEmpty()` | `erro_titulo_obrigatorio` |
  | autor | `trim().isEmpty()` | `erro_autor_obrigatorio` |
  | ano | vazio (checar **antes** do `parseInt`) | `erro_ano_obrigatorio` |
  | ano | `NumberFormatException` no `parseInt` | `erro_ano_invalido` |
  | ano | `< ANO_MINIMO` ou `> anoAtual` | `getString(R.string.erro_ano_intervalo, ANO_MINIMO, anoAtual)` |

  - `int anoAtual = Calendar.getInstance().get(Calendar.YEAR);` — **nunca** escrever o ano fixo.
  - Retorna `false` se algum campo for inválido; o primeiro campo inválido recebe `requestFocus()`.
  - **RN08:** se inválido, **não** grava nada e não navega.

- `private void salvarLivro()`:
  ```java
  if (!validarCampos()) return;
  String titulo = edtTitulo.getText().toString().trim();
  String autor  = edtAutor.getText().toString().trim();
  int ano       = Integer.parseInt(edtAno.getText().toString().trim());
  boolean lido  = chkLido.isChecked();

  if (livroAtual == null) {                     // inclusão
      boolean ok = dao.salvar(new Livro(titulo, autor, ano, lido));
      toast(ok ? R.string.msg_salvo_sucesso : R.string.msg_salvo_erro);
      if (ok) NavegacaoMenu.irPara(this, new Intent(this, ListaLivrosActivity.class));
  } else {                                      // edição (mantém o id)
      livroAtual.setTitulo(titulo); livroAtual.setAutor(autor);
      livroAtual.setAno(ano);       livroAtual.setLido(lido);
      boolean ok = dao.atualizar(livroAtual);
      toast(ok ? R.string.msg_atualizado_sucesso : R.string.msg_atualizado_erro);
      if (ok) NavegacaoMenu.irPara(this, new Intent(this, ListaLivrosActivity.class));
  }
  ```
  `toast(int resId)` = `Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();`
  - **RN09:** em falha, mostra Toast de erro e **permanece** na tela.
  - **RN10:** os valores gravados são os `trim()`ados.

### 7.3 Tela 3 — `ResumoActivity` · RF05

**Layout `activity_resumo.xml`:** cabeçalho `@string/titulo_resumo`; três TextViews `txtTotal`, `txtLidos`, `txtNaoLidos`
empilhadas abaixo do cabeçalho (~20sp, margem 16dp); `bottomNav`.

**`ResumoActivity.java`:** regras comuns (`nav_resumo`); `ILivroDAO dao = new LivroDAO(this);`
```java
txtTotal.setText(getString(R.string.resumo_total, dao.contar(null)));
txtLidos.setText(getString(R.string.resumo_lidos, dao.contar(true)));
txtNaoLidos.setText(getString(R.string.resumo_nao_lidos, dao.contar(false)));
```
No `onCreate` (como manda o PDF). Como toda navegação recria a Activity, os números estão sempre atualizados.

### 7.4 `RecyclerItemClickListener.java`

Cópia **fiel** do Anexo B (versão corrigida): interface interna `OnItemClickListener` com apenas
`onItemClick(View, int)` e `onLongItemClick(View, int)`; **não** herda de `AdapterView.OnItemClickListener`.

### 7.5 `AndroidManifest.xml`

- `<application android:label="@string/app_name" ...>`
- `ListaLivrosActivity` com `android:exported="true"` e `intent-filter` MAIN/LAUNCHER.
- `CadastroLivroActivity` e `ResumoActivity` com `android:exported="false"`, sem intent-filter.
- Nenhuma referência a `MainActivity`.

---

## 8. Fluxos principais

| Fluxo | Passos | Resultado esperado |
|---|---|---|
| F1 Inclusão | Novo → preenche → Salvar | Toast "salvo"; abre Livros; item aparece na posição alfabética |
| F2 Validação | Novo → Salvar em branco | erro em título, autor e ano; nada gravado; continua no cadastro |
| F3 Edição | Livros → toque no item → campos preenchidos, botão "Atualizar" → altera → Atualizar | Toast "atualizado"; abre Livros com dados novos; **mesmo id** (não duplica) |
| F4 Exclusão | Livros → toque longo → diálogo → Sim | Toast "excluído"; item some na hora, sem recriar a Activity |
| F4b Cancelar exclusão | toque longo → Não | nada muda |
| F5 Resumo | Resumo | total = lidos + não lidos, valores batem com a lista |
| F6 Navegação | alternar entre as 3 abas | item correto marcado em cada tela; sem loop; Voltar fecha o app |

---

## 9. Casos de borda

| Caso | Tratamento |
|---|---|
| Título/autor só com espaços | `trim()` → erro |
| Ano vazio | erro antes do `parseInt` (sem crash) |
| Ano com letras/colado | `inputType="number"` impede; `try/catch` cobre o resto |
| Ano 1449 / ano atual + 1 | erro de intervalo |
| Ano 1450 / ano atual | **válidos** (limites inclusivos) |
| Lista vazia | RecyclerView vazio (opcional: mensagem `lista_vazia`) |
| Toque fora de item / posição `-1` | ignorar (`NO_POSITION`) |
| Rotação na tela de cadastro | EditTexts com id preservam texto automaticamente |
| Títulos com maiúsc./minúsc. | `COLLATE NOCASE` (DEC03) |
| Falha de banco | métodos do DAO retornam `false`/lista vazia; Toast de erro |

---

## 10. Qualidade e código limpo (critério RT07 / 1,0 pt)

- Pacotes e nomes de arquivos **exatamente** como na seção 3.2.
- Sem texto visível literal em XML/Java; sem cor hex fora de `colors.xml` (também em `themes.xml`, se alterado).
- Sem `static` mutável; sem `startActivity` fora de `NavegacaoMenu`; sem `finish()` solto após salvar.
- Sem imports não usados, sem código comentado/morto, sem `MainActivity`/`activity_main`.
- Comentários curtos em português explicando cada método (no estilo do Anexo A/B).
- `Cursor` sempre fechado.
- Nomes de variáveis em português, coerentes com o PDF (`edtTitulo`, `chkLido`, `dao`, `adapter`...).
- `gradlew lintDebug` sem avisos `HardcodedText` / `SetTextI18n`.

---

## 11. Matriz de rastreabilidade (requisito → implementação → teste → nota)

| Req. | Onde é implementado | CA | Critério do PDF (pts) |
|---|---|---|---|
| RT03 | `Livro`, `DbHelper` (constantes, `onCreate`, `onUpgrade` v2) | CA01, CA02 | Modelo + DbHelper (1,0) |
| RT01 | `ILivroDAO` | CA03 | DAO (2,0) |
| RT02 | `LivroDAO.salvar/atualizar/excluir` | CA04 | DAO (2,0) |
| RF05 | `LivroDAO.contar` + `ResumoActivity` | CA05, CA15 | DAO (2,0) |
| RF01 | `CadastroLivroActivity.validarCampos` | CA06, CA07, CA08 | Formulário (1,0) |
| RF02 | `listarTodos` (ORDER BY) + `AdapterLivro` (tag) | CA09, CA10 | Listagem (1,5) |
| RT04 | `AdapterLivro.atualizarLista/getLivro` | CA11 | Listagem (1,5) |
| RT06 | `activity_lista_livros.xml` | CA12 | Listagem (1,5) |
| RT05 | `ListaLivrosActivity.onResume` | CA11 | Listagem (1,5) |
| RF03 | clique simples + modo edição | CA13 | Edição/Exclusão (2,0) |
| RT08 | `putExtra`/`getSerializableExtra` | CA13 | Edição/Exclusão (2,0) |
| RF04 | clique longo + AlertDialog + recarga | CA14 | Edição/Exclusão (2,0) |
| RF06 | Toasts em todas as operações | CA06–CA14 | (transversal) |
| RT09 | `NavegacaoMenu` + 3 Activities + retorno após salvar | CA16, CA17 | Navegação (1,5) |
| RT07 | `strings.xml`, `colors.xml`, pacotes | CA18, CA19 | Organização (1,0) |

---

## 12. Critérios de aceitação

| ID | Dado / Quando / Então |
|---|---|
| CA01 | `DbHelper` tem `NOME_BANCO`, `VERSAO = 1`, `TABELA_LIVROS`, `COLUNA_*` como `public static final`; `onCreate` usa `lido INTEGER DEFAULT 0`. |
| CA02 | `onUpgrade` contém `if (oldVersion < 2)` + `ALTER TABLE livros ADD COLUMN nota INTEGER`; **sem** `DROP TABLE`. |
| CA03 | `ILivroDAO` tem exatamente os 5 métodos com as assinaturas da seção 4.3. |
| CA04 | `salvar` retorna `insert != -1`; `atualizar`/`excluir` retornam `linhas > 0`. |
| CA05 | `contar` usa `SELECT COUNT(*)` via `rawQuery`; nenhum laço de contagem; `null`/`true`/`false` corretos. |
| CA06 | Salvar com tudo em branco → `setError` nos 3 EditTexts, nada gravado, continua na tela. |
| CA07 | Ano `1449` e `anoAtual+1` → erro de intervalo; `1450` e `anoAtual` aceitos. |
| CA08 | O código obtém o ano atual com `Calendar.getInstance().get(Calendar.YEAR)` (sem número fixo). |
| CA09 | Lista exibe os livros em ordem alfabética do título. |
| CA10 | Cada item mostra título, "autor • ano" e uma tag colorida diferente para Lido / Não lido. |
| CA11 | `ListaLivrosActivity` carrega dados em `onResume()` via `adapter.atualizarLista(dao.listarTodos())`; não guarda cópia da lista. |
| CA12 | RecyclerView com `LinearLayoutManager`, `layout_height="0dp"`, entre cabeçalho e `bottomNav`. |
| CA13 | Toque simples abre o cadastro preenchido, botão "Atualizar"; ao atualizar, o registro é alterado (não duplicado). Livro trafega via `putExtra`/`getSerializableExtra`. |
| CA14 | Toque longo abre AlertDialog; "Sim" exclui e atualiza a lista na hora; "Não" não muda nada; Toast em sucesso/falha. |
| CA15 | Tela Resumo mostra total, lidos, não lidos coerentes com a lista. |
| CA16 | As 3 telas têm o mesmo `BottomNavigationView`, com o item da tela atual marcado; trocar de aba não entra em loop. |
| CA17 | Toda troca de tela passa por `NavegacaoMenu.irPara` (menu, editar, voltar após salvar); não existe `startActivity` nas Activities. |
| CA18 | Nenhum texto visível hardcoded (layouts, menu, Java) e nenhuma cor hex fora de `colors.xml`. |
| CA19 | Estrutura de pacotes/arquivos igual à seção 3.2; sem `MainActivity`/`activity_main`; código limpo. |
| CA20 | Sem espaço duplo abaixo do menu (insets com bottom = 0) nas 3 telas. |

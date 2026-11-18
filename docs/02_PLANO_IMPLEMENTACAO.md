# Plano de Implementação — Biblioteca Pessoal

> Plano em **etapas pequenas e verificáveis** para executar com agentes no **Antigravity** e compilar/rodar no
> **Android Studio**. Cada etapa tem: objetivo, requisitos cobertos, arquivos, tarefas, definição de pronto (DoD),
> verificação e o **prompt pronto** para colar no agente.
>
> - Especificação: `docs/01_SDD.md` (o agente deve lê-la sempre).
> - Passos manuais (seus): `docs/03_PLANO_MANUAL.md` — os marcadores **🖐 MANUAL** abaixo apontam para eles.
> - Validação final: `docs/04_CHECKLIST_VALIDACAO.md`.

## Visão geral das etapas

| # | Etapa | Quem | Requisitos | Peso na nota | Checkpoint no emulador? |
|---|---|---|---|---|---|
| 0 | Preparação do projeto e do Antigravity | 🖐 Você | Passo 1 do PDF | base | ✅ app "Hello" roda |
| 1 | Camada de dados (Model + SQLite + DAO) | 🤖 Agente | RT01, RT02, RT03, RF05 (DAO) | 3,0 | — (só compila) |
| 2 | Recursos + navegação compartilhada | 🖐 ícones · 🤖 resto | RT07, RT09 | 2,5 (parcial) | — (só compila) |
| 3 | Esqueleto das 3 telas + menu inferior | 🤖 Agente | RT09, RT06 (parcial), CA20 | 1,5 | ✅ navegar entre 3 abas |
| 4 | Cadastro com validação | 🤖 Agente | RF01, RF06 | 1,0 | ✅ validar e salvar |
| 5 | Listagem com Adapter | 🤖 Agente | RF02, RT04, RT05, RT06 | 1,5 | ✅ ver livros ordenados |
| 6 | Edição e exclusão | 🤖 Agente | RF03, RF04, RT08, RF06 | 2,0 | ✅ editar / excluir |
| 7 | Resumo | 🤖 Agente | RF05 | (DAO) | ✅ contadores |
| 8 | Auditoria de conformidade e polimento | 🤖 Agente + 🖐 revisão | RT07 + todos | 1,0 | ✅ roteiro completo |
| 9 | Testes finais, vídeo e entrega | 🖐 Você | Passo 5 do PDF | — | ✅ |

**Regra de ouro do fluxo:** uma etapa por conversa do agente → revisar o diff → compilar → testar o checkpoint →
`git commit` → próxima etapa. Se algo quebrar, `git` volta ao último commit bom.

---

## Prompt de contexto (cole no INÍCIO de cada nova conversa do agente)

```text
Você está trabalhando no projeto Android "Biblioteca Pessoal" (Java, SQLite, pacote com.example.bibliotecapessoal),
criado no Android Studio com o template Empty Views Activity. Eu compilo e rodo no Android Studio; você edita os arquivos.

Antes de qualquer alteração, leia por completo:
- docs/01_SDD.md (especificação — fonte da verdade derivada do PDF do professor)
- docs/02_PLANO_IMPLEMENTACAO.md (plano por etapas)
- .agent/rules/biblioteca-pessoal.md (regras obrigatórias)

Regras de trabalho:
1. Implemente SOMENTE a etapa que eu pedir. Não adiante etapas, não refatore o que não foi pedido.
2. Siga nomes de classes, métodos, ids, strings e cores EXATAMENTE como no SDD.
3. Java apenas. Sem Kotlin, Room, Fragments, ViewBinding ou bibliotecas novas.
4. Nenhum texto visível ou cor hardcoded: use strings.xml / colors.xml e getString(R.string...).
5. Ao final, rode a compilação (Windows: .\gradlew.bat assembleDebug | macOS/Linux: ./gradlew assembleDebug),
   corrija erros e me entregue: lista de arquivos alterados, como cada requisito da etapa foi atendido
   (citando RF/RT/CA do SDD) e o que eu devo testar manualmente no emulador.
6. Não altere build.gradle(.kts), libs.versions.toml ou AndroidManifest.xml a menos que a etapa peça.
```

> Dica Antigravity: use o modo **Planning** para cada etapa (o agente mostra o plano antes de editar) e aprove/ajuste.
> Se o terminal do agente não conseguir rodar o Gradle, compile você no Android Studio (**Build › Make Project**) e
> cole os erros para o agente.

---

## Etapa 0 — Preparação 🖐 MANUAL

Executar **Etapa M0 e M1** de `docs/03_PLANO_MANUAL.md`:
- Criar o projeto (Empty Views Activity, Java, `com.example.bibliotecapessoal`).
- Renomear `MainActivity` → `ListaLivrosActivity` **e** `activity_main.xml` → `activity_lista_livros.xml`.
- Criar `CadastroLivroActivity` e `ResumoActivity` pelo wizard (gera Java + layout + Manifest).
- Conferir dependência Material; criar pacotes `model`, `persistencia`, `adapter`.
- Copiar `docs/` e `.agent/` deste kit para a raiz do projeto; `git init` + 1º commit.
- Abrir a pasta do projeto no Antigravity.

**DoD:** app roda no emulador mostrando a tela padrão; 3 Activities existem no Manifest; commit `chore: projeto base`.

---

## Etapa 1 — Camada de dados 🤖

**Requisitos:** RT03, RT01, RT02, RF05 (parte DAO), RN01–RN04 · **SDD:** seções 4.1–4.4

**Arquivos (criar):**
- `model/Livro.java`
- `persistencia/DbHelper.java`
- `persistencia/ILivroDAO.java`
- `persistencia/LivroDAO.java`

**Tarefas:**
1. `Livro implements Serializable` com `serialVersionUID`, atributos `id (Long)`, `titulo`, `autor`, `ano (int)`, `lido (boolean)`, 2–3 construtores, getters/setters (`isLido`).
2. `DbHelper` com constantes `public static final` (nome, `VERSAO = 1`, tabela `livros`, colunas incl. `nota`), `onCreate` com `lido INTEGER DEFAULT 0`, `onUpgrade` com `if (oldVersion < 2) ALTER TABLE ... ADD COLUMN nota INTEGER` (sem DROP).
3. `ILivroDAO` com **exatamente** os 5 métodos.
4. `LivroDAO` com `escreve`/`le`, conversão boolean↔int explícita, validação de retorno (`!= -1`, `> 0`), `listarTodos` ordenado por título, `contar` com `SELECT COUNT(*)` (sem laço), cursores fechados, try/catch + `Log.e` nos métodos de escrita.

**DoD:** compila; CA01–CA05 atendidos por leitura de código.

**Verificação:**
- `assembleDebug` OK.
- Revisão rápida (você): procurar `DROP` (não pode existir), `for`/`while` dentro de `contar` (não pode), `VERSAO = 1`.

**Prompt:**
```text
Execute a ETAPA 1 (Camada de dados) do docs/02_PLANO_IMPLEMENTACAO.md, seguindo as seções 4.1 a 4.4 do docs/01_SDD.md.
Crie: model/Livro.java, persistencia/DbHelper.java, persistencia/ILivroDAO.java, persistencia/LivroDAO.java
dentro de app/src/main/java/com/example/bibliotecapessoal/.
Pontos críticos: constantes public static final no DbHelper; VERSAO = 1; onUpgrade com "if (oldVersion < 2)"
e ALTER TABLE ADD COLUMN nota INTEGER, sem DROP TABLE; ILivroDAO com exatamente salvar, atualizar, excluir,
listarTodos e contar(Boolean lido); insert validado com != -1 e update/delete com > 0; conversão
isLido() ? 1 : 0 e getInt(...) == 1; contar() usando rawQuery com SELECT COUNT(*) (null = total, true = 1, false = 0),
sem carregar lista nem usar laço; listarTodos ordenado por titulo ASC (COLLATE NOCASE); fechar todos os Cursors.
Adicione comentários curtos em português em cada método. Não crie telas nem altere outros arquivos.
Compile e me mostre como cada critério CA01–CA05 foi atendido.
```

**Commit:** `feat: camada de dados (Livro, DbHelper, DAO)`

---

## Etapa 2 — Recursos e navegação compartilhada 🖐+🤖

**Pré-requisito 🖐 MANUAL:** Etapa **M2** do plano manual (criar `ic_livros`, `ic_novo`, `ic_resumo` via Vector Asset).

**Requisitos:** RT07, RT09 · **SDD:** seções 5, 6, 7.4

**Arquivos:**
- `res/values/strings.xml` (editar — catálogo completo da seção 5.1)
- `res/values/colors.xml` (editar — catálogo da seção 5.2)
- `res/menu/menu_bottom.xml` (criar)
- `NavegacaoMenu.java` (criar — Anexo A completo)
- `RecyclerItemClickListener.java` (criar — Anexo B fiel)
- `res/drawable/ic_*.xml` (só conferir: trocar `fillColor` hex por `@color/...` se houver)

**Tarefas:**
1. Inserir todas as strings e cores do catálogo (manter `app_name` = "Biblioteca Pessoal", `black`, `white`).
2. Criar `menu_bottom.xml` com `nav_livros`, `nav_novo`, `nav_resumo`, ícones e títulos via `@string`.
3. Criar `NavegacaoMenu` com `configurar` (setSelectedItemId **antes** do listener; tratar os 3 itens) e `irPara` idêntico ao Anexo A.
4. Criar `RecyclerItemClickListener` idêntico ao Anexo B.

**DoD:** compila; nenhuma tela usa ainda o menu (isso é a Etapa 3).

**Prompt:**
```text
Execute a ETAPA 2 do docs/02_PLANO_IMPLEMENTACAO.md seguindo as seções 5, 6 e 7.4 do docs/01_SDD.md.
1) Atualize res/values/strings.xml com TODO o catálogo da seção 5.1 (escape correto de aspas) e
   res/values/colors.xml com o catálogo da seção 5.2 (mantenha black e white).
2) Crie res/menu/menu_bottom.xml com os itens nav_livros, nav_novo e nav_resumo usando @drawable/ic_livros,
   @drawable/ic_novo, @drawable/ic_resumo (já existem) e títulos @string/menu_*.
3) Confira os 3 vetores em res/drawable/ic_*.xml: se houver cor hex (#...), troque por @color/black.
4) Crie NavegacaoMenu.java na raiz do pacote completando o Anexo A (transcrito na seção 6.1 do SDD):
   setSelectedItemId ANTES de setOnItemSelectedListener; nav_livros -> ListaLivrosActivity,
   nav_novo -> CadastroLivroActivity, nav_resumo -> ResumoActivity, todos via irPara; irPara exatamente
   como no anexo (FLAG_ACTIVITY_NO_ANIMATION, startActivity, finish).
5) Crie RecyclerItemClickListener.java na raiz do pacote, idêntico ao Anexo B (interface interna apenas com
   onItemClick e onLongItemClick, sem herdar AdapterView.OnItemClickListener).
Não altere as Activities ainda. Compile e liste os arquivos alterados.
```

**Commit:** `feat: recursos, menu inferior e NavegacaoMenu`

---

## Etapa 3 — Esqueleto das 3 telas + menu inferior 🤖

**Requisitos:** RT09, RT06 (estrutura), CA16, CA17, CA20 · **SDD:** seções 7.0, 7.1 (layout), 7.2 (layout), 7.3 (layout), 7.5

**Arquivos (editar):** `activity_lista_livros.xml`, `activity_cadastro_livro.xml`, `activity_resumo.xml`,
`ListaLivrosActivity.java`, `CadastroLivroActivity.java`, `ResumoActivity.java`, `AndroidManifest.xml` (só conferir).

**Tarefas:**
1. Cada layout: ConstraintLayout `@+id/main`, `txtCabecalho` (texto/cores via recursos), conteúdo da tela e
   `BottomNavigationView @+id/bottomNav` como **último** filho (Anexo A).
2. Lista: `RecyclerView @+id/recyclerLivros` com `0dp` x `0dp` entre cabeçalho e `bottomNav`.
3. Cadastro: ScrollView com `edtTitulo`, `edtAutor`, `edtAno (number, maxLength 4)`, `chkLido`, `btnSalvar` (sem lógica ainda).
4. Resumo: `txtTotal`, `txtLidos`, `txtNaoLidos` (sem lógica ainda).
5. Cada Activity: insets com **bottom = 0**, `NavegacaoMenu.configurar(this, R.id.nav_xxx)`.
6. Manifest: conferir LAUNCHER na `ListaLivrosActivity`, `label=@string/app_name`, sem `MainActivity`.

**DoD / Checkpoint 🖐:** rodar no emulador → alternar Livros/Novo/Resumo, item correto marcado, sem loop,
sem espaço duplo abaixo do menu, Voltar fecha o app.

**Prompt:**
```text
Execute a ETAPA 3 do docs/02_PLANO_IMPLEMENTACAO.md seguindo as seções 7.0, 7.5 e os quadros de LAYOUT das
seções 7.1, 7.2 e 7.3 do docs/01_SDD.md.
- Reescreva activity_lista_livros.xml, activity_cadastro_livro.xml e activity_resumo.xml: raiz ConstraintLayout
  com id "main", TextView txtCabecalho no topo, conteúdo, e o BottomNavigationView do Anexo A (id bottomNav)
  como ÚLTIMO elemento. Na lista, RecyclerView recyclerLivros com layout_width="0dp", layout_height="0dp",
  top_toBottomOf txtCabecalho e bottom_toTopOf bottomNav. No cadastro, os campos edtTitulo, edtAutor,
  edtAno (inputType number, maxLength 4), chkLido e btnSalvar dentro de um ScrollView entre cabeçalho e menu.
  No resumo, txtTotal, txtLidos e txtNaoLidos. Todos os textos/hints via @string e cores via @color.
- Nas 3 Activities: manter EdgeToEdge.enable, ajustar o listener de insets para bottom = 0 e chamar
  NavegacaoMenu.configurar(this, R.id.nav_livros | nav_novo | nav_resumo) logo após o setContentView.
  Ainda NÃO implemente RecyclerView/adapter, validação ou contadores.
- Confira o AndroidManifest.xml (LAUNCHER em ListaLivrosActivity, sem MainActivity, label @string/app_name).
Compile e me diga o que testar no emulador.
```

**Commit:** `feat: esqueleto das telas com navegação inferior`

---

## Etapa 4 — Cadastro com validação 🤖

**Requisitos:** RF01, RF06, RN08–RN10, CA06–CA08 · **SDD:** seção 7.2 (Java)

**Arquivo:** `CadastroLivroActivity.java`

**Tarefas:**
1. Constantes `EXTRA_LIVRO = "livroSelecionado"` e `ANO_MINIMO = 1450`; campos de instância; `dao = new LivroDAO(this)`.
2. Leitura do extra (`getSerializableExtra`) → preencher campos e botão "Atualizar" via `getString` (preparado para a Etapa 6).
3. `validarCampos()` com `setError` em **cada** campo inválido, ano vazio checado antes do `parseInt`, `try/catch`
   para `NumberFormatException`, intervalo `1450..Calendar.YEAR`, foco no primeiro inválido.
4. `salvarLivro()` inclusão/edição, Toast de sucesso/erro, `NavegacaoMenu.irPara(... ListaLivrosActivity)` em sucesso.

**Checkpoint 🖐:** salvar em branco (3 erros), ano 1449 e ano atual+1 (erro), salvar 1 livro válido (Toast + vai para
Livros — a lista ainda aparece vazia, é esperado até a Etapa 5). Opcional: conferir no **App Inspection › Database Inspector**.

**Prompt:**
```text
Execute a ETAPA 4 do docs/02_PLANO_IMPLEMENTACAO.md implementando a lógica de CadastroLivroActivity.java conforme
a seção 7.2 do docs/01_SDD.md:
- public static final String EXTRA_LIVRO = "livroSelecionado"; private static final int ANO_MINIMO = 1450;
- campos de instância (nenhum static mutável), dao do tipo ILivroDAO instanciado com new LivroDAO(this);
- no onCreate, ler (Livro) getIntent().getSerializableExtra(EXTRA_LIVRO); se não for null, preencher os campos,
  marcar o CheckBox e mudar o botão para getString(R.string.botao_atualizar);
- validarCampos(): valida todos os campos, setError com getString em cada inválido, ano vazio checado ANTES do
  Integer.parseInt, try/catch de NumberFormatException, intervalo 1450..Calendar.getInstance().get(Calendar.YEAR)
  usando R.string.erro_ano_intervalo com os dois limites, requestFocus no primeiro inválido;
- salvarLivro(): se inválido retorna; senão salvar (inclusão) ou atualizar (edição, mantendo o id do livroAtual),
  Toast via getString para sucesso e erro, e em caso de sucesso NavegacaoMenu.irPara(this, new Intent(this,
  ListaLivrosActivity.class)). Nunca usar finish() sozinho.
Compile e liste os cenários de teste (CA06, CA07, CA08).
```

**Commit:** `feat: cadastro com validação`

---

## Etapa 5 — Listagem com Adapter 🤖

**Requisitos:** RF02, RT04, RT05, RT06, CA09–CA12 · **SDD:** seção 7.1

**Arquivos:** `res/layout/adapter_livro.xml` (criar), `adapter/AdapterLivro.java` (criar), `ListaLivrosActivity.java` (editar)

**Tarefas:**
1. `adapter_livro.xml` com `txtTitulo`, `txtAutorAno`, `txtStatus` (tag colorida).
2. `AdapterLivro` com `atualizarLista(List<Livro>)` (+ `notifyDataSetChanged`) e `getLivro(int)`; indicador visual Lido/Não lido via `R.string`/`R.color`.
3. `ListaLivrosActivity`: `LinearLayoutManager`, divider, adapter iniciado com lista vazia, **`onResume()` → `carregarLivros()`**, sem campo `List<Livro>` na Activity.
4. (Opcional DEC08) `txtListaVazia` visível quando `adapter.getItemCount() == 0`.

**Checkpoint 🖐:** cadastrar 3 livros fora de ordem (ex.: "O Hobbit", "Clean Code", "Dom Casmurro"), lidos e não lidos → lista em ordem alfabética com tags de cores diferentes.

**Prompt:**
```text
Execute a ETAPA 5 do docs/02_PLANO_IMPLEMENTACAO.md conforme a seção 7.1 do docs/01_SDD.md:
1) Crie res/layout/adapter_livro.xml (ConstraintLayout, wrap_content) com txtTitulo, txtAutorAno e txtStatus
   (tag no canto superior direito), cores e textos só via recursos.
2) Crie adapter/AdapterLivro.java (RecyclerView.Adapter<AdapterLivro.MyViewHolder>) com os métodos públicos
   atualizarLista(List<Livro> novaLista) — substitui a lista e chama notifyDataSetChanged() — e getLivro(int position).
   No onBindViewHolder use getString(R.string.formato_autor_ano, autor, ano) e diferencie Lido/Não lido com
   R.string.status_lido/status_nao_lido e ContextCompat.getColor(R.color.cor_status_lido/cor_status_nao_lido).
3) Em ListaLivrosActivity.java: dao (ILivroDAO), adapter iniciado com new ArrayList<>(), LinearLayoutManager,
   setHasFixedSize(true), DividerItemDecoration, e carregarLivros() chamado em onResume() fazendo
   adapter.atualizarLista(dao.listarTodos()). A Activity NÃO deve guardar um campo List<Livro>.
Ainda NÃO implemente cliques (isso é a Etapa 6). Compile e diga o que testar.
```

**Commit:** `feat: listagem com AdapterLivro`

---

## Etapa 6 — Edição (clique simples) e exclusão (clique longo) 🤖

**Requisitos:** RF03, RF04, RT08, RF06, CA13, CA14, CA17 · **SDD:** seção 7.1 (Java), 7.2

**Arquivo:** `ListaLivrosActivity.java`

**Tarefas:**
1. `addOnItemTouchListener(new RecyclerItemClickListener(...))`.
2. `onItemClick`: guarda `NO_POSITION`; `adapter.getLivro(position)`; `putExtra(CadastroLivroActivity.EXTRA_LIVRO, livro)`; `NavegacaoMenu.irPara(this, intent)`.
3. `onLongItemClick`: `AlertDialog` (androidx.appcompat) com textos de `strings.xml`; "Sim" → `dao.excluir` → Toast + `carregarLivros()`; "Não" → nada.

**Checkpoint 🖐:** editar um livro (botão "Atualizar", dados mudam, sem duplicar); excluir outro (some na hora); cancelar exclusão.

**Prompt:**
```text
Execute a ETAPA 6 do docs/02_PLANO_IMPLEMENTACAO.md em ListaLivrosActivity.java conforme a seção 7.1 do docs/01_SDD.md:
- registre new RecyclerItemClickListener(this, recyclerLivros, new RecyclerItemClickListener.OnItemClickListener(){...});
- onItemClick: ignore RecyclerView.NO_POSITION; obtenha o livro SOMENTE com adapter.getLivro(position);
  crie Intent para CadastroLivroActivity com putExtra(CadastroLivroActivity.EXTRA_LIVRO, livro) e abra com
  NavegacaoMenu.irPara(this, intent);
- onLongItemClick: ignore NO_POSITION; adapter.getLivro(position); androidx.appcompat.app.AlertDialog com
  título R.string.dialogo_excluir_titulo, mensagem getString(R.string.dialogo_excluir_mensagem, livro.getTitulo()),
  botão positivo R.string.dialogo_sim que chama confirmarExclusao(livro) e negativo R.string.dialogo_nao;
- confirmarExclusao: se dao.excluir(livro) -> Toast msg_excluido_sucesso e carregarLivros(); senão Toast
  msg_excluido_erro. NÃO usar recreate(), finish() ou irPara aqui.
Confirme que nenhuma Activity chama startActivity diretamente (só NavegacaoMenu). Compile e liste os testes (CA13, CA14).
```

**Commit:** `feat: edição por clique e exclusão com confirmação`

---

## Etapa 7 — Resumo 🤖

**Requisitos:** RF05, CA05, CA15 · **SDD:** seção 7.3

**Arquivo:** `ResumoActivity.java`

**Prompt:**
```text
Execute a ETAPA 7 do docs/02_PLANO_IMPLEMENTACAO.md em ResumoActivity.java conforme a seção 7.3 do docs/01_SDD.md:
no onCreate, crie ILivroDAO dao = new LivroDAO(this) e preencha txtTotal, txtLidos e txtNaoLidos com
getString(R.string.resumo_total/resumo_lidos/resumo_nao_lidos, dao.contar(null/true/false)).
Não carregue a lista nem use laço para contar. Compile.
```

**Checkpoint 🖐:** com 3 livros (2 lidos, 1 não lido) → 3 / 2 / 1. Excluir um e voltar ao Resumo → atualizado.

**Commit:** `feat: tela de resumo com COUNT(*)`

---

## Etapa 8 — Auditoria de conformidade e polimento 🤖 + 🖐

**Requisitos:** RT07 e revisão de todos os CA · **SDD:** seções 10, 11, 12

**Tarefas do agente:**
1. Rodar `lintDebug` e corrigir `HardcodedText`, `SetTextI18n`, imports não usados.
2. Varredura por: textos literais em XML (`android:text="`, `android:hint="`, `android:title="` sem `@string`),
   cores hex fora de `colors.xml`, `static` mutável, `startActivity` fora de `NavegacaoMenu`, `finish()` fora de `NavegacaoMenu`,
   `DROP TABLE`, laço em `contar`, `MainActivity`/`activity_main`.
3. Produzir um relatório CA01–CA20 com status ✅/❌ e o arquivo:linha de evidência.

**Prompt:**
```text
Execute a ETAPA 8 (auditoria) do docs/02_PLANO_IMPLEMENTACAO.md.
1) Rode o lint (Windows: .\gradlew.bat lintDebug | macOS/Linux: ./gradlew lintDebug) e corrija avisos
   HardcodedText, SetTextI18n e imports não usados nos arquivos do projeto.
2) Procure e corrija, em app/src/main: textos visíveis literais em layouts/menu/Java; cores "#" fora de
   res/values/colors.xml; variáveis static não-final; chamadas startActivity ou finish() fora de NavegacaoMenu.java;
   "DROP TABLE"; laço for/while dentro de LivroDAO.contar; qualquer resto de MainActivity/activity_main.
3) Confira a estrutura de pacotes/arquivos da seção 3.2 do SDD (nem mais, nem menos arquivos de código).
4) Gere docs/RELATORIO_AUDITORIA.md com uma tabela CA01..CA20 (seção 12 do SDD): status ✅/❌,
   evidência (arquivo:linha) e correção aplicada.
Não mude comportamento que já atende ao SDD. Compile ao final.
```

**🖐 Sua parte:** ler o relatório, abrir os arquivos citados e conferir por amostragem; rodar o roteiro completo
de `04_CHECKLIST_VALIDACAO.md`.

**Commit:** `chore: auditoria de conformidade`

---

## Etapa 9 — Testes finais, vídeo e entrega 🖐 MANUAL

Executar **Etapas M8–M10** de `docs/03_PLANO_MANUAL.md` (roteiro de testes, vídeo, limpeza das pastas `build`/`.gradle`, `.zip`).

---

## Se algo der errado (diagnóstico rápido)

| Sintoma | Causa provável | Correção |
|---|---|---|
| Tela abre e fecha sem parar ao entrar | `setSelectedItemId` depois do listener | inverter a ordem (Anexo A) |
| Espaço vazio duplo abaixo do menu | insets com `systemBars.bottom` | trocar o último valor por `0` |
| App fecha ao salvar | `finish()` sozinho após salvar | `NavegacaoMenu.irPara(... ListaLivrosActivity)` |
| Crash `NumberFormatException` | `parseInt` com ano vazio | checar vazio antes + try/catch |
| Crash `no such table: livros` / colunas erradas | banco antigo de uma versão de teste no emulador | desinstalar o app do emulador e rodar de novo |
| Crash `Can't downgrade database from version 2 to 1` | testou a migração e voltou `VERSAO` para 1 | desinstalar o app do emulador |
| Lista não atualiza após editar | carga no `onCreate` em vez de `onResume` | mover para `onResume()` |
| `Cannot resolve symbol R` após edição externa | Android Studio não sincronizou | **File › Sync Project with Gradle Files** / **Build › Rebuild** |
| Clique abre item errado após excluir | lista guardada na Activity | usar só `adapter.getLivro(position)` |
| `AlertDialog` com estilo estranho / crash de tema | import `android.app.AlertDialog` | usar `androidx.appcompat.app.AlertDialog` |

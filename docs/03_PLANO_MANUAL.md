# Plano Manual — o que VOCÊ faz (além dos agentes)

> Etapas `M0`–`M10`, intercaladas com as etapas dos agentes (`docs/02_PLANO_IMPLEMENTACAO.md`).
> Ordem real de execução:
>
> **M0 → M1 → M2 → [Agente E1] → M3 → [Agente E2] → M4 → [Agente E3] → M5 → [Agente E4] → M5 → [Agente E5] → M5 →
> [Agente E6] → M5 → [Agente E7] → M5 → [Agente E8] → M6 → M7 → M8 → M9 → M10**
>
> (`M5` = ciclo de revisão/compilação/teste/commit que se repete depois de cada etapa do agente.)

---

## M0 — Pré-requisitos (uma vez só)

- [ ] Android Studio atualizado, com um **emulador** criado (Device Manager › Create Device › ex.: Pixel, API 34/35).
- [ ] Antigravity instalado e logado.
- [ ] Git instalado (`git --version` no terminal).
- [ ] Descobrir o caminho do JDK do Android Studio (para o agente conseguir rodar o Gradle pelo terminal):
  - Windows: `C:\Program Files\Android\Android Studio\jbr`
  - macOS: `/Applications/Android Studio.app/Contents/jbr/Contents/Home`
  - Linux: `<pasta-de-instalação>/android-studio/jbr`
  - Confirme em: Android Studio › **Settings › Build, Execution, Deployment › Build Tools › Gradle › Gradle JDK**.
- [ ] Definir a variável de ambiente `JAVA_HOME` com esse caminho (Windows: "Editar variáveis de ambiente do sistema";
  macOS/Linux: `export JAVA_HOME=...` no `~/.zshrc`/`~/.bashrc`). Reinicie o Antigravity depois.

---

## M1 — Criar e preparar o projeto no Android Studio (Passo 1 do PDF)

1. [ ] **New Project › Empty Views Activity** (atenção: *Views*, não "Empty Activity" que é Compose).
2. [ ] Name: `Biblioteca Pessoal` · Package: `com.example.bibliotecapessoal` · Language: **Java** · Minimum SDK: padrão ·
   Build configuration language: o padrão (Kotlin DSL) · **Finish** e aguarde o Gradle sync.
3. [ ] Rodar uma vez no emulador (▶) para garantir que o ambiente funciona.
4. [ ] Renomear a Activity: em `java/com.example.bibliotecapessoal`, botão direito em `MainActivity` ›
   **Refactor › Rename** → `ListaLivrosActivity` (aceite renomear ocorrências).
5. [ ] Renomear o layout: `res/layout/activity_main.xml` › **Refactor › Rename** → `activity_lista_livros.xml`.
   (O rename da classe **não** renomeia o layout — confira que `setContentView(R.layout.activity_lista_livros)` ficou certo.)
6. [ ] Abrir `AndroidManifest.xml` e conferir que `ListaLivrosActivity` tem o `intent-filter` MAIN/LAUNCHER.
7. [ ] Criar as outras duas telas pelo wizard (gera Java + layout + entrada no Manifest de uma vez):
   botão direito no pacote › **New › Activity › Empty Views Activity**:
   - `CadastroLivroActivity` — layout `activity_cadastro_livro` — **Launcher Activity desmarcado**.
   - `ResumoActivity` — layout `activity_resumo` — **Launcher Activity desmarcado**.
8. [ ] Abrir `app/build.gradle.kts` e conferir `implementation(libs.material)` (ou `com.google.android.material:material`).
   Se alterou algo: **Sync Now**.
9. [ ] Criar os pacotes: botão direito em `com.example.bibliotecapessoal` › **New › Package** → `model`, `persistencia`, `adapter`.
   (Pacotes vazios podem "sumir" da árvore até terem um arquivo — normal; o agente cria os arquivos.)
10. [ ] Copiar para a **raiz do projeto** (a pasta que contém `settings.gradle.kts`) as pastas deste kit:
    `docs/` e `.agent/` (a `.agent` é oculta em macOS/Linux — use `ls -a`/"mostrar ocultos").
11. [ ] Git — no terminal, na raiz do projeto:
    ```bash
    git init
    ```
    ```bash
    git add .
    ```
    ```bash
    git commit -m "chore: projeto base"
    ```
    (O `.gitignore` do template já ignora `build/`, `.gradle/` e `local.properties`.)

---

## M2 — Ícones do menu (Passo 3.2 do PDF)

Em `res/drawable` › botão direito › **New › Vector Asset** › Asset Type: **Clip Art** › clique no ícone:

| Nome do arquivo (campo *Name*) | Clip Art sugerido |
|---|---|
| `ic_livros` | `menu_book` (ou `library_books`) |
| `ic_novo` | `add` |
| `ic_resumo` | `bar_chart` (ou `insights`) |

- [ ] Criados os 3 arquivos em `res/drawable/`.
- [ ] Abra cada XML: se aparecer `android:fillColor="#FF000000"` (hex), anote — o agente troca na Etapa 2 (RT07).
- [ ] `git add . && git commit -m "chore: ícones do menu"`

---

## M3 — Configurar o Antigravity (uma vez, antes da Etapa 1 do agente)

1. [ ] **File › Open Folder** → a raiz do projeto Android (mesma pasta que o Android Studio abriu).
2. [ ] Regras do workspace: confira se o Antigravity reconheceu `.agent/rules/biblioteca-pessoal.md`
   (painel de **Customizations › Rules**). Se a sua versão usar outro local, crie uma *Workspace Rule* pela UI
   e cole o conteúdo desse arquivo.
3. [ ] (Opcional) O workflow `.agent/workflows/etapa.md` permite disparar uma etapa com `/etapa` no chat do agente.
4. [ ] Teste o terminal integrado: rode `.\gradlew.bat assembleDebug` (Windows) ou `./gradlew assembleDebug` (macOS/Linux).
   Precisa terminar em `BUILD SUCCESSFUL`. Se falhar por JDK, revise o `JAVA_HOME` (M0).
5. [ ] Política de revisão: deixe o agente **pedir aprovação** antes de rodar comandos de terminal e revise os diffs
   antes de aceitar (é seu trabalho avaliado — você precisa entender cada arquivo).
6. [ ] Abra o Android Studio e o Antigravity **ao mesmo tempo** na mesma pasta: o agente edita, o Android Studio compila/roda.

---

## M4 — Antes da Etapa 3: nada extra

Só confirme que as Etapas 1 e 2 estão commitadas.

---

## M5 — Ciclo de revisão após CADA etapa do agente (E1 … E8)

Repita esta lista ao final de cada etapa:

1. [ ] Leia o resumo do agente (arquivos alterados + requisitos atendidos).
2. [ ] Revise o diff (no Antigravity ou `git diff`). Pergunte ao agente sobre qualquer trecho que você não entendeu.
3. [ ] No Android Studio: se ele não recarregou, **File › Reload All from Disk**; depois **Build › Make Project**.
   Se mudou algo de Gradle: **Sync Project with Gradle Files**.
4. [ ] Rode no emulador e execute o **Checkpoint** da etapa (descrito no `02_PLANO_IMPLEMENTACAO.md`).
5. [ ] Deu erro? Copie o erro do *Build* ou do **Logcat** (filtre por `package:mine`, nível *Error*) e cole para o agente.
6. [ ] Tudo ok → commit com a mensagem sugerida na etapa:
   ```bash
   git add .
   ```
   ```bash
   git commit -m "feat: <mensagem da etapa>"
   ```
7. [ ] Abra **uma nova conversa** do agente para a próxima etapa (cole o *Prompt de contexto* + o prompt da etapa).

Checkpoints manuais por etapa (resumo):

| Após | O que conferir no emulador |
|---|---|
| E1 | só compila (nada visível muda) |
| E2 | só compila |
| E3 | 3 abas navegam; item certo marcado; sem loop; sem faixa dupla embaixo do menu; Voltar fecha o app |
| E4 | salvar em branco = 3 erros; ano 1449 e (ano atual+1) = erro; livro válido = Toast + volta para Livros |
| E5 | 3 livros aparecem em ordem alfabética com tags Lido/Não lido de cores diferentes |
| E6 | toque = abre preenchido com "Atualizar"; atualizar não duplica; toque longo = diálogo; Sim exclui na hora; Não cancela |
| E7 | Resumo = total / lidos / não lidos corretos |
| E8 | roteiro completo do `04_CHECKLIST_VALIDACAO.md` |

**Dica — Database Inspector:** com o app rodando, **View › Tool Windows › App Inspection › Database Inspector** →
`biblioteca_pessoal.db` › `livros`. Mostra `lido` gravado como `0`/`1` (ótimo para o vídeo e para conferir o CRUD).

---

## M6 — (Opcional) Testar a migração v1 → v2 e DESFAZER

Só se quiser ter certeza de que o `onUpgrade` funciona. **A entrega deve ficar com `VERSAO = 1`.**

1. [ ] Com livros cadastrados na versão 1, mude `VERSAO` para `2` no `DbHelper` e rode.
2. [ ] No Database Inspector, confira que a tabela `livros` ganhou a coluna `nota` e **os livros continuam lá**.
3. [ ] Volte `VERSAO` para `1`.
4. [ ] **Desinstale o app do emulador** (segure o ícone › Desinstalar) — senão dá crash *"Can't downgrade database"*.
5. [ ] `git diff` deve mostrar que o `DbHelper` está igual ao último commit.

---

## M7 — Revisão final de código (sua leitura, ~20 min)

Abra cada arquivo e confirme com seus olhos (o professor vai olhar o mesmo):

- [ ] `DbHelper`: constantes `public static final`, `VERSAO = 1`, `lido INTEGER DEFAULT 0`, `if (oldVersion < 2)` + `ALTER TABLE`.
- [ ] `ILivroDAO`: 5 métodos, nada mais.
- [ ] `LivroDAO`: `!= -1`, `> 0`, `? 1 : 0`, `== 1`, `SELECT COUNT(*)`, cursores fechados.
- [ ] `AdapterLivro`: `atualizarLista` + `getLivro`; cores e textos via recursos.
- [ ] `ListaLivrosActivity`: `onResume`, `adapter.getLivro(...)` nos cliques, `AlertDialog`, sem campo `List`.
- [ ] `CadastroLivroActivity`: `Calendar`, vazio antes do `parseInt`, `setError` com `getString`, "Atualizar" com `getString`,
  `irPara` após salvar.
- [ ] `ResumoActivity`: 3 chamadas `contar(null/true/false)`.
- [ ] `NavegacaoMenu`: `setSelectedItemId` antes do listener; 3 itens tratados.
- [ ] `RecyclerItemClickListener`: idêntico ao Anexo B.
- [ ] Layouts: nenhum `android:text="texto"` literal; nenhum `#` de cor; RecyclerView `0dp` entre cabeçalho e menu.
- [ ] Você consegue **explicar** cada arquivo (provável pergunta em apresentação/defesa).

---

## M8 — Roteiro de testes final

Execute **todo** o `docs/04_CHECKLIST_VALIDACAO.md` (seções A e B) num app **recém-instalado**
(desinstale antes para começar com o banco vazio).

---

## M9 — Gravar o vídeo de demonstração

1. [ ] Gravação: no emulador, botão **⋮ (Extended controls) › Screen record**, ou gravador de tela do sistema
   (Windows: `Win + Alt + R` / Xbox Game Bar; macOS: `Cmd + Shift + 5`). Fale enquanto demonstra, se possível.
2. [ ] Siga o **roteiro do vídeo** da seção C do `04_CHECKLIST_VALIDACAO.md` (≈ 3–5 min).
3. [ ] Assista ao vídeo antes de enviar (áudio, legibilidade, nenhum passo faltando).

---

## M10 — Empacotar e entregar (Passo 5.2 do PDF)

1. [ ] Último commit: `git add . && git commit -m "chore: versão de entrega"`.
2. [ ] Feche o Android Studio e o Antigravity (ou **Build › Clean Project** antes de fechar).
3. [ ] Apague as pastas: `build/` (raiz), `app/build/` e `.gradle/` (raiz). Também pode apagar `.idea/` se quiser um zip menor.
4. [ ] Decida sobre `docs/`, `.agent/` e `.git/`: não são exigidos. Na dúvida, **remova do zip** (ou pergunte ao professor
   se pode incluir a documentação de planejamento).
5. [ ] Compacte a pasta do projeto em `.zip` (ex.: `BibliotecaPessoal_SeuNome.zip`).
6. [ ] Teste o zip: descompacte em outra pasta, abra no Android Studio, deixe sincronizar e rode. Funcionou → envie.
7. [ ] Envie o `.zip` **junto com o vídeo** pelo canal pedido pelo professor.

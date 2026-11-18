# Checklist de Validação, Autoavaliação e Roteiro do Vídeo

> Use num app **recém-instalado** (banco vazio). Marque cada item. IDs `CAxx` = critérios de aceitação do `01_SDD.md`.

---

## A. Roteiro de testes funcionais (emulador)

### A1. Navegação (RT09 — CA16, CA17, CA20)
- [ ] App abre na tela **Meus Livros** (LAUNCHER) com o item **Livros** marcado.
- [ ] Toque em **Novo** → abre Cadastro, item **Novo** marcado.
- [ ] Toque em **Resumo** → abre Resumo, item **Resumo** marcado.
- [ ] Toque no item já selecionado → nada acontece (sem recarregar/piscar).
- [ ] Nenhuma tela entra em loop de abertura.
- [ ] Nenhuma faixa vazia dupla abaixo do menu inferior nas 3 telas.
- [ ] Botão **Voltar** fecha o app em qualquer tela (comportamento esperado).

### A2. Validação do formulário (RF01 — CA06, CA07, CA08)
- [ ] Salvar com tudo vazio → erro (`setError`) em **Título**, **Autor** e **Ano**; nada é salvo; continua no cadastro.
- [ ] Título só com espaços → erro no título.
- [ ] Ano `1449` → erro de intervalo ("entre 1450 e <ano atual>").
- [ ] Ano = ano atual **+ 1** → erro de intervalo.
- [ ] Ano `1450` → aceito. Ano = ano atual → aceito.
- [ ] Campo ano não aceita letras (teclado numérico).

### A3. Inclusão + listagem (RF02, RF06 — CA09, CA10, CA11, CA12)
Cadastre nesta ordem:

| Título | Autor | Ano | Lido |
|---|---|---|---|
| O Hobbit | J.R.R. Tolkien | 1937 | ✔ |
| Clean Code | Robert C. Martin | 2008 | ✔ |
| Dom Casmurro | Machado de Assis | 1899 | ✘ |

- [ ] Cada salvamento mostra **Toast de sucesso** e volta para **Meus Livros**.
- [ ] Ordem exibida: **Clean Code → Dom Casmurro → O Hobbit** (alfabética).
- [ ] Cada item mostra título, "Autor • Ano" e tag **Lido** (verde) / **Não lido** (vermelha).
- [ ] A lista rola corretamente e o último item não fica escondido atrás do menu.

### A4. Edição — clique simples (RF03, RT08 — CA13)
- [ ] Tocar em **Dom Casmurro** → Cadastro abre com os 4 campos preenchidos e botão **Atualizar**.
- [ ] Marcar "Já li este livro" → **Atualizar** → Toast de sucesso → volta para a lista.
- [ ] Dom Casmurro agora aparece como **Lido**; a lista continua com **3** itens (não duplicou).
- [ ] Editar e apagar o título → **Atualizar** → erro de validação (não salva).

### A5. Exclusão — clique longo (RF04 — CA14)
- [ ] Toque longo em **O Hobbit** → AlertDialog de confirmação com o título do livro.
- [ ] **Não** → nada muda.
- [ ] Toque longo de novo → **Sim** → Toast de sucesso; item some **na hora** (sem a tela piscar/reabrir).

### A6. Resumo (RF05 — CA15)
- [ ] Resumo mostra **Total 2 · Lidos 2 · Não lidos 0** (após os passos acima).
- [ ] Cadastre um livro não lido → Resumo: **3 · 2 · 1**.

### A7. Persistência
- [ ] Feche o app (remova dos recentes) e abra de novo → os livros continuam lá.
- [ ] (Opcional) Database Inspector: coluna `lido` com valores `0`/`1`.

---

## B. Auditoria de código (o que o professor vai abrir)

### B1. Varreduras automáticas (peça ao agente ou rode você na raiz do projeto)

macOS/Linux (bash):
```bash
grep -rnE 'android:(text|hint|title|contentDescription)="[^@]' app/src/main/res/layout app/src/main/res/menu
```
```bash
grep -rn '#[0-9A-Fa-f]\{6,8\}' app/src/main/res --include=*.xml | grep -v 'values/colors.xml'
```
```bash
grep -rnE 'startActivity|finish\(\)' app/src/main/java | grep -v NavegacaoMenu.java
```
```bash
grep -rnP '\bstatic\s+(?!final\b)[\w<>\[\], ]+\s+\w+\s*(=|;)' app/src/main/java
```
```bash
grep -rn 'DROP TABLE' app/src/main/java
```

Windows (PowerShell):
```powershell
Select-String -Path app\src\main\res\layout\*.xml, app\src\main\res\menu\*.xml -Pattern 'android:(text|hint|title|contentDescription)="[^@]'
```
```powershell
Get-ChildItem app\src\main\java -Recurse -Filter *.java | Where-Object Name -ne 'NavegacaoMenu.java' | Select-String -Pattern 'startActivity|finish\(\)'
```

**Resultado esperado: nenhuma linha** em todas as buscas.

Lint: `./gradlew lintDebug` (ou `.\gradlew.bat lintDebug`) → abrir `app/build/reports/lint-results-debug.html` →
sem `HardcodedText` e sem `SetTextI18n`.

### B2. Checklist de código

| ID | Item | ✔ |
|---|---|---|
| CA01 | `DbHelper`: constantes `public static final` (nome, versão=1, tabela, colunas); `lido INTEGER DEFAULT 0` | ☐ |
| CA02 | `onUpgrade`: `if (oldVersion < 2)` + `ALTER TABLE livros ADD COLUMN nota INTEGER`; sem DROP | ☐ |
| CA03 | `ILivroDAO`: exatamente `salvar`, `atualizar`, `excluir`, `listarTodos`, `contar(Boolean)` | ☐ |
| CA04 | `LivroDAO`: `insert != -1`, `update > 0`, `delete > 0` | ☐ |
| CA05 | `contar`: `SELECT COUNT(*)`, `WHERE lido = ?` para true/false, sem laço | ☐ |
| CA08 | `Calendar.getInstance().get(Calendar.YEAR)`; nenhum ano fixo | ☐ |
| CA11 | `onResume()` carrega; cliques usam `adapter.getLivro(position)`; sem `List` na Activity | ☐ |
| CA12 | RecyclerView `0dp` entre cabeçalho e `bottomNav` + `LinearLayoutManager` | ☐ |
| CA13 | `putExtra` / `getSerializableExtra`; `Livro implements Serializable`; sem static compartilhado | ☐ |
| CA17 | Só `NavegacaoMenu` chama `startActivity`/`finish` | ☐ |
| CA18 | Strings e cores 100% em recursos (inclusive Toast, setError, AlertDialog, "Atualizar", menu) | ☐ |
| CA19 | Pacotes/arquivos iguais à seção 3.2 do SDD; sem `MainActivity`/`activity_main` | ☐ |

---

## C. Roteiro do vídeo de demonstração (≈ 3–5 min)

| # | Tempo | Mostrar | Falar (sugestão) |
|---|---|---|---|
| 1 | 0:00 | Android Studio: árvore de pacotes (`model`, `persistencia`, `adapter`, Activities, `NavegacaoMenu`) | "Arquitetura em camadas conforme o enunciado." |
| 2 | 0:20 | `DbHelper` (constantes, CREATE, `onUpgrade` v2) e `ILivroDAO`/`contar` | "Lido salvo como 0/1; contadores com COUNT(*)." |
| 3 | 0:50 | App: navegação entre as 3 abas | "Menu inferior centralizado em NavegacaoMenu." |
| 4 | 1:10 | Salvar em branco → erros; ano 1449 → erro | "Validação por campo com setError; ano atual via Calendar." |
| 5 | 1:40 | Cadastrar 3 livros → lista ordenada com tags | "Ordenado por título; indicador Lido/Não lido." |
| 6 | 2:20 | Clique simples → editar → Atualizar | "Livro trafega via Intent (Serializable)." |
| 7 | 2:50 | Clique longo → Não → Sim | "Exclusão com confirmação, lista atualiza sem recriar a tela." |
| 8 | 3:20 | Resumo com os contadores | "Total, lidos e não lidos por SQL." |
| 9 | 3:40 | (Opcional) Database Inspector mostrando `lido` 0/1 | "Persistência tipada no SQLite." |
| 10 | 4:00 | Fechar e reabrir o app | "Dados persistem." |

---

## D. Autoavaliação pela rubrica do PDF (marque Integral / Parcial / Zero)

| Critério | Req. | Pts | I (100%) | P (50%) | Z |
|---|---|---|---|---|---|
| Modelo (Livro) e DbHelper com tipagem int/boolean, constantes e migração | RT03 | 1,0 | ☐ | ☐ | ☐ |
| Camada DAO com interface, validação de retornos e COUNT via SQL | RT01, RT02, RF05 | 2,0 | ☐ | ☐ | ☐ |
| Formulário de cadastro com validação por campo (setError) | RF01 | 1,0 | ☐ | ☐ | ☐ |
| Listagem com Adapter, LayoutManager e indicador visual de leitura | RF02, RT04, RT06 | 1,5 | ☐ | ☐ | ☐ |
| Edição (clique simples) e exclusão com confirmação (clique longo) | RF03, RF04, RT08 | 2,0 | ☐ | ☐ | ☐ |
| Navegação inferior nas 3 telas centralizada em NavegacaoMenu, inclusive retorno ao salvar | RT09 | 1,5 | ☐ | ☐ | ☐ |
| Organização de pacotes, strings.xml/colors.xml e código limpo | RT07 | 1,0 | ☐ | ☐ | ☐ |
| **Total** | | **10,0** | | | |

Qualquer item que não for "Integral" → volte à etapa correspondente do plano antes de entregar.

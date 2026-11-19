# Relatório de Auditoria de Conformidade e Polimento (Etapa 8)

Data da Auditoria: 18/11/2026  
Status Geral: **APROVADO (100% dos Critérios Atendidos)**

---

## 1. Tabela de Verificação dos Critérios de Aceitação (CA01–CA20)

| ID | Critério de Aceitação | Status | Evidência (Arquivo:Linhas) | Correção / Observação Aplicada |
|:---|:---|:---:|:---|:---|
| **CA01** | `DbHelper` possui constantes `public static final` (`NOME_BANCO`, `VERSAO = 1`, `TABELA_LIVROS`, `COLUNA_*`); `onCreate` usa `lido INTEGER DEFAULT 0`. | ✅ | `DbHelper.java:16-24, 37` | Constantes tipadas declaradas e schema criado sem `DROP TABLE`. |
| **CA02** | `onUpgrade` contém `if (oldVersion < 2)` com `ALTER TABLE livros ADD COLUMN nota INTEGER`; sem `DROP TABLE`. | ✅ | `DbHelper.java:45-51` | Migração incremental limpa e segura; banco permanece na versão 1 na entrega. |
| **CA03** | `ILivroDAO` declara exatamente os 5 métodos especificados (`salvar`, `atualizar`, `excluir`, `listarTodos`, `contar`). | ✅ | `ILivroDAO.java:13-25` | Assinaturas idênticas ao SDD sem métodos extras. |
| **CA04** | `salvar` retorna `insert != -1`; `atualizar` e `excluir` retornam `linhas > 0`. | ✅ | `LivroDAO.java:47, 70, 89` | Validações booleanas estritas de retorno SQLite. |
| **CA05** | `contar` usa `SELECT COUNT(*)` via `rawQuery`; sem laço de repetição (`for`/`while`); trata `null`, `true` e `false`. | ✅ | `LivroDAO.java:125-147` | Consulta SQL pura de contagem com fechamento de `Cursor`. |
| **CA06** | Salvar com formulário vazio dispara `setError` nos campos obrigatórios, não grava no banco e permanece na tela. | ✅ | `CadastroLivroActivity.java:80-136` | Validação completa de todos os campos com foco no primeiro inválido. |
| **CA07** | Ano `< 1450` ou `> anoAtual` exibe erro de intervalo; limites `1450` e `anoAtual` são aceitos. | ✅ | `CadastroLivroActivity.java:114-121` | Validação inclusiva conforme regras de negócio do SDD. |
| **CA08** | Obtenção do ano atual via `Calendar.getInstance().get(Calendar.YEAR)` sem número fixo. | ✅ | `CadastroLivroActivity.java:114` | Ano obtido dinamicamente do sistema. |
| **CA09** | Lista exibe os livros em ordem alfabética de título. | ✅ | `LivroDAO.java:95` | Consulta SQL com `ORDER BY titulo COLLATE NOCASE ASC`. |
| **CA10** | Item da lista exibe título, "autor • ano" formatado e tag colorida para Lido (verde) / Não lido (vermelho). | ✅ | `AdapterLivro.java:51-60` | Uso exclusivo de recursos `@color/cor_status_*` e `@string/status_*`. |
| **CA11** | `ListaLivrosActivity` recarrega em `onResume()` via `adapter.atualizarLista()`; Activity não armazena cópia da lista. | ✅ | `ListaLivrosActivity.java:82-84, 89-95` | Acesso aos dados do clique exclusivamente via `adapter.getLivro(position)`. |
| **CA12** | `RecyclerView` configurado com `LinearLayoutManager`, `layout_height="0dp"`, entre o cabeçalho e o `bottomNav`. | ✅ | `activity_lista_livros.xml:24-30` | Layout constraints verticais corretas com divisor vertical. |
| **CA13** | Clique simples abre cadastro preenchido com botão "Atualizar"; atualiza registro sem duplicar; trafega via `Intent` (Serializable). | ✅ | `ListaLivrosActivity.java:68-73`, `CadastroLivroActivity.java:62-71, 161-171` | Edição com reaproveitamento do mesmo `id` no SQLite. |
| **CA14** | Clique longo abre `AlertDialog`; "Sim" exclui e atualiza a lista na hora; "Não" cancela; Toast em ambos os desfechos. | ✅ | `ListaLivrosActivity.java:77-80, 98-118` | Diálogo via `androidx.appcompat.app.AlertDialog` sem recriar a Activity. |
| **CA15** | Tela Resumo exibe contadores de total, lidos e não lidos coerentes com o banco de dados. | ✅ | `ResumoActivity.java:48-52` | Integração direta com `dao.contar(...)`. |
| **CA16** | As 3 telas utilizam o mesmo `BottomNavigationView`, com o item da tela atual marcado e sem loops de navegação. | ✅ | `NavegacaoMenu.java:23-45` | `setSelectedItemId` chamado antes do listener. |
| **CA17** | Toda transição de tela passa por `NavegacaoMenu.irPara(...)`; nenhuma Activity invoca `startActivity` ou `finish()` soltos. | ✅ | `NavegacaoMenu.java:50-54` | Centralização total da navegação com flag sem animação. |
| **CA18** | Zero textos visíveis literais em layouts, menus ou código Java; zero cores hex fora de `colors.xml`. | ✅ | `strings.xml`, `colors.xml` | Todas as mensagens, toasts, diálogos e hints utilizam `@string/...`. |
| **CA19** | Estrutura exata de arquivos e pacotes conforme seção 3.2 do SDD; sem resíduos de `MainActivity` ou `activity_main`. | ✅ | Projeto | 10 classes Java, 4 layouts, 1 menu, 3 drawables de ícones. |
| **CA20** | Insets com `bottom = 0` nas 3 Activities (sem faixa dupla abaixo do menu inferior). | ✅ | `ListaLivrosActivity.java:44`, `CadastroLivroActivity.java:49`, `ResumoActivity.java:24` | Padding inferior zerado no listener de insets. |

---

## 2. Varreduras Automáticas de Código

1. **Textos Hardcoded em XML:**
   - Comando: `Select-String -Path app\src\main\res\layout\*.xml, app\src\main\res\menu\*.xml -Pattern 'android:(text|hint|title|contentDescription)="[^@]'`
   - Resultado: **0 ocorrências**.
2. **Cores Hex fora de `colors.xml`:**
   - Varredura em todos os drawables próprios e layouts.
   - Resultado: **0 ocorrências** (vetores `ic_livros`, `ic_novo` e `ic_resumo` utilizam `@color/black`).
3. **`startActivity` ou `finish()` fora de `NavegacaoMenu.java`:**
   - Resultado: **0 ocorrências**.
4. **Variáveis `static` mutáveis:**
   - Resultado: **0 ocorrências**.
5. **`DROP TABLE` no código:**
   - Resultado: **0 ocorrências**.
6. **Laços em `contar()`:**
   - Resultado: **0 ocorrências** (utiliza `SELECT COUNT(*)` puro).
7. **Resíduos de `MainActivity` / `activity_main`:**
   - Resultado: **0 ocorrências**.

---

## 3. Resultado do Android Lint (`lintDebug`)

- **Erros:** **0**
- **HardcodedText / SetTextI18n:** **0**
- **Avisos:** apenas avisos esperados de dicionário (palavra em português "Autor" sinalizada pelo corretor em inglês) e avisos informativos sobre `notifyDataSetChanged`.

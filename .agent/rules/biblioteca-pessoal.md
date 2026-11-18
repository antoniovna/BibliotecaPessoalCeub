---
trigger: always_on
---

# Regras do projeto Biblioteca Pessoal (Android · Java · SQLite)

Fonte da verdade: `docs/01_SDD.md` (derivado do PDF do professor). Plano: `docs/02_PLANO_IMPLEMENTACAO.md`.
Leia os dois antes de editar qualquer arquivo.

## Escopo e stack
- Java apenas, pacote `com.example.bibliotecapessoal`. Sem Kotlin, Room, Fragments, Navigation Component,
  ViewModel, ViewBinding/DataBinding ou bibliotecas novas. Use `findViewById`.
- Estrutura de arquivos exatamente como na seção 3.2 do SDD. Não crie classes extras.
- Implemente somente a etapa pedida. Não refatore código já aprovado sem pedido explícito.
- Não altere `build.gradle(.kts)`, `libs.versions.toml` ou `AndroidManifest.xml` sem a etapa pedir.

## Proibições (cada uma tira nota)
- Texto visível literal em layout, menu ou Java → sempre `@string/...` e `getString(R.string...)`
  (inclusive Toast, `setError`, AlertDialog e o "Atualizar" do botão).
- Cor hex fora de `res/values/colors.xml`.
- Variável `static` mutável para compartilhar dados entre telas (constantes `static final` são permitidas).
- `startActivity(...)` ou `finish()` em qualquer classe que não seja `NavegacaoMenu`.
- `finish()` sozinho após salvar — usar `NavegacaoMenu.irPara(this, new Intent(this, ListaLivrosActivity.class))`.
- `DROP TABLE` no `onUpgrade`; `VERSAO` diferente de 1 na entrega.
- Contar livros com laço ou carregando a lista: `contar` usa `SELECT COUNT(*)`.
- Guardar `List<Livro>` na Activity: cliques usam `adapter.getLivro(position)`.
- Ano atual escrito fixo: usar `Calendar.getInstance().get(Calendar.YEAR)`.
- `Integer.parseInt` do ano sem checar vazio antes.

## Obrigatórios recorrentes
- `NavegacaoMenu.configurar(this, R.id.nav_xxx)` logo após `setContentView` nas 3 Activities;
  `setSelectedItemId` antes do listener.
- Listener de insets com `bottom = 0` nas 3 Activities; raiz dos layouts com id `main`.
- `ListaLivrosActivity` carrega dados em `onResume()`.
- `LivroDAO`: `insert != -1`, `update/delete > 0`, `isLido() ? 1 : 0`, `getInt(...) == 1`, fechar Cursors.
- `RecyclerItemClickListener` idêntico ao Anexo B.
- Comentários curtos em português em cada método.

## Ao terminar cada tarefa
1. Compilar: `.\gradlew.bat assembleDebug` (Windows) ou `./gradlew assembleDebug` (macOS/Linux) e corrigir erros.
2. Responder com: arquivos alterados; requisitos atendidos (RF/RT/CA do SDD); o que testar no emulador.

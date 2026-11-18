---
description: Executa uma etapa do plano de implementação da Biblioteca Pessoal (ex.: /etapa 3)
---

1. Leia `docs/01_SDD.md`, `docs/02_PLANO_IMPLEMENTACAO.md` e `.agent/rules/biblioteca-pessoal.md` por completo.
2. Localize no plano a etapa indicada pelo usuário (número após o comando). Se nenhum número foi informado, pergunte qual etapa.
3. Use o bloco "Prompt" dessa etapa como a tarefa. Respeite a lista de arquivos da etapa: não altere outros.
4. Apresente um plano curto (arquivos e mudanças) e aguarde aprovação do usuário antes de editar.
5. Implemente seguindo o SDD literalmente (nomes, ids, strings, cores, assinaturas).
6. Compile com `./gradlew assembleDebug` (ou `.\gradlew.bat assembleDebug` no Windows) e corrija erros.
7. Responda com: arquivos alterados; como cada requisito/CA da etapa foi atendido (arquivo:linha);
   checkpoint manual que o usuário deve executar no emulador; mensagem de commit sugerida.

# Testes

Executar `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` e `:app:assembleDebugAndroidTest` após mudanças relevantes. Neste computador, definir `TEMP` e `TMP` como `D:\repositorios\simapp\.tmp` e usar o cache Gradle já instalado. Registrar resultados reais em `docs/STATUS.md`.

Testes unitários cobrem validação dos itens, motivo e fotos, hodômetro e janela de sessão offline. Testes instrumentados Room cobrem substituição completa de catálogos apenas do mesmo par empresa/usuário, preservação do snapshot de checklist, rejeição de catálogo inválido sem perda do cache, retentativa de envio com o mesmo UUID após resposta perdida, bloqueio de exclusão de rascunho após tentativa remota e edição durante o envio sem preencher `dataHoraSincronizado` indevidamente.

O AVD `Small_Phone` pode executar os testes Android. A tarefa `:app:connectedDebugAndroidTest` retornou sucesso com relatório vazio em 07/10/2026; por isso a validação foi feita instalando ambos os APKs e executando `adb shell am instrument -w app.simetris.simapp.test/androidx.test.runner.AndroidJUnitRunner`. Confirmar a contagem de testes na saída, não apenas o código de saída da tarefa Gradle.

Roteiro de integração pendente de backend: `GET /cadastros` deve responder por empresa/usuário; POST idempotente da mesma checklist deve preservar UUID; PUT deve atualizar rascunho; POST de anexos deve confirmar exatamente todos os UUIDs do array Base64; falhas e respostas perdidas devem manter `dataHoraSincronizado = null` e todos os arquivos locais. A API HTTP real ainda não foi exercitada contra servidor nesta entrega.

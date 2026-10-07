# Sincronização

Catálogos: somente com rede, `GET /cadastros` retorna o conjunto completo de modelos, itens, veículos e motoristas da empresa e usuário atuais. O repositório valida IDs, ordem e limites antes de trocar todas as linhas desse escopo em uma transação Room. Erro de rede, JSON ou validação mantém a cópia anterior. As telas consultam somente Room por Flow.

Checklist: salvar um rascunho ou alterar seus dados, respostas e mídia deixa `dataHoraSincronizado = null`, aumenta `revisaoLocal` e cria uma pendência única. Finalizar grava `FINALIZADO` e nova pendência; a checklist deixa de ser editável. WorkManager exige conexão e usa retentativa. O primeiro envio usa `POST /checklist`; envios seguintes usam `PUT /checklist/{id}`. Ambos levam o mesmo UUID criado no app. O contrato backend exige idempotência de POST para resposta perdida.

O sincronizador lê uma versão coerente no Room, envia checklist e depois todas as fotos e assinatura em um único array Base64 para `POST /checklist/{uuid}/anexos`. Exige resposta com os UUIDs de todos os anexos. Na transação final, compara `revisaoLocal`; só se a versão ainda for a mesma grava `dataHoraSincronizado` e remove a pendência. Se houve edição durante o envio, a pendência continua para reenviar a nova versão. Qualquer falha deixa dados e arquivos intactos. A data preenchida, e não o status auxiliar da fila, determina se a versão está sincronizada.

Rascunho que começou a ser enviado não pode ser excluído localmente, pois o contrato não inclui DELETE remoto. O servidor fake permanece isolado em `DemoChecklistApi` e tabelas próprias; a execução normal usa `HttpChecklistApi`. Não há sucesso simulado na API real.

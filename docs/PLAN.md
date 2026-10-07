# Plano incremental

1. Documentação e contrato inicial; compilar o app base.
2. Domínio, validação e Room com catálogo, snapshots, checklists, anexos e fila; testes de regra e DAO.
3. Repositórios, DataStore, modo demonstração e telas de catálogo, formulário, revisão e lista paginada; app executável e fluxo offline.
4. Photo Picker, CameraX, arquivos privados e assinatura; teste de persistência/retomada.
5. WorkManager, cliente real substituível, servidor fake, entrada/saída idempotentes e estados; testes de falha/repetição.
6. Previews, navegação/UI, build, testes, lint e roteiro manual em dispositivo quando disponível.

Cada marco exige um fluxo executável e atualização de `docs/STATUS.md`. Aceite final exige criar/retomar sem rede após carga, filtros/paginação locais, mídia durável, finalização atômica, sincronização honesta e falhas recuperáveis.

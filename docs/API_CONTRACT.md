# Contrato da API de checklist

Fonte: `novoplanejamento.ts`, fornecido pelo usuário em 07/10/2026. As interfaces e os três endpoints de checklist substituem a proposta anterior em `BACKEND_API_SPEC.md`.

`GET /cadastros` retorna `{ motoristas: Motorista[], veiculos: Veiculo[], modelos: ChecklistModelo[] }`. A chamada exige conexão. O app valida a resposta completa antes de substituir, em uma transação Room, os registros do mesmo par empresa/usuário. Offline, a interface lê apenas o cache local.

`Motorista` tem `id: number` e `nome: string`. `Veiculo` tem `id: number` e `placa: string`. `ChecklistModelo` tem `id`, `nome`, `descricao`, `itens`, `motivoObrigatorio`, `fotoObrigatorio`, `fotoQuantidadeMinima` e `fotoQuantidadeMaxima`. Cada item de modelo tem `id`, `ordem`, `nome` e `descricao`.

`POST /checklist` insere; `PUT /checklist/{id}` atualiza; `POST /checklist/{uuid}/anexos` recebe **um único array JSON** com todas as fotos e assinatura em Base64. O corpo da checklist usa exatamente as propriedades da interface `Checklist` do anexo; `anexos` é um array vazio nas operações de checklist, pois os bytes seguem no endpoint de anexos. Itens copiados do modelo usam UUID próprio e não levam ID do item de modelo. O `id` da checklist é o mesmo UUID no app, API e web.

`situacao` aceita `RASCUNHO` ou `FINALIZADO`. Rascunhos e finalizadas entram na fila. Ao alterar um rascunho, `dataHoraSincronizado` volta a `null`. O app só o preenche após resposta bem-sucedida da operação de checklist e confirmação de todos os IDs do lote de anexos. A resposta de `/anexos` deve conter `anexosRecebidos: [{id: string}]` com exatamente os UUIDs enviados, inclusive array vazio quando não há anexos. A data preenchida é o instante UTC local da confirmação completa, formatado no payload como `yyyy-MM-ddTHH:mm:ss`.

Para retentativa após resposta perdida, `POST /checklist` precisa ser idempotente pelo `id`, e `PUT /checklist/{id}` e o lote de anexos devem aceitar repetição do mesmo conteúdo. O app conserva checklist e arquivos quando qualquer etapa falha. Depois que uma tentativa de criação remota começa, a exclusão local de rascunho fica indisponível, pois não existe endpoint de exclusão nesse contrato.

Os endpoints de autenticação existentes não fazem parte desta alteração. Não foram criadas credenciais nem URLs adicionais.

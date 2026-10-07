# Especificação para o backend — cadastros e checklists

Esta especificação segue `novoplanejamento.ts`, fornecido pelo usuário em 07/10/2026. O app usa as rotas abaixo, além das rotas de autenticação já existentes. Não há endpoint de consulta de estado nem confirmação separada.

| Método | Rota | Corpo | Resposta necessária |
|---|---|---|---|
| GET | `/cadastros` | Nenhum | `{motoristas, veiculos, modelos}` completos para a empresa e o usuário da sessão |
| POST | `/checklist` | `Checklist` com `id` UUID | 2xx após persistência integral; se houver `id` na resposta, deve ser o mesmo UUID |
| PUT | `/checklist/{id}` | `Checklist` com o mesmo UUID | 2xx após atualização integral; se houver `id` na resposta, deve ser o mesmo UUID |
| POST | `/checklist/{uuid}/anexos` | Array JSON de `ChecklistAnexo` com `arquivo` Base64 | 2xx com `anexosRecebidos: [{id: string}]` contendo exatamente todos os UUIDs recebidos |

O contrato de campos e tipos está em `API_CONTRACT.md` e no arquivo TypeScript fornecido. Todos os instantes usam `yyyy-MM-ddTHH:mm:ss` em UTC. A API deve aceitar `RASCUNHO` e `FINALIZADO`, com itens `PENDENTE`, `CONFORME` ou `NAO_CONFORME`. Checklists finalizadas não recebem novas edições do app.

O backend identifica a empresa e o usuário da sessão e limita `GET /cadastros` a esse escopo. O app substitui a cópia local inteira desse par após resposta válida. Um `POST /checklist` repetido com o mesmo UUID deve devolver sucesso para o mesmo registro sem criar duplicata. `PUT` e o lote de anexos também devem ser idempotentes. O array de anexos representa a versão atual completa da checklist, inclusive `[]` em rascunhos sem mídia. Um 2xx de anexo só pode incluir IDs já armazenados integralmente. Falha parcial deve retornar erro e deixar a versão anterior recuperável.

Sem endpoint de exclusão remota, o app bloqueia a exclusão de rascunho após iniciar sua primeira tentativa de envio. Credenciais e URL base são gerenciadas pela integração já existente e não são incluídas neste documento.

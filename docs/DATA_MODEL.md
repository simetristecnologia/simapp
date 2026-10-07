# Modelo de dados

O Room versão 2 separa catálogos por `empresaId` e `perfilLocalId`, que corresponde ao usuário da sessão. `GET /cadastros` fornece um conjunto completo de modelos com itens, veículos e motoristas; a substituição do escopo inteiro ocorre em uma transação. Checklists existentes guardam cópias próprias e não mudam quando o catálogo é atualizado.

A checklist local usa `idLocal` como chave Room, cujo valor UUID é enviado como `Checklist.id` e permanece o mesmo na API e na web. Não existe ID remoto separado para a checklist ou seus anexos. O modelo escolhido é copiado para os campos da checklist e seus itens são copiados com UUIDs próprios, ordem, nome e descrição; `ChecklistItem` já não guarda referência ao ID do item de modelo. IDs do modelo, veículo e motorista são preservados no nível da checklist conforme a interface do anexo.

`estadoPreenchimento` guarda `RASCUNHO` ou `FINALIZADO`. `dataHoraSincronizado` nulo significa que a versão atual ainda não recebeu confirmação integral do servidor; valor não nulo significa que o POST ou PUT e o lote completo de anexos responderam com sucesso. `revisaoLocal` evita marcar como sincronizada uma versão que mudou durante o envio. `criadoRemotamente` define POST ou PUT; `envioIniciado` impede excluir localmente um rascunho que pode já existir na API após uma resposta perdida. `estadoEnvio` e `PendenciaEnvio` servem à fila e à apresentação de erro, subordinados a `dataHoraSincronizado`.

Fotos e assinatura continuam em arquivos privados com hash e metadados Room. O endpoint de anexos recebe os bytes como Base64 em um único array. Arquivos removidos de uma versão de rascunho são conservados enquanto não existe confirmação integral da versão nova. Os arquivos atuais continuam no aparelho após sincronização, pela política conservadora de retenção.

O banco antigo é recriado na atualização da versão 1 para a 2, conforme autorização explícita do usuário para perder dados locais. A sessão de autenticação fica em armazenamento separado e não é migrada por essa operação.

# Produto

O operador recebe modelos, veículos e motoristas do par empresa/usuário somente com internet. Após a carga, escolhe os registros locais por pesquisa e pode trabalhar offline. A seleção de modelo copia o nome, descrição, regras e itens para uma checklist de UUID próprio, sem referência ao ID de item do modelo. Substituições futuras do catálogo não alteram checklists existentes.

O fluxo atual de tela é início → dados → itens → assinatura → detalhe. Modelo, veículo e motorista são escolhidos em seletores locais. O rascunho exige os três IDs para satisfazer o contrato da API; hodômetro permanece um dado local exigido para avançar à assinatura. Itens começam `PENDENTE`; uma resposta `NAO_CONFORME` exige motivo e foto conforme as regras do formulário. A assinatura finaliza a checklist, que passa a `FINALIZADO` e fica imutável.

Rascunhos e finalizadas são enviados à API com o mesmo `Checklist.id` UUID. O estado visível de sincronização deriva de `dataHoraSincronizado`: nulo indica pendente ou falha, data preenchida indica confirmação da checklist e de todos os anexos. Editar rascunho limpa a data e cria nova pendência. Rascunho cuja primeira tentativa de envio já começou não pode ser excluído, pois não há rota de exclusão remota. Arquivos são mantidos até confirmação integral.

UI, domínio, persistência, mídia e API permanecem separados. A UI lê Room por Flow. O cliente real usa as rotas de `docs/API_CONTRACT.md`; o servidor fake existe apenas em implementação separada de demonstração. Login e refresh existentes não foram alterados nesta entrega.

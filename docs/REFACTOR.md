# Refatoração por responsabilidade

## Inventário antes da edição

| Responsabilidade atual | Origem | Destino | Dependências e risco |
|---|---|---|---|
| Entrada e pilha Navigation 3 | `MainActivity.kt` | `MainActivity.kt`, `ui/navigation/AppNavigation.kt` | Preservar nomes e parâmetros das rotas; nenhuma mudança no banco. |
| Início e preferências | `ui/screens/Telas.kt` | `ui/home/`, `ui/preferences/` | DataStore permanece com as mesmas chaves; ações reais de catálogos e envio. |
| Modelos de checklist e escolha no formulário | `ui/screens/Telas.kt` | `ui/checklist/model/`, `ui/checklist/form/` | Fonte local `Flow`; veículos e motoristas continuam sincronizados; não alterar dados de demonstração. |
| Dados, itens e revisão | `ui/screens/Telas.kt`, `ui/screens/ItensEMidia.kt` | `ui/checklist/form/`, `ui/checklist/review/` | Manter callbacks de gravação, foco nas faltas e regras de finalização. |
| CameraX, Photo Picker, miniatura | `ui/screens/ItensEMidia.kt` | `media/camera/`, `media/picker/`, `ui/components/` | Bytes continuam em arquivos privados; metadados Room sem alteração. |
| Assinatura | `ui/screens/ItensEMidia.kt` | `ui/checklist/signature/` | PNG e vínculo existentes; só finalizar depois de salvar. |
| Lista, pendências, detalhe | `ui/screens/Telas.kt` | `ui/checklist/list/`, `ui/checklist/detail/`, `ui/sync/` | Paging/consultas Room e estados da fila preservados. |
| ViewModel global | `ui/ChecklistViewModel.kt` | ViewModels por funcionalidade e `ui/di/` | Criar `UiState` imutável, manter fluxos e ações; factories sem `Context` dentro dos ViewModels. |
| Entidades Room, DAO e banco | `data/local/` | Entidades em arquivos individuais no mesmo pacote; DAO e banco mantidos | **Sem alteração de esquema nesta refatoração**; dados existentes são preservados, sem migração destrutiva. |
| API proposta, repositórios, DataStore e WorkManager | `data/`, `sync/` | Mantidos com ajustes pontuais | Não mudar contrato remoto nem semântica de confirmação. |

## Referências visuais

| Imagem | Rota atual | Elementos aplicáveis | Limite funcional |
|---|---|---|---|
| Início - Dashboard | `Inicio` | Fundo claro, cartões brancos, cor petróleo/turquesa, ação principal e resumo recente | Jornada, telemetria, conjunto vinculado e suporte não existem no app; não serão simulados como dados reais. |
| Histórico de Checklists | `Lista` | Busca destacada, cartões de checklist, resultado e acesso para criar | Manter filtros combináveis e Paging 3 mesmo que o mockup os omita. |
| Lançar Checklist | `TemplatesRoute`, `ChecklistDataRoute`, `InspectionItemsRoute`, `SignatureRoute` | Parâmetros com diálogos, progresso, respostas, motivo/fotos condicionais e assinatura | Fluxo em três etapas; não supor veículo vinculado nem executor autenticado. |
| Preferências e Configurações | Nova rota `Preferencias` | Grupos de conectividade e sincronização | Perfil, conta, telemetria, cache e troca de veículos não estão implementados; não criar controles fictícios. |
| Visualização de Checklist | `Detalhe` | Resumo, contadores, não conformidades, fotos e assinatura | PDF, GPS, protocolo remoto e certificação não existem no contrato atual; não mostrar sucesso inventado. |

## Etapas

1. Extrair navegação e componentes compartilhados; build.
2. Separar ViewModels e estados por função, preservando APIs dos repositórios; testes.
3. Extrair cada tela e mídia; aplicar tema e hierarquia das cinco referências; build e lint.
4. Conferir fluxos, Previews e telas no emulador; executar testes unitários e instrumentados; registrar diferenças e limites.

## Resultado

As quatro etapas foram concluídas. Os arquivos monolíticos foram removidos, o Photo Picker e a câmera ficaram em `media/`, as entidades Room estão em arquivos próprios e os ViewModels recebem portas com modelos de domínio. O esquema Room e as chaves DataStore permanecem iguais. A tela de preferências expõe apenas modo de demonstração, preferência de rede, catálogos e sincronização existentes. As cinco referências orientam cores, cartões, hierarquia e estados, mantendo as rotas funcionais separadas. A validação real e os limites do backend estão em `docs/STATUS.md`.



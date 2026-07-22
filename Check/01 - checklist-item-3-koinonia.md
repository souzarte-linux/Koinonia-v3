# ✅ CHECKLIST EXECUTÁVEL — Item 3 Koinonia-v3

**Como usar:** Abra o repositório e, para cada checkbox abaixo, procure o código correspondente. Marque com `[x]` se encontrar.

---

## **3.1 — Autorização por Papel (AppRole)**

### Estrutura Base
- [ ] Arquivo `AppRole.kt` existe em `domain/model/` ou `domain/enums/`
- [ ] Enum contém exatamente 5 papéis: `ADMIN`, `PASTOR`, `ANCIAO`, `VIEWER`, `NONE`
- [ ] Existe `TESOUREIRO` como papel adicional (referência em 3.2)?

### Propriedades Calculadas
- [ ] Existe extensão/propriedade `hasFullAccess: Boolean`
- [ ] `hasFullAccess` retorna `true` APENAS para `ADMIN`, `PASTOR`, `ANCIAO`
- [ ] Teste: `AppRole.VIEWER.hasFullAccess` deve retornar `false`
- [ ] Teste: `AppRole.ADMIN.hasFullAccess` deve retornar `true`

- [ ] Existe extensão/propriedade `hasTreasuryAccess: Boolean`
- [ ] `hasTreasuryAccess` retorna `true` para `ADMIN`, `PASTOR`, `ANCIAO`, `TESOUREIRO`
- [ ] Teste: `AppRole.VIEWER.hasTreasuryAccess` deve retornar `false`
- [ ] Teste: `AppRole.TESOUREIRO.hasTreasuryAccess` deve retornar `true`

### Resolução de Papel sem Flash de Login
- [ ] `AuthRepositoryImpl` tem um `StateFlow<AuthResolutionState>` (ou similar)
- [ ] Estados possíveis: `LOADING`, `AUTHENTICATED(role)`, `UNAUTHENTICATED`
- [ ] Durante `LOADING`, tela mostra splash ou loading, não tenta redirecionar
- [ ] `resolveRoleFromMinistries()` usa escopo apropriado (não `GlobalScope`)
- [ ] Escopo é injetado via Hilt, não hardcoded

**Arquivos esperados:**
```
domain/model/AppRole.kt
domain/model/AuthResolutionState.kt (ou similar)
data/repository/AuthRepositoryImpl.kt
```

---

## **3.2 — Autorização Específica por Tela**

### Verificações em AppNavigation.kt

#### Tela member_add
- [ ] Existe guard clause que verifica `currentRole.hasFullAccess`
- [ ] Se `hasFullAccess == false`, redireciona para "calendar" ou exibe "UnauthorizedScreen"
- [ ] Teste: Logar como VIEWER, clicar em "Membros" → não consegue acessar "Adicionar Membro"

#### Tela member_edit
- [ ] Existe guard clause que verifica `currentRole.hasFullAccess`
- [ ] Mesmo acessando via deep link, verifica permissão
- [ ] Teste: Logar como VIEWER, tentar editar um membro → acesso negado

#### Tela event_create
- [ ] Existe guard clause que verifica `currentRole.hasFullAccess`
- [ ] Se `hasFullAccess == false`, redireciona com mensagem clara
- [ ] Botão "Novo Evento" em `CalendarScreen` fica desabilitado/invisível para VIEWER?

#### Tela reception (Chamada)
- [ ] Existe guard clause que verifica `currentRole.hasFullAccess`
- [ ] Se `hasFullAccess == false`, redireciona para home
- [ ] Teste: Logar como VIEWER, clicar em culto da Agenda → não consegue abrir Chamada

### Tela de Erro/Sem Permissão
- [ ] Existe `UnauthorizedScreen` ou similar
- [ ] Exibe mensagem amigável: _"Você não tem permissão para acessar esta tela"_
- [ ] Oferece botão para voltar à Agenda

**Arquivos esperados:**
```
presentation/navigation/AppNavigation.kt
presentation/features/unauthorized/UnauthorizedScreen.kt
presentation/features/member/MemberAddScreen.kt
presentation/features/member/MemberEditScreen.kt
presentation/features/event/EventRegistrationScreen.kt
presentation/features/reception/ReceptionScreen.kt
```

---

## **3.3 — Autorização para Criação/Edição de Eventos**

### Função canManageEvent

- [ ] Existe função `canManageEvent(eventId, currentUserRole, directorMinistries)`
- [ ] Retorna `true` se `currentUserRole.hasFullAccess`
- [ ] Retorna `true` se evento pertence a um dos ministérios que o usuário dirige

#### Verificação de Conflito com Culto Ordinário
- [ ] Antes de salvar evento, consulta `EventDao.getAllEventsOnDate(date)`
- [ ] Verifica se existe `EventType.ORDINARIO` com horário sobreposto
- [ ] Se houver conflito E usuário é Diretor sem `hasFullAccess` → bloqueia
- [ ] Mensagem de erro: _"Este horário coincide com um Culto Ordinário. Apenas ADM, Pastor ou Ancião podem agendar eventos nesse horário."_
- [ ] Teste: Diretor tenta agendar evento fora do horário ordinário → consegue
- [ ] Teste: Diretor tenta agendar evento no horário ordinário → bloqueado
- [ ] Teste: ADMIN tenta agendar evento no horário ordinário → consegue

### Associação Automática de Ministério
- [ ] Novo evento criado por Diretor tem `event.ministryId` preenchido automaticamente
- [ ] Se Diretor gerencia 1 ministério → usa esse
- [ ] Se Diretor gerencia >1 ministério → pede para selecionar
- [ ] Spinner/seletor limita opções aos ministérios do Diretor

### UI - Botões Condicionais em CalendarScreen
- [ ] Botão "Editar" só aparece se `canManageEvent == true`
- [ ] Botão "Excluir" só aparece se `canManageEvent == true`
- [ ] Teste: VIEWER vê evento mas sem botões de editar/excluir
- [ ] Teste: Diretor de outro ministério vê evento mas sem botões

### UI - Botão Salvar em EventRegistrationScreen
- [ ] Botão "Salvar" fica desabilitado se `canManageEvent == false`
- [ ] Teste: Tenta entrar em modo edição para evento de outro ministério → botão desabilitado

**Arquivos esperados:**
```
domain/usecase/CanManageEventUseCase.kt (ou função em EventRepository)
data/repository/EventRepositoryImpl.kt
domain/repository/EventRepository.kt
presentation/features/event/EventRegistrationScreen.kt
presentation/features/event/EventRegistrationViewModel.kt
presentation/features/calendar/CalendarScreen.kt
```

---

## **3.4 — Acesso Restrito ao Módulo de Tesouraria**

### Rota treasury em AppNavigation.kt
- [ ] String `"treasury"` existe como destino no NavHost
- [ ] Guard clause: verifica `currentRole.hasTreasuryAccess`
- [ ] Se `hasTreasuryAccess == false`, redireciona para home
- [ ] Deep link para `"treasury"` respeita permissão

### Tela TreasuryScreen
- [ ] Arquivo `TreasuryScreen.kt` existe em `presentation/features/treasury/`
- [ ] Exibe mensagem: _"Módulo em construção"_ ou similar
- [ ] Não tenta acessar tabelas de Tesouraria (ainda não existem)

### Item "Tesouraria" no ModalDrawerSheet
- [ ] Item "Tesouraria" existe no drawer
- [ ] Fica invisível se `currentRole.hasTreasuryAccess == false`
- [ ] Clica em "Tesouraria" → navega para rota `"treasury"`
- [ ] Teste: Logar como VIEWER → item "Tesouraria" não aparece
- [ ] Teste: Logar como TESOUREIRO → item "Tesouraria" aparece

### Sem Implementação de Banco de Dados
- [ ] **Não existe** tabela Supabase para "treasury" (não é necessário agora)
- [ ] **Não existe** `TreasuryEntity`, `TreasuryDao`, `TreasuryRepository`
- [ ] O objetivo agora é apenas a trava de acesso, não a funcionalidade

**Arquivos esperados:**
```
presentation/navigation/AppNavigation.kt (rota "treasury")
presentation/features/treasury/TreasuryScreen.kt
presentation/components/ModalDrawerSheet.kt (ou similar)
```

**Arquivos que NÃO devem existir ainda:**
```
❌ TreasuryEntity.kt
❌ TreasuryDao.kt
❌ TreasuryRepository.kt
```

---

## **3.5 — Ocultação Visual de Abas/Atalhos Restritos**

### NavigationBar (Bottom Bar)

#### Abas Visíveis Sempre
- [ ] Item "Agenda" (rota "calendar") aparece para **todos**
- [ ] Teste: Logar como VIEWER → "Agenda" visível

#### Abas Condicionais
- [ ] Item "Chamada" (rota "reception") aparece **apenas se** `hasFullAccess == true`
- [ ] Item "Métricas" (rota "metrics") aparece **apenas se** `hasFullAccess == true`
- [ ] Teste: Logar como VIEWER → "Chamada" e "Métricas" **não aparecem**
- [ ] Teste: Logar como ADMIN → "Chamada" e "Métricas" aparecem

#### Comportamento com Uma Única Aba
- [ ] Se `hasFullAccess == false`, apenas "Agenda" é exibida
- [ ] Avaliar: a bottom bar fica com um único item, ou é ocultada inteiramente?

### ModalDrawerSheet (Menu Lateral "Secretaria")

#### Itens Condicionais
- [ ] Item "Membros" aparece **apenas se** `hasFullAccess == true`
- [ ] Item "Tesouraria" aparece **apenas se** `hasTreasuryAccess == true`
- [ ] Teste: Logar como VIEWER → nenhum dos dois itens aparece
- [ ] Teste: Logar como ADMIN → ambos os itens aparecem
- [ ] Teste: Logar como TESOUREIRO → apenas "Tesouraria" aparece

#### Comportamento sem Itens Aplicáveis
- [ ] Se nenhum item aplicável para o usuário, drawer é ocultado inteiramente
- [ ] Botão/ícone de abrir drawer fica invisível
- [ ] Teste: Logar como VIEWER → nenhum ícone de "menu" na top bar

### Diretor de Ministério Sem hasFullAccess

- [ ] Diretor vê apenas "Agenda" na bottom bar
- [ ] Diretor não vê "Chamada" nem "Métricas"
- [ ] Diretor não vê drawer ou drawer vazio

### Proteção em Profundidade (Guard Clauses)

- [ ] Guard clauses de rota continuam presentes em `AppNavigation.kt`
- [ ] Tentar acessar via deep link sem permissão ainda redireciona
- [ ] Teste: Deep link `app://koinonia/reception` como VIEWER → redireciona
- [ ] Ocultação visual **não substitui** guard clauses, é apenas segunda camada

**Arquivos esperados:**
```
presentation/navigation/AppNavigation.kt
presentation/components/NavigationBar.kt (ou BottomBar.kt)
presentation/components/ModalDrawerSheet.kt (ou SideMenuDrawer.kt)
```

---

## **3.6 — Bootstrap Seguro do Primeiro ADM**

### Constante de Bootstrap

- [ ] Existe `BOOTSTRAP_ADMIN_EMAIL` em `Constants.kt` ou `BuildConfig`
- [ ] Valor = `"cyber.souza@hotmail.com"` (ou configurável em produção)
- [ ] Não está hardcoded após o primeiro uso (usar BuildConfig ou propriedade)

### Lógica em AuthRepositoryImpl.resolveRoleFromMinistries

#### Verificação de Condições
- [ ] Email normalizado (lowercase) comparado com `BOOTSTRAP_ADMIN_EMAIL` (também lowercase)
- [ ] Consulta `MemberDao` ou similar para verificar se existe algum Membro com:
  - Cargo/posição ativa de `ADMIN` **OU** `PASTOR` **OU** `ANCIAO`
  - Data de término ausente ou futura
- [ ] Se email == BOOTSTRAP_ADMIN_EMAIL **E** nenhum admin ativo → retorna `AppRole.ADMIN`
- [ ] Se email != BOOTSTRAP_ADMIN_EMAIL **OU** existe admin ativo → segue fluxo normal

#### Comportamento Auto-Desativante
- [ ] Assim que Fernando (ou outro Membro) for criado com cargo ativo de ADMIN/PASTOR/ANCIAO, a regra deixa de valer
- [ ] Teste 1: App vazio, logar como cyber.souza@hotmail.com → acesso ADMIN via bootstrap
- [ ] Teste 2: Após adicionar Fernando como ADMIN, logar com nova sessão → acesso ADMIN permanente (não mais bootstrap)
- [ ] Teste 3: Criar outro Membro, logar com email dele → acesso VIEWER (não é bootstrap)

### Aviso Visual

- [ ] Ao logar via bootstrap, exibe Banner ou Snackbar (não bloqueante)
- [ ] Mensagem: _"Acesso administrativo temporário de configuração inicial. Complete seu cadastro de Membro com um cargo de ADM/Pastor/Ancião/Diácono para manter o acesso permanente."_
- [ ] Aviso desaparece após Fernando completar cadastro
- [ ] Teste: Logar como cyber.souza@hotmail.com → aviso aparece
- [ ] Teste: Após adicionar Fernando com cargo → aviso some (relogar se necessário)

### Documentação no Código

- [ ] Comentário explicativo acima de `resolveRoleFromMinistries()` menciona:
  - É uma regra de **bootstrap**, não conta root permanente
  - Motivo: evitar "ovo e galinha" — sem admin, ninguém consegue abrir `member_add`
  - Se autodesativa quando um admin real é criado
  - Referência a este documento/prompt para futuro maintainer

### Fluxo Esperado (Fernando)

```
1. [ ] App aberto, nenhum Membro cadastrado
2. [ ] Fernando clica "Entrar"
3. [ ] Inserir email: cyber.souza@hotmail.com
4. [ ] [ ] Entra como AppRole.ADMIN via bootstrap
5. [ ] [ ] Aviso visual aparece na top/bottom
6. [ ] [ ] Clica em drawer → vê "Membros" e "Tesouraria" (ambos visíveis)
7. [ ] [ ] Abre "Membros" → vê botão "Adicionar Membro"
8. [ ] [ ] Clica "Adicionar Membro"
9. [ ] [ ] Preenche: Nome = "Fernando Anunciação de Souza"
10. [ ] [ ] Preenche: Email (login) = "cyber.souza@hotmail.com"
11. [ ] [ ] Preenche: CPF, RG, etc.
12. [ ] [ ] Navega para seção "Atuação Ministerial"
13. [ ] [ ] Adiciona cargo: "Diácono", data de início = hoje, **sem data de término**
14. [ ] [ ] Salva
15. [ ] [ ] Retorna para home
16. [ ] [ ] Sai (logout)
17. [ ] [ ] Faz novo login com cyber.souza@hotmail.com
18. [ ] [ ] [ ] Acesso ADMIN é via Membro real agora, não bootstrap
19. [ ] [ ] [ ] Aviso desaparece (ou não aparece mais)
```

**Arquivos esperados:**
```
Constants.kt (ou BuildConfig)
data/repository/AuthRepositoryImpl.kt
data/local/dao/MemberDao.kt
presentation/features/member/MemberAddScreen.kt
presentation/features/member/MemberEditScreen.kt
```

---

## 🎯 Resumo — Pontuação Rápida

Copie e preencha após a verificação:

```
[Item 3.1] Autorização por Papel ............ [ ] / 10 pontos
[Item 3.2] Autorização por Tela ............ [ ] / 10 pontos
[Item 3.3] Controle de Eventos ............ [ ] / 15 pontos
[Item 3.4] Módulo Tesouraria ............. [ ] / 10 pontos
[Item 3.5] Ocultação Visual .............. [ ] / 10 pontos
[Item 3.6] Bootstrap Admin ............... [ ] / 15 pontos
────────────────────────────────────
TOTAL ITEM 3 ........................... [ ] / 70 pontos
```

**Interpretação:**
- **60-70:** Implementação completa ✅
- **50-59:** Implementação parcial, alguns gaps ⚠️
- **40-49:** Implementação incompleta, requisitos críticos faltando ❌
- **<40:** Não iniciado ou muito incompleto ❌

---

## 📝 Notas Finais

- Este checklist é **vivo** — atualize conforme novas verificações surgirem
- Cada `[ ]` marcado deve vir acompanhado de **número da linha de código** ou **screenshot** para auditoria
- Guarde uma cópia deste documento na pasta raiz do projeto para referência futura
- Compartilhe com a equipe dev para que todos saibam o que verificar


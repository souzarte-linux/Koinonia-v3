# 📋 Análise do Item 3 — Segurança e Controle de Acesso (Koinonia-v3)

**Data da análise:** 21 de julho de 2026  
**Projeto:** Koinonia-v3 (Kotlin + Jetpack Compose + Room + Supabase)

---

## 📌 Resumo Executivo

O **Item 3** especifica 6 subitens relacionados a **segurança, autorização por papel e controle de acesso**. Este documento lista todos os requisitos, indicando em cada subitem:
- ✅ O que **DEVE** estar implementado
- 🔍 Como **verificar** no código
- ⚠️ Riscos se **não** implementado

---

## **Item 3.1 — Autorização por Papel (AppRole)**

### Requisitos Obrigatórios

1. **Enumeration com 5 papéis principais:**
   - `ADMIN` — acesso total, sem restrições
   - `PASTOR` — acesso total, sem restrições
   - `ANCIAO` (Ancião) — acesso total, sem restrições  
   - `VIEWER` — acesso de leitura/visualização apenas
   - `NONE` — usuário não autenticado

2. **Propriedade calculada `hasFullAccess`:**
   ```kotlin
   val AppRole.hasFullAccess: Boolean
       get() = this in listOf(ADMIN, PASTOR, ANCIAO)
   ```
   Deve retornar `true` apenas para ADMIN, PASTOR e ANCIAO.

3. **Propriedade calculada `hasTreasuryAccess`:**
   ```kotlin
   val AppRole.hasTreasuryAccess: Boolean
       get() = this in listOf(ADMIN, PASTOR, ANCIAO, TESOUREIRO)
   ```
   Deve retornar `true` para ADMIN, PASTOR, ANCIAO e TESOUREIRO.

4. **O papel deve ser resolvido de forma síncrona** na sessão do usuário:
   - Sem flashear a tela de login
   - O estado deve ser: `LOADING` → `AUTHENTICATED(role)` ou `UNAUTHENTICATED`

### ✅ Checklist de Verificação

- [ ] Existe um `enum class AppRole` ou similar com os 5 papéis?
- [ ] Existe uma extensão `hasFullAccess` que retorna `true` apenas para ADMIN, PASTOR, ANCIAO?
- [ ] Existe uma extensão `hasTreasuryAccess` que inclui TESOUREIRO?
- [ ] O papel não fica em `NONE` enquanto carrega (existe estado `LOADING`)?
- [ ] Arquivos esperados: `domain/model/AppRole.kt`, possivelmente extensões em `AppRole+Extensions.kt`

---

## **Item 3.2 — Autorização Específica por Tela (member_add, member_edit, event_create, reception)**

### Requisitos Obrigatórios

1. **Tela `member_add` (cadastro de novo membro):**
   - ✅ Bloqueada para quem **não tem** `hasFullAccess`
   - Deve redirecionar para login ou mostrar aviso de permissão negada

2. **Tela `member_edit` (edição de membro):**
   - ✅ Bloqueada para quem **não tem** `hasFullAccess`
   - Mesmo que o membro esteja vendo seu próprio cadastro, não pode editar sem acesso total

3. **Tela `event_create` (criação de novo evento):**
   - ✅ Bloqueada para quem **não tem** `hasFullAccess`
   - Deve redirecionar com aviso claro

4. **Tela `reception` (Chamada/Presença):**
   - ✅ Bloqueada para quem **não tem** `hasFullAccess`
   - Apenas líderes podem registrar presença de membros

5. **Implementação via Guard Clause (em `AppNavigation.kt`):**
   ```kotlin
   if (!currentRole.hasFullAccess) {
       // redirecionar para home ou mostrar aviso
       navController.navigate("unauthorized") {
           popUpTo("calendar") { inclusive = false }
       }
   }
   ```

### ✅ Checklist de Verificação

- [ ] Existe verificação de `hasFullAccess` em `AppNavigation.kt` ou nos composables de destino?
- [ ] Telas `member_add`, `member_edit`, `event_create`, `reception` têm guard clauses?
- [ ] Tentativa de acessar via deep link sem permissão resulta em redirecionamento?
- [ ] Existe tela/composable `UnauthorizedScreen` ou similar para exibir mensagem de acesso negado?

---

## **Item 3.3 — Autorização para Criação/Edição de Eventos (Diretor de Ministério)**

### Requisitos Obrigatórios

1. **Nova função `canManageEvent(eventId, directorEmail, directorMinistries)`:**
   - Retorna `true` se:
     - Quem está editando tem `hasFullAccess` (ADMIN, PASTOR, ANCIAO) → **sempre pode**
     - **OU** quem está editando é um Diretor do ministério (`directorEmail` no `MemberMinistry.position = "Diretor"`)
       - **E** o evento pertence a um dos ministérios que ele dirige (`event.ministryId` está em `directorMinistries`)
       - **E NÃO há conflito** com um Culto Ordinário
   - Retorna `false` se:
     - Diretor tenta editar evento que conflita com Culto Ordinário e não tem `hasFullAccess`

2. **Lógica de conflito com Culto Ordinário:**
   - Antes de salvar um evento criado por Diretor sem `hasFullAccess`, consultar `EventDao`
   - Verificar se existe evento do tipo `EventType.ORDINARIO` com data/hora sobreposta
   - Se houver conflito → **bloquear salvamento** com mensagem:  
     _"Este horário coincide com um Culto Ordinário. Apenas ADM, Pastor ou Ancião podem agendar eventos nesse horário."_

3. **Associação automática de ministério:**
   - Quando um Diretor sem `hasFullAccess` cria evento, `event.ministryId` deve ser automaticamente preenchido
   - Se o Diretor gerencia apenas 1 ministério → usar esse
   - Se gerencia > 1 → pedir para o usuário selecionar em qual ministério o evento se enquadra

4. **Bloqueio de edição/exclusão na UI:**
   - Em `CalendarScreen.kt`, mostrar botões de editar/excluir apenas se `canManageEvent` retorna `true`
   - Em `EventRegistrationScreen.kt`, bloquear o botão "Salvar" se `canManageEvent` for `false`

### ✅ Checklist de Verificação

- [ ] Existe função `canManageEvent(eventId, userRole, userMinistries)`?
- [ ] Função verifica se existe Culto Ordinário em conflito?
- [ ] Mensagem de erro clara quando há conflito?
- [ ] Diretor sem `hasFullAccess` consegue editar eventos do próprio ministério?
- [ ] Diretor sem `hasFullAccess` **não consegue** editar eventos de outro ministério?
- [ ] Novo evento criado por Diretor tem `ministryId` preenchido automaticamente?
- [ ] Botões de editar/excluir aparecem condicionalmente em `CalendarScreen`?
- [ ] Botão de salvar fica desabilitado em `EventRegistrationScreen` quando `canManageEvent = false`?

---

## **Item 3.4 — Acesso Restrito ao Módulo de Tesouraria**

### Requisitos Obrigatórios

1. **Rota reservada `treasury` em `AppNavigation.kt`:**
   - Guard clause: `if (!currentRole.hasTreasuryAccess) { redirecionar }`
   - Só ADMIN, PASTOR, ANCIAO, TESOUREIRO têm acesso

2. **Tela placeholder "Módulo em construção":**
   - Não precisa de implementação completa de Tesouraria agora
   - Apenas estrutura pronta para quando o módulo for desenvolvido

3. **Item de menu "Tesouraria" no drawer:**
   - Visível apenas se `currentRole.hasTreasuryAccess == true`
   - Abrirá a rota `"treasury"`

4. **Sem implementação de lançamentos/relatórios agora:**
   - Apenas a trava de acesso deve estar pronta
   - Banco de dados de Tesouraria **não** precisa ser construído nesta fase

### ✅ Checklist de Verificação

- [ ] Existe rota `"treasury"` em `AppNavigation.kt`?
- [ ] Guard clause verifica `hasTreasuryAccess`?
- [ ] Existe `TreasuryScreen` ou similar com mensagem "Em construção"?
- [ ] Item "Tesouraria" no ModalDrawerSheet fica invisível para quem não tem acesso?
- [ ] Deep link para `"treasury"` redireciona se usuário não tem `hasTreasuryAccess`?

---

## **Item 3.5 — Ocultação Visual de Abas/Atalhos Restritos**

### Requisitos Obrigatórios

1. **NavigationBar (bottom bar) deve exibir condicionalmente:**
   - Mostrar "Agenda" (calendar) para **todos**
   - Mostrar "Chamada" (reception) apenas se `hasFullAccess == true`
   - Mostrar "Métricas" (metrics) apenas se `hasFullAccess == true`
   - Se usuário tem `hasFullAccess == false` → bottom bar mostra **apenas "Agenda"**
     - Avaliar se oculta a bottom bar inteira ou deixa com um único item

2. **ModalDrawerSheet (menu lateral "Secretaria") deve exibir condicionalmente:**
   - Mostrar "Membros" apenas se `hasFullAccess == true`
   - Mostrar "Tesouraria" apenas se `hasTreasuryAccess == true`
   - Se nenhum item aplicável → ocultar o botão/ícone de abrir drawer

3. **Objetivo de negócio:**
   - Um Visitante ou membro comum **não deve sequer saber** que existe aba "Chamada" ou "Métricas"
   - Evitar desconforto: "por que fulano sabe a que horas eu cheguei?"

4. **Guard clauses permanecem:**
   - Ocultação visual é a **segunda camada** de proteção
   - Guard clauses de rota em `AppNavigation.kt` continuam como proteção real

### ✅ Checklist de Verificação

- [ ] `NavigationBar` tem `if (currentRole.hasFullAccess)` para "Chamada" e "Métricas"?
- [ ] `NavigationBar` mostra "Agenda" para todos os papéis?
- [ ] `ModalDrawerSheet` tem `if (currentRole.hasFullAccess)` para "Membros"?
- [ ] `ModalDrawerSheet` tem `if (currentRole.hasTreasuryAccess)` para "Tesouraria"?
- [ ] Botão de abrir drawer fica invisível se nenhum item aplicável?
- [ ] Diretor de ministério **sem** `hasFullAccess` vê apenas "Agenda"?
- [ ] Guard clauses de rota ainda existem (não foram removidos)?

---

## **Item 3.6 — Bootstrap Seguro do Primeiro ADM**

### Requisitos Obrigatórios

1. **Constante configurável em `Constants.kt`:**
   ```kotlin
   const val BOOTSTRAP_ADMIN_EMAIL = "cyber.souza@hotmail.com"
   ```
   (ou lido de `BuildConfig` para não ficar hardcoded em produção)

2. **Lógica em `AuthRepositoryImpl.resolveRoleFromMinistries(email)`:**
   - Antes de retornar `AppRole.VIEWER` no fallback final, adicionar verificação:
     ```kotlin
     if (email.lowercase() == BOOTSTRAP_ADMIN_EMAIL.lowercase() 
         && nenhum Membro no banco tem diretoria ativa de ADMIN/PASTOR/ANCIAO) {
         return AppRole.ADMIN
     }
     ```
   - Assim que existir **pelo menos um** Membro com cargo ativo de ADMIN/PASTOR/ANCIAO, a condição fica falsa automaticamente

3. **Aviso visual (não bloqueante) ao logar com bootstrap:**
   - Banner ou Snackbar na tela principal:  
     _"Acesso administrativo temporário de configuração inicial. Complete seu cadastro de Membro com um cargo de ADM/Pastor/Ancião/Diácono para manter o acesso permanente."_

4. **Documentação no código:**
   - Comentário acima de `resolveRoleFromMinistries` explicando:
     - É uma regra de **bootstrap**, não uma conta root permanente
     - Motivo: evitar "ovo e galinha" — ninguém consegue abrir `member_add` sem acesso
     - Se autodesativa assim que um Membro real com papel administrativo é criado

5. **Fluxo esperado para o Fernando:**
   ```
   1. Logar com cyber.souza@hotmail.com
   2. → Entra como ADMIN via bootstrap
   3. → Clica em "Membros"
   4. → Cadastra (ou edita) "Fernando Anunciação de Souza"
   5. → Preenche "E-mail (Usado para login)" = cyber.souza@hotmail.com
   6. → Na seção "Atuação Ministerial", adiciona cargo ativo (ex: "Diácono") sem data de término
   7. → Salva
   8. → A partir daí, Fernando mantém acesso total mesmo sem a regra de bootstrap
   ```

### ✅ Checklist de Verificação

- [ ] Existe constante `BOOTSTRAP_ADMIN_EMAIL` em `Constants.kt` ou `BuildConfig`?
- [ ] `resolveRoleFromMinistries` verifica se email == BOOTSTRAP_ADMIN_EMAIL?
- [ ] Verificação também checa se **nenhum** Membro tem cargo de ADMIN/PASTOR/ANCIAO?
- [ ] Se ambas as condições verdadeiras → retorna `AppRole.ADMIN`?
- [ ] Aviso visual (Banner/Snackbar) aparece ao logar com bootstrap?
- [ ] Aviso menciona que é **temporário** e pede para "completar cadastro"?
- [ ] Comentário explicativo acima da função documenta a regra de bootstrap?
- [ ] Após Fernando adicionar cargo ativo, ele **continua** com acesso mesmo sem bootstrap?

---

## 📊 Matriz de Verificação Rápida

| Subitem | Requisito Principal | Implementado? | Notas |
|---------|-------------------|---------------|-------|
| 3.1 | Enum AppRole + hasFullAccess/hasTreasuryAccess | ⏳ | Buscar `AppRole.kt` |
| 3.2 | Guard clauses em member_add/edit, event_create, reception | ⏳ | Buscar `AppNavigation.kt` |
| 3.3 | canManageEvent + conflito Culto Ordinário + associação ministério | ⏳ | Buscar EventRepository, EventRegistrationScreen |
| 3.4 | Rota treasury + placeholder + menu drawer | ⏳ | Buscar rota "treasury" e TreasuryScreen |
| 3.5 | NavigationBar/Drawer com visibilidade condicional | ⏳ | Buscar NavigationBar e ModalDrawerSheet em AppNavigation |
| 3.6 | Bootstrap BOOTSTRAP_ADMIN_EMAIL + aviso visual + documentação | ⏳ | Buscar Constants.kt e AuthRepositoryImpl |

---

## 🔍 Arquivos Críticos a Examinar

Para fazer uma verificação **completa** do Item 3, procure por:

### Estrutura de Autorização
- `domain/model/AppRole.kt` (ou `domain/enums/AppRole.kt`)
- Extensões: `AppRole+Extensions.kt` ou métodos dentro da classe
- `data/repository/AuthRepositoryImpl.kt`
- `domain/usecase/resolveRoleFromMinistries` ou similar

### Navigação e Rotas
- `presentation/navigation/AppNavigation.kt` — guard clauses principais
- `presentation/navigation/Screen.kt` — definição de rotas
- `presentation/components/NavigationBar.kt` ou similar
- `presentation/components/ModalDrawerSheet.kt` ou similar

### Controle de Eventos
- `domain/model/EventEntity.kt` — campos tipo, ministryId, data/hora
- `data/repository/EventRepositoryImpl.kt` ou `EventDao.kt` — lógica de conflito
- `presentation/features/event/EventRegistrationScreen.kt` ou `EventRegistrationViewModel.kt`
- `domain/usecase/CreateEventUseCase.kt` ou `EditEventUseCase.kt`

### Membros e Ministérios
- `domain/model/MemberEntity.kt` — incluir ministério/cargo
- `domain/model/MemberMinistry.kt` ou `Ministry.kt` — posição "Diretor"
- `data/repository/MemberRepositoryImpl.kt`
- `presentation/features/member/MemberAddScreen.kt` e `MemberEditScreen.kt`

### Tesouraria
- `presentation/navigation/AppNavigation.kt` — rota "treasury"
- `presentation/features/treasury/TreasuryScreen.kt`
- `Constants.kt` — verificar se existe `BOOTSTRAP_ADMIN_EMAIL`

### Segurança Geral
- `Constants.kt` — URLs Supabase, chaves, BOOTSTRAP_ADMIN_EMAIL
- `AndroidManifest.xml` — `android:allowBackup`
- `data/database/AppDatabase.kt` — RLS mencionado em comentários?

---

## ⚠️ Problemas Conhecidos (Antes da Implementação)

De acordo com o documento de análise, **estes problemas existem:**

1. **Arquivo de credenciais público:**
   - `CREDENCIAIS DO PROJEOT Koinonia_v3.txt` está na raiz do repo

2. **Segurança de banco de dados:**
   - RLS do Supabase **pode não estar** configurado em todas as tabelas
   - CPF, RG, endereço, telefone podem estar expostos

3. **Backup do Android:**
   - `android:allowBackup="true"` + dados sensíveis = risco

4. **GlobalScope anti-pattern:**
   - `AuthRepositoryImpl` usa `GlobalScope.launch` (problema de vazamento de corrotinas)

5. **Condição de corrida na role:**
   - Resolvido de forma assíncrona, causando flash de login

---

## ✅ Próximos Passos

1. **Clone o repositório** (se privado, obter acesso)
2. **Para cada subitem (3.1 a 3.6)**, procure pelos arquivos listados
3. **Use este documento como checklist** para validar cada requisito
4. **Documente achados** em um relatório separado indicando:
   - ✅ O que foi implementado corretamente
   - ⚠️ O que foi implementado parcialmente
   - ❌ O que ainda não foi implementado
5. **Priorize:**
   - Problemas de **segurança** (Item 3.6 bootstrap, guard clauses)
   - Problemas de **UX** (Item 3.5 ocultação visual)
   - Melhorias de **negócio** (Item 3.3 conflito de Culto Ordinário)

---

## 📞 Contato / Revisão

Produzido como análise de validação do projeto Koinonia-v3.  
Baseado no documento: **Koinonia-v3-analise-e-prompts.md**  
Data: **21 de julho de 2026**


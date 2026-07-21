# Análise do projeto Koinonia-v3 e prompts para o Antigravity IDE

Projeto: Kotlin + Jetpack Compose (Android), com Room (banco local offline-first), Supabase (Auth + Postgrest) e WorkManager para sincronização.

---

## 🚨 Segurança — resolver ANTES de tudo

1. **Arquivo `CREDENCIAIS DO PROJEOT Koinonia_v3.txt`** está na raiz do repositório, agora público. Remova-o do repo e do histórico do Git, e troque qualquer senha ali contida.
2. **`Constants.kt` tem a URL e a `anon key` do Supabase hardcoded** no código-fonte, agora públicas. Isso por si só não é catastrófico (a anon key é "pública" por natureza no Supabase), **mas só é seguro se o RLS (Row Level Security) estiver ativado em TODAS as tabelas** (`members`, `attendance`, `events`, `user_roles`, `visitors`, `ministry_history`). Confirme isso no painel do Supabase — se o RLS não estiver configurado, qualquer pessoa com a anon key (agora pública) pode ler/escrever direto na tabela `members`, que contém **CPF, RG, endereço e telefone** de todos os membros.
3. `android:allowBackup="true"` no `AndroidManifest.xml` permite que o backup automático do Android exporte o banco Room local (com esses mesmos dados sensíveis) sem criptografia adicional. Considere `allowBackup="false"` ou regras de backup específicas.
4. Depois de resolver os itens acima, **volte a tornar o repositório privado.**

---

## 1) Prompt — corrigir a aba "Chamada"

### O que encontrei
- A tela (`ReceptionScreen.kt`) tem uma linha que **ignora o culto selecionado na Agenda**:
  ```kotlin
  LaunchedEffect(Unit) {
      viewModel.initReception("evento_hoje", ZonedDateTime.now())
  }
  ```
  Isso sobrescreve o `eventId` real que já vem corretamente da navegação (`AppNavigation.kt` chama `viewModel.initReception(eventId, null)`), fazendo a Chamada sempre tentar abrir o "culto de hoje" em vez do culto que o usuário tocou.
- O backend **já calcula atraso corretamente** (`AttendanceRepositoryImpl.markPresence` usa `TimeManager.calculateLateMinutes` e salva `isLate` + `lateDurationMins` em `AttendanceEntity`), mas a **UI mostra só um `Checkbox` binário** (presente/não presente) — não existe exibição de "atrasado" nem dos minutos/nível de atraso, e não existe nenhuma ação explícita para marcar "ausente" (isso só acontece em lote ao apertar o botão de finalizar o culto).

### Prompt para o Antigravity IDE
```
No projeto Koinonia-v3 (Kotlin + Jetpack Compose + Room + Supabase), corrija e complete a funcionalidade da aba "Chamada" (arquivos ReceptionScreen.kt e ReceptionViewModel.kt, pacote presentation/features/reception):

1. Remova o LaunchedEffect(Unit) que chama viewModel.initReception("evento_hoje", ZonedDateTime.now()) dentro de ReceptionScreen.kt. O eventId já é passado corretamente pela navegação (AppNavigation.kt chama viewModel.initReception(eventId, null)) e não deve ser sobrescrito por um valor fixo de teste.

2. Substitua o Checkbox binário de cada item da lista por um controle de 3 estados visualmente distintos, refletindo o que já existe no modelo de dados (AttendanceEntity: isLate, lateDurationMins, isAbsent):
   - Presente pontual (chegou até o horário do culto): indicador verde.
   - Presente com atraso: indicador amarelo/laranja, mostrando os minutos de atraso (ex: "Atraso: 12 min") e, se fizer sentido, um nível textual calculado a partir de lateDurationMins (ex: "Atraso leve" até 15 min, "Atraso moderado" de 16 a 30 min, "Atraso grave" acima de 30 min).
   - Ausente: indicador vermelho.
   Use um componente tipo SegmentedButton, um menu de status ou 3 ícones tocáveis (não precisa ser só um checkbox) para o usuário poder marcar manualmente qualquer um dos 3 estados a qualquer momento, e não apenas presente/ausente por toggle único.

3. Garanta que ao marcar "Ausente" manualmente antes da finalização do culto, o AttendanceEntity correspondente seja criado com isAbsent = true imediatamente (hoje isso só acontece em lote pelo FinalizeEventUseCase ao apertar o FAB de finalizar). O FAB de finalizar continua existindo para marcar como ausentes automaticamente quem não recebeu nenhum registro até o fim do culto.

4. Mantenha toda a lógica já existente de cálculo de atraso (TimeManager.calculateLateMinutes) e de sincronização (triggerSync / WorkManager) sem alterações — apenas conecte a UI ao que o ViewModel/repositório já fornece.

5. Depois de aplicar, rode o projeto e confirme que abrir a Chamada a partir de um evento específico da Agenda usa o eventId correto (não mais "evento_hoje" fixo).
```

---

## 2) Prompt — corrigir a tela inicial (deve ser a aba Agenda)

### O que encontrei
- No `AppNavigation.kt`, o `NavHost` já está configurado com `startDestination = "calendar"` (Agenda), então o problema **não é a rota inicial em si**. O bug real está em dois pontos:
  1. **Condição de corrida no papel do usuário:** `AuthRepositoryImpl` inicia `_currentUserRole` como `AppRole.NONE` e só resolve a role de verdade de forma assíncrona (`GlobalScope.launch { resolveRoleFromMinistries(email) }`). Enquanto isso não termina, o `LaunchedEffect(currentRole, currentRoute)` do `AppNavigation.kt` enxerga `currentRole == NONE` e **redireciona para a tela de login**, mesmo que o usuário já tenha sessão ativa no Supabase — causando um "flash" da tela de login antes de cair na Agenda.
  2. **Restauração de estado do Android:** como o `NavController` é criado com `rememberNavController()` (que salva/restaura estado automaticamente via `savedInstanceState`), se o app for encerrado pelo sistema enquanto o usuário estava em "Chamada" ou "Métricas" e reaberto depois, ele pode voltar exatamente para essa última tela em vez de sempre abrir na Agenda.

### Prompt para o Antigravity IDE
```
No projeto Koinonia-v3, a tela inicial do app deve ser sempre a aba Agenda (rota "calendar"), mas isso não está acontecendo de forma consistente. Corrija as duas causas abaixo:

1. Em AuthRepositoryImpl.kt, o bloco init resolve a role do usuário (resolveRoleFromMinistries) de forma assíncrona usando GlobalScope.launch, deixando _currentUserRole em AppRole.NONE até essa resolução terminar. Isso faz o AppNavigation.kt redirecionar para a tela de login mesmo quando já existe uma sessão ativa, antes de cair na Agenda. Troque esse comportamento por um estado de carregamento explícito: crie um terceiro estado (ex: um StateFlow<AuthResolutionState> com valores LOADING, AUTHENTICATED(role), UNAUTHENTICATED), e enquanto estiver LOADING mostre uma tela de splash/carregamento no AppNavigation.kt em vez de decidir entre "login" e "calendar" prematuramente. Substitua também o GlobalScope.launch por um escopo apropriado (ex: injete um CoroutineScope de aplicação via Hilt, com SupervisorJob, em vez de GlobalScope).

2. Garanta que a Agenda seja sempre a tela mostrada ao reabrir o app, independentemente da última aba visitada antes de o processo ser encerrado pelo sistema Android. Ajuste o NavHost/NavController em AppNavigation.kt (por exemplo, não restaurando o backstack salvo entre reaberturas do processo, ou forçando explicitamente a navegação para "calendar" logo após a resolução de autenticação ser concluída com sucesso).

3. Não altere a navegação interna normal entre abas (Chamada, Agenda, Métricas) uma vez que o app já está aberto — a exigência é apenas sobre qual tela aparece no momento em que o app é iniciado do zero.
```

---

## 3) Prompt — controle de acesso por função ministerial (RBAC)

### O que encontrei
- **A ligação entre "quem fez login" e "qual Membro/perfil ministerial é esse" é frágil e, na prática, não funciona.** O login (Supabase Auth) só autentica e-mail/senha; o cadastro de Membro é um formulário totalmente separado, sem nenhum campo real de e-mail — `resolveRoleFromMinistries(email)` busca o Membro via `memberDao.getMemberByEmail(email)`, que na verdade consulta a coluna `socialMedia` (rotulada no formulário como **"Rede Social (@)"**). Só há vínculo se o texto digitado nesse campo bater exatamente com o e-mail de login. Além disso, `signUp()` em `AuthRepositoryImpl` cria o usuário no Supabase Auth mas **nunca cria nem vincula um registro de Membro** — então mesmo cadastrando um Membro como Diácono com o Histórico Ministerial preenchido, se o e-mail de login não estiver (exatamente) no campo "Rede Social (@)", a pessoa cai em `AppRole.VIEWER` e, ao tocar em "Chamada", é redirecionada para "Métricas" (regra já existente em `AppNavigation.kt` para o papel VIEWER).
- Já existe uma lógica parcial e inteligente: `AuthRepositoryImpl.resolveRoleFromMinistries()` já lê o histórico ministerial do membro (`ministry_history`) e mapeia texto livre do cargo para uma role:
  - contém "PASTOR", "ANCIÃO"/"ANCIAO", "ADMIN"/"ADM" → `AppRole.ADMIN`
  - contém "DIÁCONO"/"DIACONO", "LÍDER"/"LIDER", "DIRETOR", "COORDENADOR" → `AppRole.DIACONO`
  - qualquer outro caso → `AppRole.VIEWER`
- Só que o enum `AppRole` só tem `ADMIN, DIACONO, VIEWER, NONE` — **não existem papéis nomeados para Pastor/Ancião/Diácono separadamente**, todos caem em só duas categorias (ADMIN ou DIACONO), o que dificulta relatórios/auditoria e não corresponde exatamente ao que você quer (ADM do sistema, Pastor, Ancião e Diácono como categorias com "poder total").
- **A proteção de rota só existe na tela de Chamada** (`reception`, verificando `ADMIN || DIACONO`). As rotas `members_list`, `member_add`, `member_edit/{id}`, `event_create` **não têm nenhuma verificação de role** — hoje um usuário com role `VIEWER` consegue navegar (por deep link ou manipulando o NavController) para cadastro/edição de membros e criação de eventos, mesmo sem permissão visível na UI.
- A role só é resolvida a partir do **banco Room local** (`memberDao.getMemberByEmail` + `getMinistryHistoryByMemberId`). Se um Pastor/Ancião/Diácono fizer login em um aparelho novo, antes da primeira sincronização completa o Room estará vazio, `getMemberByEmail` retorna null, e a pessoa cai automaticamente em `VIEWER` — perdendo acesso até que a sincronização termine.
- A seção **"7. Atuação Ministerial"** do cadastro de membro (`MemberRegistrationScreen.kt`, função `MinistrySection`) já existe, mas usa listas **fixas e genéricas** que não correspondem à estrutura oficial da igreja: `ministryOptions` tem só 8 itens fictícios ("Louvor e Adoração", "Ensino e Discipulado", etc.) e `roleOptions` tem 6 cargos genéricos ("Líder de Ministério", "Vice-Líder", "Integrante"...). Além disso, o campo `ministryId` é preenchido com o próprio nome do ministério (`ministryId = name`) — **não existe um catálogo de ministérios de verdade**, é só texto livre.
- `EventEntity` já tem os campos `ministryId` e `creatorEmail` (hoje sem nenhuma regra de permissão usando eles) e `EventType` já tem o valor `ORDINARIO` — ou seja, a base para "evento pertence a um ministério" e "distinguir culto ordinário de outros eventos" já existe, só falta a lógica de permissão em cima disso.
- Ainda não existe nenhum módulo de Tesouraria no app (nenhuma tela, entidade ou rota) — é uma funcionalidade nova a ser criada.

### 3.0 — Prompt: cadastro pela Secretaria com senha temporária, login por e-mail ou celular, e vínculo sólido com o Membro

> Este bloco substitui e amplia a ideia original de "só adicionar um campo de e-mail": agora o fluxo de acesso passa a ser sempre iniciado pela Secretaria (ou por quem tiver `hasFullAccess`), com senha temporária e login por e-mail OU celular — mais adequado para uma igreja, onde nem todo membro tem/lembra e-mail.

### 3.0.1 — Prompt: campos de contato reais em Membro (base para tudo)
```
No projeto Koinonia-v3, adicione os campos de contato necessários para vincular um Membro a uma conta de login, sem depender mais do campo "Rede Social (@)" para isso:

1. Em MemberEntity (tabela "members"), adicione: email: String? (e-mail real do membro, campo próprio, não reaproveite socialMedia) e authUserId: String? (UUID do usuário no Supabase Auth). O campo phone já existe na entidade — reaproveite-o normalmente para login por celular, apenas garanta que ele seja normalizado (somente dígitos, com DDI/DDD) antes de salvar, para evitar problemas de comparação depois.

2. Adicione índices únicos (ignorando nulos) para email, phone e authUserId em MemberEntity, para impedir que dois Membros fiquem vinculados à mesma conta ou aos mesmos dados de contato.

3. Em MemberRegistrationScreen.kt, adicione um campo real "E-mail" na seção de dados de contato do formulário (separado do campo existente "Rede Social (@)", que continua sendo só para rede social). Deixe claro na label que e-mail e celular são os dados usados para login. Nem e-mail nem celular precisam ser obrigatórios no cadastro básico do Membro — mas pelo menos um dos dois passa a ser obrigatório apenas quando a Secretaria optar por gerar acesso ao app (ver prompt 3.0.2).

4. Crie uma migração Room para os dados já existentes: para cada Membro cujo campo socialMedia já contenha um valor no formato de e-mail válido, copie esse valor para o novo campo email (mantendo socialMedia como está).
```

### 3.0.2 — Prompt: pré-cadastro com acesso ao app pela Secretaria (senha temporária)
```
No projeto Koinonia-v3, implemente a criação de acesso ao app por quem tem hasFullAccess (ADM, Pastor, Ancião ou Diácono) no momento do cadastro (ou edição) de um Membro — pensado para membros recém-batizados ou recém-transferidos de outra unidade:

1. Na tela de cadastro/edição de Membro (MemberRegistrationScreen.kt), adicione uma seção opcional "Criar acesso ao app" com um botão/switch, visível apenas para quem tem hasFullAccess. Ao ativar, exija que pelo menos um dos dois campos esteja preenchido: E-mail ou Celular (o campo phone existente).

2. Ao confirmar, no ViewModel/repositório correspondente: gere uma senha temporária aleatória seguras (ex.: 8 a 10 caracteres, letras e números). Determine o e-mail a ser usado no Supabase Auth: se o Membro tiver um e-mail real preenchido, use-o diretamente; se só tiver celular, gere um "e-mail técnico" interno a partir do número normalizado (ex.: "<celular_normalizado>@membros.koinonia.app"), usado apenas internamente pelo Supabase Auth — nunca exibido ao usuário final.

3. Crie a conta no Supabase Auth (supabaseClient.auth.signUpWith(Email) com esse e-mail e a senha temporária), vincule authUserId ao Membro (do prompt 3.0.1), e grave uma flag mustChangePassword = true (adicione esse campo em MemberEntity ou, preferencialmente, em uma tabela remota "user_roles"/"user_access" já existente no Supabase, mantendo uma cópia local em cache).

4. Depois de criada a conta, mostre a senha temporária em tela UMA ÚNICA VEZ para a Secretaria, com um aviso claro (ex.: "Anote e informe esta senha ao membro com segurança — ela não será mostrada novamente") e um botão de copiar. Não envie a senha automaticamente por e-mail/SMS nesta etapa (fora do escopo atual); a comunicação com o membro é feita manualmente pela Secretaria.

5. Crie uma nova tela CreatePermanentPasswordScreen.kt: no login, se mustChangePassword for true para o usuário autenticado, redirecione obrigatoriamente para essa tela antes de liberar qualquer outra navegação (mesmo guard clause usado hoje para currentRole == NONE em AppNavigation.kt). Nela, peça a nova senha (com confirmação), chame supabaseClient.auth.updateUser para trocar a senha, grave mustChangePassword = false, e só então libere a navegação normal para a Agenda.
```

### 3.0.3 — Prompt: login por e-mail ou número de celular
```
No projeto Koinonia-v3, permita que o login (LoginScreen.kt / AuthViewModel.kt / AuthRepositoryImpl.kt) aceite tanto e-mail quanto número de celular como identificador:

1. Troque o campo único de "E-mail" da tela de login por um campo "E-mail ou celular", aceitando os dois formatos.

2. Antes de chamar supabaseClient.auth.signInWith(Email), detecte o formato do que foi digitado: se for um e-mail válido, use-o diretamente. Se for um número de celular (só dígitos, com tamanho compatível), normalize-o da mesma forma usada no cadastro (prompt 3.0.1) e busque o Membro correspondente por telefone (nova query memberDao.getMemberByPhone(phone) no MemberDao) para descobrir qual e-mail (real ou técnico, criado no prompt 3.0.2) está de fato associado à conta no Supabase Auth, e use esse e-mail resolvido na chamada de login.

3. Se o número de celular não for encontrado em nenhum Membro local, tente um fallback consultando o Supabase diretamente (Postgrest) pela mesma lógica de e-mail técnico "<celular_normalizado>@membros.koinonia.app", antes de retornar erro — cobrindo o caso de um aparelho novo ainda sem sincronização local completa.

4. Mantenha o fluxo de "Esqueci minha senha" (ForgotPasswordScreen.kt) funcionando também a partir de celular, usando a mesma resolução de e-mail associado antes de chamar resetPasswordForEmail.
```

### 3.0.4 — Prompt: ajustar o papel de "Visitante do app" (autocadastro público)
```
No projeto Koinonia-v3, agora que o acesso principal passa a ser sempre criado pela Secretaria (prompt 3.0.2), trate o autocadastro público (alguém que baixa o app na loja e se cadastra sozinho, sem ser vinculado a nenhum Membro) como um "Visitante do app", com acesso somente de leitura à Agenda pública:

1. Mantenha o comportamento atual de quem se autocadastra (via signUp, sem Membro correspondente) caindo em AppRole.VIEWER.

2. Em AppNavigation.kt, troque o destino do redirecionamento forçado para VIEWER: hoje, ao tentar entrar em "reception", o VIEWER é mandado para "reports" (Métricas) — troque para "calendar" (Agenda), já que dados internos de frequência/métricas não devem ficar visíveis a quem não é membro vinculado. Aplique a mesma regra para qualquer outra rota administrativa protegida (members_list, event_create, reports) — VIEWER sempre cai em "calendar".

3. Não confunda esse "Visitante do app" (conta de autocadastro sem vínculo a nenhum Membro, tratada aqui) com a entidade VisitorEntity já existente no projeto (usada para registrar visitantes presenciais durante a Chamada de um culto) — são conceitos diferentes e não devem ser unificados nesta etapa.
```

### 3.1 — Prompt: catálogo oficial de Ministérios e Cargos
```
No projeto Koinonia-v3, substitua as listas fixas e genéricas de ministérios/cargos usadas hoje em MemberRegistrationScreen.kt (função MinistrySection: ministryOptions e roleOptions) por um catálogo real baseado na estrutura oficial de Ministérios e Departamentos da Igreja Adventista do Sétimo Dia (conforme Manual da Igreja, edição 2025, e a Divisão Sul-Americana):

1. Crie uma nova entidade Room MinistryEntity (tabela "ministries") como catálogo, com os campos:
   id: String, name: String, parentMinistryId: String? (para subcategorias, ex.: Aventureiros/Desbravadores pertencem ao Ministério Jovem Adventista), minAge: Int?, maxAge: Int?, minMembershipMonths: Int? (regra dos 6 meses de batismo para quem atua com crianças/menores), notes: String? (observações como restrições de sexo/estado civil quando aplicável).

2. Popule (seed, via Room pre-population ou uma migração) o catálogo com os seguintes ministérios e subministérios (use os nomes como name, e parentMinistryId para as subcategorias do item 1):
   1. Ministério Jovem Adventista (MJA) — ministério-pai
      1.1 Aventureiros (minAge 6, maxAge 9)
      1.2 Desbravadores (minAge 10, maxAge 15)
      1.3 Ministério de Embaixadores (minAge 16, maxAge 21)
      1.4 Ministério de Jovens Adultos (minAge 22, maxAge 30)
      1.5 Ministério de Universitários (minAge 16, maxAge null)
   2. Ministério da Criança (minMembershipMonths 6)
   3. Ministério do Adolescente (minAge 13, maxAge 16)
   4. Escola Sabatina
   5. Ministério Pessoal
      5.1 Sociedade de Homens Adventistas
      5.2 Classe Bíblica
   6. Ação Solidária Adventista (ASA / Dorcas)
   7. Ministério da Família
   8. Ministério da Mulher
   9. Ministério Adventista das Possibilidades (MAP) e Ministério de Surdos
   10. Ministério de Saúde e Temperança
   11. Ministério da Música
   12. Ministério de Mordomia Cristã
   13. Ministério de Publicações
   14. Comunicação
   15. Assuntos Públicos e Liberdade Religiosa
   16. Educação
   17. Escritos do Espírito de Profecia
   18. Ministério da Recepção
   19. Diaconato (mantenha este, já usado hoje para localizar Diáconos/Diaconisas)

3. Defina um conjunto controlado de cargos possíveis (reaproveitável para qualquer ministério), com um enum MinistryPositionTier { DIRECTOR, TREASURY, SUPPORT }, e associe cada opção de cargo do dropdown a um tier:
   - DIRECTOR: "Diretor(a)", "Diretor(a) Associado(a) / Vice-Diretor(a)", "Coordenador(a)", "Líder"
   - TREASURY: "Tesoureiro(a)", "Secretário(a)-Tesoureiro(a)", "Secretário(a)-Tesoureiro(a) Associado(a)"
   - SUPPORT: "Secretário(a)", "Secretário(a) Associado(a)", "Conselheiro(a)", "Instrutor(a)", "Professor(a)", "Diretor(a) de Música", "Pianista/Organista", "Músico(a)", "Diácono / Diaconisa", "Membro da Comissão/Conselho", "Colportor(a)-Evangelista", "Bibliotecário(a)"

4. Atualize MinistryHistoryEntity (tabela "ministry_history") para que ministryId passe a ser uma referência real ao id de MinistryEntity (e não mais o nome do ministério como hoje), mantendo ministryName apenas como cópia de exibição/cache. Ajuste MinistrySection em MemberRegistrationScreen.kt para carregar as opções de ministério e cargo a partir desse catálogo (via ViewModel) em vez das listas fixas atuais.

5. Preserve os dados já salvos: escreva uma migração Room que tente casar o texto livre já gravado em ministryName/role com os itens do novo catálogo (por aproximação de texto, como resolveRoleFromMinistries já faz), sem apagar histórico não reconhecido — mantenha-o como registro "legado" visível, mas sinalizado para revisão manual.
```

### 3.2 — Prompt: papéis globais, Tesouraria e Diretores de Ministério
```
No projeto Koinonia-v3, expanda o modelo de permissões para cobrir Tesouraria e o poder de Diretores sobre seus próprios ministérios, mantendo o que já existe hoje (ADM, Pastor, Ancião e Diácono com poder total):

1. Em AppRole.kt, expanda o enum para os papéis globais nomeados:
   enum class AppRole { ADMIN, PASTOR, ANCIAO, DIACONO, TESOUREIRO, VIEWER, NONE }
   Adicione duas propriedades/funções de extensão:
   - hasFullAccess: true para ADMIN, PASTOR, ANCIAO e DIACONO (poder total, como já definido antes) — false para os demais.
   - hasTreasuryAccess: true para ADMIN, PASTOR, ANCIAO e TESOUREIRO. Note que DIACONO NÃO entra aqui — acesso à Tesouraria é restrito a Tesoureiro(a), Pastor, Ancião e ADM, mesmo o Diácono tendo "poder total" nas demais áreas do sistema.

2. Em AuthRepositoryImpl.kt, ajuste resolveRoleFromMinistries(email) para: (a) continuar identificando PASTOR, ANCIAO, ADMIN, DIACONO como antes, agora retornando o AppRole específico de cada um; (b) reconhecer cargos cujo texto contenha "TESOUREIRO" (usando o catálogo de cargos do prompt 3.1, tier TREASURY) fora de qualquer ministério específico de liderança geral como AppRole.TESOUREIRO, quando a pessoa não tiver nenhum dos papéis de poder total.

3. Crie uma nova função/consulta (ex.: em MemberDao ou um novo caso de uso GetMinistryDirectorshipsUseCase) que retorne, para o membro logado, a lista de ministérios em que ele é Diretor(a) ou Diretor(a) Associado(a)/Vice-Diretor(a) (histórico ministerial ativo, endDate == null, com cargo de tier DIRECTOR do catálogo do prompt 3.1). Trate Diretor e Diretor Associado/Vice-Diretor exatamente com o mesmo nível de permissão (ambos têm controle total sobre o ministério, conforme solicitado). Exponha essa lista como um StateFlow<List<MinistryDirectorship>> (data class com ministryId e ministryName) no AuthRepositoryImpl/AuthViewModel, disponível junto com currentUserRole.

4. Documente claramente (em comentário no código) que este é um controle de acesso em duas camadas: (a) papel global (AppRole), que dá poder total ou acesso a Tesouraria; (b) diretoria de ministério (lista de MinistryDirectorship), que dá poder apenas sobre os eventos do(s) ministério(s) que a pessoa dirige, independentemente do seu AppRole global.
```

### 3.3 — Prompt: Diretores controlando os próprios eventos, com proteção dos Cultos Ordinários
```
No projeto Koinonia-v3, implemente a regra de que cada Diretor (ou Diretor Associado/Vice) tem controle total sobre a criação, edição e exclusão dos eventos do(s) ministério(s) que dirige, exceto quando o horário conflita com um Culto Ordinário:

1. Crie uma função de permissão (ex.: em um novo arquivo EventPermissions.kt) canManageEvent(event: EventEntity?, targetMinistryId: String?, targetStartTime: Date?, targetEndTime: Date?, currentRole: AppRole, directedMinistries: List<MinistryDirectorship>, hasOrdinaryConflict: Boolean): Boolean com a regra:
   - Se currentRole.hasFullAccess (ADMIN, PASTOR, ANCIAO ou DIACONO) → sempre true, inclusive para eventos do tipo ORDINARIO ou que conflitem com um.
   - Senão, se a pessoa for Diretor(a)/Vice de algum ministério (directedMinistries não vazio) E o evento (existente ou a ser criado) pertencer a um desses ministryId → true, DESDE QUE hasOrdinaryConflict seja false.
   - Se houver conflito com um Culto Ordinário (hasOrdinaryConflict = true) e a pessoa não tiver hasFullAccess → false, mesmo sendo Diretor do ministério.
   - Em qualquer outro caso → false.

2. Implemente a verificação de conflito com Culto Ordinário: antes de salvar um evento criado/editado por um Diretor sem hasFullAccess, consulte os eventos do tipo EventType.ORDINARIO já cadastrados (ex.: via EventDao) e verifique se a data e a faixa de horário (startTime/endTime) do novo evento coincide com algum deles. Se coincidir, bloqueie o salvamento e mostre uma mensagem clara ao usuário, por exemplo: "Este horário coincide com um Culto Ordinário. Apenas ADM, Pastor ou Ancião podem agendar eventos nesse horário." — não é necessário criar um fluxo de solicitação/aprovação por enquanto, o bloqueio direto já atende ao requisito (podemos evoluir para um fluxo de solicitação de autorização depois, se fizer sentido).

3. Aplique canManageEvent na tela EventRegistrationScreen.kt / EventRegistrationViewModel (verifique o arquivo correspondente ao ViewModel de criação/edição de eventos): esconda ou desabilite o botão de salvar, e ao tentar editar/excluir um evento existente pela Agenda (CalendarScreen.kt), só mostre as ações de editar/excluir se canManageEvent retornar true para aquele evento específico.

4. Ao criar um novo evento, se quem está criando não tiver hasFullAccess, associe automaticamente o ministryId do evento a um dos ministérios que a pessoa dirige (se ela dirigir mais de um, peça para selecionar em qual ministério o evento se enquadra, restringindo as opções apenas aos ministérios que ela dirige).
```

### 3.4 — Prompt: acesso restrito ao futuro módulo de Tesouraria
```
No projeto Koinonia-v3, prepare a estrutura de acesso para o módulo de Tesouraria, que ainda será construído em detalhe futuramente. Por enquanto:

1. Adicione uma nova rota reservada "treasury" em AppNavigation.kt com uma tela placeholder simples (ex.: "Módulo em construção"), já protegida por um guard clause usando currentRole.hasTreasuryAccess (do prompt 3.2) — apenas TESOUREIRO, PASTOR, ANCIAO e ADMIN podem acessar; qualquer outro papel deve ser redirecionado, do mesmo jeito que já acontece hoje na rota "reception" para quem não tem permissão.

2. Adicione um item de menu/drawer "Tesouraria" visível apenas quando currentRole.hasTreasuryAccess for true.

3. Não implemente ainda telas de lançamentos financeiros, relatórios ou integrações — isso deve ser tratado como um pedido separado e detalhado quando as regras de negócio da Tesouraria (categorias de lançamento, relatórios, quem pode aprovar despesas, etc.) forem definidas. O objetivo agora é apenas garantir que a trava de acesso já exista pronta para quando o módulo for construído.
```

### 3.5 — Prompt: esconder completamente a aba Métricas (e demais atalhos restritos) de quem não tem acesso total
```
No projeto Koinonia-v3, em AppNavigation.kt, ajuste a bottom bar e o menu lateral (drawer) para que abas e itens restritos fiquem completamente invisíveis para quem não tem permissão — não apenas bloqueados ao tocar. O objetivo é que um Visitante do app ou um membro comum sem função de liderança nem saiba que essas funcionalidades existem, evitando desconforto do tipo "por que fulano sabe a que horas eu cheguei ao culto?":

1. Na NavigationBar (bottom bar), mostre os NavigationBarItem "Chamada" e "Métricas" apenas quando currentRole.hasFullAccess for true. Para quem não tem hasFullAccess (VIEWER, ou um Membro comum sem nenhum papel de poder total), a bottom bar deve mostrar somente "Agenda" — avalie se, nesse caso, faz mais sentido exibir a bottom bar com um único item ou ocultá-la inteiramente, já que só resta uma aba.

2. No ModalDrawerSheet (menu lateral "Secretaria"), mostre o item "Membros" apenas quando currentRole.hasFullAccess for true, e o item "Tesouraria" (do prompt 3.4) apenas quando currentRole.hasTreasuryAccess for true. Se, para o usuário atual, nenhum item do menu lateral for aplicável, oculte também o botão/ícone que abre esse menu.

3. Essa ocultação visual não substitui as proteções de rota (guard clauses) já previstas nos prompts anteriores para "reception", "members_list", "event_create" e "treasury" — mantenha-as, pois continuam sendo a proteção real contra deep links ou navegação forçada; a ocultação de UI é apenas a segunda camada, para não expor a existência dessas funcionalidades a quem não deveria nem saber delas.

4. Confirme que um Diretor de ministério sem nenhum papel de poder total (do prompt 3.2/3.3) também não vê "Chamada" nem "Métricas" na bottom bar — ele só deve enxergar a Agenda (de onde consegue gerenciar os eventos do próprio ministério, conforme já implementado no prompt 3.3).
```

---

## 4) Outros pontos encontrados na revisão geral (para corrigir quando possível)

- **`Screen.kt`** define um `sealed class` de rotas que **não é usado em nenhum lugar** — `AppNavigation.kt` usa strings soltas ("calendar", "reception", etc). Isso é uma fonte de bugs por digitação (ex.: erro de string não seria pego em tempo de compilação). Vale migrar `AppNavigation.kt` para usar as constantes de `Screen.kt`.
- **`MemberEntity.toDomain()`/`fromDomain()`** usa o campo `socialMedia` como um "email temporário" e `vehicleType` como um "role temporário" (comentado no próprio código como "campo de apoio provisório"/"usado para mapear a função"). Isso é frágil — o ideal é ter campos `email` e `role` dedicados na entidade em vez de reaproveitar colunas com propósito diferente.
- **`AttendanceEntity.fromDomain()`** usa `lateDurationMins = if (isLate) 15 else 0` — um valor fixo de teste ("valor default de teste", conforme comentário no código), que deveria vir do cálculo real (`CalculateLateTimeUseCase`/`TimeManager`) e não de um número fixo.
- **`AuthRepositoryImpl`** usa `GlobalScope.launch` (marcado como `@DelicateCoroutinesApi`), o que é um anti-padrão: esse escopo nunca é cancelado e pode causar vazamento de corrotinas. Trocar por um escopo de aplicação gerenciado pelo Hilt (já mencionado no prompt 2).
- **`android:allowBackup="true"`** no manifesto, junto com CPF/RG/endereço no banco local — considere desativar ou restringir o backup automático (ver seção de segurança acima).
- Revise se as políticas de **RLS no Supabase** cobrem todas as tabelas sensíveis antes de deixar o app em produção com dados reais de membros.

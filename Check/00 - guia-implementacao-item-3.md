# 🎯 Guia de Implementação e Validação — Item 3 Koinonia-v3

**Objetivo:** Verificar se o projeto Koinonia-v3 implementou corretamente os 6 subitens de segurança e controle de acesso descritos no documento de análise.

---

## 📚 Documentos Fornecidos

Você recebeu **3 documentos** complementares:

### 1️⃣ **analise-item-3-koinonia.md** (Detalhado)
- **Descrição:** Análise completa de cada subitem (3.1 a 3.6)
- **Uso:** Leitura de contexto, entendimento profundo dos requisitos
- **Seções principais:**
  - Requisitos obrigatórios para cada subitem
  - Arquivos críticos a examinar
  - Problemas conhecidos
  - Explicação do motivo de cada requisito

### 2️⃣ **checklist-item-3-koinonia.md** (Executável)
- **Descrição:** Checklist prático com caixas de seleção
- **Uso:** Ir marcando ☑️ conforme encontra cada requisito no código
- **Vantagens:**
  - Estruturado por subitem
  - Fácil de seguir
  - Pode ser exportado/compartilhado
  - Matriz de pontuação final

### 3️⃣ **guia-implementacao-item-3.md** (Este arquivo)
- **Descrição:** Mapa de jogo — como usar os outros dois e próximos passos
- **Uso:** Roteiro executivo, planejamento e delegação

---

## 🚀 Como Começar

### Passo 1: Preparação (5 min)
```bash
# Clone o repositório (se privado, peça acesso)
git clone https://github.com/souzarte-linux/Koinonia-v3.git
cd Koinonia-v3

# Abra em sua IDE (Android Studio, IntelliJ)
# Selecione a pasta raiz do projeto
```

### Passo 2: Leitura de Contexto (15 min)
```
1. Leia o arquivo "analise-item-3-koinonia.md"
   - Foco inicial: seção "📌 Resumo Executivo" (1 min)
   - Depois, leia cada subitem (3.1 a 3.6) em ordem
   - Anote dúvidas ou termos desconhecidos
```

### Passo 3: Verificação Prática (30-60 min)
```
1. Abra o arquivo "checklist-item-3-koinonia.md"
2. Para cada subitem (na ordem):
   a. Localize os arquivos esperados listados
   b. Abra-os no IDE (Ctrl+Shift+O ou Cmd+Shift+O)
   c. Procure pelos padrões de código descritos
   d. Marque ☑️ cada item encontrado
   e. Anote linha de código ou screenshot
3. Repita para todos os 6 subitens
```

### Passo 4: Consolidação (15 min)
```
1. Calcule a pontuação final (secção "🎯 Resumo")
2. Crie um relatório com:
   - Subitem | Status | Notas
   - (veja template abaixo)
3. Se faltar algo, liste em "Ações Necessárias"
```

---

## 🔎 Técnicas de Busca no IDE

### Android Studio / IntelliJ IDEA

**Buscar arquivo por nome:**
```
Ctrl+Shift+N (Windows/Linux)
Cmd+Shift+O (Mac)
Digita: "AppRole.kt"
```

**Buscar texto em arquivo:**
```
Ctrl+F (Windows/Linux)
Cmd+F (Mac)
Digita: "hasFullAccess"
```

**Buscar em toda a codebase:**
```
Ctrl+Shift+F (Windows/Linux)
Cmd+Shift+F (Mac)
Digita: "hasFullAccess"
→ Mostra todas as ocorrências no projeto
```

**Navegar para definição:**
```
Coloque cursor no símbolo
Ctrl+Click (Windows/Linux)
Cmd+Click (Mac)
→ Abre a definição da classe/função
```

**Ver hierarquia de classes:**
```
Clique em "AppRole" (enum)
Ctrl+Alt+H (Windows/Linux)
Cmd+Alt+H (Mac)
→ Mostra quem estende/implementa
```

---

## 📋 Mapa de Arquivos por Subitem

Use este mapa para saber exatamente **onde procurar**:

### **Item 3.1 — AppRole e Propriedades**
```
📁 domain/
  📁 model/
    ✓ AppRole.kt           ← Enum dos papéis
    ✓ AppRole+Ext.kt       ← Extensões hasFullAccess, hasTreasuryAccess
  📁 (ou) enums/
    ✓ AppRole.kt           ← Alternativa

📁 data/
  📁 repository/
    ✓ AuthRepositoryImpl.kt ← Resolução de papel
```

### **Item 3.2 — Guard Clauses em AppNavigation**
```
📁 presentation/
  📁 navigation/
    ✓ AppNavigation.kt     ← Guard clauses principais
    ✓ Screen.kt            ← Rotas definidas
  📁 features/
    📁 unauthorized/
      ✓ UnauthorizedScreen.kt ← Tela de acesso negado
    📁 member/
      ✓ MemberAddScreen.kt    ← Tela "Adicionar Membro"
      ✓ MemberEditScreen.kt   ← Tela "Editar Membro"
    📁 event/
      ✓ EventRegistrationScreen.kt ← Tela "Novo Evento"
    📁 reception/
      ✓ ReceptionScreen.kt ← Tela "Chamada"
```

### **Item 3.3 — Controle de Eventos**
```
📁 domain/
  📁 model/
    ✓ EventEntity.kt       ← Tipo, ministryId, data/hora
  📁 repository/
    ✓ EventRepository.kt   ← Interface (se existe)
  📁 usecase/
    ✓ CanManageEventUseCase.kt (ou similar) ← Lógica canManageEvent
    ✓ CreateEventUseCase.kt ← Cria evento com verificação

📁 data/
  📁 local/
    📁 dao/
      ✓ EventDao.kt        ← Consultas ao Room
  📁 repository/
    ✓ EventRepositoryImpl.kt ← Implementação, conflito com ordinário

📁 presentation/
  📁 features/
    📁 event/
      ✓ EventRegistrationScreen.kt
      ✓ EventRegistrationViewModel.kt
    📁 calendar/
      ✓ CalendarScreen.kt  ← Botões condicionais
```

### **Item 3.4 — Tesouraria**
```
📁 presentation/
  📁 navigation/
    ✓ AppNavigation.kt     ← Rota "treasury" + guard clause
  📁 features/
    📁 treasury/
      ✓ TreasuryScreen.kt  ← Placeholder
    📁 components/
      ✓ ModalDrawerSheet.kt ou SideMenuDrawer.kt ← Item "Tesouraria"
```

### **Item 3.5 — Ocultação Visual**
```
📁 presentation/
  📁 components/
    ✓ NavigationBar.kt     ← Abas condicionais (Chamada, Métricas)
    ✓ ModalDrawerSheet.kt  ← Itens condicionais (Membros, Tesouraria)
  📁 navigation/
    ✓ AppNavigation.kt     ← Guard clauses (segunda camada)
```

### **Item 3.6 — Bootstrap Admin**
```
📁 Constants.kt (raiz src/)
  ✓ BOOTSTRAP_ADMIN_EMAIL = "cyber.souza@hotmail.com"

📁 data/
  📁 repository/
    ✓ AuthRepositoryImpl.kt ← resolveRoleFromMinistries()
  📁 local/
    📁 dao/
      ✓ MemberDao.kt       ← Verificar se existe admin ativo

📁 presentation/
  ✓ screens/
    ✓ HomeScreen.kt ou similar ← Aviso visual (Banner/Snackbar)
```

---

## 📊 Template de Relatório

Copie e complete após a verificação:

```markdown
# Relatório de Implementação — Item 3 Koinonia-v3

**Projeto:** Koinonia-v3  
**Data da Verificação:** [data]  
**Verificador:** [nome]  
**Duração:** [tempo]

---

## Resumo Executivo

- **Status Geral:** [ ] Completo | [ ] Parcial | [ ] Incompleto
- **Pontuação:** ___ / 70 pontos
- **Recomendação:** [ ] Pronto para produção | [ ] Revisão necessária | [ ] Refazimento urgente

---

## Item 3.1 — Autorização por Papel (AppRole)

| Requisito | Status | Evidência | Notas |
|-----------|--------|-----------|-------|
| Enum com 5 papéis | ✅/❌ | Arquivo: AppRole.kt, linha XXX | |
| hasFullAccess | ✅/❌ | Arquivo: AppRole+Ext.kt, linha XXX | |
| hasTreasuryAccess | ✅/❌ | Arquivo: AppRole+Ext.kt, linha XXX | |
| Estado LOADING | ✅/❌ | Arquivo: AuthRepositoryImpl.kt, linha XXX | |

**Status 3.1:** [ ] ✅ Completo | [ ] ⚠️ Parcial | [ ] ❌ Incompleto

---

## Item 3.2 — Autorização por Tela

| Tela | Verificado | Linha/Arquivo | Notas |
|------|-----------|---------------|-------|
| member_add | ✅/❌ | AppNavigation.kt:XXX | |
| member_edit | ✅/❌ | AppNavigation.kt:XXX | |
| event_create | ✅/❌ | AppNavigation.kt:XXX | |
| reception | ✅/❌ | AppNavigation.kt:XXX | |
| UnauthorizedScreen | ✅/❌ | UnauthorizedScreen.kt | |

**Status 3.2:** [ ] ✅ Completo | [ ] ⚠️ Parcial | [ ] ❌ Incompleto

---

## Item 3.3 — Controle de Eventos

| Requisito | Status | Arquivo/Linha | Notas |
|-----------|--------|---------------|-------|
| canManageEvent() | ✅/❌ | EventRepository.kt:XXX | |
| Conflito Ordinário | ✅/❌ | EventRepositoryImpl.kt:XXX | |
| Msg de erro | ✅/❌ | EventRegistrationScreen.kt:XXX | |
| Associação ministério | ✅/❌ | CreateEventUseCase.kt:XXX | |
| Botões condicionais | ✅/❌ | CalendarScreen.kt:XXX | |
| Botão salvar desabilitado | ✅/❌ | EventRegistrationScreen.kt:XXX | |

**Status 3.3:** [ ] ✅ Completo | [ ] ⚠️ Parcial | [ ] ❌ Incompleto

---

## Item 3.4 — Tesouraria

| Requisito | Status | Arquivo/Linha | Notas |
|-----------|--------|---------------|-------|
| Rota "treasury" | ✅/❌ | AppNavigation.kt:XXX | |
| Guard clause | ✅/❌ | AppNavigation.kt:XXX | |
| TreasuryScreen | ✅/❌ | TreasuryScreen.kt | |
| Item drawer | ✅/❌ | ModalDrawerSheet.kt:XXX | |
| **Não tem** banco de dados | ✅/❌ | (confirmado: nenhuma entity/dao) | |

**Status 3.4:** [ ] ✅ Completo | [ ] ⚠️ Parcial | [ ] ❌ Incompleto

---

## Item 3.5 — Ocultação Visual

| Elemento | Visível para | Invisível para | Status |
|----------|-------------|----------------|--------|
| Agenda | Todos | — | ✅/❌ |
| Chamada | ADMIN, PASTOR, ANCIAO | VIEWER, Diretor | ✅/❌ |
| Métricas | ADMIN, PASTOR, ANCIAO | VIEWER, Diretor | ✅/❌ |
| Membros (drawer) | ADMIN, PASTOR, ANCIAO | VIEWER, Diretor | ✅/❌ |
| Tesouraria (drawer) | TESOUREIRO+ | VIEWER | ✅/❌ |
| Drawer vazio → invisível | Sim | | ✅/❌ |
| Guard clauses persistem | Sim (segunda camada) | | ✅/❌ |

**Status 3.5:** [ ] ✅ Completo | [ ] ⚠️ Parcial | [ ] ❌ Incompleto

---

## Item 3.6 — Bootstrap Admin

| Requisito | Status | Arquivo/Linha | Notas |
|-----------|--------|---------------|-------|
| BOOTSTRAP_ADMIN_EMAIL | ✅/❌ | Constants.kt:XXX | |
| Verificação lowercase | ✅/❌ | AuthRepositoryImpl.kt:XXX | |
| Verificação admin ativo | ✅/❌ | AuthRepositoryImpl.kt:XXX | |
| Auto-desativante | ✅/❌ | [testado] | |
| Aviso visual | ✅/❌ | HomeScreen.kt:XXX | |
| Mensagem clara | ✅/❌ | [texto verificado] | |
| Comentário documentação | ✅/❌ | AuthRepositoryImpl.kt:XXX | |
| Fluxo Fernando | ✅/❌ | [testado end-to-end] | |

**Status 3.6:** [ ] ✅ Completo | [ ] ⚠️ Parcial | [ ] ❌ Incompleto

---

## Pontuação Detalhada

```
Item 3.1 (10 pts) ... [ ] / 10
Item 3.2 (10 pts) ... [ ] / 10
Item 3.3 (15 pts) ... [ ] / 15
Item 3.4 (10 pts) ... [ ] / 10
Item 3.5 (10 pts) ... [ ] / 10
Item 3.6 (15 pts) ... [ ] / 15
─────────────────────────────
TOTAL (70 pts) ....... [ ] / 70
```

---

## Ações Necessárias

### Críticas (Impedem produção)
- [ ] [Descrição]
- [ ] [Descrição]

### Importantes (Recomendadas)
- [ ] [Descrição]
- [ ] [Descrição]

### Melhorias (Futuro)
- [ ] [Descrição]
- [ ] [Descrição]

---

## Assinatura

Verificado por: ________________  
Data: ________________  
Aprovação: [ ] Sim | [ ] Com ressalvas | [ ] Não
```

---

## ⚡ Verificação Rápida (10 min)

Se tem pressa, faça apenas isto:

```bash
# 1. Procure AppRole.kt
grep -r "enum class AppRole" src/

# 2. Procure AppNavigation.kt com guard
grep -r "hasFullAccess" src/ | grep -i "navigation\|guard"

# 3. Procure canManageEvent
grep -r "canManageEvent" src/

# 4. Procure BOOTSTRAP_ADMIN_EMAIL
grep -r "BOOTSTRAP_ADMIN_EMAIL" src/

# 5. Procure "treasury" route
grep -r "\"treasury\"" src/
```

**Se todos os greps retornarem resultados → Item 3 teve início**  
**Se algum não retornar → subitem faltando**

---

## 📞 Dúvidas Comuns

### P: Por que Item 3.6 (bootstrap) é tão importante?
**R:** Sem ele, é impossível fazer o primeiro cadastro de administrador — o app fica preso em "ninguém tem permissão para editar membros".

### P: Posso deixar Item 3.5 (ocultação visual) para depois?
**R:** Tecnicamente sim (guard clauses já protegem), mas **não** — expor funcionalidades secretas quebra UX. Priorize.

### P: Item 3.4 (Tesouraria placeholder) é realmente necessário?
**R:** Sim. Prepara a arquitetura. Quando o time de Tesouraria estiver pronto, é só completar a tela e adicionar banco.

### P: E se o repositório for privado?
**R:** Peça acesso ao proprietário (cyber.souza@hotmail.com ou @souzarte-linux). Se privado, a verificação é mais segura.

### P: Posso fazer essa verificação remotamente (sem clonar)?
**R:** Não recomendado. A análise exige buscar em múltiplos arquivos e testar fluxos. Clone sempre.

---

## 🎓 Próxima Fase (Após Verificação)

Se **faltar implementação**, a sequência recomendada é:

1. **Item 3.1 (AppRole)** — Base de tudo, faça primeiro
2. **Item 3.6 (Bootstrap)** — Sem isso, não conseguem testar (crítico)
3. **Item 3.2 (Guard clauses)** — Proteção real, em seguida
4. **Item 3.5 (Ocultação visual)** — Melhora UX
5. **Item 3.3 (Eventos)** — Feature complexa, depois das bases
6. **Item 3.4 (Tesouraria)** — Últimas, é placeholder

---

## 📌 Resumo Final

| Fase | Duração | O Que Fazer |
|------|---------|------------|
| **Preparação** | 5 min | Clonar repo, abrir IDE |
| **Estudo** | 15 min | Ler analise-item-3-koinonia.md |
| **Verificação** | 30-60 min | Usar checklist-item-3-koinonia.md |
| **Consolidação** | 15 min | Preencher relatório template |
| **Total** | **60-95 min** | Verificação completa |

**Se acelerar:** use "Verificação Rápida (10 min)" acima para ter uma noção em poucos minutos.

---

**Bom trabalho! 🚀**


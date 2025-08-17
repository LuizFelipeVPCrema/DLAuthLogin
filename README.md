# DLAuthLogin - Plugin de Autenticação para Minecraft

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://java.com/)
[![Bukkit](https://img.shields.io/badge/Bukkit-1.13+-green.svg)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen.svg)](https://github.com/LuizFelipeVPCrema/DLAuthLogin/releases)

## 📋 Descrição

O **DLAuthLogin** é um plugin de autenticação avançado para servidores Minecraft que oferece um sistema completo de registro e login com múltiplas camadas de segurança. Desenvolvido para proteger seu servidor contra acessos não autorizados, o plugin inclui recursos como sessões persistentes, proteção contra força bruta, múltiplos idiomas e sistema de logs detalhado.

## ✨ Funcionalidades Principais

### 🔐 Sistema de Autenticação
- **Registro de contas** com validação de força de senha
- **Login seguro** com proteção contra força bruta
- **Sessões persistentes** para manter jogadores logados
- **Logout automático** quando o jogador sai do servidor
- **Alteração de senha** com validação da senha atual
- **Remoção de conta** com confirmação de senha

### 🛡️ Sistema de Proteção
- **Proteção de movimento** - Jogadores não logados não podem se mover
- **Proteção de interação** - Bloqueia quebra/colocação de blocos
- **Proteção de chat** - Impede uso do chat sem autenticação
- **Proteção de comandos** - Bloqueia comandos não autorizados
- **Proteção de dano** - Jogadores não logados não sofrem dano
- **Proteção de teleporte** - Impede teleportes não autorizados

### 🔧 Recursos Avançados
- **Múltiplos idiomas** (Português, Inglês, Espanhol)
- **Sistema de bypass** para administradores
- **Timeout de login** configurável
- **Bloqueio de conta** após múltiplas tentativas falhadas
- **Logs detalhados** de todas as ações
- **Configuração flexível** via arquivo YAML

### 🎨 Interface e UX
- **Mensagens coloridas** com suporte a códigos de cor
- **Placeholders dinâmicos** nas mensagens
- **Feedback visual** claro para o usuário
- **Comandos intuitivos** e bem documentados

## 🚀 Instalação

### Pré-requisitos
- **Java 8** ou superior
- **Bukkit/Spigot 1.13** ou superior
- **MySQL** (opcional, para banco de dados externo)

### Passos de Instalação

1. **Baixe o plugin**
   ```bash
   # Clone o repositório
   git clone https://github.com/LuizFelipeVPCrema/DLAuthLogin.git
   
   # Ou baixe o JAR da seção Releases
   ```

2. **Compile o projeto**
   ```bash
   cd DLAuthLogin
   mvn clean package
   ```

3. **Instale no servidor**
   - Copie o arquivo `target/dlauthlogin-1.0-SNAPSHOT.jar` para a pasta `plugins/` do seu servidor
   - Reinicie o servidor
   - O plugin criará automaticamente os arquivos de configuração

4. **Configure o plugin**
   - Edite `plugins/DLAuthLogin/config.yml` conforme suas necessidades
   - Configure o banco de dados se necessário
   - Ajuste as mensagens nos arquivos de idioma

## ⚙️ Configuração

### Configuração Principal (`config.yml`)

```yaml
# Configurações do Banco de Dados
database:
  type: "sqlite" # sqlite, mysql
  mysql:
    host: "localhost"
    port: 3306
    database: "dlauthlogin"
    username: "root"
    password: "password"

# Configurações de Segurança
security:
  password_strength: 3 # 1-6 (1=fraco, 6=muito forte)
  max_login_attempts: 5
  lockout_duration: 300 # segundos
  login_timeout: 60 # segundos
  session_timeout: 1440 # minutos (24 horas)

# Configurações de Proteção
protection:
  block_movement: true
  block_block_interaction: true
  block_entity_interaction: true
  block_item_use: true
  block_chat: true
  block_commands: true
  block_damage: true
  block_teleport: true

# Configurações de Log
logging:
  log_registrations: true
  log_login_attempts: true
  log_password_changes: true
  log_unregistrations: true
  log_admin_actions: true

# Configurações Gerais
general:
  default_language: "pt_BR"
  keep_session: true
  use_bcrypt: true
  bcrypt_rounds: 12
```

### Configuração de Mensagens

O plugin suporta múltiplos idiomas através de arquivos YAML:

- `messages_pt_BR.yml` - Português Brasileiro
- `messages_en_US.yml` - Inglês
- `messages_es_ES.yml` - Espanhol

Exemplo de mensagem:
```yaml
login:
  success: "&a✅ Login realizado com sucesso! Bem-vindo(a) ao servidor!"
  failed: "&c❌ Senha incorreta! Tentativa {attempt}/{max_attempts}"
  usage: "&e📝 Uso: /login <senha>"
```

## 📝 Comandos

### Comandos de Jogador

| Comando | Descrição | Permissão |
|---------|-----------|-----------|
| `/register <senha> <confirmar>` | Registra uma nova conta | `dlauthlogin.register` |
| `/login <senha>` | Faz login na conta | `dlauthlogin.login` |
| `/changepassword <atual> <nova> <confirmar>` | Altera a senha | `dlauthlogin.changepassword` |
| `/unregister <senha>` | Remove a conta | `dlauthlogin.unregister` |

### Comandos de Administrador

| Comando | Descrição | Permissão |
|---------|-----------|-----------|
| `/dlauthlogin reload` | Recarrega a configuração | `dlauthlogin.admin` |
| `/dlauthlogin status` | Mostra status do sistema | `dlauthlogin.admin` |
| `/dlauthlogin clearsessions` | Limpa todas as sessões | `dlauthlogin.admin` |
| `/dlauthlogin testpassword <senha>` | Testa força de senha | `dlauthlogin.admin` |
| `/dlauthlogin debug <jogador>` | Debug de jogador | `dlauthlogin.admin` |
| `/dlauthlogin testcolors` | Testa sistema de cores | `dlauthlogin.admin` |

## 🔑 Permissões

### Permissões de Jogador
- `dlauthlogin.register` - Permite registrar conta
- `dlauthlogin.login` - Permite fazer login
- `dlauthlogin.changepassword` - Permite alterar senha
- `dlauthlogin.unregister` - Permite remover conta

### Permissões de Administrador
- `dlauthlogin.admin` - Acesso total aos comandos admin
- `dlauthlogin.bypass` - Bypass de todas as proteções

### Permissões de Log
- `dlauthlogin.log.view` - Visualizar logs
- `dlauthlogin.log.clear` - Limpar logs

## 🎯 Como Funciona

### Fluxo de Autenticação

1. **Jogador entra no servidor**
   - Sistema verifica se já está registrado
   - Se não registrado: solicita registro
   - Se registrado: solicita login

2. **Registro**
   - Jogador usa `/register <senha> <confirmar>`
   - Sistema valida força da senha
   - Cria conta no banco de dados
   - Remove proteções automaticamente

3. **Login**
   - Jogador usa `/login <senha>`
   - Sistema verifica credenciais
   - Cria sessão se habilitado
   - Remove proteções automaticamente

4. **Sessão**
   - Se habilitada, mantém jogador logado
   - Expira após tempo configurado
   - Pode ser estendida automaticamente

### Sistema de Proteção

O plugin protege jogadores não autenticados de:

- **Movimento**: Impede que se movam pelo mundo
- **Interação**: Bloqueia quebra/colocação de blocos
- **Chat**: Impede uso do chat
- **Comandos**: Bloqueia comandos não autorizados
- **Dano**: Impede que sofram dano
- **Teleporte**: Bloqueia teleportes

### Sistema de Segurança

- **Força de senha**: Validação configurável (1-6 níveis)
- **Proteção contra força bruta**: Bloqueio após tentativas falhadas
- **Timeout de login**: Expulsão após tempo limite
- **Logs detalhados**: Registro de todas as ações
- **Criptografia**: Senhas criptografadas com BCrypt

## 🧪 Demonstração

### Cenário 1: Primeiro Acesso
```
[Servidor] ⚠️ Você precisa se registrar para usar o servidor!
[Servidor] 📝 Uso: /register <senha> <confirmar_senha>
[Jogador] /register MinhaSenha123 MinhaSenha123
[Servidor] ✅ Conta registrada com sucesso! Use /login <senha> para entrar.
[Jogador] /login MinhaSenha123
[Servidor] ✅ Login realizado com sucesso! Bem-vindo(a) ao servidor!
```

### Cenário 2: Retorno ao Servidor
```
[Servidor] ✅ Sessão restaurada! Bem-vindo(a) de volta!
```

### Cenário 3: Tentativa de Login Falhada
```
[Jogador] /login SenhaErrada
[Servidor] ❌ Senha incorreta! Tentativa 1/5
[Jogador] /login SenhaErrada
[Servidor] ❌ Senha incorreta! Tentativa 2/5
...
[Jogador] /login SenhaErrada
[Servidor] 🔒 Conta bloqueada por 300 segundos devido a múltiplas tentativas falhadas.
```

### Cenário 4: Tentativa de Ação sem Login
```
[Jogador] Tenta quebrar bloco
[Servidor] ❌ Você precisa se autenticar antes de interagir!
[Jogador] Tenta usar chat
[Servidor] ❌ Você precisa se autenticar antes de usar o chat!
[Jogador] Tenta se mover
[Servidor] ❌ Você precisa se autenticar antes de se mover!
```

## 🔧 Comandos de Debug

### Teste de Força de Senha
```
/dlauthlogin testpassword MinhaSenha123
```
**Saída:**
```
=== Teste de Força de Senha ===
Senha: ************
Força atual: 4/6
Força necessária: 3/6
Status: ✅ Aceita

=== Detalhes ===
- Letras: ✅
- Números: ✅
- 8+ caracteres: ✅
- Maiúsculas + Minúsculas: ✅
- Caracteres especiais: ❌
- 12+ caracteres: ❌
```

### Status do Sistema
```
/dlauthlogin status
```
**Saída:**
```
=== Status do DLAuthLogin ===
Jogadores logados: 5
Jogadores protegidos: 2
Sessões ativas: 3

=== Jogadores Online ===
- Player1: ✅ Logado
- Player2: ⚠️ Registrado mas não logado
- Player3: ❌ Não registrado
- Admin1: 🔓 Bypass
```

## 📊 Logs e Monitoramento

O plugin gera logs detalhados para:

- **Registros de conta**
- **Tentativas de login** (sucesso/falha)
- **Alterações de senha**
- **Remoções de conta**
- **Ações administrativas**
- **Eventos de segurança**

Exemplo de log:
```
[2024-01-15 10:30:15] [INFO] [LOG] Player1 registrou uma nova conta
[2024-01-15 10:30:20] [INFO] [LOG] Player1 fez login com sucesso
[2024-01-15 10:35:10] [WARN] [SECURITY] Player2 falhou no login (tentativa 3/5)
[2024-01-15 10:35:15] [WARN] [SECURITY] Player2 falhou no login (tentativa 4/5)
[2024-01-15 10:35:20] [WARN] [SECURITY] Player2 falhou no login (tentativa 5/5)
[2024-01-15 10:35:20] [WARN] [SECURITY] Conta de Player2 bloqueada por 300 segundos
```

## 🐛 Solução de Problemas

### Problemas Comuns

**1. Jogador não consegue se mover após login**
```
Solução: Use /dlauthlogin debug <jogador> para verificar status
```

**2. Cores não aparecem nas mensagens**
```
Solução: Use /dlauthlogin testcolors para testar sistema de cores
```

**3. Plugin não carrega**
```
Verifique:
- Java 8+ instalado
- Bukkit/Spigot 1.13+
- Permissões de arquivo
- Logs de erro no console
```

**4. Problemas de banco de dados**
```
Verifique:
- Configuração do banco
- Permissões de acesso
- Conexão de rede (MySQL)
```

### Debug Avançado

Use o comando de debug para verificar o estado de um jogador:
```
/dlauthlogin debug PlayerName
```

**Informações mostradas:**
- UUID do jogador
- Status online
- Permissões de bypass
- Status de registro/login
- Sessão válida
- Status de proteção
- Necessidades de ação

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. **Fork** o projeto
2. **Crie** uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. **Push** para a branch (`git push origin feature/AmazingFeature`)
5. **Abra** um Pull Request

### Diretrizes de Contribuição

- Siga o padrão de código existente
- Adicione testes para novas funcionalidades
- Atualize a documentação conforme necessário
- Use commits descritivos
- Mantenha o código limpo e bem comentado

## 📄 Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [LICENSE](LICENSE) para detalhes.

### MIT License

```
MIT License

Copyright (c) 2024 DLAuthLogin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Agradecimentos

- **Bukkit/Spigot** - Plataforma de desenvolvimento
- **Comunidade Minecraft** - Feedback e suporte
- **Contribuidores** - Todos que ajudaram no projeto

## 📞 Suporte

- **Issues**: [GitHub Issues](https://github.com/LuizFelipeVPCrema/DLAuthLogin/issues)
- **Discord**: [Servidor Discord](https://discord.gg/seu-servidor)
- **Email**: suporte@dlauthlogin.com

## 🔄 Changelog

### v1.0.0 (2024-01-15)
- ✅ Sistema completo de autenticação
- ✅ Proteções configuráveis
- ✅ Sistema de sessões
- ✅ Múltiplos idiomas
- ✅ Logs detalhados
- ✅ Comandos de administração
- ✅ Sistema de cores
- ✅ Proteção contra força bruta

---

**DLAuthLogin** - Protegendo seu servidor Minecraft com segurança e simplicidade! 🛡️⚡

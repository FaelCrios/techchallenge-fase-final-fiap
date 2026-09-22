# 🏥 FilaCerta SUS

### Sistema Inteligente de Otimização de Filas e Reaproveitamento de Vagas do SUS

**Hackathon FIAP | Pós-Graduação em Engenharia de Software**

---

## 📋 Sobre o projeto

O FilaCerta SUS é uma solução de backend desenvolvida como MVP para o Hackathon da FIAP, com o objetivo de contribuir para a otimização das filas de espera e o reaproveitamento de vagas de consultas médicas no Sistema Único de Saúde (SUS).

A proposta consiste em conectar vagas disponíveis a pacientes que aguardam atendimento, utilizando um algoritmo de priorização que considera critérios clínicos, tempo de espera e preferência de horário.

Quando uma vaga é disponibilizada, o sistema identifica os pacientes elegíveis e permite a criação de uma oferta de agendamento.

Caso o paciente recuse ou não responda dentro do prazo estabelecido, a vaga poderá ser disponibilizada para outro paciente, evitando que oportunidades de atendimento sejam desperdiçadas.

O projeto foi desenvolvido como uma API REST, utilizando Java 21, Spring Boot e PostgreSQL, com documentação interativa por meio do Swagger.

---

## 🎯 Problema identificado

As filas de espera por consultas especializadas representam um desafio para o sistema público de saúde.

Entre os fatores que contribuem para a dificuldade de acesso aos atendimentos estão:

- Demanda elevada por consultas especializadas.
- Dificuldade de organizar e priorizar pacientes em espera.
- Cancelamentos e recusas que podem deixar horários disponíveis.
- Demora na redistribuição de vagas não confirmadas.
- Necessidade de maior rastreabilidade no processo de agendamento.

Uma vaga que não é utilizada representa uma oportunidade perdida de atendimento para outro paciente que permanece aguardando na fila.

Nesse contexto, o FilaCerta SUS propõe um mecanismo de identificação de candidatos elegíveis e redistribuição de vagas.

---

## 💡 Solução proposta

O FilaCerta SUS utiliza um fluxo de gerenciamento de vagas e pacientes, no qual cada oportunidade de atendimento pode ser oferecida a um candidato da fila de espera.

O sistema permite:

1. Cadastrar pacientes, unidades de saúde e especialidades.
2. Cadastrar profissionais de saúde responsáveis pelos atendimentos.
3. Registrar pacientes nas filas de espera.
4. Classificar candidatos utilizando critérios de priorização.
5. Disponibilizar vagas para consultas.
6. Criar ofertas de agendamento com prazo de resposta.
7. Registrar a aceitação ou recusa de uma oferta.
8. Identificar ofertas expiradas e disponibilizar novamente as vagas.
9. Consultar o estado das vagas, das ofertas e das entradas na fila.

O objetivo do MVP é demonstrar a viabilidade técnica de um sistema capaz de apoiar a distribuição de vagas de maneira organizada e rastreável.

O FilaCerta SUS não realiza diagnósticos nem determina autonomamente a prioridade clínica de um paciente. A prioridade utilizada pelo algoritmo é informada no cadastro da entrada na fila.

---

## 🏗️ Arquitetura da solução

A aplicação foi desenvolvida utilizando uma arquitetura de monólito modular, com organização por domínios de negócio.

Os módulos possuem responsabilidades específicas e são organizados em camadas de API, aplicação, domínio e infraestrutura.

### Organização das camadas

| Camada | Responsabilidade |
|--------|------------------|
| API | Exposição dos endpoints REST, recebimento das requisições e retorno das respostas. |
| Application | Implementação dos casos de uso e coordenação das regras de negócio. |
| Domain | Representação das entidades, estados e comportamentos do sistema. |
| Infrastructure | Persistência de dados e comunicação com o PostgreSQL por meio do Spring Data JPA. |

### Visão geral

```text
                 CLIENTE HTTP
             Swagger / Postman
                     |
                     v
              API REST (Spring)
                     |
                     v
             CAMADA DE APLICAÇÃO
                     |
          +----------+----------+
          |          |          |
          v          v          v
      Pacientes    Matching   Agendamento
          |          |          |
          +----------+----------+
                     |
                     v
               OFERTAS DE VAGAS
                     |
              Aceite / Recusa
                     |
                     v
             EXPIRAÇÃO AUTOMÁTICA
                     |
                     v
             SPRING DATA JPA
                     |
                     v
                POSTGRESQL
```

### Principais módulos

```text
br.com.fiap.filacerta
|
|-- healthunit       # Unidades de saúde
|-- specialty        # Especialidades médicas
|-- patient          # Cadastro de pacientes
|-- professional     # Profissionais de saúde
|-- waitlist         # Gerenciamento das filas
|-- scheduling       # Vagas de consultas
|-- matching         # Priorização dos candidatos
|-- offer            # Ofertas e expiração
|-- status           # Status da aplicação
|
|-- shared
    |-- config       # Configurações compartilhadas
    |-- exception    # Tratamento de exceções
```

Essa organização permite separar as responsabilidades e facilita a manutenção e a evolução dos diferentes componentes da aplicação.

---

## 🛠️ Tecnologias utilizadas

| Tecnologia | Finalidade |
|------------|------------|
| Java 21 | Linguagem principal do backend. |
| Spring Boot 3.5.16 | Framework de desenvolvimento da aplicação. |
| Spring Web | Implementação da API REST. |
| Spring Data JPA | Persistência e acesso aos dados. |
| Hibernate | Mapeamento objeto-relacional. |
| PostgreSQL 17 | Banco de dados relacional. |
| Flyway | Versionamento e migração do banco de dados. |
| Docker Compose | Execução do PostgreSQL em ambiente local. |
| Maven | Gerenciamento das dependências e build. |
| Springdoc OpenAPI | Documentação interativa da API pelo Swagger. |
| Jakarta Bean Validation | Validação dos dados recebidos pela API. |
| Spring Scheduling | Execução periódica do processamento de ofertas expiradas. |
| JUnit 5 e Mockito | Estrutura dos testes unitários. |
| Testcontainers | Integração dos testes com PostgreSQL em contêiner. |
| JaCoCo | Geração de relatórios de cobertura de código. |

---

## ⚙️ Funcionalidades implementadas

### 1. Gerenciamento de unidades de saúde

Permite cadastrar e consultar unidades de saúde.

Cada unidade possui um identificador único, um código e um nome.

As unidades são utilizadas para associar pacientes, profissionais e vagas a um local de atendimento.

### 2. Gerenciamento de especialidades

Permite cadastrar e consultar as especialidades disponíveis.

As especialidades são utilizadas para organizar as filas de espera e identificar os pacientes elegíveis para determinada vaga.

### 3. Cadastro de pacientes

Permite cadastrar pacientes utilizando um identificador SUS e um nome.

Cada paciente possui um UUID próprio, utilizado nos relacionamentos com as entradas na fila de espera.

### 4. Cadastro de profissionais de saúde

Permite cadastrar profissionais responsáveis pelos atendimentos.

O cadastro contém:

- Nome do profissional.
- Registro profissional.
- Unidade de saúde vinculada.
- Especialidade vinculada.

O sistema associa cada profissional a uma unidade e especialidade.

### 5. Gerenciamento da fila de espera

Permite registrar pacientes em uma fila vinculada a uma unidade de saúde e especialidade.

Cada entrada contém:

- Identificador do paciente.
- Unidade de saúde.
- Especialidade.
- Prioridade clínica.
- Período preferencial.
- Data de entrada na fila.
- Status atual.

O sistema impede a criação de uma nova entrada para a mesma combinação de paciente, unidade e especialidade enquanto existir uma entrada com status WAITING, OFFERED ou SCHEDULED.

### 6. Gerenciamento de vagas

Permite cadastrar e consultar vagas de consultas.

Cada nova vaga é associada a uma unidade, especialidade, profissional responsável e horário de atendimento.

O sistema impede que o mesmo profissional possua duas vagas com o mesmo horário de início.

Profissionais distintos podem possuir vagas simultâneas na mesma unidade e especialidade.

As vagas cadastradas antes da implementação do módulo de profissionais são preservadas no banco de dados e podem não possuir profissional associado.

### 7. Algoritmo de Matching

O Matching é responsável por identificar e ordenar os pacientes elegíveis para uma vaga.

O algoritmo considera:

1. Prioridade clínica.
2. Tempo de espera.
3. Compatibilidade com o período preferencial.
4. Data de entrada na fila.

Os candidatos precisam estar aguardando atendimento na unidade e especialidade correspondentes à vaga.

Pacientes que já recusaram ou deixaram expirar uma oferta para a mesma vaga são desconsiderados em novas seleções dessa vaga.

O algoritmo gera uma lista ordenada de candidatos, permitindo que o sistema identifique o próximo paciente que poderá receber a oferta.

### 8. Criação e gerenciamento de ofertas

Uma vaga disponível pode ser oferecida a um paciente elegível identificado pelo Matching.

Cada oferta possui:

- Token individual.
- Identificador da vaga.
- Identificador da entrada na fila.
- Status.
- Data de criação.
- Prazo de expiração.
- Data da resposta, quando aplicável.

O paciente possui 15 minutos para responder à oferta.

### 9. Aceitação de ofertas

Quando uma oferta é aceita dentro do prazo:

- A oferta passa para ACCEPTED.
- A vaga passa para BOOKED.
- A entrada do paciente na fila passa para SCHEDULED.

A operação utiliza transações para manter a consistência das alterações.

### 10. Recusa de ofertas

Quando o paciente recusa uma oferta:

- A oferta passa para REJECTED.
- A vaga retorna para AVAILABLE.
- O paciente retorna para WAITING.

O paciente permanece na fila, mas não pode receber novamente a mesma vaga recusada.

Uma nova oferta pode ser criada para o próximo candidato elegível.

### 11. Expiração automática

As ofertas possuem validade de 15 minutos.

O sistema utiliza um agendador do Spring para consultar periodicamente as ofertas pendentes cujo prazo de resposta terminou.

O processamento é configurado com um intervalo de cinco minutos após a conclusão da execução anterior.

Quando uma oferta expira:

- A oferta passa para EXPIRED.
- A vaga é liberada.
- O paciente retorna para WAITING.
- O sistema tenta criar uma nova oferta para outro candidato elegível.

Caso não existam outros candidatos, a vaga permanece disponível.

O histórico de ofertas impede que o mesmo paciente receba novamente uma vaga cuja oferta anterior tenha sido recusada ou expirada.

---

## 🔄 Fluxo de funcionamento

```text
       PACIENTE ENTRA NA FILA
                 |
                 v
       VAGA DISPONIBILIZADA
                 |
                 v
        ALGORITMO DE MATCHING
                 |
                 v
       IDENTIFICAÇÃO DO CANDIDATO
                 |
                 v
         CRIAÇÃO DA OFERTA
                 |
                 v
       PRAZO DE RESPOSTA: 15 MIN
                 |
          +------+------+
          |      |      |
          v      v      v
        ACEITE RECUSA EXPIRAÇÃO
          |      |      |
          v      +------+
     AGENDAMENTO    |
                    v
             LIBERAÇÃO DA VAGA
                    |
                    v
           PRÓXIMO CANDIDATO
```

O reaproveitamento da vaga após a recusa pode ser iniciado por uma nova chamada ao endpoint de criação de ofertas.

No caso de expiração, o processamento automático tenta criar a próxima oferta após liberar a vaga.

---

## 📊 Estados das entidades

### Estados da fila de espera

| Status | Descrição |
|--------|-----------|
| WAITING | Paciente aguardando atendimento. |
| OFFERED | Paciente possui uma oferta pendente. |
| SCHEDULED | Paciente possui uma consulta agendada. |
| CANCELLED | Entrada da fila cancelada. |

### Estados das vagas

| Status | Descrição |
|--------|-----------|
| AVAILABLE | Vaga disponível para oferta. |
| OFFERED | Vaga associada a uma oferta pendente. |
| BOOKED | Vaga reservada após a aceitação da oferta. |

### Estados das ofertas

| Status | Descrição |
|--------|-----------|
| PENDING | Oferta aguardando resposta. |
| ACCEPTED | Oferta aceita pelo paciente. |
| REJECTED | Oferta recusada pelo paciente. |
| EXPIRED | Oferta cujo prazo de resposta terminou e cuja expiração foi processada. |

---

## 🗄️ Banco de dados

O projeto utiliza PostgreSQL como banco de dados relacional.

As principais tabelas são:

| Tabela | Responsabilidade |
|--------|------------------|
| health_unit | Armazenamento das unidades de saúde. |
| specialty | Armazenamento das especialidades. |
| patient | Cadastro dos pacientes. |
| health_professional | Cadastro dos profissionais de saúde. |
| waitlist_entry | Entradas dos pacientes nas filas de espera. |
| appointment_slot | Vagas disponibilizadas para atendimento. |
| slot_offer | Histórico das ofertas de vagas. |

### Migrações

O versionamento do banco é realizado pelo Flyway.

As migrações ficam no diretório:

```text
src/main/resources/db/migration/
```

O projeto possui migrações para criação das tabelas, restrições de ofertas pendentes, prevenção de duplicidade na fila e associação de profissionais às vagas.

O Hibernate utiliza `ddl-auto: validate`, enquanto o Flyway é responsável pelas alterações estruturais do banco de dados.

---

## 🚀 Como executar o projeto

### Pré-requisitos

Para executar a aplicação localmente, é necessário possuir:

- JDK 21.
- Maven compatível com o projeto.
- Docker e Docker Compose.
- Git.

### 1. Clonar o repositório

```bash
git clone https://github.com/FaelCrios/techchallenge-fase-final-fiap.git

cd techchallenge-fase-final-fiap
```

### 2. Iniciar o PostgreSQL

O projeto possui um arquivo `compose.yaml` para executar o banco de dados utilizando Docker.

Na raiz do projeto, execute:

```bash
docker compose up -d
```

O Docker criará o contêiner PostgreSQL e disponibilizará o banco na porta 5432.

As configurações locais padrão são:

| Configuração | Valor |
|--------------|-------|
| Banco | filacerta |
| Usuário | filacerta |
| Senha | filacerta |
| Porta | 5432 |

Essas credenciais são destinadas exclusivamente ao ambiente local de desenvolvimento.

### 3. Configurar as variáveis de ambiente

O arquivo `.env.example` documenta as variáveis utilizadas para a conexão com o banco:

```properties
DB_URL=jdbc:postgresql://localhost:5432/filacerta
DB_USERNAME=filacerta
DB_PASSWORD=filacerta
```

O arquivo `application.yml` utiliza esses valores como padrão quando as variáveis de ambiente não estão definidas.

Para uma instalação local com o Docker Compose fornecido, não é necessário configurar variáveis adicionais.

### 4. Iniciar a aplicação

Com o PostgreSQL em execução, inicie o projeto:

```bash
mvn spring-boot:run
```

O Flyway executará as migrações pendentes durante a inicialização.

A aplicação estará disponível em:

```text
http://localhost:8080
```

### 5. Acessar o Swagger

A documentação interativa dos endpoints está disponível em:

http://localhost:8080/swagger-ui.html

O Swagger permite consultar os contratos da API e enviar requisições HTTP para demonstrar o funcionamento do sistema.

---

## 🌐 Principais endpoints

### Unidades de saúde

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/v1/health-units | Cadastrar unidade. |
| GET | /api/v1/health-units | Listar unidades. |
| GET | /api/v1/health-units/{id} | Consultar unidade por ID. |

### Especialidades

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/v1/specialties | Cadastrar especialidade. |
| GET | /api/v1/specialties | Listar especialidades. |
| GET | /api/v1/specialties/{id} | Consultar especialidade por ID. |

### Pacientes

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/v1/patients | Cadastrar paciente. |
| GET | /api/v1/patients | Listar pacientes. |
| GET | /api/v1/patients/{id} | Consultar paciente por ID. |

### Profissionais de saúde

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/v1/professionals | Cadastrar profissional. |
| GET | /api/v1/professionals | Listar profissionais. |
| GET | /api/v1/professionals/{id} | Consultar profissional por ID. |

### Fila de espera

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/v1/waitlist | Incluir paciente na fila. |
| GET | /api/v1/waitlist | Listar entradas da fila. |
| GET | /api/v1/waitlist/{id} | Consultar entrada por ID. |

### Vagas e Matching

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/v1/slots | Cadastrar vaga. |
| GET | /api/v1/slots | Listar vagas. |
| GET | /api/v1/slots/{id} | Consultar vaga por ID. |
| GET | /api/v1/slots/{slotId}/candidates | Consultar candidatos ordenados para uma vaga. |

### Ofertas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/v1/slots/{slotId}/offers | Criar oferta para uma vaga. |
| GET | /api/v1/slots/offers/{token} | Consultar oferta pelo token. |
| POST | /api/v1/slots/offers/{token}/accept | Aceitar oferta. |
| POST | /api/v1/slots/offers/{token}/reject | Recusar oferta. |

### Monitoramento

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/v1/status | Consultar status da aplicação. |
| GET | /actuator/health | Consultar disponibilidade pelo Spring Actuator. |

Os contratos completos de entrada e saída podem ser consultados pelo Swagger.

---

## 🧪 Estratégia de testes

O projeto contém testes automatizados utilizando JUnit 5, Mockito e Testcontainers.

Os testes foram desenvolvidos para verificar regras de negócio, transições de status, comportamento dos serviços, integração com PostgreSQL e cenários de concorrência.

O projeto também possui configuração do JaCoCo para geração de relatórios de cobertura.

A existência dessas classes e configurações não representa uma garantia de aprovação da suíte completa ou de um percentual específico de cobertura.

A compilação e a execução dos testes devem ser verificadas no ambiente utilizado para a entrega.

---

## 🔒 Segurança e limitações do MVP

O FilaCerta SUS foi desenvolvido como protótipo acadêmico para demonstrar uma solução de backend e arquitetura.

O MVP ainda não possui autenticação e autorização de usuários.

Os endpoints de consulta e gerenciamento de ofertas não devem ser expostos publicamente para utilização com dados reais de pacientes sem a implementação dos controles de segurança necessários.

A demonstração acadêmica deve utilizar dados fictícios.

O projeto não representa uma integração oficial com sistemas do SUS e não substitui os processos clínicos e administrativos das unidades de saúde.

---

## 📈 Resultados da implementação

O MVP implementa o fluxo de cadastro, priorização, oferta e confirmação de consultas.

Durante a validação funcional pelo Swagger, foi demonstrado um cenário em que uma vaga foi oferecida a uma paciente, recusada e posteriormente oferecida a outro paciente, que confirmou o agendamento.

Esse fluxo permitiu observar a atualização dos estados das ofertas e o reaproveitamento da mesma vaga.

O sistema também possui implementação de expiração automática, cuja validação manual completa não faz parte dos resultados apresentados nesta demonstração.

Os resultados demonstram o funcionamento do fluxo principal em ambiente de desenvolvimento. Não foram mensurados impactos reais na redução das filas do SUS.

---

## 🔮 Melhorias futuras

Como possibilidades de evolução do projeto, destacam-se:

- Autenticação e autorização de usuários.
- Integração com sistemas oficiais de agendamento e prontuários eletrônicos.
- Notificações aos pacientes por canais externos.
- Interface web ou aplicativo para pacientes e gestores.
- Painéis de indicadores e monitoramento das filas.
- Auditoria detalhada das operações realizadas.
- Controle de duração e sobreposição de consultas.
- Suporte a profissionais vinculados a múltiplas unidades.
- Configuração de fuso horário por unidade de saúde.
- Evolução das regras de priorização conforme protocolos clínicos e critérios definidos pelos órgãos responsáveis.

Essas funcionalidades representam possíveis evoluções e não fazem parte do escopo implementado do MVP atual.

---

## 🎓 Contexto acadêmico

Projeto desenvolvido para o Hackathon da pós-graduação da FIAP.

O FilaCerta SUS apresenta uma proposta de aplicação de engenharia de software a um problema do sistema público de saúde, utilizando arquitetura de backend, persistência relacional, regras de negócio, processamento automático e documentação de API.

O desenvolvimento busca demonstrar como a tecnologia pode apoiar a organização das filas de espera e contribuir para o melhor aproveitamento das vagas disponíveis.

---

## 👨‍💻 Autor

**Rafa — FaelCrios**

Pós-graduação FIAP

GitHub: https://github.com/FaelCrios

Repositório: https://github.com/FaelCrios/techchallenge-fase-final-fiap

---
# users-api

API REST de cadastro de usuários e suas *stacks* tecnológicas, desenvolvida como projeto de estudo com foco em **Kotlin** e **Spring Boot**. O objetivo é exercitar boas práticas de organização em camadas, mapeamento JPA, validação de entrada, paginação com ordenação controlada e testes automatizados — utilizando um banco de dados real (Oracle) provisionado via Docker.

## Domínio

O modelo é intencionalmente simples: um usuário possui um conjunto de stacks (tecnologias), cada uma com um nível numérico de proficiência.

| Entidade | Campos |
|---|---|
| `User` | `id`, `name`, `nick?`, `birthDate` |
| `Stack` | `id`, `name`, `level`, `user_id` |

- Relação `@OneToMany` com `cascade = ALL` e `orphanRemoval = true`.
- Identificadores em `UUID`, gerados pelo Hibernate via `@UuidGenerator`.
- `nick` é opcional, porém único quando presente — a unicidade é validada na camada de serviço tanto na criação quanto na atualização.

## Recursos

- CRUD completo de usuários em `/api/users`.
- Listagem paginada com ordenação restrita a campos previamente autorizados por uma anotação customizada (`@Sortable`), evitando expor nomes internos da entidade e prevenindo `PropertyReferenceException`.
- Endpoint dedicado para consulta das stacks de um usuário.
- Validação declarativa do payload de entrada com Jakarta Bean Validation (nome obrigatório, data de nascimento no passado, tamanho da coleção de stacks, etc.).
- Validação customizada garantindo que não haja stacks duplicadas em um mesmo payload (`@UniqueStack`).
- Tratamento centralizado de exceções via `@RestControllerAdvice`, retornando um formato consistente de erro (`ErrorResponse`).
- Serialização JSON em `snake_case` configurada globalmente pelo Jackson.

## Tecnologias

| Categoria | Ferramenta |
|---|---|
| Linguagem | Kotlin 2.2 |
| Runtime | JVM 21 |
| Framework | Spring Boot 4.0 |
| Web | Spring Web MVC |
| Persistência | Spring Data JPA + Hibernate |
| Banco de dados | Oracle Database Free |
| Driver JDBC | `ojdbc11` |
| Validação | Jakarta Bean Validation |
| Serialização | Jackson (`jackson-module-kotlin`) |
| Build | Gradle (Kotlin DSL) |
| Testes | JUnit 5 + Spring Boot Test |

## Arquitetura

A aplicação segue uma arquitetura em camadas convencional, com separação clara de responsabilidades:

```
src/main/kotlin/com/estudos/users_api
├── UsersApiApplication.kt   # ponto de entrada da aplicação
├── controller/              # camada de exposição HTTP (REST)
├── service/                 # regras de negócio e orquestração
├── repository/              # abstrações Spring Data JPA
├── model/                   # entidades JPA (User, Stack)
├── dto/
│   ├── mapper/              # conversões entre entidade e DTO
│   └── pagination/          # PageQuery, PageResponse, Paginator, PageLinks
├── validation/
│   ├── annotation/          # anotações customizadas (@UniqueStack)
│   └── validator/           # implementações de ConstraintValidator
├── annotation/              # @Sortable (whitelist de ordenação)
├── enums/                   # SortDirection
└── exception/               # exceções de domínio e GlobalExceptionHandler
```

O fluxo de uma requisição segue o caminho `Controller → Service → Repository`. O controller é responsável apenas por receber a requisição, aplicar validações declarativas e delegar; o service concentra as regras de negócio e o gerenciamento transacional; o repositório expõe as operações de persistência.

A camada de paginação é isolada em `dto/pagination`, encapsulando a conversão dos parâmetros da query string em um `Pageable` do Spring Data e a construção da resposta paginada — incluindo os links de navegação (`first`, `prev`, `next`, `last`).

## Endpoints

Todos os recursos estão sob o prefixo `/api/users`.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/users` | Cria um novo usuário |
| `GET` | `/api/users` | Lista usuários de forma paginada |
| `GET` | `/api/users/{id}` | Retorna um usuário pelo identificador |
| `GET` | `/api/users/{userId}/stacks` | Retorna as stacks de um usuário |
| `PUT` | `/api/users/{id}` | Atualiza um usuário existente |
| `DELETE` | `/api/users/{id}` | Remove um usuário |

Parâmetros de paginação suportados em `GET /api/users`:

- `offset` — posição inicial dos registros (deve ser múltiplo de `limit`, padrão 0)
- `limit` — quantidade de elementos por página (mínimo 1, máximo 100, padrão 20)
- `sort` — ordenação no formato `campo:direcao`, separados por vírgula para múltiplos campos

Campos disponíveis para ordenação: `name`, `nick`, `birth_date`.
Direções: `asc`, `desc`.

### Exemplo de payload

```json
{
  "name": "Ana Silva",
  "nick": "anasilva",
  "birth_date": "1998-04-12",
  "stack": [
    { "name": "Kotlin", "level": 8 },
    { "name": "Spring", "level": 7 }
  ]
}
```

### Exemplos de requisição

```bash
# Criar usuário
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ana Silva",
    "nick": "anasilva",
    "birth_date": "1998-04-12",
    "stack": [
      { "name": "Kotlin", "level": 8 },
      { "name": "Spring", "level": 7 }
    ]
  }'

# Listar usuários paginado e ordenado
curl "http://localhost:8080/api/users?offset=0&limit=10&sort=name:asc"

# Buscar por ID
curl http://localhost:8080/api/users/{id}

# Listar stacks de um usuário
curl http://localhost:8080/api/users/{id}/stacks

# Atualizar usuário
curl -X PUT http://localhost:8080/api/users/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ana S.",
    "nick": "anasilva",
    "birth_date": "1998-04-12",
    "stack": [{ "name": "Kotlin", "level": 9 }]
  }'

# Remover usuário
curl -X DELETE http://localhost:8080/api/users/{id} -i
```

## Executando a aplicação

### Pré-requisitos

- JDK 21
- Docker e Docker Compose

### 1. Provisionar o banco de dados

```bash
docker compose up -d
```

O comando inicia um container Oracle Free expondo a porta `1521`. As credenciais utilizadas pela aplicação já estão configuradas em `src/main/resources/application.yml`:

- URL: `jdbc:oracle:thin:@localhost:1521/FREEPDB1`
- Usuário: `app_user`
- Senha: `UsersApi!`

> A inicialização do Oracle pode levar alguns minutos na primeira execução. Antes de iniciar a aplicação, acompanhe os logs do container até que a mensagem `DATABASE IS READY TO USE` seja exibida:
>
> ```bash
> docker logs -f oracle-free
> ```

### 2. Iniciar a aplicação

Linux, macOS ou WSL:

```bash
./gradlew bootRun
```

Windows:

```bat
gradlew.bat bootRun
```

A aplicação ficará disponível em `http://localhost:8080`. Como `spring.jpa.hibernate.ddl-auto` está definido como `update`, o esquema é criado e atualizado automaticamente a partir das entidades.

### Empacotamento

Para gerar um JAR executável:

```bash
./gradlew bootJar
java -jar build/libs/users-api-0.0.1-SNAPSHOT.jar
```

## Executando os testes

```bash
./gradlew test
```

A suíte está organizada em `src/test/kotlin/com/estudos/users_api`:

- **`UsersApiApplicationTests`** — verifica a inicialização do contexto Spring.
- **`service/UserServiceTest`** — testes unitários da camada de serviço.
- **`controller/UserControllerIntegrationTest`** — testes de integração da camada web, agrupados por operação em classes aninhadas (`CreateTests`, `ReadTests`, `UpdateTests`, `DeleteTests`, `StackTests`).

Após a execução, o relatório HTML é gerado em:

```
build/reports/tests/test/index.html
```

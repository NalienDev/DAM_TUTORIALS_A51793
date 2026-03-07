# Desenvolvimento de Aplicações Móveis - Trabalho Prático 1 (DAM_TP1)

Este repositório contém os projetos desenvolvidos no âmbito do **Trabalho Prático 1** da unidade curricular de Desenvolvimento de Aplicações Móveis (DAM). O projeto está dividido em duas componentes principais: uma aplicação de consola em Kotlin e uma aplicação Android nativa denominada **Movie Buddy**.

---

## Estrutura do Repositório

- `/kotlin` - Projeto Maven com código Kotlin (Aplicação de Consola e Exercícios).
- `/android/code/MovieBuddy` - Projeto principal em Android Studio (Aplicação Movie Buddy).
- `/android/code/HelloWorldOptional` e `/android/code/HelloWorldV2` - Projetos introdutórios opcionais.

---

## 1. Componente Kotlin (Aplicação de Consola e Exercícios)

A pasta `/kotlin` contém múltiplos exercícios implementados em Kotlin puro e configurados através do Maven (`pom.xml`). Estes focam-se em conceitos fundamentais da linguagem, desde sintaxe básica a orientação a objetos avançada.

### 1.1. Exercícios Básicos

- **`exer_1` (Fundamentos e Arrays)**: Explora a sintaxe inicial (Hello World) e a instanciação de arrays utilizando *lambdas* e transformações funcionais (ex: `map` sobre um `IntRange`).
- **`exer_2` (Calculadora Interativa)**: Implementa uma calculadora de consola interactiva com suporte para operações aritméticas (`+`, `-`, `*`, `/`), lógicas booleanas (`&&`, `||`, `!`) e *bitwise shift* (`>>`, `<<`). Destaca-se pelo *parse* manual de *strings* introduzidas pelo utilizador e tratamento de erros ou valores inválidos.
- **`exer_3` (Sequências Funcionais)**: Simula a queda de uma bola que perde 40% da sua altura a cada ressalto, até ser inferior a 1 metro. Recorre a conceitos funcionais avançados como `generateSequence` e `takeWhile`.

### 1.2. Exercício de Validação Avançado (`exer_vl`): Gestão de Biblioteca

O package `exer_vl` implementa um modelo de domínio para um sistema de biblioteca, aplicando extensamente os princípios de **Programação Orientada a Objetos (POO)** no Kotlin.

- **`Book` (Classe Abstrata)**: Entidade base que agrupa as semânticas comuns de um livro. Exemplifica a utilização do bloco `init` aliado a funções internas de validação `require` para garantir regras de negócio intrínsecas (ex: rejeitar anos e copias negativas na instanciação). Incorpora também *getters/setters* customizados em backing fields — categorizando as épocas da publicação numa *String* formatada computada on-the-fly consoante o ano de publicação fornecido; e lançando um print-aviso na consola sempre que a alteração local do inventário (`availableCopies`) deixe o stock a zero.
- **`PhysicalBook` e `DigitalBook` (Especialização/Herança)**: 
  - Físicos expandem características de peso em gramas e *booleans* de capa dura.
  - Digitais modelam metadados de tamanho digital e delegam o formato real em construtores tipados de um *Enum* (PDF/EPUB/MOBI). 
  - As duas classes garantem especificidade forçada e polimorfismo ao implementar o contrato do método abstrato `getStorageInfo()`.
- **`LibraryMember` (Data Class)**: Demonstra o poder sintético do Kotlin para registos. Define implicitamente de forma auto-gerada os equivalentes ao *hashCode/equals/toString*, mantendo apenas um ID, nome e uma lista nativa de histórico de empréstimos individuais gerida através de *MutableLists*.
- **`Library` (Gestor Funcional)**: Aglomerador de lista de instâncias com as operações de sistema. Efetua a gestão de devoluções, requisições de empréstimo (cuidando não só da decrementação da cópia base como adicionando referências dinamicamente às listas do Membro que está a ser servido) e agregações funcionais de procura (recorrendo a curtos lambdas em filtros e *forEach* para formatar os retornos baseados em certos Autores). Demonstra ainda o encapsulamento de estado *static-like* no Kotlin através de um **`companion object`**, que age à margem das múltiplas bibliotecas individuais, criando variáveis contadoras do sistema unificadas globalmente em tempo real.

---

## 2. Componente Android: Movie Buddy

A aplicação principal, **Movie Buddy**, permite aos utilizadores pesquisar informações sobre filmes/séries através de uma API gratuita, visualizar detalhes específicos e atribuir avaliações (ratings) que são guardadas localmente.

### 2.1. Arquitetura e Padrões de Desenho

A aplicação segue a recomendação oficial da Google para o desenvolvimento Android, adotando a arquitetura **MVVM (Model-View-ViewModel)**.

- **Model (Camada de Dados)**: Contém os Data Classes (como `MovieDto`, `TvMazeResponse`) e Repositórios (`MovieRepository`, `RatedMovieRepository`). Responsável por ir buscar e guardar os dados.
- **ViewModel (Camada de Lógica de Negócio)**: Componentes como `MovieListViewModel` comunicam com as UI states através de `StateFlow`. Mantêm e processam os dados da UI com consciência do ciclo de vida, sobrevivendo a mudanças de configuração (ex: rotação do ecrã).
- **View (Camada de Apresentação)**: Implementada usando *Fragments* (`MovieListFragment`, `MovieDetailFragment`, `RatedMoviesFragment`) em conjunto com **View Binding** para aceder às views de forma segura e sem a necessidade de `findViewById`.

### 2.2. Tecnologias e Bibliotecas Utilizadas

O `build.gradle.kts` revela o uso de ferramentas modernas e bibliotecas robustas:
- **Linguagem**: Kotlin (com target SDK 34).
- **Navigation Component**: Utilizado para gerir a navegação entre Fragments num ambiente single-activity.
- **Coroutines & Kotlin Flow**: Para operações assíncronas (como pedidos de rede) de forma sequencial na thread de I/O (`Dispatchers.IO`), sem bloquear a Interface Web (UI Thread).
- **Retrofit & OkHttp**: Biblioteca base para comunicação com a API REST remota. O OkHttp é utilizado como intercetor de logging.
- **Gson**: Utilizado pelo Retrofit para converter o JSON da resposta diretamente para Data Classes de Kotlin de forma automática através das tags `@SerializedName`.
- **Glide**: Biblioteca robusta e eficiente utilizada para carregar, armazenar em cache e apresentar as capas (posters) dos filmes nas Listas (RecyclerView) e no Detalhe.

### 2.3. Implementação e Funcionalidades

#### 2.3.1. Pesquisa e Consumo de API
A aplicação utiliza a API pública do **TVMaze**. Quando o utilizador pesquisa por um título:
1. A view (`MovieListFragment`) capta a ação de pesquisa do utilizador no teclado.
2. O `MovieListViewModel` lança uma corrotina e pede os dados ao `MovieRepository`.
3. O `MovieRepository` utiliza a interface `RetrofitClient.apiService` para efetuar o request `GET` pela keyword em background thread.
4. O resultado é encapsulado num enum/sealed class de Estado (`Result.success` ou `Result.failure`) e emitido como `MovieListUiState`.
5. A View observa (collect) o estado de forma lifecycle-aware (`repeatOnLifecycle`) e atualiza o ecrã instantaneamente (exibindo *Loading*, preenchendo o `RecyclerView` através do `MovieAdapter` com *Success* ou as mensagens de *Error*/*No results*).

#### 2.3.2. Detalhe do Filme
Ao clicar num filme do `RecyclerView`, a view atual invoca o `findNavController().navigate()` transportando os metadados do filme num `Bundle` (Arguments). O `MovieDetailFragment` recebe estes dados e exibe o resumo textual, género, capa (carregada pelo Glide) e um interface (`RatingBar` ou afim) para classificar o filme.

#### 2.3.3. Persistência de Dados (Ratings)
A aplicação necessita de guardar os ratings de forma a que resistam ao fechar da app. Para tal, em vez de implementar uma base de dados complexa como o SQLite/Room (uma vez que os dados são simples e limitados), a escolha arquitetural foi o uso inteligente do **SharedPreferences**.
- O `RatedMovieRepository` é inicializado com um ficheiro de credenciais fechado: `"MovieRatings"`.
- As avaliações são guardadas como pares de chave/valor em que a "key" é uma string composta por `"rating_<ID_DO_FILME>"` e o "value" é o valor (Float) classificado.
- A lista de ratings é extraída ao correr todo o dicionário e filtrada pelas strings que dão "startWith" às métricas da aplicação. O ecrã "Rated Movies" permite listar os filmes que apenas obtiveram avaliações efetuadas pelo próprio utilizador.

---

## Como Configurar e Executar

### 1. Aplicação Android (Movie Buddy)
1. Abrir o **Android Studio**.
2. Selecionar `File > Open` e navegar até `DAM_TP1/android/code/MovieBuddy`.
3. Aguardar pelo "Gradle Sync" terminar para descarregar as dependências.
4. Conectar um telemóvel Android ou utilizar um Emulador (API 24+).
5. Clicar em **Run (Shift + F10)**.

### 2. Aplicação Kotlin (Consola)
1. É possível abrir pelo **IntelliJ IDEA**.
2. Fazer "Open Project" escolhendo o nível da pasta `DAM_TP1/kotlin/pom.xml`.
3. O IntelliJ tratará de importar a configuração Maven e permitir a execução nativa. Em alternativa usar terminal (`mvn package` -> `java -jar ...`).

---
_Nota: Trata-se de um projeto académico com propósitos educativos e de demonstração ao abrigo da unidade curricular DAM._

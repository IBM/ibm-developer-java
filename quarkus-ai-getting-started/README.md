# Sample app to generate AI poems with Quarkus and LangChain4j

This sample application demonstrates how to write AI-infused applications in Java using LangChain4j and Quarkus. It is based on the Quarkiverse LangChain4j sample application [email-a-poem](https://github.com/quarkiverse/quarkus-langchain4j/tree/main/samples/email-a-poem).

The sample application accompanies an article [Create your first AI Java application with Quarkus and LangChain4j](https://developer.ibm.com/tutorials/create-ai-java-app-quarkus-langchain/) on IBM Developer.

See the article for instructions on how to build and run the application.

---

## Prerequisites

| Prerequisite | Version | Notes |
|---|---|---|
| Java (JDK) | 21 or later | Required to build and run the application |
| Podman | Latest stable | Used to run the Ollama container |

The Maven Wrapper (`./mvnw`) is bundled in the repository — no separate Maven installation is needed.

### Install Java 21

On Linux and Mac, the easiest way to install Java 21 is to first install [SDKMAN!](https://sdkman.io/). You can also use SDKMAN! to install the optional Quarkus CLI, which is used in the tutorial.

Alternatively, manually download and install a JDK 21 distribution, for example [IBM Semeru](https://developer.ibm.com/languages/java/semeru-runtimes/downloads/) or [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=21). Verify the installation:

```bash
java -version
```

### Install Podman

Download and install Podman from the [official Podman website](https://podman.io/docs/installation). Verify the installation:

```bash
podman --version
```

#### macOS — additional setup

On macOS, Podman requires a Linux virtual machine. Run the following commands once after installation:

```bash
podman machine init
podman machine start
```

Verify the machine is running with:

```bash
podman machine list
```

#### Linux — no extra setup needed

On Linux, Podman runs natively. No machine initialization is required.

#### Windows — additional setup

On Windows, Podman requires Windows Subsystem for Linux (WSL 2) and a Podman machine. Run the following command in an elevated PowerShell, then restart your machine:

```powershell
wsl --install
```

After the restart, install Podman, then initialize and start the machine:

```powershell
podman machine init
podman machine start
```

Verify the machine is running with:

```powershell
podman machine list
```

---

## Overview

This is a minimal Quarkus REST application that uses a **local large language model (LLM) (via Ollama)** to generate poems on demand. It is built on three key technologies:

- **Quarkus** — the Java framework
- **LangChain4j** (via the `quarkus-langchain4j-ollama` extension) — the AI abstraction layer
- **Ollama** — runs the `granite4:3b` model locally on your machine

---

## The request flow

```
HTTP GET /poems/{topic}/{lines}
        │
        ▼
   Poems.java  ──────────────────────────────────►  AiPoemService.java
  (JAX-RS resource)  injects & calls writeAPoem()   (LangChain4j AI service)
                                                            │
                                                            ▼
                                                   Ollama (granite4:3b)
                                                   running locally
                                                            │
                                                            ▼
                                              Returns HTML poem as String
```

---

## How the code is structured

### `Poems.java` — REST resource

Exposes two endpoints:

| Method | Path | Returns |
|--------|------|---------|
| `GET` | `/poems` | Plain text `"hello"` (health/sanity check) |
| `GET` | `/poems/{topic}/{lines}` | An AI-generated poem as HTML |

The `{topic}` and `{lines}` path parameters are passed directly to the AI service.

### `AiPoemService.java` — AI service interface

This is the heart of the app. It is a plain Java **interface** — Quarkus + LangChain4j generate its implementation at build time via `@RegisterAiService`.

Two annotations define the prompt:

- `@SystemMessage` — sets the LLM's persona/role: *"You are a professional poet. Display the poem in well-formed HTML with line breaks."*
- `@UserMessage` — the user-facing prompt template: *"Write a poem about {poemTopic}. The poem should be {poemLines} lines long."* The `{poemTopic}` and `{poemLines}` placeholders are bound to the method parameters at call time.

### `application.properties` — configuration

```properties
quarkus.langchain4j.ollama.chat-model.model-id=granite4:3b
quarkus.langchain4j.ollama.timeout=60s
```

Points the Ollama integration at the **IBM Granite 4 3B** model, with a 60-second timeout, because local LLM inference can be slow.

### `PoemsTest.java` — test

A single `@QuarkusTest` that hits `GET /poems` and verifies it returns `200 OK` with body `"hello"`. It only tests the non-AI endpoint. This means Ollama does not need to be running during tests.

---

## Dependencies

| Dependency | Purpose |
|---|---|
| `quarkus-langchain4j-ollama` | Connects LangChain4j to a locally running Ollama server |
| `quarkus-arc` | Quarkus Contexts and Dependency Injection (CDI) container (enables `@Inject`) |
| `quarkus-rest` | JAX-RS REST support |
| `quarkus-junit5` + `rest-assured` | Testing |

The project targets **Java 21** and uses **Quarkus 3.22.3**.

---

## Usage

Once Ollama is running locally with `granite4:3b` pulled, send a request like:

```
GET http://localhost:8080/poems/autumn/8
```

This prompts the model to write an 8-line poem about autumn and returns it as rendered HTML.

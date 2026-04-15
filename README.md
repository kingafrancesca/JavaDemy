# JavaDemy

> An interactive desktop application for learning Java programming from scratch.

JavaDemy is an interactive desktop application designed to teach Java programming from the ground up. It offers structured lessons, multiple-choice quizzes, and practical coding exercises with real-time compilation and automated test feedback. Built with JavaFX 23, Java 25, and Maven.

---

## Features

- **Structured Lessons** — 14 topic-based lessons covering Java fundamentals, from variables and loops to OOP and collections. Each lesson is divided into steps with formatted content and runnable code examples.
- **Quizzes** — Multiple-choice quizzes per lesson with randomized questions and instant feedback. Includes a total review quiz across all studied material.
- **Coding Exercises** — Hands-on exercises with a built-in code editor, real-time compilation, and automated test runner. 
- **Progress Tracking** — Per-topic progress bars, an overall completion ring, and a login streak tracker.

---

## Technology used

| Layer | Technology |
|---|---|
| Language | Java 25 |
| UI Framework | JavaFX 23.0.1 |
| Build Tool | Maven |
| Data Serialization | Gson 2.10.1 |

---

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.8+
- JavaFX 23 (included via Maven dependencies)

### Run

```bash
git clone https://github.com/your-username/javademy.git
cd javademy
mvn javafx:run
```

---

## Data Files

All content is stored as JSON in `src/main/resources/data/`.

User progress and attempt history are saved locally in `data/attempts/`.

---

## Author

Built by Kinga Kinowska.

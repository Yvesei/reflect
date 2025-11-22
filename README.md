# Static Java Code Analyzer  

A modern static-analysis tool that extracts structural information from Java code and detects 10 classical Object-Oriented Bad Smells using Neo4j graph queries.

- Supports `.java`, `.class`, `.jar` files  
- Full-project recursive directory analysis  
- JavaFX dashboard  
- JSON model export  
- Graph-based reasoning powered by Neo4j  

## Features

### 1. Multi-format Input

The analyzer supports:

- `.java` files (compiled automatically → `.class`)  
- `.class` files (reflection)  
- `.jar` files (reflection-based bytecode loader)  
- Full project directory scanning (recursive)

### 2. Extracted Information

For each class, the system extracts:

- Class name  
- Fields  
- Methods with parameters  
- Superclass  
- Interfaces  

**Metrics:**

- WMC (Weighted Methods per Class)  
- ATFD (Access to Foreign Data)  
- TCC (Cohesion metric)  
- LOC approximation  

### 3. Neo4j Integration

- Model exported to `/tmp/model.json`  
- Automatically imported into Neo4j  
- Each smell detected using Cypher graph queries  

### 4. Detected Bad Smells

| #  | Bad Smell            | Description                                 |
|----|----------------------|---------------------------------------------|
| 1  | God Class            | High complexity, many responsibilities       |
| 2  | Large Class          | Too many methods/fields                      |
| 3  | Long Method          | Methods with high LOC/complexity             |
| 4  | Long Parameter List  | Methods with >5 parameters                   |
| 5  | Feature Envy         | Methods using another class more than their own |
| 6  | Data Class           | State only, little behavior                  |
| 7  | Middle Man           | Excessive delegation                         |
| 8  | Message Chains       | Sequences like m1 → m2 → m3                  |
| 9  | Shotgun Surgery      | Many callers affect the same method/class    |
| 10 | Divergent Change     | Low cohesion (TCC < 0.2)                     |

## JavaFX Dashboard

- Scrollable, structured smell categories  
- TitledPane groups by severity  
- Automatic results population  
- Multi-file support  
- Full project UI workflow  

## Project Structure

```
reflect/
│
├── app/
│   ├── Main.java
│   └── controller/
│       ├── MainController.java
│       └── ResultsController.java
│
├── core/
│   ├── detection/
│   ├── reflection/
│   ├── neo4j/
│   └── metrics/
│
├── resources/
│   ├── main.fxml
│   └── results.fxml
│
└── README.md
```

## Requirements

| Component | Version |
|----------|---------|
| Java     | 17+ |
| JavaFX   | 21 |
| Neo4j    | 5.x |
| Maven    | 3.x |

## Run the Project

### 1. Clone
```sh
git clone https://github.com/Yvesei/reflect
cd reflect
```

### 2. Build
```sh
mvn clean package
```

### 3. Run
```sh
java   --module-path /opt/javafx/lib   --add-modules javafx.controls,javafx.fxml   -jar target/reflect.jar
```

## Using the App

### Step 1
```
+------------------------+
|     Java Analyzer      |
+------------------------+
| [Open Java Project]    |
| Status: Waiting…       |
+------------------------+
```

### Step 2  
Choose `.java`, `.class`, `.jar`, or a whole folder.

### Step 3  
- Compile  
- Build model  
- Export JSON → `/tmp/model.json`  
- Import Neo4j  
- Detect smells  
- Show dashboard  

## Output JSON Example

```json
[
  {
    "name": "ExampleClass",
    "attributes": ["x", "y"],
    "methods": [
      {"name": "compute", "parameters": ["int", "int"]},
      {"name": "helper", "parameters": []}
    ],
    "superClass": "java.lang.Object",
    "interfaces": [],
    "metrics": {
      "wmc": 12,
      "atfd": 3,
      "tcc": 0.083
    }
  }
]
```

## Architecture

```
UI → Analyzer → Model → JSON → Neo4j Import → Smell Detector → Results UI
```

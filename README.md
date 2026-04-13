# SOEN 342 - Winter 2026 Project

## Team Information

| Name | Student ID | GitHub Username |
|------|------------|-----------------|
| Wiame Kotbi | 40299105 | `wiamekotbi` |
| Maria Laghmari | 40297167 | `marialagh` |
| Marrwah Burke | 40299631 | `MarrwahBurke` |

## Course Information

- Course: SOEN 342 - Software Requirements and Deployment
- Term: Winter 2026

## Project Overview

This project is a Java-based task management system that supports:

- importing tasks from CSV files
- searching and viewing tasks
- exporting tasks to CSV
- exporting tasks to iCal (`.ics`)
- listing overloaded collaborators
- persisting task data with SQLite

## Configuration

### Prerequisites

Before running the project, make sure you have:

- Java 17 or newer installed
- a terminal opened at the project root
- the SQLite JDBC driver available in `lib/sqlite-jdbc-3.46.1.3.jar`

### Project folders used at runtime

- `src/main/java` contains the source code
- `lib/` contains the SQLite JDBC dependency
- `data/soen342-tasks.db` is the SQLite database created/used by the application
- `samples/` contains sample CSV and iCal files
- `out/` stores compiled `.class` files

## How To Compile

Run the following from the project root:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
cmd /c "javac -cp lib\sqlite-jdbc-3.46.1.3.jar -d out src\main\java\ca\concordia\soen342\poc\model\*.java src\main\java\ca\concordia\soen342\poc\repository\*.java src\main\java\ca\concordia\soen342\poc\search\*.java src\main\java\ca\concordia\soen342\poc\overload\*.java src\main\java\ca\concordia\soen342\poc\csv\*.java src\main\java\ca\concordia\soen342\poc\ical\*.java src\main\java\ca\concordia\soen342\poc\*.java"
```

## How To Run

### Run the main application

```powershell
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.Driver
```

The main menu lets you:

- import tasks from a CSV file
- search and view tasks
- export tasks to CSV
- export tasks to iCal
- list overloaded collaborators
- view all tasks

### Run the demo programs

CSV import/export demo:

```powershell
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.CsvImportExportDemo
```

iCal export demo:

```powershell
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.IcsExportDemo
```

## How To Test

Run the self-tests with:

```powershell
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.CsvImportExportSelfTest
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.IcsExportSelfTest
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.TaskSearchViewSelfTest
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.OverloadedCollaboratorsSelfTest
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.SqliteTaskRepositorySelfTest
```

Expected success messages include:

- `CSV self-test passed.`
- `All iCal self-tests passed.`
- `Task search/view self-test passed.`
- `All overloaded collaborator self-tests passed.`
- `SQLite repository self-test passed.`

For additional testing details, see [TESTING.md](TESTING.md).

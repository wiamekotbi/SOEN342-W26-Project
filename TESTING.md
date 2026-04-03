# Testing Instructions

This project uses Java `main` classes as self-tests instead of JUnit. Persistence is implemented with SQLite, so the SQLite JDBC driver in `lib/` must be included when compiling and running.

## Prerequisites

- Java 17 or newer installed
- A terminal opened at the project root

## 1. Compile the project

Run:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
cmd /c "javac -cp lib\sqlite-jdbc-3.46.1.3.jar -d out src\main\java\ca\concordia\soen342\poc\model\*.java src\main\java\ca\concordia\soen342\poc\repository\*.java src\main\java\ca\concordia\soen342\poc\search\*.java src\main\java\ca\concordia\soen342\poc\overload\*.java src\main\java\ca\concordia\soen342\poc\csv\*.java src\main\java\ca\concordia\soen342\poc\ical\*.java src\main\java\ca\concordia\soen342\poc\*.java"
```

This creates compiled `.class` files in the `out` folder.

## 2. Run the self-tests

Run each test with:

```powershell
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.CsvImportExportSelfTest
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.IcsExportSelfTest
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.TaskSearchViewSelfTest
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.OverloadedCollaboratorsSelfTest
java -cp "out;lib/sqlite-jdbc-3.46.1.3.jar" ca.concordia.soen342.poc.SqliteTaskRepositorySelfTest
```

## Expected results

If everything is working correctly, you should see:

- `CSV self-test passed.`
- `All iCal self-tests passed.`
- `Task search/view self-test passed.`
- `All overloaded collaborator self-tests passed.`
- `SQLite repository self-test passed.`

## Notes

- The CSV self-test creates temporary files in your system temp folder.
- The database-backed demos persist to `data/soen342-tasks.db`.
- The iCal self-test writes `.ics` files in the `samples` folder:
  - `samples/selftest-single.ics`
  - `samples/selftest-project.ics`
  - `samples/selftest-filtered.ics`

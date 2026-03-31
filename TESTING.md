# Testing Instructions

This project uses Java `main` classes as self-tests instead of JUnit.

## Prerequisites

- Java 17 or newer installed
- A terminal opened at the project root

## 1. Compile the project

Run:

```powershell
javac -d out (Get-ChildItem -Recurse src\main\java\*.java | ForEach-Object { $_.FullName })
```

This creates compiled `.class` files in the `out` folder.

## 2. Run the self-tests

Run each test with:

```powershell
java -cp out ca.concordia.soen342.poc.CsvImportExportSelfTest
java -cp out ca.concordia.soen342.poc.IcsExportSelfTest
java -cp out ca.concordia.soen342.poc.TaskSearchViewSelfTest
java -cp out ca.concordia.soen342.poc.OverloadedCollaboratorsSelfTest
```

## Expected results

If everything is working correctly, you should see:

- `CSV self-test passed.`
- `All iCal self-tests passed.`
- `Task search/view self-test passed.`
- `All overloaded collaborator self-tests passed.`

## Notes

- The CSV self-test creates temporary files in your system temp folder.
- The iCal self-test writes `.ics` files in the `samples` folder:
  - `samples/selftest-single.ics`
  - `samples/selftest-project.ics`
  - `samples/selftest-filtered.ics`

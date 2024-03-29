# PE Parser

A simple PE parser written in Java that returns detailed warnings and errors.

## Usage

First, create an input stream.

```java
// Stream from file
CadesStreamReader stream = new CadesFileStream(new File("peFile.dll"));
// Stream from bytes
CadesStreamReader stream = new CadesBufferStream(peFileBytes);
// Or, make your own stream by implementing CadesStreamReader
```

Next, attempt to parse the PE headers _(and print any errors or warnings)_.

```java
// Print the error on failure
PeImage pe = PeImage.read(stream).ifErr(err -> {
    System.out.println("Error: " + err);
}).ifOk(val -> { // Print any warnings after parsing
    for (ParseError warning : val.warnings)
        System.out.println("Warning: " + warning);
}).getOkOrDefault(null); // Return the parsed value, or null
```

Now, you can print any info.

```java
if (pe != null) {
    System.out.println("is64bit: " + pe.ntHeaders.is64bit());

    pe.imports.ifOk(imports -> {
        for (LibraryImports lib : imports) {
            System.out.printf("%s imports from \"%s\":%n", lib.entries.size(), lib.name);
            for (ImportEntry entry : lib.entries)
                System.out.printf("\tname=%s, ordinal=%s%n", entry.name, entry.ordinal);
        }
    }).ifErr(err -> System.out.printf("No imports: %s%n", err.toString()));

    pe.exports.ifOk(exports -> {
        System.out.printf("This file exports under the library name \"%s\"%n", exports.name);
        for (ExportEntry entry : exports.entries)
            System.out.printf("\tname=%s, ordinal=%s%n", entry.name, entry.ordinal);
    }).ifErr(err -> System.out.printf("No exports: %s%n", err.toString()));
}
```

## More usage

Much of the data in a portable executable is optional.
Optional data uses the `ParseResult` class,
which holds either a value or `ParseError` object.
There are multiple ways to handle `ParseResult`.

You can manually check for values and errors accordingly.

```java
ParseResult<ArrayList<LibraryImports>> imports = pe.imports;
if (imports.isOk())
    printImports(imports.getOk());
else
    System.out.println(imports.getErr());
```

You can ignore any errors and only use the `ok` values _(when available)_.

```java
pe.imports.ifOk(imports -> printImports(imports));
```

Or, you can use a classic `null` check _(and log errors on the side)_.

```java
ArrayList<LibraryImports> imports = pe.imports
        .ifErr(err -> System.out.println(err)) // Optional call
        .getOkOrDefault(null);

if (imports != null)
    printImports(imports);
```

## Commit reminders
- Update version in `pom.xml`
- Compile with `mvn package`
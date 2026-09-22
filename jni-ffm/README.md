# JNI Examples Project

A simple Java Native Interface (JNI) example project that demonstrates calling C library functions from Java and callback functionality from C to Java.

## Overview

This project showcases:
- Calling a native C function from Java
- Passing string parameters from Java to C
- Returning integer values from C to Java
- Callback functionality from C back to Java methods
- Cross-platform build system (macOS, Linux, Windows)

## Project Structure

```
jni-examples/
├── src/main/
│   ├── java/com/ibm/examples/jni/
│   │   ├── JNIExamples.java      # Main JNI class with native method declarations
│   │   └── JNIExampleMain.java   # Demo application
│   └── c/
│       ├── jni_examples.c        # C implementation of native methods
│       └── jni_examples.h        # JNI header file
├── build/                        # Generated build directory
│   ├── classes/                  # Compiled Java classes
│   └── lib/                      # Compiled native library
├── Makefile                      # Cross-platform build script
└── README.md                     # This file
```

## What This Example Does

The example implements a simple "RNG seed improvement" function that:

1. **Java Side (`JNIExamples.java`)**:
   - Accepts a string input (initial seed)
   - Validates and truncates input if necessary
   - Calls the native C function `improveRNGSeed0`

2. **C Side (`jni_examples.c`)**:
   - Receives the string from Java
   - Applies a hash function (djb2 variant) to generate a better seed
   - Demonstrates callback to Java by calling `setInfo` method
   - Returns an improved integer seed

3. **Demo Application (`JNIExampleMain.java`)**:
   - Tests the functionality with various input strings
   - Shows the input-to-output transformation
   - Demonstrates error handling

## Prerequisites

Before building this project, ensure you have:

- **Java Development Kit (JDK) 8 or later**
- **GCC or Clang compiler**
- **Make utility**

### Platform-Specific Notes

#### macOS
- Xcode Command Line Tools: `xcode-select --install`
- Java: Install from Oracle or use `brew install openjdk`

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install build-essential default-jdk
```

#### Linux (CentOS/RHEL)
```bash
sudo yum groupinstall "Development Tools"
sudo yum install java-1.8.0-openjdk-devel
```

#### Windows
- Install MinGW-w64 or use Microsoft Build Tools
- Install JDK from Oracle or OpenJDK
- Ensure `gcc` and `make` are in your PATH

## Build Instructions

### Quick Start

1. **Clone or navigate to the project directory**
2. **Build everything**:
   ```bash
   make
   ```
3. **Run the example**:
   ```bash
   make run
   ```

### Step-by-Step Build

1. **Compile Java classes**:
   ```bash
   make classes
   ```

2. **Build native library**:
   ```bash
   make all
   ```

3. **Run the demonstration**:
   ```bash
   make run
   ```

### Additional Make Targets

- `make clean` - Remove all build artifacts
- `make help` - Show available targets
- `make header` - Generate JNI header file (optional)

### Manual Build (Alternative)

If you prefer not to use the Makefile:

1. **Compile Java classes**:
   ```bash
   mkdir -p build/classes
   javac -d build/classes src/main/java/com/ibm/examples/jni/*.java
   ```

2. **Generate JNI header** (optional):
   ```bash
   javah -d src/main/c -classpath build/classes com.ibm.examples.jni.JNIExamples
   ```

3. **Compile native library**:

   **macOS**:
   ```bash
   mkdir -p build/lib
   gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin \
       -dynamiclib -undefined dynamic_lookup \
       -o build/lib/libjni-examples.dylib src/main/c/jni_examples.c
   ```

   **Linux**:
   ```bash
   mkdir -p build/lib
   gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux \
       -shared -fPIC \
       -o build/lib/libjni-examples.so src/main/c/jni_examples.c
   ```

   **Windows**:
   ```bash
   mkdir -p build/lib
   gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/win32 \
       -shared -Wl,--add-stdcall-alias \
       -o build/lib/libjni-examples.dll src/main/c/jni_examples.c
   ```

4. **Run the application**:
   ```bash
   java -Djava.library.path=build/lib -cp build/classes com.ibm.examples.jni.JNIExampleMain
   ```

## Expected Output

When you run the example, you should see output similar to:

```
=== JNI Examples Demo ===

Testing improveRNGSeed function:
Input String -> Improved Seed
-----------------------------
Calling back from native: 5
'hello' -> 210676686
Calling back from native: 5
'world' -> 113318802
Calling back from native: 4
'java' -> 1702063201
Calling back from native: 6
'native' -> 1730031616
Calling back from native: 9
'interface' -> 1847647584
...

=== Test with very long string ===
Original length: 400 characters
Calling back from native: 63
Improved seed: 1234567890

=== Demo Complete ===
Note: The 'Calling back from native' messages show
the native C code calling back into Java methods.
```

## How JNI Works in This Example

### 1. Library Loading
```java
static {
    System.loadLibrary("jni-examples");
}
```
This loads the native library when the class is first loaded.

### 2. Native Method Declaration
```java
public native int improveRNGSeed0(String initialSeed);
```
This declares a method that will be implemented in native code.

### 3. JNI Function Naming Convention
The C function name follows the pattern:
```
Java_<package_name>_<class_name>_<method_name>
```
So our method becomes:
```c
Java_com_ibm_examples_jni_JNIExamples_improveRNGSeed0
```

### 4. JNI Function Signature
```c
JNIEXPORT jint JNICALL Java_com_ibm_examples_jni_JNIExamples_improveRNGSeed0
  (JNIEnv *env, jobject thisObj, jstring initialSeed)
```

- `JNIEnv *env` - Interface to JVM
- `jobject thisObj` - Reference to the Java object
- `jstring initialSeed` - The Java string parameter

### 5. String Handling
```c
const char *nativeString = (*env)->GetStringUTFChars(env, initialSeed, 0);
// ... use the string ...
(*env)->ReleaseStringUTFChars(env, initialSeed, nativeString);
```

### 6. Calling Back to Java
```c
jclass cls = (*env)->GetObjectClass(env, thisObj);
jmethodID mid = (*env)->GetStaticMethodID(env, cls, "setInfo", "(I)V");
(*env)->CallStaticVoidMethod(env, cls, mid, (jint)len);
```

## Troubleshooting

### Common Issues

1. **"java.lang.UnsatisfiedLinkError: no jni-examples in java.library.path"**
   - Ensure the native library was built successfully
   - Check that you're using the correct `-Djava.library.path` option
   - Verify the library name matches what's expected on your platform

2. **"javah: command not found"**
   - Use `javac -h` instead (Java 8+), or install a JDK that includes javah

3. **"jni.h: No such file or directory"**
   - Ensure `JAVA_HOME` is set correctly
   - Install the JDK (not just JRE)

4. **Permission denied errors**
   - Ensure the native library has execute permissions
   - On Linux/macOS: `chmod +x build/lib/libjni-examples.*`

### Platform-Specific Issues

#### macOS
- If you get "developer tools not found", run: `xcode-select --install`
- For newer macOS versions, you might need to accept Xcode license: `sudo xcodebuild -license accept`

#### Linux
- Ensure you have the development packages installed
- Some distributions require explicit installation of JNI headers

#### Windows
- Ensure MinGW/MSYS2 is properly configured
- PATH should include the directory containing `gcc.exe`

## Learning Resources

- [Oracle JNI Documentation](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [JNI Types and Data Structures](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html)
- [JNI Functions](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/functions.html)

## License

This example project is provided for educational purposes (only).
Feel free to use and modify as needed - but this is not production code and is purely provided for demonstration purposes.

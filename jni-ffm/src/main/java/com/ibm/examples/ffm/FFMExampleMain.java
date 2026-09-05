package com.ibm.examples.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * Main class to demonstrate FFM functionality
 * 
 * This class shows how to call native C functions from Java using FFM
 *
 * To run:
 * java -cp build/classes -Djava.library.path=./build/lib com.ibm.examples.ffm.FFMExampleMain
 *
 */
public class FFMExampleMain {
    private static final String LIB_NAME = System.mapLibraryName("jni-examples");
    private static final String PATH_TO_LIB = System.getProperty("java.library.path") +"/"+ LIB_NAME;
    private static final SymbolLookup libraryLookup = SymbolLookup.libraryLookup(PATH_TO_LIB, Arena.global());

    public static int getHashBase() {
        try (Arena offHeap = Arena.ofConfined()) {
            SymbolLookup libJniExamples = SymbolLookup.libraryLookup(PATH_TO_LIB, offHeap);
            MemorySegment msHashBase = libJniExamples.findOrThrow("hash_base").reinterpret(4);
            return msHashBase.get(JAVA_INT,0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // The Java method signature must expect a MemorySegment, and it's up to the Java code to convert it as needed
    public static int printObj(MemorySegment s) {
        System.out.println(s);
        System.out.println(s.reinterpret(Integer.MAX_VALUE).getString(0));
        return 42;
    }

    public static int improveRNGSeed(String initialSeed) {
        if (initialSeed.length() > 1024) {
            initialSeed = initialSeed.substring(0, 1023);
        }
        if (initialSeed == null) {
            initialSeed = "";
        }

        MemorySegment hsHash = libraryLookup.findOrThrow("hash_string");
        FunctionDescriptor funcDef = FunctionDescriptor.of(JAVA_INT, ADDRESS);
        Linker linker = Linker.nativeLinker();
        MethodHandle hHashString = linker.downcallHandle(hsHash, funcDef);

        MemorySegment cString = Arena.global().allocateFrom(initialSeed);
        try {
            return (int)hHashString.invoke(cString);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void upcallExample(String param) {
        // First get a native method handle for the C function we will call from Java
        var linker = Linker.nativeLinker();
        // The last parameter in the function descriptor is the callback
        var funcDefWithCallback = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
        var callbackFfm = libraryLookup.findOrThrow("callback_ffm");
        MethodHandle mhCallback = linker.downcallHandle(callbackFfm, funcDefWithCallback);

        // Now get a Java method handle for the Java method we'll call back to
        var lookup = MethodHandles.lookup();
        MethodHandle mh = null;
        try {
            mh = lookup.findStatic(lookup.lookupClass(), "printObj", MethodType.methodType(int.class, MemorySegment.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Convert the callback MH to an upcall stub
        var funcDef = FunctionDescriptor.of(JAVA_INT, ADDRESS);
        MemorySegment pPrintObj = linker.upcallStub(mh, funcDef, Arena.global());

        MemorySegment cString = Arena.global().allocateFrom(param);
        try {
            var res = mhCallback.invoke(cString, pPrintObj);
            System.out.println(res);
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        System.out.println("=== FFM Examples Demo ===");
        System.out.println();


        int hashBase = getHashBase();
        System.out.printf("Hash Base -> %d%n", hashBase);
        System.out.println();
        System.out.println();

        // Test cases with different seed strings
        String[] testSeeds = {
            "hello",
            "world",
            "java",
            "native",
            "interface",
            "random-seed-string",
            "this-is-a-longer-seed-value-for-testing",
            "",
            "123456789"
        };
        
        System.out.println("Testing hash_string function:");
        System.out.println("Input String -> Improved Seed");
        System.out.println("-----------------------------");
        
        for (String seed : testSeeds) {
            try {
                int improvedSeed = improveRNGSeed(seed);
                System.out.printf("'%s' -> %d%n", seed, improvedSeed);
                // Do upcalls next
                upcallExample(seed);
            } catch (Exception e) {
                System.err.printf("Error processing seed '%s': %s%n", seed, e.getMessage());
            }
        }
        System.out.println();

        System.out.println("=== Demo Complete ===");
    }

}
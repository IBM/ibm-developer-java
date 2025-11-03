package com.ibm.examples.ffm;

import com.ibm.examples.auto.FFMAuto;
import com.ibm.examples.auto.callback_ffm$printObj;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class FFAutoMain {

    public static void main(String[] args) {
        System.out.println("=== FFM Auto Examples Demo ===");
        System.out.println();

        int hashBase = FFMAuto.hash_base();
        System.out.printf("Hash Base -> %d%n", hashBase);
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
                MemorySegment cString = Arena.ofAuto().allocateFrom(seed);
                int improvedSeed = FFMAuto.hash_string(cString);
                System.out.printf("'%s' -> %d%n", seed, improvedSeed);
                // Do upcalls next
                MemorySegment upcall = callback_ffm$printObj.allocate(s -> FFMExampleMain.printObj(s), Arena.ofAuto());
                FFMAuto.callback_ffm(cString, upcall);
            } catch (Exception e) {
                System.err.printf("Error processing seed '%s': %s%n", seed, e.getMessage());
            }
        }
        System.out.println();

        System.out.println("=== Demo Complete ===");
    }

}

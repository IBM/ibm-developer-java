# JNI Examples Makefile
# Supports macOS (Darwin), Linux, and Windows (with MinGW)

# Detect operating system
UNAME_S := $(shell uname -s)

# Java paths - adjust these if needed
JAVA_HOME ?= $(shell /usr/libexec/java_home 2>/dev/null || echo /usr/lib/jvm/default-java)
JAVA_INCLUDE = $(JAVA_HOME)/include

# Platform-specific settings
ifeq ($(UNAME_S),Darwin)
    # macOS
    JAVA_PLATFORM_INCLUDE = $(JAVA_INCLUDE)/darwin
    LIB_EXT = dylib
    SHARED_FLAG = -dynamiclib
    PLATFORM_FLAGS = -undefined dynamic_lookup
else ifeq ($(UNAME_S),Linux)
    # Linux
    JAVA_PLATFORM_INCLUDE = $(JAVA_INCLUDE)/linux
    LIB_EXT = so
    SHARED_FLAG = -shared
    PLATFORM_FLAGS = -fPIC
else
    # Windows (assume MinGW)
    JAVA_PLATFORM_INCLUDE = $(JAVA_INCLUDE)/win32
    LIB_EXT = dll
    SHARED_FLAG = -shared
    PLATFORM_FLAGS = -Wl,--add-stdcall-alias
endif

# Directories
SRC_DIR = src/main
JAVA_SRC_DIR = $(SRC_DIR)/java
JNI_SRC_DIR = $(JAVA_SRC_DIR)/com/ibm/examples/jni
C_SRC_DIR = $(SRC_DIR)/c
BUILD_DIR = build
CLASSES_DIR = $(BUILD_DIR)/classes
LIB_DIR = $(BUILD_DIR)/lib

# Source files
C_SOURCES = $(C_SRC_DIR)/jni_examples.c

# Output files
LIBRARY_NAME = libjni-examples.$(LIB_EXT)
LIBRARY_PATH = $(LIB_DIR)/$(LIBRARY_NAME)

# Compiler settings
CC = gcc
CFLAGS = -Wall -O2 $(PLATFORM_FLAGS)
INCLUDES = -I$(JAVA_INCLUDE) -I$(JAVA_PLATFORM_INCLUDE) -I$(C_SRC_DIR)

# Default target
all: $(LIBRARY_PATH) classes

# Create build directories
$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

$(CLASSES_DIR): $(BUILD_DIR)
	mkdir -p $(CLASSES_DIR)

$(LIB_DIR): $(BUILD_DIR)
	mkdir -p $(LIB_DIR)

# Compile Java classes
classes: $(CLASSES_DIR)
	find $(JAVA_SRC_DIR) -name "*.java" -exec javac -d $(CLASSES_DIR) {} +

# Only compile JNI classes
jni-classes: $(CLASSES_DIR)
	find $(JNI_SRC_DIR) -name "*.java" -exec javac -d $(CLASSES_DIR) {} +

# Compile native library
$(LIBRARY_PATH): $(LIB_DIR) $(C_SOURCES)
	$(CC) $(CFLAGS) $(INCLUDES) $(SHARED_FLAG) $(PLATFORM_FLAGS) -o $@ $(C_SOURCES)

# Generate JNI header (optional - we created it manually)
header: classes
	javah -d $(C_SRC_DIR) -classpath $(CLASSES_DIR) com.ibm.examples.jni.JNIExamples

# Run the example
run: all
	java -Djava.library.path=$(LIB_DIR) -cp $(CLASSES_DIR) com.ibm.examples.jni.JNIExampleMain

# Clean build artifacts
clean:
	rm -rf $(BUILD_DIR)

# Help target
help:
	@echo "Available targets:"
	@echo "  all     - Build everything (default)"
	@echo "  classes - Compile Java classes"
	@echo "  header  - Generate JNI header file"
	@echo "  run     - Run the example application"
	@echo "  clean   - Remove build artifacts"
	@echo "  help    - Show this help message"
	@echo ""
	@echo "Environment variables:"
	@echo "  JAVA_HOME - Path to Java installation"

.PHONY: all classes header run clean help 
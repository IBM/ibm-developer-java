#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "com_ibm_examples_jni_JNIExamples.h"

#define ERR_FIND_CLASS_FAILED -1
#define ERR_GET_STATIC_METHOD_FAILED -2
#define ERR_CALL_STATIC_METHOD_FAILED -3

extern unsigned int hash_base = 5381;

/*
 * Simple hash function to convert a string into a better RNG seed
 * Uses a variant of the djb2 hash algorithm
 */
 unsigned int hash_string(const char* str) {
    int c;
    unsigned int hash = hash_base;

    while ((c = *str++)) {
        hash = ((hash << 5) + hash) + c; /* hash * 33 + c */
    }
    
    return hash;
}

int callback(JNIEnv* env, jobject obj, jint val);

/*
 * Class:     com_ibm_examples_jni_JNIExamples
 * Method:    improveRNGSeed0
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_ibm_examples_jni_JNIExamples_improveRNGSeed0
  (JNIEnv *env, jobject thisObj, jstring initialSeed) {
    
    // Get the string from Java
    const char *nativeString = (*env)->GetStringUTFChars(env, initialSeed, 0);
    
    if (nativeString == NULL) {
        return 0; // Handle null case
    }
    
    // Compute improved seed using hash function
    unsigned int improvedSeed = hash_string(nativeString);
    
    // Add some additional randomization based on string length
    int len = strlen(nativeString);
    improvedSeed = improvedSeed ^ (len * 31);
    
    // Release the string
    (*env)->ReleaseStringUTFChars(env, initialSeed, nativeString);

    jint ret = (jint)(improvedSeed & 0x7FFFFFFF);
    // Demonstrate callback to Java method
    callback(env, thisObj, ret);

    // Return the improved seed (ensure it's positive)
    return ret;
}


int callback_ffm(const char * str, int (*printObj)(const void *)) {
    return printObj(str);
}

int callback(JNIEnv* env, jobject obj, jint val) {

    jclass cls = (*env)->FindClass(env, "com/ibm/examples/jni/JNIExamples");
    if ((*env)->ExceptionCheck(env)) {
        return ERR_FIND_CLASS_FAILED;
    }

    jmethodID method = (*env)->GetStaticMethodID(env, cls, "setInfo", "(I)V");
    if ((*env)->ExceptionCheck(env)) {
        return ERR_GET_STATIC_METHOD_FAILED;
    }

    (*env)->CallStaticVoidMethod(env, cls, method, val);
    if ((*env)->ExceptionCheck(env)) {
        return ERR_CALL_STATIC_METHOD_FAILED;
    }

    return 0;
}

int sumValues2(JNIEnv* env, jobject obj, jobject allValues){
   jclass cls = (*env)->GetObjectClass(env,allValues);
   jfieldID a = (*env)->GetFieldID(env, cls, "a", "I");
   jfieldID b = (*env)->GetFieldID(env, cls, "b", "I");
   jfieldID c = (*env)->GetFieldID(env, cls, "c", "I");
   jfieldID d = (*env)->GetFieldID(env, cls, "d", "I");
   jfieldID e = (*env)->GetFieldID(env, cls, "e", "I");
   jfieldID f = (*env)->GetFieldID(env, cls, "f", "I");

   jint avalue = (*env)->GetIntField(env, allValues, a);
   jint bvalue = (*env)->GetIntField(env, allValues, b);
   jint cvalue = (*env)->GetIntField(env, allValues, c);
   jint dvalue = (*env)->GetIntField(env, allValues, d);
   jint evalue = (*env)->GetIntField(env, allValues, e);
   jint fvalue = (*env)->GetIntField(env, allValues, f);
   return avalue + bvalue + cvalue + dvalue + evalue + fvalue;
}

// Reaching back
jlong getElement(JNIEnv* env, jobject obj, jlongArray arr_j,
                 int element){
   jboolean isCopy;
   jlong result;
   jlong* buffer_j = (*env)->GetLongArrayElements(env, arr_j, &isCopy);
   result = buffer_j[element];
   (*env)->ReleaseLongArrayElements(env, arr_j, buffer_j, 0);
   return result;
}

// Reaching back
jlong getElement2(JNIEnv* env, jobject obj, jlongArray arr_j,
                  int element){
     jlong result;
     (*env)->GetLongArrayRegion(env, arr_j, element, 1, &result);
     return result;
}

void workOnArray(JNIEnv* env, jobject obj, jarray array){
   jint i;
   jint count = (*env)->GetArrayLength(env, array);
   for (i=0; i < count; i++) {
      jobject element = (*env)->GetObjectArrayElement(env, array, i);
      if((*env)->ExceptionOccurred(env)) {
         break;
      }

      /* do something with array element */
   }
}

void workOnArray2(JNIEnv* env, jobject obj, jarray array){
   jint i;
   jint count = (*env)->GetArrayLength(env, array);
   for (i=0; i < count; i++) {
      jobject element = (*env)->GetObjectArrayElement(env, array, i);
      if((*env)->ExceptionOccurred(env)) {
         break;
      }

      /* do something with array element */

      (*env)->DeleteLocalRef(env, element);
   }
}

jchar noexceptions(JNIEnv* env, jobject obj) {
    jclass objectClass;
    jfieldID fieldID;
    jchar result = 0;

    objectClass = (*env)->GetObjectClass(env, obj);
    fieldID = (*env)->GetFieldID(env, objectClass, "charField", "C");
    result = (*env)->GetCharField(env, obj, fieldID);

    return result;
}

void withexceptions(JNIEnv* env, jobject obj) {
    jclass objectClass;
    jfieldID fieldID;
    jchar result = 0;

    objectClass = (*env)->GetObjectClass(env, obj);
    fieldID = (*env)->GetFieldID(env, objectClass, "charField", "C");
    if((*env)->ExceptionOccurred(env)) {
        return;
    }

    result = (*env)->GetCharField(env, obj, fieldID);
}

void modifyArrayWithoutRelease(JNIEnv* env, jobject obj, jarray arr1) {
   jboolean isCopy;
   jbyte* buffer = (*env)->GetByteArrayElements(env,arr1,&isCopy);
   if ((*env)->ExceptionCheck(env)) return;

   buffer[0] = 1;
}

void modifyArrayWithRelease(JNIEnv* env, jobject obj, jarray arr1) {
   jboolean isCopy;
   jbyte* buffer = (*env)->GetByteArrayElements(env,arr1,&isCopy);
   if ((*env)->ExceptionCheck(env)) return;

   buffer[0] = 1;

   (*env)->ReleaseByteArrayElements(env, arr1, buffer, JNI_COMMIT);
   if ((*env)->ExceptionCheck(env)) return;
}

void processBufferHelper(jbyte* buffer);

void workOnPrimitiveArray(JNIEnv* env, jobject obj, jarray arr1) {
   jboolean isCopy;
   jbyte* buffer = (*env)->GetPrimitiveArrayCritical(env, arr1, &isCopy);
   if ((*env)->ExceptionCheck(env)) return;

   processBufferHelper(buffer);

   (*env)->ReleasePrimitiveArrayCritical(env, arr1, buffer, 0);
   if ((*env)->ExceptionCheck(env)) return;
}

void lostGlobalRef(JNIEnv* env, jobject obj, jobject keepObj) {
   jobject gref = (*env)->NewGlobalRef(env, keepObj);
}

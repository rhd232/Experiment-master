#include <jni.h> //定义了所有JNI函数和数据类型
#include <string>

/*extern "C"
JNIEXPORT jstring JNICALL
Java_mainActivity_stringFromJNI(
        JNIEnv *env,
        jobject) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}*/

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wind_xxx_JniHelper_getSecretKey(  JNIEnv *env,
                                                   jclass jclazz){
    std::string key="ovQcVIao4ciJTpBGISR74I7LXge8u2pt";
    return env->NewStringUTF(key.c_str());

}

static JNINativeMethod gMethods[] = {
        {"getSecretKey", "()Ljava/lang/String;", (void *) Java_com_wind_xxx_JniHelper_getSecretKey}
};

static int registerNativeMethods(JNIEnv* env, const char*className, JNINativeMethod* gMethods,int numMethods){
    jclass  clazz;
    clazz=env->FindClass(className);
    if (clazz==NULL){
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz,gMethods,numMethods)<0){
        return JNI_FALSE;
    }
    return JNI_TRUE;
}
static int registerNatives(JNIEnv* env){

    return registerNativeMethods(env,"com/wind/xxx/JniHelper",gMethods, sizeof(gMethods)/ sizeof(gMethods[0]));
}
/*
 * 虚拟机执行System.loadLibrary()方法后，进入该so文件后会首先执行该方法
 */
jint JNI_OnLoad(JavaVM* vm,void* reserved){
    JNIEnv* env = NULL;
    jint result=-1;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    if (registerNatives(env)==JNI_TRUE){
        result=JNI_VERSION_1_4;
    }
    return result;
}


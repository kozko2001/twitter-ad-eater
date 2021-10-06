package com.acme.twitteradeater

import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Field
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import java.io.*
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


@JvmOverloads
fun hookFun(clsName: String, clsLoader: ClassLoader, funName: String, vararg args: Any) {
    XposedHelpers.findAndHookMethod(clsName, clsLoader, funName, *args)
}

fun Any?.call(methodName: String, vararg args: Any): Any? {
    return XposedHelpers.callMethod(this, methodName, *args)
}


operator fun Any.get(name: String): Any? = when (this) {
    is Class<*> -> try {
        getField(this, name)?.apply {
            isAccessible = true
        }?.get(null)
    } catch (e: NoSuchFieldException) {
        null
    }
    else -> try {
        getField(this.javaClass, name)?.apply {
            isAccessible = true
        }?.get(this)
    } catch (e: NoSuchFieldException) {
        null
    }
}


private fun getField(clazz: Class<*>, name: String): Field? = try {
    clazz.getDeclaredField(name)
} catch (e: NoSuchFieldException) {
    clazz.superclass?.let {
        getField(it, name)
    }
}


fun String.newInstance(clsLoader: ClassLoader): Any {
    return this.toClass(clsLoader).newInstance()
}

fun String.toClass(clsLoader: ClassLoader): Class<*> = XposedHelpers.findClass(this, clsLoader)

fun Any?.safeToString(): String = this?.toString() ?: ""

private val UTF8 = Charset.forName("UTF-8")

private fun getCharset(contentType: Any?): Charset {
    return contentType?.let { XposedHelpers.callMethod(contentType, "charset", UTF8) as Charset }
        ?: UTF8
}

class TwitterAdEaterModule : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: LoadPackageParam?) {
        log("NOM NOM NOM %s", lpparam!!.packageName)

        if (lpparam.packageName == "com.acme.twitteradeater") {
            Utils.readPrefs()
            logcat("prefs = %s", Utils.prefs.all)
        }

        if (lpparam.packageName == "com.twitter.android") {
            Utils.readPrefs()
            logcat("prefs = %s", Utils.prefs.all)
            log("Twitter app loaded, initializing hooks...")
            initHooks(lpparam)
        }
    }
    private val hasHookedInterceptorSet = Collections.synchronizedSet(HashSet<Class<*>>())

    private fun initHooks(lpparam: LoadPackageParam) {

        XposedHelpers.findAndHookConstructor("okhttp3.OkHttpClient", lpparam.classLoader, "okhttp3.OkHttpClient.Builder", object
            : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val builder = param.args?.getOrNull(0) ?: return
                val firstUserInterceptorCls = (builder["interceptors"] as? ArrayList<*>)?.getOrNull(0)?.javaClass
                    ?: return

                if (hasHookedInterceptorSet.add(firstUserInterceptorCls)) {
                    hookInterceptor(firstUserInterceptorCls.name, lpparam.classLoader)
                }
            }
        })
    }

    private fun hookInterceptor(interceptorName: String, clsLoader: ClassLoader) {
        hookFun(interceptorName, clsLoader, "intercept", "okhttp3.Interceptor.Chain", object :XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val copyRequest = param.args?.get(0)?.call("request")?.call("newBuilder")?.call("build")
                val copyResponse = param.result?.call("newBuilder")?.call("build")
                val copyResponseBody = copyResponse?.call("body")

                val path = (copyRequest.call("url").call("uri") as? URI)?.path

                if(path == null || !path.contains("/timeline/")) {
                    return
                }

                if(copyResponseBody == null) {
                    return
                }

                val contentType = copyResponseBody?.call("contentType")
                val p = copyResponseBody.call("byteStream") as InputStream
                val b = GZIPInputStream(p).readBytes()

                if(b == null) {
                    return
                }
                val body = String(b, getCharset(contentType))

                val modifiedBody = JsonTimeline(body).removePromote()


                val obj = ByteArrayOutputStream()
                val gzip = GZIPOutputStream(obj)

                val pp = modifiedBody.toByteArray( getCharset(contentType))

                gzip.write(pp)
                gzip.flush()
                gzip.close()
                val outputBytes2 = obj.toByteArray()

                val responseBody = XposedHelpers.callStaticMethod("okhttp3.ResponseBody".toClass(clsLoader),
                    "create", contentType, outputBytes2)
                param.result = param.result?.call("newBuilder")?.call("body", responseBody)?.call("build")
            }
        })
    }
}

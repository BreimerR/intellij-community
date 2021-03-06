// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity.*

class IdeaLogger : IFernflowerLogger() {
  private val LOG = Logger.getInstance(IdeaDecompiler::class.java)

  class InternalException(message: String, cause: Throwable) : RuntimeException(message, cause)

  private var myClass: String? = null

  override fun writeMessage(message: String, severity: IFernflowerLogger.Severity) {
    val text = extendMessage(message)
    when (severity) {
      ERROR -> LOG.warn(text)
      WARN -> LOG.warn(text)
      INFO -> LOG.info(text)
      else -> LOG.debug(text)
    }
  }

  override fun writeMessage(message: String, severity: IFernflowerLogger.Severity, t: Throwable) {
    when (t) {
      is InternalException -> throw t
      is ProcessCanceledException -> throw t
      is InterruptedException -> throw ProcessCanceledException(t)
    }

    if (severity == ERROR) {
      throw InternalException(extendMessage(message), t)
    }
    else {
      val text = extendMessage(message)
      when (severity) {
        WARN -> LOG.warn(text, t)
        INFO -> LOG.info(text, t)
        else -> LOG.debug(text, t)
      }
    }
  }

  private fun extendMessage(message: String) = if (myClass != null) "$message [$myClass]" else message

  override fun startReadingClass(className: String) {
    LOG.debug("decompiling class $className")
    myClass = className
  }

  override fun endReadingClass() {
    LOG.debug("... class decompiled")
    myClass = null
  }

  override fun startClass(className: String) = LOG.debug("processing class $className")

  override fun endClass() = LOG.debug("... class processed")

  override fun startMethod(methodName: String) = LOG.debug("processing method $methodName")

  override fun endMethod() = LOG.debug("... method processed")

  override fun startWriteClass(className: String) = LOG.debug("writing class $className")

  override fun endWriteClass() = LOG.debug("... class written")
}
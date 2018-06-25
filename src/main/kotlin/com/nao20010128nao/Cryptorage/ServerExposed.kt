package com.nao20010128nao.Cryptorage

import com.nao20010128nao.Cryptorage.internal.CryptorageServer
import net.freeutils.httpserver.HTTPServer

fun Cryptorage.asHttpServer(port: Int = 80, indexHtmlAsRoot: Boolean, supportPartial: Boolean): HTTPServer = CryptorageServer(port, this, indexHtmlAsRoot, supportPartial)
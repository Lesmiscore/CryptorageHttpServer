package com.nao20010128nao.Cryptorage.internal

import com.nao20010128nao.Cryptorage.Cryptorage
import net.freeutils.httpserver.HTTPServer

internal class CryptorageServer(
        port: Int,
        private val cryptorage: Cryptorage,
        private val indexHtmlAsRoot: Boolean,
        private val supportPartial: Boolean
) : HTTPServer(port) {
    private val files = cryptorage.list().asList()

    override fun serve(req: Request, resp: Response) {
        val path = req.path!!.replace("^/".toRegex(), "").let {
            // try to decode the path
            try {
                it.split('/').last().split('.').first()
            } catch (e: Throwable) {
                it
            }
        }
        val whatToDeliver: String? = path.run {
            val slashIndex = mustBeEndedWith("/", "index.html")
            if (this in cryptorage.list()) {
                this
            } else if (indexHtmlAsRoot && slashIndex in cryptorage.list()) {
                slashIndex
            } else {
                null
            }
        }
        if (whatToDeliver == null) {
            resp.sendHeaders(200)
            resp.body.writer().flushing {
                write("""<!doctype html>
<html>
<head>
<title>Index of /</title>
</head>
<body>
<h1>Index of /</h1>
<hr />
""")
                val toSend = files.filter { it.toLowerCase().contains(path.toLowerCase()) }
                toSend.forEach {
                    val inName = StringBuilder(it.toByteArray().encodeHex())
                    if (it.contains('.')) {
                        inName.append('.')
                        inName.append(it.split("\\.".toRegex()).last())
                    }
                    write("""
<a href="/$inName">$it</a><br />
""")
                }
                write("""
</body>
</html>
""")
            }
        } else {
            var start = 0
            var length = -1
            if (supportPartial && "Range" in req.headers) {
                val range = req.headers["Range"]
                require(range.startsWith("bytes="))
                val bytesRange = range.substring(6).split('-')
                start = bytesRange[0].toInt()
                if (!range.endsWith("-")) {
                    length = bytesRange[1].toInt() - start
                }
            }
            if(supportPartial){
                resp.headers.add("Accept-Ranges", "bytes")
            }
            resp.sendHeaders(
                    if (start == 0) 200 else 206,
                    if (length == -1) cryptorage.size(whatToDeliver) - start else length.toLong(),
                    cryptorage.lastModified(whatToDeliver),
                    null,
                    HTTPServer.getContentType(whatToDeliver, null),
                    if (length == -1) null else longArrayOf(start.toLong(), start.toLong() + length - 1, cryptorage.size(whatToDeliver))
            )
            val source = cryptorage.open(whatToDeliver, start).openStream().limit(length.toLong()).buffered()
            source.copyTo(resp.body)
        }
    }
}
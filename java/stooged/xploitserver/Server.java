package stooged.xploitserver;

import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {

    public Server(int SvrPort)
    {
        super(SvrPort);
    }

    @Override
    public Response serve(IHTTPSession session)
    {
        Map<String, String> headers = session.getHeaders();
        Method method = session.getMethod();
        String uri = session.getUri();
        Map<String, String> files = new HashMap<>();

        if (Method.POST.equals(method) || Method.PUT.equals(method)) {
            try {
                session.parseBody(files);
            } catch (IOException e) {
                return getResponse("Error: " + e.getMessage());
            } catch (ResponseException e) {
                return NanoHTTPD.newFixedLengthResponse(e.getStatus(), MIME_PLAINTEXT, e.getMessage());
            }
        }

        if (uri.contains("/document/")) // replace ps4 user guide path just incase
        {
            String[] urispl = uri.split("/");
            uri = "/" + urispl[urispl.length-1];
        }

        if (uri.equals("/") || uri.isEmpty())
        {
            uri =  "/index.html";
        }

        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }
        File f = new File(Environment.getExternalStorageDirectory().toString() + "/xPloitServer/" + uri);
        if (uri.toLowerCase().contains(f.getName().toLowerCase()))
        {
            return serveFile(f.getName(), headers, f);
        }
        else
        {
            return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404: File not found");
        }
    }

    private Response serveFile(String uri, Map<String, String> header, File file) {
        Response res;
        String mime = getMimeTypeForFile(uri);
        try {
            String etag = Integer.toHexString((file.getPath() + file.lastModified() + "" + file.length()).hashCode());
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);
                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getResponse("Forbidden: Reading file failed");
        }
        return (res == null) ? getResponse("Error 404: File not found") : res;
    }

    private Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = NanoHTTPD.newChunkedResponse(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = NanoHTTPD.newFixedLengthResponse(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response getResponse(String message) {
        return createResponse(Response.Status.OK, "text/plain", message);
    }
}

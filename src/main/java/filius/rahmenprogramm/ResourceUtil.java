package filius.rahmenprogramm;

import java.io.File;
import java.net.URL;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import filius.Main;

public class ResourceUtil {

    public static File getResourceFile(String relativePath) {
        String path = getResourcePath(relativePath);
        if (path == null) {
            return null;
        }
        return new File(path);
    }

    public static String getResourcePath(String relativePath) {
        String urlEncodedPath = getResourceUrlEncodedPath(relativePath);
        String path = null;
        if (urlEncodedPath != null) {
            try {
                Main.debug.println(urlEncodedPath);
                path = URIUtil.decode(urlEncodedPath);
                Main.debug.println("Resolved path: " + path);
            } catch (URIException e) {
                Main.debug.println("Resource " + relativePath + " could not be resolved (" + urlEncodedPath + ")");
            }
        }
        return path;
    }

    public static String getResourceUrlEncodedPath(String relativePath) {
        String urlEncodedPath = null;
        URL systemResource = ClassLoader.getSystemResource(relativePath);
        if (null == systemResource) {
            Main.debug.println("Resource " + relativePath + " could not be found!");
        } else {
            Main.debug.println("Resource " + systemResource);
            urlEncodedPath = systemResource.getPath().replace("+", "%2b");
        }
        return urlEncodedPath;
    }
}

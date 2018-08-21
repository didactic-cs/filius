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
        URL systemResource = ClassLoader.getSystemResource(relativePath);
        if (null == systemResource) {
            Main.debug.println("Resource " + relativePath + " could not be found!");
            return null;
        }
        String urlEncodedPath = systemResource.getPath();
        String path = null;
        try {
            path = URIUtil.decode(urlEncodedPath);
        } catch (URIException e) {
            Main.debug.println("Resource " + relativePath + " could not be resolved (" + urlEncodedPath + ")");
        }
        return path;
    }
}

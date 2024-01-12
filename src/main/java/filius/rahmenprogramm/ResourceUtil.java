package filius.rahmenprogramm;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLDecoder;

public class ResourceUtil {
    private static Logger LOG = LoggerFactory.getLogger(ResourceUtil.class);

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
            LOG.debug(urlEncodedPath);
            try {
                path = URLDecoder.decode(urlEncodedPath, "utf8");
            } catch (Exception e) {
            }
            LOG.debug("Resolved path: " + path);
        }
        return path;
    }

    public static String getResourceUrlEncodedPath(String relativePath) {
        String urlEncodedPath = null;
        URL systemResource = ClassLoader.getSystemResource(relativePath);
        if (null == systemResource) {
            LOG.debug("Resource " + relativePath + " could not be found!");
        } else {
            LOG.debug("Resource " + systemResource);
            urlEncodedPath = systemResource.getPath().replace("+", "%2b");
        }
        return urlEncodedPath;
    }
}

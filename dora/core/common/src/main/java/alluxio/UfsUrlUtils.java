package alluxio;

import alluxio.grpc.MountPointInfo;
import alluxio.grpc.UfsUrl;
import alluxio.uri.Authority;
import alluxio.uri.URI;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.List;

/**
 *
 * TODO: implement the equivalence of URI
 * TODO: implement the equivalence of PathUtils
 * TODO: implement the equivalence of AlluixoURI
 * TODO: move/rename the UTs of above, to make sure we have guaranteed the same functionalities
 */
public class UfsUrlUtils {
  // TODO: try to avoid the copy by a RelativeUrl class
  public static UfsUrl getParentURL(UfsUrl ufsPath) {
    List<String> pathComponents = ufsPath.getPathComponentsList();
    return UfsUrl.newBuilder().setScheme(ufsPath.getScheme()).setAuthority(ufsPath.getAuthority())
        // TODO: how many copies are there
        .addAllPathComponents(pathComponents.subList(0, pathComponents.size() - 1)).build();
  }

  // TODO: try to avoid the copy by a RelativeUrl class
  public static UfsUrl getChildURL(UfsUrl ufsPath, String childName) {
    List<String> pathComponents = ufsPath.getPathComponentsList();
    return UfsUrl.newBuilder().setScheme(ufsPath.getScheme()).setAuthority(ufsPath.getAuthority())
        .addAllPathComponents(pathComponents).addPathComponents(childName).build();
  }

  // TODO(jiacheng): this should be implemented elsewhere, where the MountTable is accessible
  public static MountPointInfo findMountPoint(UfsUrl ufsPath) {
    return null;
  }

  public static boolean isPrefix(UfsUrl ufsPath, UfsUrl another, boolean allowEquals) {
    // TODO: implement this
    return false;
  }

  /**
   * Generate an URL in scheme://authority/path format
   */
  public static String asString(UfsUrl ufsPath) {
    // TODO: consider corner cases
    StringBuilder sb = new StringBuilder();
    sb.append(ufsPath.getScheme());
    sb.append("://");
    sb.append(ufsPath.getAuthority());
    sb.append(AlluxioURI.SEPARATOR);
    List<String> pathComponents = ufsPath.getPathComponentsList();
    for (int i = 0; i < pathComponents.size(); i++) {
      sb.append(pathComponents.get(i));
      if (i < pathComponents.size() - 1) {
        sb.append(AlluxioURI.SEPARATOR);
      }
      // TODO: need a trailing separator if the path is dir?
    }
    return sb.toString();
  }

  // TODO: implement equals(a, b)

  /**
   * Creates a {@link URI} from a string.
   *
   * @param uriStr URI string to create the {@link URI} from
   * @return the created {@link URI}
   */
  public static UfsUrl create(String uriStr) {
    Preconditions.checkArgument(uriStr != null, "Can not create a uri with a null path.");

    // TODO: throw error if it is windows format
    // add a slash in front of paths with Windows drive letters
//    if (AlluxioURI.hasWindowsDrive(uriStr, false)) {
//      uriStr = "/" + uriStr;
//    }

    // parse uri components
    String scheme = null;
    String authority = null;

    int start = 0;

    // parse uri scheme, if any
    int colon = uriStr.indexOf(':');
    int slash = uriStr.indexOf('/');
    if ((colon != -1) && ((slash == -1) || (colon < slash))) { // has a scheme
      if (slash != -1) {
        // There is a slash. The scheme may have multiple parts, so the scheme is everything
        // before the slash.
        start = slash;

        // Ignore any trailing colons from the scheme.
        while (slash > 0 && uriStr.charAt(slash - 1) == ':') {
          slash--;
        }
        scheme = uriStr.substring(0, slash);
      } else {
        // There is no slash. The scheme is the component before the first colon.
        scheme = uriStr.substring(0, colon);
        start = colon + 1;
      }
    }

    // parse uri authority, if any
    if (uriStr.startsWith("//", start) && (uriStr.length() - start > 2)) { // has authority
      int nextSlash = uriStr.indexOf('/', start + 2);
      int authEnd = nextSlash > 0 ? nextSlash : uriStr.length();
      // TODO: sanity check for the string
      authority = uriStr.substring(start + 2, authEnd);
      start = authEnd;
    }

    // uri path is the rest of the string -- fragment not supported
    String path = uriStr.substring(start, uriStr.length());

    // TODO: we do not support a query format like path?id=abc, do we want to throw an error?
    //  A ? check can be slow
//    // Parse the query part.
//    int question = path.indexOf('?');
//    if (question != -1) {
//      // There is a query.
//      query = path.substring(question + 1);
//      path = path.substring(0, question);
//    }
    return create(scheme, authority, path);
  }

  /**
   * Creates a {@link UfsUrlUtils} from components.
   *
   * @param scheme the scheme string of the URI
   * @param authority the authority of the URI
   * @param path the path component of the URI
   * @return the created {@link URI}
   */
  public static UfsUrl create(String scheme, Authority authority, String path) {
    Preconditions.checkArgument(path != null, "Can not create a uri with a null path.");
    Preconditions.checkArgument(scheme != null && !scheme.isEmpty(), "Scheme is empty");
    return UfsUrl.newBuilder().setScheme(scheme).setAuthority(authority.toString())
        .addAllPathComponents(Arrays.asList(path.split(AlluxioURI.SEPARATOR))).build();
  }

  public static UfsUrl create(String scheme, String authorityStr, String path) {
    Preconditions.checkArgument(path != null, "Can not create a uri with a null path.");
    Preconditions.checkArgument(scheme != null && !scheme.isEmpty(), "Scheme is empty");
    return UfsUrl.newBuilder().setScheme(scheme).setAuthority(authorityStr)
            .addAllPathComponents(Arrays.asList(path.split(AlluxioURI.SEPARATOR))).build();
  }

  /**
   * Resolves a child {@link URI} against a parent {@link URI}.
   *
   * @param parent the parent
   * @param childPathComponents the child
   * @return the created {@link URI}
   */
  public static UfsUrl create(UfsUrl parent, String[] childPathComponents) {
    if (childPathComponents == null || childPathComponents.length == 0) {
      return parent;
    }
    return UfsUrl.newBuilder().setScheme(parent.getScheme()).setAuthority(parent.getAuthority())
            .addAllPathComponents(parent.getPathComponentsList())
            .addAllPathComponents(Arrays.asList(childPathComponents)).build();
  }

  /**
   *
   * @return a new URI based off a URI and a new path component
   */
  public static UfsUrl create(UfsUrl parent, String childPath) {
    return create(parent, childPath.split(AlluxioURI.SEPARATOR));
  }

  public static String getFullPath(UfsUrl ufsPath) {
    return Strings.join(ufsPath.getPathComponentsList(), AlluxioURI.SEPARATOR.charAt(0));
  }

  public static AlluxioURI toAlluxioURI(UfsUrl ufsPath) {
    return new AlluxioURI(ufsPath.getScheme(), Authority.fromString(ufsPath.getAuthority()), getFullPath(ufsPath));
  }
}
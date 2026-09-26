package net.sourceforge.kolmafia.scripts.svn.dav;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class DavXml {
  static final String DAV_NS = "DAV:";
  static final String SVN_DAV_NS = "http://subversion.tigris.org/xmlns/dav/";

  private DavXml() {}

  static Document parse(String body) throws SubversionException {
    try {
      var factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      var builder = factory.newDocumentBuilder();
      return builder.parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    } catch (ParserConfigurationException e) {
      throw new SubversionException("Could not configure XML parser", e);
    } catch (Exception e) {
      throw new SubversionException("Malformed response from Subversion server", e);
    }
  }

  static List<Element> children(Element parent, String namespace, String localName) {
    var found = new ArrayList<Element>();
    var nodes = parent.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      var node = nodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) continue;
      var element = (Element) node;
      if (!localName.equals(element.getLocalName())) continue;
      if (!namespace.equals(element.getNamespaceURI())) continue;
      found.add(element);
    }
    return found;
  }

  static Element child(Element parent, String namespace, String localName) {
    var found = children(parent, namespace, localName);
    return found.isEmpty() ? null : found.getFirst();
  }

  static List<Element> descendants(Document document, String namespace, String localName) {
    var found = new ArrayList<Element>();
    var nodes = document.getElementsByTagNameNS(namespace, localName);
    for (int i = 0; i < nodes.getLength(); i++) {
      found.add((Element) nodes.item(i));
    }
    return found;
  }

  static String text(Element parent, String namespace, String localName) {
    var element = child(parent, namespace, localName);
    return element == null ? null : element.getTextContent();
  }

  static Element successfulProp(Element response) {
    for (var propstat : children(response, DAV_NS, "propstat")) {
      var status = text(propstat, DAV_NS, "status");
      if (status == null || !status.contains(" 200 ")) continue;
      return child(propstat, DAV_NS, "prop");
    }
    return null;
  }
}

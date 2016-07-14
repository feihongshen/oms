package cn.explink.b2c.tools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import java.io.PrintStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class WsUtil
{
  private static XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
  private static final String DATE_FORMAT = "yyyyMMddhh24mmss";

  public static <T> T xmlToPojo(String xmlStr, Class<T> clazz)
  {
    XStream xstream = new XStream(new DomDriver());
    xstream.processAnnotations(clazz);
    String newXmlStr = removeXMLHeaderStr(xmlStr);
    Object obj = xstream.fromXML(newXmlStr);
    return (T) obj;
  }

  public static String pojoToXml(Object obj)
  {
    String xmlStr = "";

    xstream.processAnnotations(obj.getClass());
    xmlStr = xstream.toXML(obj);
    return xmlStr;
  }

  public static String getXmlStr(Object obj)
  {
    String str = pojoToXml(obj);
    String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + replaceStrBlank(str);
    return result;
  }

  public static String getXmlStrWithBlank(Object obj)
  {
    xstream.processAnnotations(obj.getClass());
    StringWriter sw = new StringWriter();
    xstream.marshal(obj, new CompactWriter(sw));

    String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + sw.toString();
    String Newresult = result.replace("__", "_");
    return Newresult;
  }

  public static String getXmlStrGBKWithBlank(Object obj)
  {
    xstream.processAnnotations(obj.getClass());
    StringWriter sw = new StringWriter();
    xstream.marshal(obj, new CompactWriter(sw));

    String result = "<?xml version=\"1.0\" encoding=\"GBK\" standalone=\"yes\"?>" + sw.toString();
    String Newresult = result.replace("__", "_");
    return Newresult;
  }

  public static String removeXMLHeaderStr(String orgXml)
  {
    String pix = "<?";
    String suf = "?>";
    String newXml = "";
    if ((orgXml.indexOf(pix) != -1) && (orgXml.indexOf(suf) != -1)) {
      int sufIndex = orgXml.indexOf(suf) + suf.length();
      newXml = orgXml.substring(sufIndex, orgXml.length());
    } else {
      return orgXml;
    }
    return newXml;
  }

  public static String getNowDate()
  {
    String nowDate = "";
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhh24mmss");
    nowDate = sdf.format(date);
    return nowDate;
  }

  public static String replaceStrBlank(String xmlStr)
  {
    String strNoBlank = "";
    if (StringUtils.isNotEmpty(xmlStr)) {
      Pattern p = Pattern.compile("\\s*|\t|\r|\n");
      Matcher m = p.matcher(xmlStr);
      strNoBlank = m.replaceAll("");
    }
    return strNoBlank;
  }

  public static String replaceStr(String xmlStr)
  {
    String strNoBlank = "";
    if (StringUtils.isNotEmpty(xmlStr)) {
      Pattern p = Pattern.compile("\\*|\t|\r|\n");
      Matcher m = p.matcher(xmlStr);
      strNoBlank = m.replaceAll("");
    }
    return strNoBlank;
  }

  public static String isNull(String str, String newStr)
  {
    if ((str == null) || (str.equals("")) || (str.equals("null"))) {
      str = newStr;
    }
    return str;
  }

  public static String isNull(String str)
  {
    if ((str == null) || (str.equals("")) || (str.equals("null"))) {
      str = "";
    }
    return str;
  }

  public static String getXmlStrNoHeader(Object obj)
  {
    xstream.processAnnotations(obj.getClass());
    StringWriter sw = new StringWriter();
    xstream.marshal(obj, new CompactWriter(sw));

    String result = sw.toString();
    String Newresult = result.replace("__", "_");
    return Newresult;
  }

  public static void main(String[] args)
  {
    String a = getNowDate();
    System.out.println(a);
  }
}
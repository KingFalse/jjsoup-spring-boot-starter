package me.kagura.util;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JJsoupUtil {
    /**
     * 将document中所有相对路径的href跟src转换成绝对路径
     *
     * @param document
     * @return
     */
    public static Document convertToAbsUrlDocument(Document document) {
        Validate.notEmpty(document.baseUri(), "document.baseUri() must not be empty");
        Elements relativePathElements = document.select("[src^=./],[src^=../],[src^=/]:not([src^=//]),[src^=/]:not([src^=//]),[href^=./],[href^=../],[href^=/]:not([href^=//]),[href^=/]:not([href^=//])");
        for (Element element : relativePathElements) {
            if (element.hasAttr("href")) {
                element.attr("href", element.attr("abs:href"));
            }
            if (element.hasAttr("src")) {
                element.attr("src", element.attr("abs:src"));
            }
        }
        return document;
    }
}

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
        Elements relativePathElements = document.select("[src],[href]");
        for (Element element : relativePathElements) {
            if (element.hasAttr("href")) {
                String href = element.attr("href");
                if (!href.matches("^http[s]?://[\\d\\D]*") && !href.equals("#")) {
                    element.attr("href", element.attr("abs:href"));
                }
            }
            if (element.hasAttr("src")) {
                String src = element.attr("src");
                if (!src.matches("^http[s]?://[\\d\\D]*") && !src.startsWith("data:image/")) {
                    element.attr("src", element.attr("abs:src"));
                }

            }

        }
        return document;
    }

}

package me.kagura.util;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JJsoupUtil {

    static String script = System.getProperty("java.io.tmpdir") + "sleep.vbs";
    static boolean isWin = System.getProperty("os.name").toLowerCase().startsWith("windows");

    /**
     * 将document中所有相对路径的href跟src转换成绝对路径，兼容伪协议
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
                if (!href.matches("^.*:[\\d\\D]*") && !href.equals("#")) {
                    element.attr("href", element.attr("abs:href"));
                }
            }
            if (element.hasAttr("src")) {
                String src = element.attr("src");
                if (!src.matches("^.*:[\\d\\D]*")) {
                    element.attr("src", element.attr("abs:src"));
                }

            }

        }
        return document;
    }

    /**
     * 执行系统对应的脚本进行sleep操作，单位毫秒
     *
     * @param ms
     */
    public static void sleep(int ms) {
        try {
            if (isWin) {
                sleepWin(ms);
            } else {
                sleepLinux(ms);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用Linux命令sleep，单位毫秒
     *
     * @param ms
     * @throws IOException
     * @throws InterruptedException
     */
    private static void sleepLinux(int ms) throws IOException, InterruptedException {
        Runtime.getRuntime().exec("sleep " + ms * 0.001).waitFor();
    }

    /**
     * 调用vbs脚本sleep，单位毫秒
     *
     * @param ms
     * @throws IOException
     * @throws InterruptedException
     */
    private static void sleepWin(int ms) throws IOException, InterruptedException {
        if (!new File(script).exists()) {
            FileWriter fileWriter = new FileWriter(script);
            fileWriter.write("WScript.sleep WScript.Arguments(0)");
            fileWriter.flush();
            fileWriter.close();
        }
        String cmd = String.format("CScript.exe %s %d >nul", script, ms);
        Runtime.getRuntime().exec(cmd).waitFor();
    }

}

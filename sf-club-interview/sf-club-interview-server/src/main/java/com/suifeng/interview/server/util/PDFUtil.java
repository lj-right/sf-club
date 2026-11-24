package com.suifeng.interview.server.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

@Slf4j
public class PDFUtil {

    private static Pattern pattern = Pattern.compile("\\s*|\t|\r|\n");

    /**
     * 获取pdf的text (通过远程URL)
     */
    public static String getPdfText(String pdfUrl) {
        try {
            URL url = new URL(pdfUrl);
            HttpURLConnection htpcon = (HttpURLConnection) url.openConnection();
            htpcon.setRequestMethod("GET");
            htpcon.setDoOutput(true);
            htpcon.setDoInput(true);
            htpcon.setUseCaches(false);
            htpcon.setConnectTimeout(10000);
            htpcon.setReadTimeout(10000);
            try (InputStream in = htpcon.getInputStream()) {
                return getPdfText(in);
            }
        } catch (Exception e) {
            log.error("通过URL获取pdf转为文字错误: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 获取pdf的text (通过输入流直接解析，无需MinIO存储)
     */
    public static String getPdfText(InputStream in) {
        PDDocument document = null;
        String text = "";
        try {
            document = PDDocument.load(in);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(0);
            stripper.setEndPage(Integer.MAX_VALUE);
            text = stripper.getText(document);
            text = pattern.matcher(text).replaceAll("");
            if (log.isInfoEnabled()) {
                log.info("识别到的pdf内容长度: {}", text.length());
            }
        } catch (Exception e) {
            log.error("从输入流解析pdf文字错误: {}", e.getMessage(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    log.error("close error", e);
                }
            }
        }
        return text;
    }
}

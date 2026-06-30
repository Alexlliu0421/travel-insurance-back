package com.example.travel_insurance_back.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.travel_insurance_back.dto.response.PolicyResponseDto;

@Component
public class PdfGenerator {

    private static final String BODY_FONT_PATH = "fonts/NotoSansTC-Regular.ttf";
    private static final String WATERMARK_FONT_PATH = "fonts/Clerical-Script.ttf";

    public byte[] generatePolicyPdf(PolicyResponseDto policy) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDFont chineseFont = loadChineseFont(document);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                writeTitle(content, chineseFont, "緯致旅行平安保險保單");
                writeField(content, chineseFont, 720, "保單號碼", policy.getPolicyNumber());
                writeField(content, chineseFont, 690, "保單狀態", translateStatus(policy.getStatus()));
                writeField(content, chineseFont, 650, "【被保人資訊】", "");
                writeField(content, chineseFont, 630, "姓名", policy.getInsuredName());
                writeField(content, chineseFont, 610, "身份證號", policy.getInsuredIdNumber());
                writeField(content, chineseFont, 590, "出生日期", policy.getInsuredBirthDate().toString());
                writeField(content, chineseFont, 570, "職業", policy.getOccupationName());
                writeField(content, chineseFont, 530, "【旅遊資訊】", "");
                writeField(content, chineseFont, 510, "出發日", policy.getDepartureDate().toString());
                writeField(content, chineseFont, 490, "回程日", policy.getReturnDate().toString());
                writeField(content, chineseFont, 470, "投保天數", policy.getInsuredDays() + " 天");
                writeField(content, chineseFont, 430, "【保費資訊】", "");
                writeField(content, chineseFont, 410, "保額", "NT$ " + policy.getCoverageAmount());
                writeField(content, chineseFont, 390, "基本保費", "NT$ " + policy.getBasePremium());
                writeField(content, chineseFont, 370, "實收保費", "NT$ " + policy.getFinalPremium());
                writeField(content, chineseFont, 330, "投保日期", policy.getCreatedDate().toLocalDate().toString());
            }

            addWatermark(document, page, translateStatus(policy.getStatus()));

            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("保單 PDF 產生失敗", e);
        }
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "DRAFT"    -> "待審核";
            case "SIGNING"  -> "審核中";
            case "FINISH"   -> "已核准";
            case "REJECTED" -> "已駁回";
            case "VOID"     -> "已取消";
            default         -> status;
        };
    }

    // 浮水印相關
    private void addWatermark(PDDocument document, PDPage page, String statusText) throws IOException {

        BufferedImage watermarkImage = createCircleWatermarkImage(statusText, "緯致旅平險");

        PDImageXObject pdImage = LosslessFactory.createFromImage(document, watermarkImage);

        try (PDPageContentStream content = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            float imageSize = 420f;
            float x = (pageWidth - imageSize) / 2;
            float y = (pageHeight - imageSize) / 2;

            content.drawImage(pdImage, x, y, imageSize, imageSize);
        }
    }

    private BufferedImage createCircleWatermarkImage(String mainText, String subText) throws IOException {
        int size = 500;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        Color circleColor = new Color(100, 150, 220, 50);
        g2d.setColor(circleColor);
        g2d.setStroke(new BasicStroke(6f));
        int margin = 6;
        g2d.drawOval(margin, margin, size - margin * 2, size - margin * 2);
        g2d.rotate(Math.toRadians(-8), size / 2.0, size / 2.0);

        Font mainFont = loadWatermarkFont(size, 3.6f);
        mainFont = mainFont.deriveFont(Font.BOLD, mainFont.getSize2D());
        g2d.setFont(mainFont);
        g2d.setColor(circleColor);

        FontMetrics mainFm = g2d.getFontMetrics();
        int mainTextWidth = mainFm.stringWidth(mainText);
        int mainTextX = (size - mainTextWidth) / 2;
        int mainTextY = size / 2 - 5;
        g2d.drawString(mainText, mainTextX, mainTextY);

        Font subFont = loadWatermarkFont(size, 8f);
        subFont = subFont.deriveFont(Font.BOLD, subFont.getSize2D());
        g2d.setFont(subFont);
        FontMetrics subFm = g2d.getFontMetrics();
        int subTextWidth = subFm.stringWidth(subText);
        int subTextX = (size - subTextWidth) / 2;
        int subTextY = mainTextY + mainFm.getAscent();
        g2d.drawString(subText, subTextX, subTextY);

        g2d.dispose();
        return image;
    }

    private Font loadWatermarkFont(int circleSize, float divisor) throws IOException {
        try (InputStream fontStream =
                     new ClassPathResource(WATERMARK_FONT_PATH).getInputStream()) {
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            return baseFont.deriveFont(Font.PLAIN, circleSize / divisor);
        } catch (FontFormatException e) {
            throw new IOException("浮水印字型載入失敗", e);
        }
    }

    // 保單內文相關
    private PDFont loadChineseFont(PDDocument document) throws IOException {
        try (InputStream fontStream =
                     new ClassPathResource(BODY_FONT_PATH).getInputStream()) {
            return PDType0Font.load(document, fontStream);
        }
    }

    private void writeTitle(PDPageContentStream content, PDFont font, String title) throws IOException {
        content.beginText();
        content.setFont(font, 18);
        content.newLineAtOffset(200, 780);
        content.showText(title);
        content.endText();
    }

    private void writeField(PDPageContentStream content, PDFont font,
                             float y, String label, String value) throws IOException {
        content.beginText();
        content.setFont(font, 12);
        content.newLineAtOffset(60, y);
        content.showText(label + (value.isBlank() ? "" : "：" + value));
        content.endText();
    }
}
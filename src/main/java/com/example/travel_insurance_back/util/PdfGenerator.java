package com.example.travel_insurance_back.util;

import com.example.travel_insurance_back.dto.response.PolicyResponseDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfGenerator {

    public byte[] generatePolicyPdf(PolicyResponseDto policy) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDFont chineseFont = loadChineseFont(document);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                writeTitle(content, chineseFont, "旅行平安保險保單");
                writeField(content, chineseFont, 720, "保單號碼", policy.getPolicyNumber());
                writeField(content, chineseFont, 690, "保單狀態", policy.getStatus());
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
                writeField(content, chineseFont, 330, "投保日期", policy.getCreatedAt().toLocalDate().toString());
            }

            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("保單 PDF 產生失敗", e);
        }
    }

    private PDFont loadChineseFont(PDDocument document) throws IOException {
        try (InputStream fontStream =
                     new ClassPathResource("fonts/NotoSansTC-Regular.ttf").getInputStream()) {
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
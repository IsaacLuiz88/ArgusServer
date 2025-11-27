//package com.argus.server.util;
//
//import com.lowagie.text.*;
//import com.lowagie.text.pdf.PdfWriter;
//import com.argus.server.model.EventEntity;
//
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//
//public class PDFUtil {
//
//    public static byte[] generate(List<EventEntity> events) throws Exception {
//        Document doc = new Document();
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PdfWriter.getInstance(doc, out);
//        doc.open();
//
//        doc.add(new Paragraph("Relatório Argus - Eventos"));
//        doc.add(new Paragraph(" "));
//
//        for (EventEntity e : events) {
//            doc.add(new Paragraph(e.toString()));
//        }
//
//        doc.close();
//        return out.toByteArray();
//    }
//}

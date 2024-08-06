package com.gmail.litalways.toolset.util;

import com.fasterxml.jackson.annotation.JsonValue;
import com.gmail.litalways.toolset.exception.ProxyException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Jasper报表生成 / PDF工具类
 *
 * @author IceRain
 * @since 2024/8/1
 */
public class PdfGenerateUtil {

    private static final Pattern base64Pattern = Pattern.compile("^[a-zA-Z0-9/+]{1000,}=*$");

    /**
     * 生成PDF报表
     *
     * @param vo 请求对象
     * @return PDF Base64
     * @throws ProxyException 生成失败
     */
    public static String generate(Request vo) throws ProxyException {
        if (vo.getSubReport() != null) {
            vo.getSubReport().sort(Comparator.comparingInt(Request.SubReport::getIndex));
            for (Request.SubReport sub : vo.getSubReport()) {
                if (base64Pattern.matcher(sub.getJrXml()).matches()) {
                    try {
                        sub.setJrXml(new String(Base64.getDecoder().decode(sub.getJrXml()), StandardCharsets.UTF_8));
                    } catch (IllegalArgumentException e) {
                        throw new ProxyException(MessageUtil.getMessage("util.report.generator.sub.report.base64.decode.failed", sub.getIndex()), e.getCause());
                    }
                }
                JasperReport jasper = designToReport(sub.getParamName(), sub.getJrXml());
                vo.getParams().put(sub.getParamName(), jasper);
            }
        }
        if (base64Pattern.matcher(vo.getJrXml()).matches()) {
            try {
                vo.setJrXml(new String(Base64.getDecoder().decode(vo.getJrXml()), StandardCharsets.UTF_8));
            } catch (IllegalArgumentException e) {
                throw new ProxyException(MessageUtil.getMessage("util.report.generator.base64.decode.failed"), e.getCause());
            }
        }
        JasperReport jasper = designToReport(MessageUtil.getMessage("util.report.generator.main.report"), vo.getJrXml());
        JRDataSource dataSource;
        if (vo.getDataSource() == null) {
            dataSource = new JREmptyDataSource();
        } else {
            // 利用dataSource生成DetailBand <-> SubReport
            for (Object data : vo.getDataSource()) {
                if (data instanceof Map) {
                    if (((Map<?, ?>) data).containsKey("jrXml")) {
                        // 是SubReport对象
                        //noinspection unchecked
                        Map<String, Object> d = ((Map<String, Object>) data);
                        if (base64Pattern.matcher((String) d.get("jrXml")).matches()) {
                            try {
                                d.put("jrXml", new String(Base64.getDecoder().decode((String) d.get("jrXml")), StandardCharsets.UTF_8));
                            } catch (IllegalArgumentException e) {
                                throw new ProxyException(MessageUtil.getMessage("util.report.generator.sub.report.datasource.base64.decode.failed", d.get("paramName")), e.getCause());
                            }
                        }
                        d.put((String) d.get("paramName"), designToReport((String) d.get("paramName"), (String) d.get("jrXml")));
                    }
                }
            }
            dataSource = new JRBeanCollectionDataSource(vo.getDataSource());
        }
        JasperPrint jasperPrint;
        try {
            jasperPrint = JasperFillManager.fillReport(jasper, vo.getParams(), dataSource);
        } catch (Exception e) {
            throw new ProxyException(MessageUtil.getMessage("util.report.generator.fill.error"), e);
        }
        String result;
        try {
            switch (vo.getExportType()) {
                case PDF:
                    byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint);
                    result = Base64.getEncoder().encodeToString(bytes);
                    break;
                case EXCEL:
                    ByteArrayOutputStream xlsx = new ByteArrayOutputStream();
                    JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                    xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(xlsx));
                    xlsxExporter.exportReport();
                    result = Base64.getEncoder().encodeToString(xlsx.toByteArray());
                    break;
                case WORD:
                    ByteArrayOutputStream docx = new ByteArrayOutputStream();
                    JRDocxExporter docxExporter = new JRDocxExporter();
                    docxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    docxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(docx));
                    docxExporter.exportReport();
                    result = Base64.getEncoder().encodeToString(docx.toByteArray());
                    break;
                case HTML:
                    StringBuffer html = new StringBuffer();
                    HtmlExporter htmlExporter = new HtmlExporter();
                    htmlExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    htmlExporter.setExporterOutput(new SimpleHtmlExporterOutput(html));
                    htmlExporter.exportReport();
                    result = html.toString();
                    break;
                default:
                    throw new ProxyException(MessageUtil.getMessage("util.report.generator.wrong.export.type"));
            }
        } catch (Throwable e) {
            throw new ProxyException(MessageUtil.getMessage("util.report.generator.export.failed"), e);
        }
        return result;
    }

    private static JasperReport designToReport(String name, String jrXml) throws ProxyException {
        JasperDesign design;
        try {
            design = JRXmlLoader.load(new ByteArrayInputStream(jrXml.getBytes(StandardCharsets.UTF_8)));
//            byte[] data = JRLoader.loadBytes(new ByteArrayInputStream(jrXml.getBytes(StandardCharsets.UTF_8)));
//            JacksonReportLoader xmlLoader = JacksonReportLoader.instance();
//            design = xmlLoader.loadReport(DefaultJasperReportsContext.getInstance(), data);
        } catch (Exception e) {
            throw new ProxyException(MessageUtil.getMessage("util.report.generator.load.error", name), e);
        }
        JasperReport jasper;
        try {
            jasper = JasperCompileManager.compileReport(design);
        } catch (Exception e) {
            throw new ProxyException(MessageUtil.getMessage("util.report.generator.compile.error", name), e);
        }
        return jasper;
    }

    @Data
    public static class Request {
        /**
         * 主报表jrXml内容，可以是base64，会使用UTF-8解码成jrXml
         */
        private String jrXml;
        /**
         * 子报表
         */
        private List<SubReport> subReport;
        /**
         * 参数
         */
        private Map<String, Object> params;
        /**
         * 数据源（用于主报表直接塞表格）
         */
        private List<?> dataSource;
        /**
         * 报表类型
         */
        private ExportType exportType;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SubReport {
            /**
             * 子报表加载顺序
             */
            private Integer index;
            /**
             * 子报表位于主报表的属性名
             */
            private String paramName;
            /**
             * 子报表jrXml内容，可以是base64，会使用UTF-8解码成jrXml
             */
            private String jrXml;
        }

        @Getter
        public enum ExportType {
            PDF("PDF"),
            EXCEL("EXCEL"),
            // Do not export reports having more than 63 columns. In Microsoft Word, you cannot create tables having more than 63 columns.
            WORD("WORD"),
            HTML("HTML")
            ;
            @JsonValue
            private final String code;
            ExportType(String code) {
                this.code = code;
            }
        }

    }

//    /**
//     * 重构XMLInputFactory获取方式
//     */
//    @Slf4j
//    static class JacksonReportLoader extends net.sf.jasperreports.engine.xml.JacksonReportLoader {
//        private static final JacksonReportLoader INSTANCE = new JacksonReportLoader();
//        public static JacksonReportLoader instance() {
//            return INSTANCE;
//        }
//        @Override
//        protected boolean detectRootElement(byte[] data, String elementName) {
//            ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
//            XMLInputFactory inputFactory = new WstxInputFactory();
//            inputFactory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
//            inputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
//            inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
//            try {
//                XMLStreamReader reader = inputFactory.createXMLStreamReader(dataStream);
//                boolean foundRoot = false;
//                boolean ended = false;
//                while(!ended && reader.hasNext()) {
//                    reader.next();
//                    if (reader.getEventType() == 1) {
//                        String localName = reader.getLocalName();
//                        String namespaceURI = reader.getNamespaceURI();
//                        if (elementName.equals(localName) && (namespaceURI == null || namespaceURI.isEmpty())) {
//                            foundRoot = true;
//                        }
//                        return foundRoot;
//                    }
//                }
//                return foundRoot;
//            } catch (XMLStreamException e) {
//                log.debug("failed to load jrXml", e);
//                return false;
//            }
//        }
//    }

    /**
     * 图片转PDF
     *
     * @param img 图片
     * @return PDF
     * @throws DocumentException PDF生成异常
     * @throws IOException 图片解析异常
     */
    public static byte[] image2Pdf(byte[] img) throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, byteArrayOutputStream);
        doc.open();
        Image image = Image.getInstance(img);
        image.getHeight();
        image.setAlignment(Image.MIDDLE);
        float height = image.getHeight();
        float width = image.getWidth();
        doc.setPageSize(new Rectangle(width, height));
        doc.newPage();
        doc.add(image);
        doc.close();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * PDF合并
     *
     * @param pdf1                PDF1
     * @param pdf2                PDF2
     * @param bookmarkWithPageNum 书签信息储存Map&lt;书签名称,书签页码>
     * @param bookmarkName        书签名称
     * @return PDF
     * @throws DocumentException PDF生成异常
     * @throws IOException PDF解析异常
     */
    public static byte[] pdfCombine(byte[] pdf1, byte[] pdf2, Map<String, Integer> bookmarkWithPageNum, String bookmarkName) throws DocumentException, IOException {
        PdfReader pdfReader = new PdfReader(pdf1);
        int numberOfPages = pdfReader.getNumberOfPages();
        PdfReader pdfReader2 = new PdfReader(pdf2);
        int numberOfPages2 = pdfReader2.getNumberOfPages();
        if (numberOfPages2 < 1) {
            return pdf1;
        }
        Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfCopy pdfCopy = new PdfCopy(doc, byteArrayOutputStream);
        doc.open();
        for (int p = 1; p <= numberOfPages; p++) {
            doc.newPage();
            PdfImportedPage importedPage = pdfCopy.getImportedPage(pdfReader, p);
            pdfCopy.addPage(importedPage);
        }
        if (bookmarkName != null && !bookmarkName.trim().isEmpty()) {
            int number = pdfCopy.getPageNumber();
            bookmarkWithPageNum.put(bookmarkName, number);
        }
        for (int p = 1; p <= numberOfPages2; p++) {
            doc.newPage();
            PdfImportedPage importedPage = pdfCopy.getImportedPage(pdfReader2, p);
            pdfCopy.addPage(importedPage);
        }
        doc.close();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 将书签信息应用至PDF中
     *
     * @param pdf                 PDF
     * @param bookmarkWithPageNum 书签信息储存Map&lt;书签名称,书签页码>
     * @return PDF
     * @throws DocumentException PDF生成异常
     * @throws IOException PDF解析异常
     */
    public static byte[] bookmark(byte[] pdf, Map<String, Integer> bookmarkWithPageNum) throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfCopy pdfCopy = new PdfCopy(doc, byteArrayOutputStream);
        pdfCopy.setViewerPreferences(PdfWriter.PageModeUseOutlines);
        doc.open();
        PdfReader pdfReader = new PdfReader(pdf);
        int numberOfPages = pdfReader.getNumberOfPages();
        for (int p = 1; p <= numberOfPages; p++) {
            doc.newPage();
            PdfImportedPage importedPage = pdfCopy.getImportedPage(pdfReader, p);
            pdfCopy.addPage(importedPage);
        }
        pdfCopy.freeReader(pdfReader);
        PdfOutline root = pdfCopy.getRootOutline();
        for (Map.Entry<String, Integer> mark : bookmarkWithPageNum.entrySet()) {
            new PdfOutline(root, PdfAction.gotoLocalPage(mark.getValue(), new PdfDestination(PdfDestination.FIT), pdfCopy), mark.getKey(), true);
        }
        doc.close();
        return byteArrayOutputStream.toByteArray();
    }

}

package util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Tạo file XLSX tối giản (1 sheet) mà không cần Apache POI. - Dùng inlineStr
 * cho toàn bộ cell (đơn giản, đủ mở bằng Excel).
 *
 * Lưu ý: đây là writer "tối thiểu", không can thiệp style/format nâng cao.
 */
public class XlsxExportUtil {

    public static void writeSingleSheetXlsx(OutputStream out,
            String sheetName,
            List<String> headers,
            List<List<String>> rows) throws IOException {
        if (sheetName == null || sheetName.isBlank()) {
            sheetName = "Sheet1";
        }

        int headerCols = headers == null ? 0 : headers.size();
        int dataCols = 0;
        if (rows != null) {
            for (List<String> r : rows) {
                if (r != null) {
                    dataCols = Math.max(dataCols, r.size());
                }
            }
        }
        int cols = Math.max(headerCols, dataCols);

        int totalRows = 1 + (rows == null ? 0 : rows.size());

        // Build sheet1.xml (inlineStr)
        String sheetXml = buildSheetXml(sheetName, headers, rows, cols);

        String contentTypes = buildContentTypes();
        String rels = buildRootRels();
        String workbookXml = buildWorkbookXml(sheetName);
        String workbookRels = buildWorkbookRels();
        String stylesXml = buildStylesXml();
        String coreProps = buildCoreProps();
        String appProps = buildAppProps();

        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            writeEntry(zos, "[Content_Types].xml", contentTypes);
            writeEntry(zos, "_rels/.rels", rels);

            writeEntry(zos, "xl/workbook.xml", workbookXml);
            writeEntry(zos, "xl/_rels/workbook.xml.rels", workbookRels);

            writeEntry(zos, "xl/worksheets/sheet1.xml", sheetXml);
            writeEntry(zos, "xl/styles.xml", stylesXml);

            writeEntry(zos, "docProps/core.xml", coreProps);
            writeEntry(zos, "docProps/app.xml", appProps);

            // Ensure zip is valid even when rows are empty
            if (totalRows <= 0) {
                // nothing
            }
        }
    }

    private static void writeEntry(ZipOutputStream zos, String name, String xml) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        zos.write(bytes);
        zos.closeEntry();
    }

    private static String buildSheetXml(String sheetName,
            List<String> headers,
            List<List<String>> rows,
            int cols) {
        String safeSheetName = escapeXml(sheetName);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
                .append("<sheetData>");

        // Header row = row 1
        sb.append("<row r=\"1\">");
        for (int c = 0; c < cols; c++) {
            String v = (headers != null && c < headers.size() && headers.get(c) != null)
                    ? headers.get(c) : "";
            String cellRef = columnName(c) + "1";
            sb.append("<c r=\"").append(escapeXml(cellRef)).append("\" t=\"inlineStr\">")
                    .append("<is><t xml:space=\"preserve\">").append(escapeXml(v)).append("</t></is>")
                    .append("</c>");
        }
        sb.append("</row>");

        int rowIdx = 2;
        if (rows != null) {
            for (List<String> r : rows) {
                sb.append("<row r=\"").append(rowIdx).append("\">");
                for (int c = 0; c < cols; c++) {
                    String v = (r != null && c < r.size() && r.get(c) != null) ? r.get(c) : "";
                    String cellRef = columnName(c) + rowIdx;
                    sb.append("<c r=\"").append(escapeXml(cellRef)).append("\" t=\"inlineStr\">")
                            .append("<is><t xml:space=\"preserve\">").append(escapeXml(v)).append("</t></is>")
                            .append("</c>");
                }
                sb.append("</row>");
                rowIdx++;
            }
        }

        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static String columnName(int idx0) {
        int idx = idx0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            int rem = idx % 26;
            sb.insert(0, (char) ('A' + rem));
            idx = (idx / 26) - 1;
            if (idx < 0) {
                break;
            }
        }
        return sb.toString();
    }

    private static String buildContentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>"
                + "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>"
                + "</Types>";
    }

    private static String buildRootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String buildWorkbookXml(String sheetName) {
        String safeName = escapeXml(sheetName);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets>"
                + "<sheet name=\"" + safeName + "\" sheetId=\"1\" r:id=\"rId1\"/>"
                + "</sheets>"
                + "</workbook>";
    }

    private static String buildWorkbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "</Relationships>";
    }

    private static String buildStylesXml() {
        // Minimal styles needed for Excel to open the file
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"1\">"
                + "<font>"
                + "<sz val=\"11\"/>"
                + "<color theme=\"1\"/>"
                + "<name val=\"Calibri\"/>"
                + "<family val=\"2\"/>"
                + "<scheme val=\"minor\"/>"
                + "</font>"
                + "</fonts>"
                + "<fills count=\"2\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "</fills>"
                + "<borders count=\"1\">"
                + "<border><left/><right/><top/><bottom/><diagonal/></border>"
                + "</borders>"
                + "<cellStyleXfs count=\"1\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/>"
                + "</cellStyleXfs>"
                + "<cellXfs count=\"1\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "</cellXfs>"
                + "<tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium9\" defaultPivotStyle=\"PivotStyleLight16\"/>"
                + "</styleSheet>";
    }

    private static String buildCoreProps() {
        String now = DateTimeFormatter.ISO_INSTANT
                .format(Instant.now().atZone(ZoneOffset.UTC));
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<cp:coreProperties "
                + "xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
                + "xmlns:dcterms=\"http://purl.org/dc/terms/\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<dc:title>RapViet Report</dc:title>"
                + "<dc:creator>RapViet</dc:creator>"
                + "<cp:lastModifiedBy>RapViet</cp:lastModifiedBy>"
                + "<dcterms:created xsi:type=\"dcterms:W3CDTF\">" + now + "</dcterms:created>"
                + "<dcterms:modified xsi:type=\"dcterms:W3CDTF\">" + now + "</dcterms:modified>"
                + "</cp:coreProperties>";
    }

    private static String buildAppProps() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" "
                + "xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/variant-types\">"
                + "<Application>RapViet CMS</Application>"
                + "</Properties>";
    }

    private static String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

package com.tdem.milesheetcreator;

import org.apache.poi.ss.usermodel.*;

import java.util.EnumMap;

public class Styles {
    private final CellStyle header;
    private final CellStyle centered;
    private final CellStyle bold;
    private final CellStyle bordered;
    private final CellStyle boldRight;

    private final EnumMap<CellStyleType, CellStyle> styles
            = new EnumMap<>(CellStyleType.class);

    public Styles(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        header = workbook.createCellStyle();
        header.setFont(headerFont);
        header.setBorderBottom(BorderStyle.MEDIUM);
        header.setBorderLeft(BorderStyle.MEDIUM);
        header.setBorderTop(BorderStyle.MEDIUM);
        header.setBorderRight(BorderStyle.MEDIUM);

        centered = workbook.createCellStyle();
        centered.setFont(headerFont);
        centered.setBorderBottom(BorderStyle.MEDIUM);
        centered.setBorderLeft(BorderStyle.MEDIUM);
        centered.setBorderTop(BorderStyle.MEDIUM);
        centered.setBorderRight(BorderStyle.MEDIUM);

        centered.setAlignment(HorizontalAlignment.CENTER);
        centered.setVerticalAlignment(VerticalAlignment.CENTER);

        bold = workbook.createCellStyle();
        bold.setFont(headerFont);
        bold.setAlignment(HorizontalAlignment.LEFT);
        bold.setBorderBottom(BorderStyle.THIN);
        bold.setBorderLeft(BorderStyle.THIN);
        bold.setBorderTop(BorderStyle.THIN);
        bold.setBorderRight(BorderStyle.THIN);

        bordered = workbook.createCellStyle();
        bordered.setBorderBottom(BorderStyle.THIN);
        bordered.setBorderLeft(BorderStyle.THIN);
        bordered.setBorderTop(BorderStyle.THIN);
        bordered.setBorderRight(BorderStyle.THIN);

        boldRight = workbook.createCellStyle();
        boldRight.cloneStyleFrom(bold);
        boldRight.setAlignment(HorizontalAlignment.RIGHT);

        styles.put(CellStyleType.HEADER, header);
        styles.put(CellStyleType.CENTERED_HEADER, centered);
        styles.put(CellStyleType.BOLD, bold);
        styles.put(CellStyleType.BORDERED, bordered);
        styles.put(CellStyleType.BOLD_RIGHT, boldRight);

    }

    public CellStyle get(CellStyleType type) {
        return styles.get(type);
    }
}

package com.tdem.milesheetcreator;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.*;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MilesSheetCreator {
    public static File generate(File inputFile, File outputFolder, Config config, Consumer<String> statusUpdate) throws IOException {

        //export data from missionary portal as .csv file with names, zone, district,
        // ideally leadership position, etc.
        //copy it into missionaryinput.txt
        //take missionaryinput.txt for input
        //parse through there and separate out into companions, areas, districts, zones
        //make arraylists of all of those
        //above part done!

        //use that to make a spreadsheet of the given data


        int transfersThisMonth = 1;



        statusUpdate.accept("Creating spreadsheet...");

        //Load the program configuration and allow the user to update it if needed.
        int carNum = config.getCarCount();
        String exceptions = config.getExceptions();
        boolean secondTransfer = config.isSecondTransfer();



        if(secondTransfer){
            transfersThisMonth++;
        }

        List<String> exceptionsList = new ArrayList<>();

        Scanner scanner = new Scanner(exceptions);
        scanner.useDelimiter(",");

        while (scanner.hasNext()) {
            exceptionsList.add(scanner.next().trim());
        }



        Workbook workbook = new XSSFWorkbook();

        Styles styles = new Styles(workbook);

        //creates all the areas from file

        List<Area> areas = createAreas(inputFile, statusUpdate);

        //creates a hashmap of all the areas sorted by zone
        Map<String, List<Area>> areasByZone = sortIntoZones(areas);

        String heavyCheckMark = "✔ ";

        statusUpdate.accept("Grouping into zones...");
        statusUpdate.accept(heavyCheckMark + areasByZone.size() + " zones found.");

        //creates the summary sheet
        statusUpdate.accept("Creating summary sheet...");
        createSummarySheet(workbook, areasByZone, carNum, exceptionsList, styles);

        //creates the workbook with individual sheets for each zone
        for (String zone : areasByZone.keySet()) {
            statusUpdate.accept("Creating " + zone + "...");
            Sheet sheet = workbook.createSheet(zone);


            // ----- Create worksheet headers -----
            List<String> titles = new ArrayList<>();
            Collections.addAll(titles, "District", "Zone", "Miles", "Missionaries");
            titles.addAll(exceptionsList);
            Collections.addAll(titles, "Actual", "Over");

            //merges header cells together to be merged and centered
            mergeCells(sheet, 0, 0, 0, titles.size() - 1);

            //bolds and creates a header row for each zone
            createCell(styles, sheet, 0, 0, zone, CellStyleType.CENTERED_HEADER);

            //goes down one row and creates smaller header rows for each column
            for(int i = 0; i < titles.size(); i++){
                createCell(styles, sheet, 1, i, titles.get(i), CellStyleType.CENTERED_HEADER);
            }

            // -----puts all the area info into each row-----
            int rowNum = 3;
            for (Area area : areasByZone.get(zone)) {
                createCell(styles, sheet, rowNum, 0, area.getDistrict(), CellStyleType.BORDERED); //district name
                createCell(styles, sheet, rowNum, 1, area.getName(), CellStyleType.BORDERED); //area name
                createCell(styles, sheet, rowNum, 2, area.getMilesAllowed(), CellStyleType.BORDERED);
                createCell(styles, sheet, rowNum, 3, area.getMissionaryNames(), CellStyleType.BORDERED);

                for(int i = 4; i < titles.size(); i++) { //creates blank cells with cell borders
                    createCell(styles, sheet, rowNum, i, "", CellStyleType.BORDERED);
                }


                createFormula(sheet, rowNum + 1, rowNum + 1,titles.size() - 3, titles.size() - 2, rowNum, titles.size() - 1, FormulaType.BOTH, styles, CellStyleType.BOLD_RIGHT);
                createFormula(sheet, rowNum + 1, rowNum + 1, 4, titles.size() - 4, rowNum, titles.size() - 3, FormulaType.SUM, styles, CellStyleType.BOLD_RIGHT);

                rowNum++;
            }

            createCell(styles, sheet, 2, 1, "Totals", CellStyleType.BOLD);
            createFormula(sheet, 4, rowNum, 2, 2, 2, 2, FormulaType.SUM, styles, CellStyleType.BOLD_RIGHT);

            //creates sum formulas for all the total columns
            int size = 4 + exceptionsList.size();
            for(int j = 4; j < size; j++) {
                createFormula(sheet, 4, rowNum, j, j, 2, j, FormulaType.SUM, styles, CellStyleType.BOLD_RIGHT);
            }

            //auto sizes the first 4 columns
            for(int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            //sets the last 5 columns to width 1 to look good
            for(int i = 4; i < exceptionsList.size() - 1; i++) {
                sheet.setColumnWidth(i, 1);
            }

            outlineTable(workbook, sheet, 0, rowNum - 1, 0, titles.size() - 1, styles);

        }

        //forces workbook to recalculate formulas
        workbook.setForceFormulaRecalculation(true);



        //saves the workbook
        String fileName = LocalDate.now().getYear() + "."
                + LocalDate.now().getMonth() + "."
                + transfersThisMonth + ".xlsx";


        File outputFile = new File(outputFolder, fileName);

        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            workbook.write(out);
        }

        statusUpdate.accept("Saving workbook...");
        statusUpdate.accept("Done!");

        //closes workbook
        workbook.close();

        return outputFile;

    }

    public static void createSummarySheet(Workbook workbook, Map<String, List<Area>> areasByZone, int carNum, List<String> exceptions, Styles styles) {
        List<String> headers = new ArrayList<>(exceptions);
        headers.add("Total");

        int STARTING_ROW = 3;
        int STARTING_COLUMN = 3;
        int FIRST_EXCEPTION_COLUMN = 3;
        int LAST_EXCEPTION_COLUMN = FIRST_EXCEPTION_COLUMN + headers.size() - 2;
        int TOTAL_COLUMN = FIRST_EXCEPTION_COLUMN + headers.size() - 1;
        Sheet summarySheet = workbook.createSheet("Summary");

        headers.add("Total");

        //create title row
        createCell(styles, summarySheet, STARTING_ROW, 1, "Miles Summary " + LocalDate.now().getMonth() + " " + LocalDate.now().getYear(), CellStyleType.CENTERED_HEADER);
        mergeCells(summarySheet, STARTING_ROW, STARTING_ROW, 1, TOTAL_COLUMN);

        STARTING_ROW += 3;

        //creates headers for all the titles in summary sheet
        createCell(styles, summarySheet, STARTING_ROW, 1, "Zone", CellStyleType.HEADER);

        createCell(styles, summarySheet, STARTING_ROW, 2, "Miles", CellStyleType.HEADER);

        mergeCells(summarySheet, STARTING_ROW - 1, STARTING_ROW - 1, FIRST_EXCEPTION_COLUMN, TOTAL_COLUMN);
        createCell(styles, summarySheet, 5, STARTING_COLUMN, "Exceptions", CellStyleType.CENTERED_HEADER);

        int titlePos = 0;
        for(int col = STARTING_COLUMN; col <= TOTAL_COLUMN; col++) {
            createCell(styles, summarySheet, STARTING_ROW, col, headers.get(titlePos), CellStyleType.HEADER);
            titlePos++;
        }

        int startZoneRow = 8;
        int endZoneRow = areasByZone.size() + startZoneRow - 1;
        int nextRowNum = areasByZone.size() + 7; //puts it 2 rows below the zones

        //Puts out all the zones into summary sheet
        int rowNum = 7;
        for (String zone : areasByZone.keySet()) {
            createCell(styles, summarySheet, rowNum, 1, zone, CellStyleType.BORDERED); //create a cell with all borders and left thick border
            createFormula(summarySheet, rowNum + 1, rowNum + 1, FIRST_EXCEPTION_COLUMN, LAST_EXCEPTION_COLUMN, rowNum, TOTAL_COLUMN, FormulaType.SUM, styles, CellStyleType.BORDERED);

            linkCells(summarySheet, zone, 3, 2, rowNum, 2, styles);

            for(int i = FIRST_EXCEPTION_COLUMN; i <= LAST_EXCEPTION_COLUMN; i++) {
                linkCells(summarySheet, zone, 3, i + 1, rowNum, i, styles);
            }

            rowNum++;
        }


        createCell(styles, summarySheet, nextRowNum, 1, "Assigned Miles", CellStyleType.BOLD);
        createFormula(summarySheet, startZoneRow, endZoneRow, 2, 2, nextRowNum, 2, FormulaType.SUM, styles, CellStyleType.BOLD);

        nextRowNum += 2;
        createCell(styles, summarySheet, nextRowNum, 1, "Allotment", CellStyleType.BOLD); //creates a cell 2 below the number of zones
        createCell(styles, summarySheet, nextRowNum, 2, carNum * 1250, CellStyleType.BOLD); //miles allotment = number of cars * 1250 miles each


        nextRowNum++;
        createCell(styles, summarySheet, nextRowNum, 1, "Total Surplus", CellStyleType.BOLD);
        createFormula(summarySheet, nextRowNum - 2, nextRowNum, 2, 2, nextRowNum, 2, FormulaType.DIFFERENCE, styles, CellStyleType.BOLD); //create formula with style
        for(int i = FIRST_EXCEPTION_COLUMN; i <= TOTAL_COLUMN; i++) {
            createFormula(summarySheet, startZoneRow, endZoneRow, i, i, rowNum, i, FormulaType.SUM, styles, CellStyleType.BOLD_RIGHT);
        }

        nextRowNum++;
        createCell(styles, summarySheet, nextRowNum, 1, "Exceptions", CellStyleType.BOLD);
        createFormula(summarySheet, endZoneRow + 1, endZoneRow + 1, FIRST_EXCEPTION_COLUMN, LAST_EXCEPTION_COLUMN, nextRowNum, 2, FormulaType.SUM, styles, CellStyleType.BOLD);

        summarySheet.autoSizeColumn(1);

        nextRowNum++;
        createCell(styles, summarySheet, nextRowNum, 1, "Monthly Miles", CellStyleType.BOLD);
        createFormula(summarySheet, nextRowNum, nextRowNum - 1, 2, 2, nextRowNum, 2, FormulaType.DIFFERENCE, styles, CellStyleType.BOLD); //create formula with style

        outlineTable(workbook, summarySheet, 7, rowNum - 1, 1, TOTAL_COLUMN, styles);
        outlineTable(workbook, summarySheet, rowNum, rowNum, 1, TOTAL_COLUMN, styles);
        outlineTable(workbook, summarySheet, rowNum + 2, nextRowNum, 1, 2, styles);
    }

    //takes a sheet and ints for each row, col, to merge and merges them together
    public static void mergeCells(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddress region = (
                new CellRangeAddress(
                        firstRow, //first row
                        lastRow, //last row
                        firstCol, //first column (A)
                        lastCol  //last column (L)
                )
        );
        sheet.addMergedRegion(region);

        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);

    }

    //creates a cell in given workbook on given sheet at given rowNum and colNum with given cellValue and styles it with given styleType
    public static void createCell(Styles styles, Sheet sheet, int rowNum, int colNum, String cellValue, CellStyleType styleType) {
        Row row = createRow(sheet, rowNum);
        Cell cell = row.createCell(colNum);
        cell.setCellValue(cellValue);

        cell.setCellStyle(styles.get(styleType));
    }

    //overloaded version of function
    //creates a cell in given workbook on given sheet at given rowNum and colNum with given integer cellValue and styles it with given styleType
    public static void createCell(Styles styles, Sheet sheet, int rowNum, int colNum, int cellValue, CellStyleType styleType) {
        Row row = createRow(sheet, rowNum);
        Cell cell = row.createCell(colNum);
        cell.setCellValue(cellValue);
        cell.setCellStyle(styles.get(styleType));
    }

    //determines if row has already been created at rowNum. if not, create new one
    public static Row createRow(Sheet sheet, int rowNum) {
        if(sheet.getRow(rowNum) != null) { //if the row has something in it...
            return sheet.getRow(rowNum); //return the current row
        }
        return sheet.createRow(rowNum); //else, create a new row
    }

    /**
     * @param startRow -- first row in formula
     * @param endRow -- last row in formula
     * @param startCol -- first column in formula
     * @param endCol -- last column in formula
     * @param rowNum -- what row the formula needs to go to
     * @param colNum -- what column the formula needs to go to
     * @param formulaType -- what type of formula it is
     * @param styleType -- what type of style it needs
     */
    public static void createFormula(Sheet sheet, int startRow, int endRow, int startCol, int endCol, int rowNum, int colNum, FormulaType formulaType, Styles styles, CellStyleType styleType) {
        Row row = createRow(sheet, rowNum);
        Cell cell = row.createCell(colNum);

        String startColStr = CellReference.convertNumToColString(startCol);
        String endColStr = CellReference.convertNumToColString(endCol);

        String formula = "";

        switch(formulaType) {
            case SUM: {
                formula = "SUM(" + startColStr + startRow + ":" + endColStr + endRow + ")";
                break;
            }
            case DIFFERENCE: {
                formula = endColStr + endRow + "-" + startColStr + startRow;
                break;
            }
            case BOTH: { //add and subtract
                formula = endColStr + startRow + "-" + "(" + startColStr + startRow + "+ C" + endRow + ")";
            }
        }

        cell.setCellFormula(formula);
        cell.setCellStyle(styles.get(styleType));
    }

    //Creates a formula that links up cell formulas from other sheet
    public static void linkCells(Sheet currentSheet, String otherSheetName, int rowNum, int colNum, int formRow, int formCol, Styles styles) {

        Row row = createRow(currentSheet, formRow);
        Cell cell = row.createCell(formCol);

        String formula = "'" + otherSheetName + "'!" + CellReference.convertNumToColString(colNum) + rowNum;

        cell.setCellFormula(formula);

        cell.setCellStyle(styles.get(CellStyleType.BORDERED));
    }

    //Applies a medium border around the outside edge of a table while preserving the existing formatting of each cell
    public static void outlineTable(Workbook workbook, Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol, Styles styles) {
        for (int r = firstRow; r <= lastRow; r++) {
            for (int c = firstCol; c <= lastCol; c++) {
                if (r != firstRow &&
                        r != lastRow &&
                        c != firstCol &&
                        c != lastCol) {
                    continue;
                }

                Cell cell = sheet.getRow(r).getCell(c);

                //if cell is null, create a new temp cell in its place to be used
                if(cell == null) {
                    createCell(styles, sheet, r, c, "", CellStyleType.BORDERED);
                    cell = sheet.getRow(r).getCell(c);
                }

                CellStyle newStyle =
                        workbook.createCellStyle();

                newStyle.cloneStyleFrom(cell.getCellStyle());

                if (r == firstRow)
                    newStyle.setBorderTop(BorderStyle.MEDIUM);

                if (r == lastRow)
                    newStyle.setBorderBottom(BorderStyle.MEDIUM);

                if (c == firstCol)
                    newStyle.setBorderLeft(BorderStyle.MEDIUM);

                if (c == lastCol)
                    newStyle.setBorderRight(BorderStyle.MEDIUM);

                cell.setCellStyle(newStyle);
            }
        }
    }

    public static List<Area> createAreas(File file, Consumer<String> statusUpdate) throws IOException {

        List<Area> areas = new ArrayList<>();
        //File file = chooseInputFile();

        if (file == null) {
            statusUpdate.accept("No input file selected");
            System.exit(0);
        }

        if (!file.exists()) {
            statusUpdate.accept("File does not exist.");
            System.exit(0);
        }

        try (Workbook workbook = WorkbookFactory.create(file)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 1. Build dynamic column map
            Map<String, Integer> columnMap = buildColumnMap(sheet);

            // 2. Required columns (THIS is your contract)
            List<String> requiredColumns = List.of(
                    "zone",
                    "district",
                    "area",
                    "monthly distance allowed",
                    "missionaries",
                    "car"
            );

            // 3. Validate required columns exist
            validateColumns(columnMap, requiredColumns, statusUpdate);

            // 4. Warn about extra columns (optional)
            Set<String> allowed = new HashSet<>();
            for (String c : requiredColumns) {
                allowed.add(c.toLowerCase());
            }

            for (String header : columnMap.keySet()) {
                if (!allowed.contains(header)) {
                    statusUpdate.accept("Warning: extra column ignored -> " + header);
                }
            }

            statusUpdate.accept("Reading missionary file...");

            int areaCount = 0;

            // 5. Process rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String zone = getString(row, columnMap, "zone");
                String district = getString(row, columnMap, "district");
                String areaName = getString(row, columnMap, "area");
                String missionaries = getString(row, columnMap, "missionaries");

                Integer miles = getInteger(row, columnMap, "monthly distance allowed");

                if (zone == null || district == null || areaName == null) {
                    statusUpdate.accept("Input file missing required text field at row " + i);
                    System.exit(1);
                }

                if (miles == null) {
                    statusUpdate.accept("Invalid miles at row " + i);
                    System.exit(1);
                }

                areas.add(new Area(
                        zone,
                        district,
                        areaName,
                        miles,
                        missionaries
                ));

                areaCount++;
            }

            statusUpdate.accept("✔ Loaded " + areaCount + " areas.");

            return areas;
        }
    }

    public static String getString(Row row, Map<String, Integer> columnMap, String columnName) {

        Integer colIndex = columnMap.get(columnName.toLowerCase());
        if (colIndex == null) {
            return null; // column doesn't exist
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }

        try {
            CellType type = cell.getCellType();

            switch (type) {

                case STRING:
                    return cell.getStringCellValue().trim();

                case NUMERIC:
                    // avoids "123.0" weirdness if it's actually an int-like value
                    double num = cell.getNumericCellValue();
                    if (num == (long) num) {
                        return String.valueOf((long) num);
                    }
                    return String.valueOf(num);

                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());

                case FORMULA:
                    return cell.getCellFormula(); // or evaluate if you want later

                case BLANK:
                    return null;

                default:
                    return cell.toString().trim();
            }

        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getInteger(Row row, Map<String, Integer> map, String col) {
        Integer idx = map.get(col.toLowerCase());
        if (idx == null) return null;

        Cell cell = row.getCell(idx);
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }
            return Integer.parseInt(cell.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    //builds a column map
    public static Map<String, Integer> buildColumnMap(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Missing header row");
        }

        Map<String, Integer> columnMap = new HashMap<>();

        for (Cell cell : headerRow) {
            String header = normalize(cell.getStringCellValue());
            columnMap.put(header, cell.getColumnIndex());
        }

        return columnMap;
    }

    //makes sure all columns are there that should be
    public static void validateColumns(Map<String, Integer> columnMap, List<String> required, Consumer<String> statusUpdate) {
        List<String> missing = new ArrayList<>();

        for (String col : required) {
            if (!columnMap.containsKey(col.toLowerCase())) {
                missing.add(col);
            }
        }

        if (!missing.isEmpty()) {
            statusUpdate.accept("Missing required columns: " + String.join(", ", missing));
            statusUpdate.accept("Required columns are:  " + String.join(", ", required));
            statusUpdate.accept("Please export a new file from Missionary Portal with the missing columns.");
            throw new IllegalArgumentException(
                    "Missing required columns: " + String.join(", ", missing)
            );
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    //allows user to choose the input file from missionary portal
    public static File chooseInputFile() {
        //makes file dialog box look better
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFileChooser chooser = new JFileChooser();

        //open in Downloads by default
        chooser.setCurrentDirectory(
                new File(System.getProperty("user.home"), "Downloads"));

        //Only show Excel files
        chooser.setFileFilter(
                new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx")
        );

        chooser.setDialogTitle("Select Missionary Portal Export");

        int result = chooser.showOpenDialog(null);

        if(result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }

        return null;
    }

    //creates a hashmap of each arraylist sorted by zone then prints it out
    public static Map<String, List<Area>> sortIntoZones(List<Area> areas) {
        Map<String, List<Area>> zoneLists = new TreeMap<>();

        for (Area area : areas) {
            zoneLists
                    .computeIfAbsent(area.getZone(), z -> new ArrayList<>())
                    .add(area);
        }

        return zoneLists;
    }


}


package org.ytcuber.parser;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ytcuber.model.Group;
import org.ytcuber.model.Lesson;
import org.ytcuber.repository.GroupRepository;
import org.ytcuber.repository.LessonRepository;
import org.ytcuber.types.DayOfWeek;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class Initialization {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        int squadNumber;
//        try {
//            // Указываем URL-адрес веб-страницы
//            Document doc = Jsoup.connect("https://newlms.magtu.ru/mod/folder/view.php?id=219213").get();
//            String startLink = "https://newlms.magtu.ru/pluginfile.php/2510348/mod_folder/content/0/%D0%98%D0%A1%D0%BF%D0%92-21-1.pdf?forcedownload=1";
//            String lastLink = "https://newlms.magtu.ru/login/index.php";
//            squadNumber = 2;
//            squads(doc, startLink, lastLink, squadNumber);
//
//            doc = Jsoup.connect("https://newlms.magtu.ru/mod/folder/view.php?id=219213").get();
//            startLink = "https://newlms.magtu.ru/pluginfile.php/622200/mod_folder/content/0/%D0%90%D0%A2%D0%BF-23-1.pdf?forcedownload=1";
//            lastLink = "◄ 5 КУРС";
//            squadNumber = 1;
//            squads(doc, startLink, lastLink, squadNumber);
//
////            doc = Jsoup.connect("https://newlms.magtu.ru/mod/folder/view.php?id=1584691").get();
////            startLink = "https://newlms.magtu.ru/pluginfile.php/2510348/mod_folder/content/0/%D0%98%D0%A1%D0%BF%D0%92-21-1.pdf?forcedownload=1";
////            lastLink = "◄ ОТДЕЛЕНИЕ № 2 «ИНФОРМАЦИОННЫЕ ТЕХНОЛОГИИ И ТРАНСПОРТ»";
////            squadNumber = 3;
////            squads(doc, startLink, lastLink, squadNumber);
//
////            doc = Jsoup.connect("https://newlms.magtu.ru/mod/folder/view.php?id=219205").get();
////            startLink = "https://newlms.magtu.ru/pluginfile.php/622192/mod_folder/content/0/%D0%90%D0%A2%D0%BF-20-1.pdf?forcedownload=1";
////            lastLink = "◄ ОТДЕЛЕНИЕ № 3 «СТРОИТЕЛЬСТВО, ЭКОНОМИКА И СФЕРА ОБСЛУЖИВАНИЯ>";
////            squadNumber = 4;
////            squads(doc, startLink, lastLink, squadNumber);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        String groupName = "ИСпПК-21-1";
        String squadNum = "2";
//        Group groupId = groupRepository.findByName(groupName);
        String inputFilePath = "./mainexcel/squad" + squadNum + "/" + groupName + ".xlsx";
        XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(inputFilePath));
        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);
        minusUnion(inputFilePath);
        minusUnion(inputFilePath);
        Thread.sleep(1000);

        List<Lesson> lessonsList = parseExcel(myExcelSheet, null);

        lessonRepository.saveAllAndFlush(lessonsList);

        Thread.sleep(500);
    }

    public void squads(Document doc, String startLink, String lastLink, Integer squadNumber) {
        // Извлекаем все элементы с определенным селектором
        String filePath = "./mainexcel/squad" + squadNumber + "/";
        Elements elements = doc.select("a");

        // Цикл по всем элементам
        boolean startPrinting = false;
        // Перебираем все извлеченные элементы и выводим их на экран
        for (Element element : elements) {
            String title = element.text(); // Название файла
            String link = element.absUrl("href"); // Ссылка на скачивание файла

            if (link.equals(startLink)) {
                startPrinting = true;
            }

            if (title.equals(lastLink)) {
                break;
            }

            if (startPrinting && !title.endsWith(".pdf")) {
                System.out.println(new String(title.getBytes(StandardCharsets.UTF_8)));
                System.out.println();

                // Скачиваем файл
                try (InputStream in = new URL(link).openStream()) {
                    Files.copy(in, Path.of(filePath + title), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Group groups = new Group();
                if (title.endsWith(".xlsx")) {
                    title = title.substring(0, title.length() - 5);
                }
                groups.setTitle(title);
                groups.setSquad(squadNumber);

                groupRepository.save(groups);

            }
        }
    }

    public void minusUnion(String file) {
        List<Integer> columnsToUnmerge = Arrays.asList(0, 6, 12); // Столбцы A, G, M

        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                if (mergedRegion != null) {
                    int firstRow = mergedRegion.getFirstRow();
                    int lastRow = mergedRegion.getLastRow();
                    int firstColumn = mergedRegion.getFirstColumn();
                    int lastColumn = mergedRegion.getLastColumn();

                    if (columnsToUnmerge.contains(firstColumn) && firstColumn == lastColumn) {
                        sheet.removeMergedRegion(i);
                        for (int row = firstRow; row <= lastRow; row++) {
                            Row r = sheet.getRow(row);
                            Cell c = r.getCell(firstColumn);
                            if (c == null) {
                                r.createCell(firstColumn);
                            }
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            System.out.println("Объединённые ячейки разъединены успешно!");
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Lesson> parseExcel(XSSFSheet myExcelSheet, Group groupId) {
        int odd = 0;
        final List<Lesson> lessonsList = new ArrayList<>();
        int rowId = 1;
        int cellId = 0;
        int u = 1;
        int tmpDay = 1;
        int tmpLog = 0;
        int isWeekOdd;
        DayOfWeek datOfWeek;

        // Проверка на начало Рассписания Группы

        Lesson lesson = new Lesson();
        int weekOdd;
        int weekOddRowId = 0;
        DayOfWeek dayOfWeek;
        for (int y = 1; y <= 2; y++) {
            for (int i = 1; i <= 3; i++) {
                while (u <= 2) {
                    // Проверка недели
                    weekOdd = weekOdd(odd, rowId, myExcelSheet);
                    if (y == 2 && i == 1){
                        weekOddRowId = rowId;
                    }
                    while (tmpDay <= 2) {
                        rowId++;
                        // Проверка и внесение из enum дня
//                    dayOfWeek = weekDay(file, rowId, cellId, myExcelSheet);
                        datOfWeek = weekDay(rowId, cellId, myExcelSheet);
                        boolean isEndDayOfWeek = endDayOfWeek(datOfWeek, u);

                        rowId++;
                        boolean tmpLes = false;
                        while (!tmpLes) {
                            lesson.setOrdinal((int) myExcelSheet.getRow(rowId).getCell(cellId).getNumericCellValue()); // Внесение Номер пары
                            int para = (int) myExcelSheet.getRow(rowId).getCell(cellId).getNumericCellValue(); // Запоминание пары

                            lesson.setOdd(weekOdd);
                            lesson.setDayOfWeek(datOfWeek);

                            logicalAll(lesson, rowId, cellId, para, weekOdd, datOfWeek, myExcelSheet, groupId);
                            lesson = new Lesson();
                            rowId += 2;

                            Row rowT = myExcelSheet.getRow(rowId);
                            Cell cell = null;
                            if (rowT != null) {
                                cell = rowT.getCell(cellId);
                            }

                            dayOfWeek = parseDayOfWeek(cell);

                            // Проверка, является ли ячейка null или содержит пустую строку
                            if (cell == null ||
                                    (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) ||
                                    (cell.getCellType() == CellType.STRING && !parseOddWeek(cell)) ||
                                    (cell.getCellType() == CellType.BLANK)) {
                                tmpLes = true;
                            } else if (dayOfWeek != null) {
                                tmpLes = true;
                            }
                        }
                        if (!isEndDayOfWeek) {
                            while (tmpLes) { // Проверка, что там написан день недели или Тип недели
                                Row row = myExcelSheet.getRow(rowId);

                                if (row == null) {
                                    if (tmpLog == 10) {
                                        break;
                                    }
                                    rowId++;
                                    isWeekOdd = weekOdd(odd, rowId, myExcelSheet); // Проверка на неделю !!!
                                    if (isWeekOdd == 1 && i == 2 || isWeekOdd == 2 && i == 2 || isWeekOdd == 1 && i == 3 || isWeekOdd == 2 && i == 3) {
                                        rowId++;
                                        break;
                                    }
                                    tmpLog++;
                                    continue;
                                }

                                Row rowT = myExcelSheet.getRow(rowId);
                                Cell cell = null;
                                if (rowT != null) {
                                    cell = rowT.getCell(cellId);
                                }

                                dayOfWeek = parseDayOfWeek(cell);

                                boolean isOddWeek = parseOddWeek(cell);

                                if (!isOddWeek) {
                                    tmpLes = false;
                                    rowId++;
                                } else if (dayOfWeek != null) {
                                    tmpLes = false;
                                } else {
                                    rowId += 2;
                                }
                            }
                        }
                        tmpDay++;
                        u++;
                        rowId--;
                    }
                    tmpDay = 1;

                }
                if (i != 3) {
                    cellId += 6;
                    if (y == 1) {
                        rowId = 1;
                    } else if(weekOddRowId != 0) {
                        rowId = weekOddRowId;
                    }
                } else {
                    cellId = 0;
                    rowId += 1;

                    // Проверка, что ячейка существует и корректно обработана
                    if (y != 2) {
                        boolean isNewWeek = parseOddWeek(myExcelSheet.getRow(rowId).getCell(cellId));
                        int tmpLogging = 0;
                        while (isNewWeek) {
                            rowId++;
                            try {
                                isNewWeek = parseOddWeek(myExcelSheet.getRow(rowId).getCell(cellId));
                            } catch (NullPointerException e) { }
//                            if (!isNewWeek) {
//                                rowId--;
//                            }
                            if (tmpLogging == 100) {
                                System.err.println("ПРОИЗОШЛА НЕПРЕДВИДЕННАЯ ОШИБКА!");
                                break;
                            }
                            tmpLogging++;
                        }

//                        Cell cell = myExcelSheet.getRow(rowId).getCell(cellId);
//                        if (cell != null && parseOddWeek(cell)) {
//                            while (cell != null && parseOddWeek(cell)) {
//                                rowId++;
//
//                                // Обновляем ячейку для следующей проверки
//                                if (myExcelSheet.getRow(rowId) != null) {
//                                    cell = myExcelSheet.getRow(rowId).getCell(cellId);
//                                } else {
//                                    break; // Выходим из цикла, если строки заканчиваются
//                                }
//                            }
//                        }
                    }
                }
                u = 1;
                tmpDay = 1;
                tmpLog = 0;
//     lessonsList.add(lesson);
            }
        }
        return lessonsList;
    }
// Логика написания id группы
    public DayOfWeek parseDayOfWeek(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.STRING) return null;

        return switch (cell.getStringCellValue()) {
            case "Понедельник" -> DayOfWeek.MONDAY;
            case "Вторник" -> DayOfWeek.TUESDAY;
            case "Среда" -> DayOfWeek.WEDNESDAY;
            case "Четверг" -> DayOfWeek.THURSDAY;
            case "Пятница" -> DayOfWeek.FRIDAY;
            case "Суббота" -> DayOfWeek.SATURDAY;
            default -> null;
        };
    }

    public boolean endDayOfWeek(DayOfWeek datOfWeek, int u) {
        if (u == 2) {
            return switch (datOfWeek) {
                case THURSDAY, FRIDAY, SATURDAY -> true;
                default -> false;
            };
        } else {
            return false;
        }
    }

    public boolean parseOddWeek(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK || (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) || cell.getCellType() == CellType.NUMERIC) return true;
        return switch (cell.getStringCellValue()) {
            case "Нечетная неделя", "Четная неделя" -> false;
            default -> true;
        };
    }

    public void logicalAll(Lesson lesson, int row, int cell, int para1, int weekOdd, DayOfWeek datOfWeek, XSSFSheet myExcelSheet, Group groupId) {
        int tmpSub;
        cell++;
//        lesson.setGroup(groupRepository.findById(groupid).get());
        if (!Objects.equals(myExcelSheet.getRow(row).getCell(cell).getStringCellValue(), "")) {

            String cellValue = myExcelSheet.getRow(row).getCell(cell).getStringCellValue();
            String cellValue2 = myExcelSheet.getRow(row).getCell(cell + 2).getStringCellValue();
            String cellValue3 = myExcelSheet.getRow(row + 1).getCell(cell + 3).getStringCellValue();
            String cellValue4 = myExcelSheet.getRow(row + 1).getCell(cell + 1).getStringCellValue();
            tmpSub = cellVal(cellValue, cellValue2, cellValue3, cellValue4); // Проверка какая пара, Общая/Не Общая
            if (tmpSub == 0) {
                lesson.setSubgroup(tmpSub);
                lesson.setSubject(String.valueOf(myExcelSheet.getRow(row).getCell(cell).getStringCellValue())); // Внесение Предмет

                row++;
                String teacher = String.valueOf(myExcelSheet.getRow(row).getCell(cell));
                lesson.setTeacher(teacher); // Внесение Преподаватель

                cell += 3;
                lesson.setLocation(String.valueOf(myExcelSheet.getRow(row).getCell(cell).getStringCellValue())); // Внесение Кабинета
                System.out.println();

                lessonRepository.save(lesson);
            } else {
                logicalSub1(lesson, row, cell, para1, weekOdd, datOfWeek, myExcelSheet, groupId);
            }

        } else {
            cell += 2;
            logicalSub2(row, cell, para1, weekOdd, datOfWeek, myExcelSheet, groupId);
        }

    }

    public void logicalSub1(Lesson lesson, int row, int cell, int para, int weekOdd, DayOfWeek datOfWeek, XSSFSheet myExcelSheet, Group groupId) {
        if (!Objects.equals(myExcelSheet.getRow(row).getCell(cell).getStringCellValue(), "")) {
            lesson.setSubgroup(1);

            lesson.setSubject(String.valueOf(myExcelSheet.getRow(row).getCell(cell).getStringCellValue())); // Внесение Предмет

            row++;
            lesson.setTeacher(myExcelSheet.getRow(row).getCell(cell).getStringCellValue()); // Внесение Преподаватель

            cell++;
            lesson.setLocation(String.valueOf(myExcelSheet.getRow(row).getCell(cell).getStringCellValue())); // Внесение Кабинета

            lessonRepository.save(lesson);

            cell++;
            row--;
            logicalSub2(row, cell, para, weekOdd, datOfWeek, myExcelSheet, groupId);
        } else {
            cell += 2;
            logicalSub2(row, cell, para, weekOdd, datOfWeek, myExcelSheet, groupId);
        }
    }

    public void logicalSub2(int row, int cell, int para, int weekOdd, DayOfWeek datOfWeek, XSSFSheet myExcelSheet, Group groupId) {

        Lesson lesson = new Lesson();
        if (!Objects.equals(myExcelSheet.getRow(row).getCell(cell).getStringCellValue(), "")) {
//            lesson.setGroup(groupRepository.findById(groupId).get());
            lesson.setOdd(weekOdd);
            lesson.setDayOfWeek(datOfWeek);

            lesson.setOrdinal(para);
            lesson.setSubgroup(2);

            lesson.setSubject(String.valueOf(myExcelSheet.getRow(row).getCell(cell).getStringCellValue())); // Внесение Предмет

            row++;
            lesson.setTeacher(myExcelSheet.getRow(row).getCell(cell).getStringCellValue()); // Внесение Преподаватель

            cell++;
            lesson.setLocation(String.valueOf(myExcelSheet.getRow(row).getCell(cell).getStringCellValue())); // Внесение Кабинета

            lessonRepository.save(lesson);
        }
    }

    public int weekOdd(int odd, int row, XSSFSheet myExcelSheet) {
        int cell = 0;
        switch (myExcelSheet.getRow(row).getCell(cell).getStringCellValue()) {
            case "Нечетная неделя" -> //lesson.setOdd(odd);
                    odd = 1;
            case "Четная неделя" -> //lesson.setOdd(odd);
                    odd = 2;
            default -> {
                System.err.println("!");
                System.err.println("Ошибка!");
                System.err.println("Неделя не опознана!");
                System.err.println("!");
            }
        }
        return odd;
    }

    public DayOfWeek weekDay(int row, int cell, XSSFSheet myExcelSheet) {
        String weekDayRawValue = myExcelSheet.getRow(row).getCell(cell).getStringCellValue();

        return DayOfWeek.valueOfLabel(weekDayRawValue);
    }

    public int cellVal(String cellValue, String cellValue2, String cellValue3, String cellValue4) {
        int tmpSub;
        if(cellValue2 == null || cellValue2.isEmpty() || cellValue2 == "") {
            if (!cellValue3.isEmpty() || cellValue3 != null || cellValue3 != "") {
                if (cellValue4.isEmpty() || cellValue4 == null || cellValue4 == "") {
                    tmpSub = 0;
                } else {
                    tmpSub = 1;
                }
            } else {
                tmpSub = 1;
            }
        } else if (cellValue.startsWith("(КП)") || cellValue.startsWith("(Лаб)") || cellValue.startsWith("(Пр)") || cellValue.startsWith("Ин.яз")) {
            if (cellValue2 == null || cellValue2.isEmpty() || cellValue2 == "") {
                if (cellValue3.isEmpty() || cellValue3 == null || cellValue3 == "") {
                    tmpSub = 1;
                } else {
                    tmpSub = 0;
                }
                tmpSub = 1;
            }
            tmpSub = 1;
        } else {
            tmpSub = 0;
        }
        return tmpSub;
    }
}
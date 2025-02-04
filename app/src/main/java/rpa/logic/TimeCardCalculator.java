package rpa.logic;

import rpa.record.InOutRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import rpa.record.Interrupts;
import rpa.record.UsageDuration;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class TimeCardCalculator {

    private static final int FEE_PER_15MIN = 200;


    public File inputFile;
    public Path outputDirPath;

    TimeCardCalculator() {

    }

    public TimeCardCalculator(File inputFile, Path outputDirPath) {
        this.inputFile = inputFile;
        this.outputDirPath = outputDirPath;

        if (!outputDirPath.toFile().exists()) {
            outputDirPath.toFile().mkdirs();
        }
    }


    void outputStringsToFile(List<String> reportOutput, Path outputDirPath, String fileName) {
        File f = new File(outputDirPath.toFile(), fileName);
        try (Writer w = new FileWriter(f, StandardCharsets.UTF_8)) {
            reportOutput.forEach(s -> {
                try {
                    w.append(s).append('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    List<InOutRecord> loadToInOutRecords(File trimmedCSVFile, List<String> reportOutput) {
        try (Reader in = new BufferedReader(new FileReader(trimmedCSVFile, StandardCharsets.UTF_8))) {
            reportOutput.add(String.format(":%s", inputFile.toString()));
            String[] HEADERS = {
                    "児童名", "学年", "入退室日", "曜日", "出欠",
                    "入室時刻", "退室時刻", "中抜1", "戻り1", "中抜2", "戻り2", "中抜3", "戻り3"
            };

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .setAllowMissingColumnNames(true)
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(in);

            List<InOutRecord> output = new ArrayList<>();

            int count = 0;
            int errorCount = 0;
            int notValidCount = 0;

            for (CSVRecord record : records) {
                count++;
                if (!record.get("入室時刻").isEmpty() && !record.get("退室時刻").isEmpty()) {
                    LocalTime inTime = LocalTime.parse(record.get("入室時刻"));
                    LocalTime outTime = LocalTime.parse(record.get("退室時刻"));


                    List<Interrupts> interruptHours = new ArrayList<>();
                    interruptHours.add(Interrupts.build(getLocalTime(record, "中抜1"), getLocalTime(record, "戻り1"), outTime));
                    interruptHours.add(Interrupts.build(getLocalTime(record, "中抜2"), getLocalTime(record, "戻り2"), outTime));
                    interruptHours.add(Interrupts.build(getLocalTime(record, "中抜3"), getLocalTime(record, "戻り3"), outTime));

                    List<UsageDuration> usageHours = new ArrayList<>();
                    usageHours.add(new UsageDuration(inTime, outTime));

                    for (Interrupts interruptHour : interruptHours) {
                        usageHours = usageHours.stream()
                                .flatMap(usageHour -> usageHour.split(interruptHour).stream())
                                .toList();
                    }

                    output.add(new InOutRecord(record.get("児童名"), record.get("入退室日"), usageHours));


                } else {
                    if (!record.get("入室時刻").isEmpty() && record.get("退室時刻").isEmpty()) {
                        reportOutput.add(
                                String.format("退室時刻が設定されていません。（%s,%s) #%d",
                                        record.get("児童名"),
                                        record.get("入退室日"),
                                        record.getRecordNumber() + 1
                                )
                        );
                        InOutRecord errorRecord = new InOutRecord(record.get("児童名"), record.get("入退室日"), new ArrayList<>());
                        errorRecord.setError();
                        output.add(errorRecord);
                        errorCount++;
                    } else if (record.get("入室時刻").isEmpty() && !record.get("退室時刻").isEmpty()) {
                        reportOutput.add(
                                String.format("入室時刻が設定されていません。（%s,%s) #%d",
                                        record.get("児童名"),
                                        record.get("入退室日"),
                                        record.getRecordNumber() + 1
                                )
                        );
                        InOutRecord errorRecord = new InOutRecord(record.get("児童名"), record.get("入退室日"), new ArrayList<>());
                        errorRecord.setError();
                        output.add(errorRecord);
                        errorCount++;
                    } else {
                        notValidCount++;
                    }
                }

            }
            reportOutput.add(String.format("%d件の行を処理しました。", count));
            reportOutput.add(String.format("%d件は無効行です。", notValidCount));
            reportOutput.add(String.format("%d件は入室または退室の打刻がないデータです。", errorCount));
            reportOutput.add(String.format("%d件が有効行です。\n", output.size() - errorCount));

            return output.stream().sorted().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LocalTime getLocalTime(CSVRecord record, String header) {
        try {
            return LocalTime.parse(record.get(header));
        } catch (Exception e) {
            return null;
        }
    }

    public void run() {
        List<String> reportOutput = new ArrayList<>();

        try {
            List<InOutRecord> records = loadToInOutRecords(inputFile, reportOutput);

            Map<String, Integer> total = calculateExtendedChildcareFees(records);
            Map<String, List<InOutRecord>> detail = makeDetail(records);

            outputCSV(total);
            outputDetails(detail, total);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputStringsToFile(reportOutput, outputDirPath, "report.txt");
        }

    }

    static Map<String, Integer> calculateExtendedChildcareFees(List<InOutRecord> records) {
        Map<String, Integer> total = new HashMap<>();

        for (InOutRecord r : records) {
            if(!r.isError()) {
                int roundedBeforeTime = r.getRoundedBeforeMin();
                int roundedExtendTime = r.getRoundedExtendMin();
                int beforeSum = total.get(r.getName()) != null ? total.get(r.getName()) : 0;

                total.put(r.getName(), beforeSum + roundedBeforeTime + roundedExtendTime);
            }
        }
        return total;
    }

    Map<String, List<InOutRecord>> makeDetail(List<InOutRecord> records) {
        Map<String,List<InOutRecord>> detail = new HashMap<>();

        for (InOutRecord r : records) {
            if (!detail.containsKey(r.getName())) {
                detail.put(r.getName(), new ArrayList<>());
            }
            detail.get(r.getName()).add(r);
        }
        return detail;
    }

    void outputCSV(Map<String, Integer> total) {
        List<String> output = new ArrayList<>();
        output.add("\"児童名\",\"延長(単位)\",\"延長料金(円)\"");

        total.entrySet().stream()
                .filter(s -> s.getValue() != 0)
                .forEach(s ->
                        output.add(
                                String.format("%s,%d,%d",
                                        s.getKey(),
                                        minutes2Unit(s.getValue()),
                                        minutes2Unit(s.getValue()) * FEE_PER_15MIN
                                )
                        )
                );
        outputStringsToFile(output, outputDirPath, "output.csv");
    }

    void outputDetails(Map<String, List<InOutRecord>> detail, Map<String, Integer> total) {
        List<String> output = new ArrayList<>();
        detail.keySet().stream().sorted().forEach(s -> {
            output.add(String.format("%s", s));

            List<InOutRecord> subDetail = detail.get(s);
            subDetail.stream()
                    .sorted()
                    .forEach(k -> output.add(k.toStringForDetail())
                    );
            String warning = subDetail.stream().anyMatch(InOutRecord::isError) ? " !!" : "";
            Optional<Integer> totalExtend = Optional.ofNullable(total.get(s));
            output.add(
                    String.format("total : %3d (%5d)%s\n",
                            minutes2Unit(totalExtend.orElse(0)),
                            minutes2Unit(totalExtend.orElse(0)) * FEE_PER_15MIN,
                            warning
                    )
            );
        });

        outputStringsToFile(output, outputDirPath, "details.txt");
    }

    public static int minutes2Unit(int minutes) {
        int remainder = minutes % 15 == 0 ? 0 : 1;
        return minutes / 15 + remainder;
    }
}

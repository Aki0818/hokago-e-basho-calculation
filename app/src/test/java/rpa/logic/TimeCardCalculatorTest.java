package rpa.logic;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import rpa.record.InOutRecord;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeCardCalculatorTest {

    static class TimeCardCalculatorForTest extends  TimeCardCalculator{

        @Override
        public void run() {

        }

        protected InOutRecord inOutRecordFactory(String id, String name, String date, LocalTime inTime, LocalTime outTime) {
            return null;
        }

        protected boolean isTargetRecord(CSVRecord record) {
            return false;
        }
    }

    @Test
    void outputStringsToFile() throws IOException {
        List<String> input = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            input.add(String.valueOf(i));
        }

        String fileName = "test.tmp";
        Path outputDirPath = Paths.get("./");

        TimeCardCalculator sut = new TimeCardCalculatorForTest();
        sut.outputStringsToFile(input, outputDirPath, fileName);

        File actual = new File(outputDirPath.toString(), fileName);
        {   // prepare File
            try (BufferedReader r = new BufferedReader(new FileReader(actual))) {
                for (int i = 0; i < 10; i++) {
                    assertEquals(String.valueOf(i), r.readLine());
                }
            }
        }

        actual.delete();
    }

}
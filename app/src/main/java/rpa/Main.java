package rpa;

import org.apache.commons.cli.*;
import rpa.logic.TimeCardCalculator;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static Set<Path> ls(Path dir) throws IOException {
        Set<Path> fileSet = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileSet.add(path);
                }
            }
        }
        return fileSet;
    }

    public static void main(String[] args) {
        var options = new Options();
        options.addOption(
                Option.builder("i")
                        .argName("inputDir")
                        .hasArg()
                        .required()
                        .desc( "input dir")
                        .build()
        );
        options.addOption(
                Option.builder("o")
                        .argName("outputDir")
                        .hasArg()
                        .required()
                        .desc( "output dir")
                        .build()
        );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("cmd parser failed.");
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("[opts]", options);
            return;
        }

        if (!cmd.hasOption("i") || !cmd.hasOption("o")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("[opts]", options);
            return;
        }

        Path inputDir = Paths.get(cmd.getOptionValue("i"));
        Path outputDirForStudents = Paths.get(cmd.getOptionValue("o"));

        Path inputFile = null;
        {
            try {
                Set<Path> files = ls(inputDir);

                long csvCount = files.stream()
                        .filter(s -> s.toString().endsWith(".csv"))
                        .count();

                if (csvCount != 1) {
                    System.err.println("The input file is missing or multiple files exist. Please check files under the \"./input\" dir.");
                    return;
                }

                inputFile = files.stream()
                        .filter(s -> s.toString().endsWith(".csv"))
                        .findFirst()
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            TimeCardCalculator ef = new TimeCardCalculator(inputFile.toFile(), outputDirForStudents);
            ef.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

package ru.rage.smasm;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Not enough arguments");
            return;
        }

        String program;
        try {
            program = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
        }
        catch (Exception ex)
        {
            System.out.println("Reading error: " + ex.getMessage());
            return;
        }

        Parser parser;
        List<Command> cmds;
        try {
            parser = new Parser(program);
            cmds = parser.parse();
        }
        catch (Exception ex)
        {
            System.out.println("Parsing error: " + ex.getMessage());
            return;
        }

        Translator translator = new Translator(cmds, parser.hasData() ? parser.getData() : null);
        try {
            translator.analyze();
        }
        catch (Exception ex)
        {
            System.out.println("Analyzing error: " + ex.getMessage());
            return;
        }
        try {
            FileOutputStream fout = new FileOutputStream("output.sme");
            fout.write(translator.compile());
            fout.close();
            if (parser.hasData())
            {
                fout = new FileOutputStream("output.smd");
                fout.write(translator.getData());
                fout.close();
            }
        }
        catch (Exception ex)
        {
            System.out.println("Translation error: " + ex.getMessage());
        }
    }
}

package ru.rage.smasm;

import ru.rage.spoml.*;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Formatter;

public class Main
{
    private static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Not enough arguments");
            return;
        }

        String program;
        try
        {
            program = new String(Files.readAllBytes(Paths.get(args[0])), FILE_CHARSET);
        }
        catch (Exception ex)
        {
            System.out.println("Reading error: " + ex.getMessage());
            return;
        }

        Parser parser;
        try
        {
            parser = new Parser(program);
            parser.parse();
        }
        catch (Exception ex)
        {
            System.out.println("Parsing error: " + ex.getMessage());
            return;
        }

        Translator translator;
        try
        {
            translator = new Translator(parser.getIncludes(),
                                        parser.getData(),
                                        parser.getCode(),
                                        parser.getExternals());
            translator.compile();
        }
        catch (Exception ex)
        {
            System.out.println("Compiling error: " + ex.getMessage());
            return;
        }

        try
        {
            FileOutputStream fout;
            Formatter formatter;

            if (translator.hasIncludes())
            {
                formatter = new Formatter();
                for (Include inc : translator.getIncludes())
                    formatter.format("%s\n", inc.toString());

                fout = new FileOutputStream("output.smi");
                fout.write(formatter.toString().getBytes(FILE_CHARSET));
                fout.close();
            }
            if (translator.hasData())
            {
                fout = new FileOutputStream("output.smd");
                for (Data data : translator.getData())
                    fout.write(data.toByteArray());
                fout.close();
            }
            if (translator.hasCode())
            {
                fout = new FileOutputStream("output.smc");
                for (Command cmd : translator.getCode())
                    fout.write(cmd.toByteArray());
                fout.close();
            }
            if (translator.hasExterns())
            {
                formatter = new Formatter();
                for (Extern extern : translator.getExterns())
                    formatter.format("%s\n", extern.toString());

                fout = new FileOutputStream("output.sme");
                fout.write(formatter.toString().getBytes(FILE_CHARSET));
                fout.close();
            }
        }
        catch (Exception ex)
        {
            System.out.println("Translation error: " + ex.getMessage());
        }
    }
}

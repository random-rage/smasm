package ru.rage.smasm;

import ru.rage.spoml.*;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Formatter;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Usage: smasm <source file name>");
            return;
        }

        String program;
        try
        {
            program = new String(Files.readAllBytes(Paths.get(args[0])), Include.FILE_CHARSET);
        }
        catch (Exception ex)
        {
            System.out.printf("Reading error: %s\n", ex);
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
            System.out.printf("Parsing error: %s\n", ex);
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
            System.out.printf("Compiling error: %s\n", ex);
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
                fout = new FileOutputStream(getFileBasename(args[0]) + Include.FILE_EXT);
                fout.write(formatter.toString().getBytes(Include.FILE_CHARSET));
                fout.close();
            }
            if (translator.hasData())
            {
                fout = new FileOutputStream(getFileBasename(args[0]) + Data.FILE_EXT);
                for (Data data : translator.getData())
                    fout.write(data.toByteArray());
                fout.close();
            }
            if (translator.hasCode())
            {
                fout = new FileOutputStream(getFileBasename(args[0]) + Command.FILE_EXT);
                for (Command cmd : translator.getCode())
                    fout.write(cmd.toByteArray());
                fout.close();
            }
            if (translator.hasExterns())
            {
                formatter = new Formatter();
                for (Extern extern : translator.getExterns())
                    formatter.format("%s\n", extern.toString());

                fout = new FileOutputStream(getFileBasename(args[0]) + Extern.FILE_EXT);
                fout.write(formatter.toString().getBytes(Include.FILE_CHARSET));
                fout.close();
            }
        }
        catch (Exception ex)
        {
            System.out.printf("Translation error: %s\n", ex);
        }
    }

    private static String getFileBasename(String path)
    {
        String name = Paths.get(path).getFileName().toString();
        int dot = name.lastIndexOf('.');
        return (dot < 0) ? name : name.substring(0, dot);
    }
}

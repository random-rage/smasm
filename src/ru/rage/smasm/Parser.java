package ru.rage.smasm;

import ru.rage.spoml.*;

import java.util.ArrayList;
import java.util.List;

class Parser
{
    private String[]           _lines;
    private int                _curAddr;
    private ArrayList<Include> _includes;
    private ArrayList<Data>    _data;
    private ArrayList<Command> _cmds;
    private ArrayList<Extern>  _externs;

    Parser(String program)
    {
        _lines = preprocess(program).split("[\\r\\n]+");
        _includes = new ArrayList<>();
        _data = new ArrayList<>();
        _cmds = new ArrayList<>();
        _externs = new ArrayList<>();
        _curAddr = 0;
    }

    /**
     * Удаляет комментарии и лишние символы
     *
     * @param source Исходная строка
     *
     * @return Чистый код
     */
    private static String preprocess(String source)
    {
        StringBuilder result = new StringBuilder();
        if (!source.isEmpty())
        {
            int i = 0;
            while (i < source.length() - 1)
            {
                if (source.charAt(i) == '/' &&
                    source.charAt(i + 1) == '*')
                {
                    while (i < source.length() &&
                           !(source.charAt(i) == '*' && source.charAt(i + 1) == '/'))
                        ++i;
                    i += 2;
                }
                else if (source.charAt(i) == '/' && source.charAt(i + 1) == '/')
                {
                    while (i < source.length() && source.charAt(i) != '\n')
                        ++i;
                }
                else if (source.charAt(i) == '\n' && source.charAt(i + 1) == '\n')
                    ++i;
                else
                    result.append(source.charAt(i++));
            }
            if (i == source.length() - 1 && source.charAt(i) != '\n')
                result.append(source.charAt(i));
        }
        return result.toString();
    }

    void parse() throws Exception
    {
        Segment curSeg = Segment.CODE;
        Extern curExt = null;

        for (String line : _lines)
        {
            line = line.replace('\t', ' ').trim();

            switch (line)
            {
                case ".data":
                    curSeg = Segment.DATA;
                    break;
                case ".code":
                    curSeg = Segment.CODE;
                    break;
                case "end extern":
                    if (curExt == null)
                        throw new Exception("Extern must be declared before end");
                    curExt.setEnd(_curAddr);
                    curExt = null;
                    break;

                default:
                    String[] lexeme = line.split("(?<!\\b[ ]['])[ ]+");
                    if (curSeg == Segment.CODE)
                    {
                        if (lexeme[0].equals("extern"))
                        {
                            if (curExt != null)
                                curExt.setEnd(_curAddr);

                            curExt = new Extern(lexeme[1], _curAddr);
                            _externs.add(curExt);
                        }
                        else if (lexeme[0].equals("include"))
                            _includes.add(new Include(lexeme[1], lexeme[2], _curAddr));
                        else if (lexeme[0].endsWith(":"))
                        {
                            String label = lexeme[0].replace(":", "");
                            if (lexeme.length == 3)
                                addCommand(new Command(lexeme[1], lexeme[2], label));
                            else if (lexeme.length == 2)
                                addCommand(new Command(lexeme[1], null, label));
                            else
                                throw new Exception("Label without command");
                        }
                        else if (lexeme.length == 2)
                            addCommand(new Command(lexeme[0], lexeme[1]));
                        else
                            addCommand(new Command(lexeme[0], null));
                    }
                    else
                    {
                        if (lexeme.length == 2)
                            _data.add(new Data(lexeme[0], lexeme[1]));
                        else if (lexeme.length == 3)
                            _data.add(new Data(lexeme[0], lexeme[1], lexeme[2]));
                        else
                            throw new Exception("Invalid data definition");
                    }
                    break;
            }
        }
        if (curExt != null)
            curExt.setEnd(_curAddr);
    }

    private void addCommand(Command cmd)
    {
        _cmds.add(cmd);
        _curAddr += cmd.length();
    }

    List<Data> getData()
    {
        return _data;
    }

    List<Command> getCode()
    {
        return _cmds;
    }

    List<Include> getIncludes()
    {
        return _includes;
    }

    List<Extern> getExternals()
    {
        return _externs;
    }
}

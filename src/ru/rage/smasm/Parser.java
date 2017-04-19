package ru.rage.smasm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

enum Segment
{
    CODE,
    DATA
}

class Parser
{
    private static final HashMap<String, CmdType> _cmds = new HashMap<String, CmdType>()
    {{
        put("NOP", CmdType.NOP);
        put("ADD", CmdType.ADD);
        put("SUB", CmdType.SUB);
        put("INC", CmdType.INC);
        put("DEC", CmdType.DEC);
        put("LDR", CmdType.LDR);
        put("STR", CmdType.STR);
        put("IN",  CmdType.IN);
        put("OUT", CmdType.OUT);
        put("JMP", CmdType.JMP);
        put("IFZ", CmdType.IFZ);
        put("IFN", CmdType.IFN);
        put("HLT", CmdType.HLT);
    }};
    private String[] lines;
    private LinkedList<Data> _data;

    Parser(String program)
    {
        lines = preprocess(program).split("[\\r\\n]+");
    }

    boolean hasData()
    {
        return (_data != null) && (_data.size() > 0);
    }

    List<Data> getData()
    {
        return _data;
    }

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

    List<Command> parse() throws Exception
    {
        LinkedList<Command> cmds = new LinkedList<>();
        Segment curSeg = Segment.CODE;

        for (String line: lines) {
            line = line.replace('\t', ' ').trim();

            switch (line) {
                case ".code":
                    curSeg = Segment.CODE;
                    break;
                case ".data":
                    curSeg = Segment.DATA;
                    _data = new LinkedList<>();
                    break;
                default:
                    String[] lexeme = line.split("[ ]+");
                    if (curSeg == Segment.CODE) {
                        if (lexeme[0].endsWith(":")) {
                            String label = lexeme[0].replace(":", "");
                            if (lexeme.length == 3)
                                cmds.add(new Command(_cmds.get(lexeme[1]), new Argument(lexeme[2]), label));
                            else if (lexeme.length == 2)
                                cmds.add(new Command(_cmds.get(lexeme[1]), new Argument(null)));
                            else
                                throw new Exception("Label without command");
                        } else if (lexeme.length == 2)
                            cmds.add(new Command(_cmds.get(lexeme[0]), new Argument(lexeme[1])));
                        else
                            cmds.add(new Command(_cmds.get(lexeme[0]), new Argument(null)));
                    } else {
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
        return cmds;
    }
}

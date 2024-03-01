import java.io.File;
import java.io.FileNotFoundException;
import java.io.*;
import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class main {
    public static void generateMachineCode(y86Parser.ProgramContext parseTree, JSONObject machineCodeMapping) {
        List<ParseTree> codeLines = parseTree.children;
        for (ParseTree codeLine : codeLines) {
            if (Objects.equals(codeLine.toString(), "<EOF>")) {
                break;
            } else {
                StringBuilder newLine = new StringBuilder();
                for (ParseTree opTree : codeLine.children) {
                    String op = opTree.toString().strip();
                    if (op.charAt(0) == '%') {
                        newLine.append(machineCodeMapping.get("registers").get(op));
                    } else if (op.charAt(0) == '$') {

                    }
                }
            }
        }
    }

    public static JSONObject getMachineCodeMapping() throws IOException, ParseException {
        Object object = new JSONParser().parse(new FileReader("mapping.json"));
        return (JSONObject) object;
    }

    public static void main(String[] args) throws IOException, ParseException {
//        String inputfile = args[0];
//        parseTree.children.get(0).children.get(0).children.get(0).toString()
//
//        Scanner scanner = new Scanner(new File(inputfile));
//
//        while(scanner.hasNext()) {
//            System.out.println(scanner.nextLine());
//        }
//
//        scanner.close();

        CharStream input = CharStreams.fromFileName("test.txt");
        y86Lexer lexer = new y86Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        y86Parser parser = new y86Parser(tokens);

        y86Parser.ProgramContext parseTree = parser.program();

        JSONObject machineCodeMapping = getMachineCodeMapping();

        generateMachineCode(parseTree, machineCodeMapping);

        System.out.println(parseTree.toStringTree());
    }
}

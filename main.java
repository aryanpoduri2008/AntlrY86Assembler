import java.io.*;
import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class main {

    /*
    * Executes and creates machine code line for the mrmovl instruction
    *
    * @param codeLine: codeline from the input y86 sectioned into instruction, register, etc.
    * @param registers: mapping of registers to their corresponding machine code
    * @param pos: current position
    * @return: machine code line for mrmovl instruction
    * */
    public static String mrmovl(ParseTree codeLine, JSONObject registers, int pos) {
        if (codeLine.getChildCount() == 6) {
            String rB = codeLine.getChild(2).toString().strip();
            String rA = codeLine.getChild(5).toString().strip();
            return "50" + registers.get(rA) + registers.get(rB) + littleEndian("0");
        } else if (codeLine.getChildCount() == 7) {
            String D = codeLine.getChild(1).toString().strip();
            String rB = codeLine.getChild(3).toString().strip();
            String rA = codeLine.getChild(6).toString().strip();
            return "50" + registers.get(rA) + registers.get(rB) + littleEndian(D);
        } else {
            return "Line " + Integer.toHexString(pos) + " has incorrect instruction.";
        }
    }

    /*
    * Executes and creates machine code line for the rmmovl instruction
    *
    * @param codeLine: codeline from the input y86 sectioned into instruction, register, etc.
    * @param registers: mapping of registers to their corresponding machine code
    * @param pos: current position
    * @return: machine code line for rmmovl instruction
    * */
    public static String rmmovl(ParseTree codeLine, JSONObject registers, int pos) {
        if (codeLine.getChildCount() == 5) {
            String rA = codeLine.getChild(1).toString().strip();
            String rB = codeLine.getChild(3).toString().strip();
            return "40" + registers.get(rA) + registers.get(rB) + littleEndian("0");
        } else if (codeLine.getChildCount() == 6) {
            String rA = codeLine.getChild(1).toString().strip();
            String D = codeLine.getChild(2).toString().strip().substring(1);
            String rB = codeLine.getChild(4).toString().strip();
            return "40" + registers.get(rA) + registers.get(rB) + littleEndian(D);
        } else {
            return "Line " + Integer.toHexString(pos) + " has incorrect instruction.";
        }
    }

    /*
    * This function turns an integer into a hexstring in little endian format
    *
    * @param integer: input number in decimal format or hexadecimal format
    * @return: a hexadecimal formatted integer
    * */
    public static String littleEndian(String integer) {
        if (!integer.startsWith("0x"))
            integer = Integer.toHexString(Integer.parseInt(integer));
        else
            integer = integer.substring(2);

        if (integer.length() % 2 != 0)
            integer = "0" + integer;

        StringBuilder newInteger = new StringBuilder();
        for (int i = integer.length(); i >= 2; i -= 2) {
            newInteger.append(integer, i - 2, i);
        }

        for (int i = integer.length(); i < 8; i++)
            newInteger.append("0");

        return newInteger.toString();
    }

    /*
    * This function generates the machine code, line-by-line, and writes it to output.txt
    *
    * @param parseTree: full parsetree of the y86 assembly input split into registers, values, parameters, instructions
    * @param machineCodeMapping: map from mapping.json that contains register and instruction correspondents and
    * position increments
    * @param labels: sequential labels/positions for each line in machine code
    * @return: None
    * */
    public static void generateMachineCode(y86Parser.ProgramContext parseTree, JSONObject machineCodeMapping, HashMap<String, String> labels) {
        List<ParseTree> codeLines = parseTree.children;
        int pos = 0;
        JSONObject registers = (JSONObject) machineCodeMapping.get("registers");
        JSONObject instructions = (JSONObject) machineCodeMapping.get("instructions");
        JSONObject pos_increments = (JSONObject) machineCodeMapping.get("position_increments");

        try {
            FileWriter output = new FileWriter("output.txt");
            for (ParseTree codeLine : codeLines) {
                if (Objects.equals(codeLine.toString(), "<EOF>") || Objects.equals(codeLine.toString(), "\t") || Objects.equals(codeLine.toString(), "\n")) {
                    continue;
                }

                codeLine = codeLine.getChild(0);

                if (labels.containsKey(codeLine.getChild(0).toString())) {
                    continue;
                } else {
                    StringBuilder newLine = new StringBuilder();
                    String instruction = "";
                    String value = "";
                    boolean success = true;
                    for (int i = 0; i < codeLine.getChildCount(); i++) {
                        ParseTree opTree = codeLine.getChild(i);
                        String op = opTree.toString().strip();
                        if (op.equals(",") || op.equals("(") || op.equals(")")) {
                            continue;
                        } else if (op.charAt(0) == '%') {
                            newLine.append(registers.get(op));
                            if (instruction.equals("pushl") || instruction.equals("popl"))
                                newLine.append("f");
                        } else if (op.charAt(0) == '$') {
                            value = littleEndian(op.split("\\$")[1]);
                        } else if (op.equals("mrmovl")) {
                            newLine.append(mrmovl(codeLine, registers, pos));
                            instruction = "mrmovl";
                            break;
                        } else if (op.equals("rmmovl")) {
                            newLine.append(rmmovl(codeLine, registers, pos));
                            instruction = "rmmovl";
                            break;
                        } else if (instructions.containsKey(op)) {
                            instruction = op;
                            newLine.append(instructions.get(op));
                        } else if (labels.containsKey(op)) {
                            value = littleEndian("0x" + labels.get(op));
                        } else if (op.equals("Stack")) {
                            value = littleEndian("0x100");
                        } else if (op.equals(".long") || op.equals(".align")) {
                            instruction = ".long";
                            newLine.append(littleEndian(codeLine.getChild(i + 1).toString().strip()));
                            break;
                        } else if (op.equals(".pos")) {
                            success = false;
                            break;
                        } else {
                            success = false;
                            output.write("Line " + Integer.toHexString(pos) + " has incorrect instruction.\n");
                            System.out.println("Line " + Integer.toHexString(pos) + " has incorrect instruction.");
                            break;
                        }
                    }
                    if (success) {
                        newLine.append(value);
                        output.write("0x" + Integer.toHexString(pos) + ": " + newLine + "\n");
                        System.out.println("0x" + Integer.toHexString(pos) + ": " + newLine);
                    }
                    if (Objects.equals(codeLine.getChild(0).toString().strip(), ".pos")) {
                        if (codeLine.getChild(1).toString().startsWith("0x"))
                            pos = Integer.parseInt(codeLine.getChild(1).toString().strip().substring(2), 16);
                        else
                            pos = Integer.parseInt(codeLine.getChild(1).toString().strip());
                        continue;
                    } else
                        pos += Integer.parseInt(pos_increments.get(instruction).toString());
                }
            }
            output.close();
        } catch (IOException e) {
            System.out.println("An error has occured.");
            e.printStackTrace();
        }
    }

    /*
    * @return: the JSONObject for the map from mapping.json
    * */
    public static JSONObject getMachineCodeMapping() throws IOException, ParseException {
        Object object = new JSONParser().parse(new FileReader("mapping.json"));
        return (JSONObject) object;
    }

    /*
    * Using the position increments in the machine code mapping and the instructions from the Antlr parse tree, the
    * positions for each code line are generated.
    * @param parseTree: full parsetree of the y86 assembly input split into registers, values, parameters, instructions
    * @param machineCodeMapping: map from mapping.json that contains register and instruction correspondents and
    * position increments
    * @return: sequential labels/positions for each line in machine code
    * */
    public static HashMap<String, String> getLabels(y86Parser.ProgramContext parseTree, JSONObject machineCodeMapping) {
        HashMap<String, String> labels = new HashMap<>();

        JSONObject pos_increments = (JSONObject) machineCodeMapping.get("position_increments");
        List<ParseTree> codeLines = parseTree.children;
        int pos = 0;
        for (ParseTree codeLine : codeLines) {
            if (Objects.equals(codeLine.toString(), "<EOF>") || Objects.equals(codeLine.toString(), "\t") || Objects.equals(codeLine.toString(), "\n")) {
                continue;
            }

            codeLine = codeLine.getChild(0);
            if (Objects.equals(codeLine.getChild(0).toString().strip(), ".pos")) {
                if (codeLine.getChild(1).toString().startsWith("0x"))
                    pos = Integer.parseInt(codeLine.getChild(1).toString().strip().substring(2), 16);
                else
                    pos = Integer.parseInt(codeLine.getChild(1).toString().strip());
                continue;
            }

            if (codeLine.getChildCount() > 1 && codeLine.getChild(1).toString().strip().equals(":")) {
                labels.put(codeLine.getChild(0).toString(), Integer.toHexString(pos));
                continue;
            }
            pos += Integer.parseInt(pos_increments.get(codeLine.getChild(0).toString().strip()).toString());
        }

        return labels;
    }

    public static void main(String[] args) throws IOException, ParseException {

        CharStream input = CharStreams.fromFileName("test.txt");
        y86Lexer lexer = new y86Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        y86Parser parser = new y86Parser(tokens);

        // Use Antlr grammar to parse the y86 assembly code into instructions, registers, paramters, values, etc.
        y86Parser.ProgramContext parseTree = parser.program();

        JSONObject machineCodeMapping = getMachineCodeMapping();

        // Get position labels for machine code
        HashMap<String, String> labels = getLabels(parseTree, machineCodeMapping);

        generateMachineCode(parseTree, machineCodeMapping, labels);

        System.out.println("\nAlso outputted into file: output.txt");
    }
}
